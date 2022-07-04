package me.greysilly7.salir;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerListImageRandomizer implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ServerListImageRandomizer");

    @Override
    public void onInitialize() {
        File servericonsDir = new File("./server-icons/");
        if (!servericonsDir.exists()) {
            ServerListImageRandomizer.LOGGER.error("Server Icons Dir doesn't exist, making now");
            Path path = Paths.get("./server-icons");
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Hello from Greysilly7");
    }
}
