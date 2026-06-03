package tfar.quickstack.util;

import org.apache.logging.log4j.message.*;
import tfar.quickstack.QuickStack;

public class LogMessageFactory implements MessageFactory {

    public static final LogMessageFactory INSTANCE = new LogMessageFactory();

    private final String prefix = "[" + QuickStack.MOD_ID + "]: ";

    @Override
    public Message newMessage(Object message) {
        return new ObjectMessage(prefix + message);
    }

    @Override
    public Message newMessage(String message) {
        return new SimpleMessage(prefix + message);
    }

    @Override
    public Message newMessage(String message, Object... params) {
        return new ParameterizedMessage(prefix + message, params);
    }

}
