package tfar.quickstack.networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableInt;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.config.DropOffConfig;
import tfar.quickstack.util.ItemStackUtils;
import tfar.quickstack.util.SuccedableInventoryData;

public record C2SPacketRequestDropoff(boolean ignoreHotbar, boolean dump, List<BlockEntityType<?>> teTypes,
                                      int minSlotCount) implements C2SPacket {

    public C2SPacketRequestDropoff(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), buf.readList(buf1 ->
            buf1.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE)), buf.readInt());
    }

    public void handleServer(ServerPlayer player) {
        Set<SuccedableInventoryData> nearbyInventories = getNearbyInventories(player);
        final MutableInt itemCounter  = new MutableInt();
        nearbyInventories.forEach(inventoryData -> {
            inventoryData.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .ifPresent(
                    target -> {
                        if (dump) {
                            itemCounter.add(dropOff(player, target, inventoryData));
                        } else {
                            itemCounter.add(dropOffExisting(player, target, inventoryData));
                        }
                    });
        });

        List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
        int affectedContainers = 0;
        player.containerMenu.broadcastChanges();

        for (SuccedableInventoryData inventoryData : nearbyInventories) {
            int color;

            if (inventoryData.success) {
                affectedContainers++;
                color = 0x00FF00;
            } else {
                color = 0xFF0000;
            }

            RendererCubeTarget rendererCubeTarget = new RendererCubeTarget(inventoryData.blockEntity.getBlockPos(),
                color);
            rendererCubeTargets.add(rendererCubeTarget);
        }

        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
            new S2CReportPacket(itemCounter.intValue(), affectedContainers, nearbyInventories.size(),
                rendererCubeTargets));

    }

    public int dropOff(Player player, IItemHandler target, SuccedableInventoryData data) {
        IItemHandler playerstacks = new InvWrapper(player.getInventory());
        int itemsCounter = 0;
        for (int i = 0; i < 36; ++i) {
            if (ignoreHotbar && i < 9)
                continue;
            ItemStack playerstack = playerstacks.getStackInSlot(i);

            if (playerstack.isEmpty() || ItemStackUtils.isFavorite(playerstack))
                continue;
            data.setSuccessful();
            itemsCounter += playerstack.getCount();
            ItemStack rem = playerstacks.extractItem(i, Integer.MAX_VALUE, false);
            for (int j = 0; j < target.getSlots(); ++j) {
                rem = target.insertItem(j, rem, false);
                if (rem.isEmpty())
                    break;
            }
            if (!rem.isEmpty()) {
                itemsCounter -= rem.getCount();
                playerstacks.insertItem(i, rem, false);
            }
        }
        return itemsCounter;
    }

    public int dropOffExisting(Player player, IItemHandler target, SuccedableInventoryData data) {
        IItemHandler playerstacks = new InvWrapper(player.getInventory());
        int itemsCounter = 0;
        for (int i = 0; i < 36; ++i) {
            if (ignoreHotbar && i < 9)
                continue;
            ItemStack playerstack = playerstacks.getStackInSlot(i);
            if (playerstack.isEmpty() || ItemStackUtils.isFavorite(playerstack))
                continue;
            boolean hasExistingStack = IntStream.range(0, target.getSlots()).mapToObj(target::getStackInSlot)
                .filter(existing -> !existing.isEmpty())
                .anyMatch(existing -> existing.getItem() == playerstack.getItem());
            if (!hasExistingStack)
                continue;
            data.setSuccessful();
            itemsCounter += playerstack.getCount();
            ItemStack rem = playerstacks.extractItem(i, Integer.MAX_VALUE, false);
            // Create array that will store all locations of empty slots in target inventory
            int[] emptySlots = new int[target.getSlots()];
            int numEmptySlots = 0;
            for (int j = 0; j < target.getSlots(); ++j) {
                // If the current slot in chest inventory is empty, store in array as we will attempt to populate later if other stacks are full
                if (target.getStackInSlot(j).isEmpty()) {
                    emptySlots[numEmptySlots] = j;
                    numEmptySlots++;
                }
                // If the current slot in chest inventory is different object, don't attempt to stack.
                if (rem.getItem() != target.getStackInSlot(j).getItem()) {
                    continue;
                }

                rem = target.insertItem(j, rem, false);
                if (rem.isEmpty())
                    break;
            }
            // Attempt to populate all the empty slots
            for (int j = 0; j < numEmptySlots; ++j) {
                rem = target.insertItem(emptySlots[j], rem, false);
                if (rem.isEmpty())
                    break;

            }


            if (!rem.isEmpty()) {
                itemsCounter -= rem.getCount();
                playerstacks.insertItem(i, rem, false);
            }
        }
        return itemsCounter;
    }

    public Set<SuccedableInventoryData> getNearbyInventories(ServerPlayer player) {
        double playerX = player.position().x;
        double playerY = player.position().y;
        double playerZ = player.position().z;
        int minX = (int) (playerX - DropOffConfig.scanRadius.get());
        int maxX = (int) (playerX + DropOffConfig.scanRadius.get());

        int minY = (int) (playerY - DropOffConfig.scanRadius.get());
        int maxY = (int) (playerY + DropOffConfig.scanRadius.get());

        int minZ = (int) (playerZ - DropOffConfig.scanRadius.get());
        int maxZ = (int) (playerZ + DropOffConfig.scanRadius.get());

        Level world = player.level();
        return BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ)
            .map(world::getBlockEntity)
            .filter(Objects::nonNull)
            .filter(tileEntity -> tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .filter(iItemHandler -> iItemHandler.getSlots() >= minSlotCount)
                .isPresent())
            .filter(tileEntity -> !teTypes.contains(tileEntity.getType()))
            .map(SuccedableInventoryData::new)
            .collect(Collectors.toSet());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(ignoreHotbar);
        buf.writeBoolean(dump);
        buf.writeCollection(teTypes, (buf1, blockEntityType) ->
            buf1.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, blockEntityType));
        buf.writeInt(minSlotCount);
    }
}
