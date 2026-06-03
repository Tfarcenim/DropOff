package tfar.quickstack;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.config.QuickStackConfig;
import tfar.quickstack.network.server.C2SPacketRequestDropoff;
import tfar.quickstack.network.client.S2CReportPacket;
import tfar.quickstack.platform.Services;
import tfar.quickstack.util.ItemStackUtils;
import tfar.quickstack.util.LogMessageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mod(QuickStack.MOD_ID)

public class QuickStackNeoForge {

    public static final String MOD_ID = "quickstack";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID, LogMessageFactory.INSTANCE);

    public QuickStackNeoForge(IEventBus bus, ModContainer container) {

        bus.addListener(this::setup);
        bus.addListener(this::register);
        bus.addListener(this::configChanged);
        bus.addListener(PacketHandlerNeoForge::register);
        container.registerConfig(ModConfig.Type.CLIENT, QuickStackConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, QuickStackConfig.SERVER_SPEC);
        QuickStack.init();

    }

    void register(RegisterEvent event) {
        FavDataComponent.init();
    }

    private void configChanged(ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(QuickStack.MOD_ID)) {
        return;
    }
        QuickStackConfig.onConfigChanged();
    }


    private void setup(FMLCommonSetupEvent event) {
    }

    public static void moveItems(ServerPlayer player, C2SPacketRequestDropoff packet) {
            Set<BlockEntity> nearbyInventories = getNearbyInventories(player,packet);
            final MutableInt itemCounter  = new MutableInt();
        List<RendererCubeTarget> rendererCubeTargets = new ArrayList<>();
        int affectedContainers = 0;
        for (BlockEntity blockEntity : nearbyInventories) {
            IItemHandler iItemHandler = Capabilities.ItemHandler.BLOCK.getCapability(player.level(), blockEntity.getBlockPos(),
                blockEntity.getBlockState(), blockEntity, null);
            int itemsMoved = packet.dump() ? dropOff(player, iItemHandler, packet) :dropOffExisting(player, iItemHandler, packet);
            itemCounter.add(itemsMoved);
            boolean movedAnything = itemsMoved > 0;
            if (movedAnything) {
                affectedContainers++;
            }
            rendererCubeTargets.add(new RendererCubeTarget(blockEntity.getBlockPos(),movedAnything ? 0xff00ff00 : 0xffff0000));
        }
        player.containerMenu.broadcastChanges();

        Services.PLATFORM.sendToClient( new S2CReportPacket(itemCounter.intValue(), affectedContainers, nearbyInventories.size(),
                    rendererCubeTargets), player);

        }

        public static int dropOff(Player player, IItemHandler target,C2SPacketRequestDropoff packet) {
            IItemHandler playerstacks = new InvWrapper(player.getInventory());
            int itemsCounter = 0;
            for (int i = 0; i < 36; ++i) {
                if (packet.ignoreHotbar() && i < 9)
                    continue;
                ItemStack playerstack = playerstacks.getStackInSlot(i);

                if (playerstack.isEmpty() || ItemStackUtils.isFavorite(playerstack))
                    continue;
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

        public static int dropOffExisting(Player player, IItemHandler target,C2SPacketRequestDropoff packet) {
            IItemHandler playerstacks = new InvWrapper(player.getInventory());
            int itemsCounter = 0;
            for (int i = 0; i < 36; ++i) {
                if (packet.ignoreHotbar() && i < 9)
                    continue;
                ItemStack playerstack = playerstacks.getStackInSlot(i);
                if (playerstack.isEmpty() || ItemStackUtils.isFavorite(playerstack))
                    continue;
                boolean hasExistingStack = IntStream.range(0, target.getSlots()).mapToObj(target::getStackInSlot)
                    .filter(existing -> !existing.isEmpty())
                    .anyMatch(existing -> existing.getItem() == playerstack.getItem());
                if (!hasExistingStack)
                    continue;
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

        public static Set<BlockEntity> getNearbyInventories(ServerPlayer player, C2SPacketRequestDropoff packet) {
            double playerX = player.position().x;
            double playerY = player.position().y;
            double playerZ = player.position().z;
            int minX = (int) (playerX - QuickStackConfig.scanRadius.get());
            int maxX = (int) (playerX + QuickStackConfig.scanRadius.get());

            int minY = (int) (playerY - QuickStackConfig.scanRadius.get());
            int maxY = (int) (playerY + QuickStackConfig.scanRadius.get());

            int minZ = (int) (playerZ - QuickStackConfig.scanRadius.get());
            int maxZ = (int) (playerZ + QuickStackConfig.scanRadius.get());

            Level world = player.level();
            return BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ)
                .map(world::getBlockEntity)
                .filter(Objects::nonNull)
                .filter(tileEntity -> !packet.teTypes().contains(tileEntity.getType()))
                .filter(blockEntity -> {
                    IItemHandler capability = Capabilities.ItemHandler.BLOCK.getCapability(player.level(), blockEntity.getBlockPos(),
                        blockEntity.getBlockState(), blockEntity, null);
                    return capability != null && capability.getSlots() >= packet.minSlotCount();
                })
                .collect(Collectors.toSet());
        }
    }
