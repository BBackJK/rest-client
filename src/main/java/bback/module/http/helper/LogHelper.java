package bback.module.http.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogHelper.class);
    private final String context;

    public LogHelper(String context) {
        this.context = context;
    }

    public LogHelper(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    public static LogHelper of(Class<?> clazz) {
        return of(clazz.getSimpleName());
    }

    public static LogHelper of(String context) {
        return new LogHelper(context);
    }

    public void log(String message, Object... args) {
        LOGGER.info(getFormatMessage(message, "LOG"), args);
    }

    public void log(String message) {
        log(message, (Object) null);
    }

    public void warn(String message, Object... args) {
        LOGGER.warn(getFormatMessage(message, "WARN"), args);
    }

    public void warn(String message) {
        warn(message, (Object) null);
    }

    public void err(String message, Object... args) {
        LOGGER.error(getFormatMessage(message, "ERROR"), args);
    }

    public void err(String message) {
        err(message, (Object) null);
    }

    private String getFormatMessage(String msg, String logLevel) {
        return String.format("[%s %s] %s", this.context, logLevel, msg);
    }
}
