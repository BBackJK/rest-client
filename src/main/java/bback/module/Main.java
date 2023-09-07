package bback.module;

import bback.module.logger.Log;
import bback.module.logger.LogFactory;

public class Main {

    private static final Log LOGGER = LogFactory.getLog(Main.class);

    public static void main(String[] args) {
        LOGGER.debug("rest-client on..");
    }
}
