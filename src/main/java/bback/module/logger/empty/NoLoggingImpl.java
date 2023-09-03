package bback.module.logger.empty;


import bback.module.logger.Log;

public class NoLoggingImpl implements Log {

    public NoLoggingImpl(String clazz) {
        // ignore...
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void error(String s, Throwable e) {
        // ignore...
    }

    @Override
    public void error(String s) {
        // ignore...
    }

    @Override
    public void debug(String s) {
        // ignore...
    }

    @Override
    public void info(String s) {
        // ignore...
    }

    @Override
    public void trace(String s) {
        // ignore...
    }

    @Override
    public void warn(String s) {
        // ignore...
    }
}
