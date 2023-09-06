package bback.module.http.helper;

import bback.module.logger.Log;
import bback.module.logger.LogFactory;

public class LogHelper {
    private static final Log LOGGER = LogFactory.getLog(LogHelper.class);
    private static final String LOG_LEVEL_DEBUG = "DEBUG";
    private static final String LOG_LEVEL_LOG = "LOG";
    private static final String LOG_LEVEL_WARN = "WARN";
    private static final String LOG_LEVEL_ERROR = "ERROR";
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

    public void log(String message) {
        LOGGER.info(getFormatMessage(message, LOG_LEVEL_LOG));
    }

    public void debug(String message) {
        LOGGER.debug(getFormatMessage(message, LOG_LEVEL_DEBUG));
    }

    public void warn(String message) {
        LOGGER.warn(getFormatMessage(message, LOG_LEVEL_WARN));
    }

    public void err(String message) {
        LOGGER.error(getFormatMessage(message, LOG_LEVEL_ERROR));
    }

    private String getFormatMessage(String msg, String logLevel) {
        return String.format("[%s %s] %s", this.context, logLevel, msg);
    }
}
