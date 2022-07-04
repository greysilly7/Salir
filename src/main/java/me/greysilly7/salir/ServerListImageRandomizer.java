package me.greysilly7.salir;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerListImageRandomizer implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ServerListImageRandomizer");

    @Override
    public void onInitialize() {
        LOGGER.info("Hello from Greysilly7");
    }
}
