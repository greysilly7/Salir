package me.greysilly7.salir.mixin;

import me.greysilly7.salir.ServerListImageRandomizer;
import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {
    private static final Text IGNORING_STATUS_REQUEST_MESSAGE = Text.literal("Ignoring status request");
    private final MinecraftServer server;
    private final ClientConnection connection;

    public ServerHandshakeNetworkHandlerMixin(MinecraftServer server, ClientConnection connection) {
        this.server = server;
        this.connection = connection;
    }


    private static <T> T getRandomElement(T[] arr){
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    /**
     * @author Greysilly7
     * @reason I need to set custom image :P
     */
    @Overwrite()
    public void onHandshake(HandshakeC2SPacket packet) {
        switch (packet.getIntendedState()) {
            case LOGIN -> {
                this.connection.setState(NetworkState.LOGIN);
                if (packet.getProtocolVersion() != SharedConstants.getGameVersion().getProtocolVersion()) {
                    MutableText text;
                    if (packet.getProtocolVersion() < 754) {
                        text = Text.translatable("multiplayer.disconnect.outdated_client", new Object[]{SharedConstants.getGameVersion().getName()});
                    } else {
                        text = Text.translatable("multiplayer.disconnect.incompatible", new Object[]{SharedConstants.getGameVersion().getName()});
                    }

                    this.connection.send(new LoginDisconnectS2CPacket(text));
                    this.connection.disconnect(text);
                } else {
                    this.connection.setPacketListener(new ServerLoginNetworkHandler(this.server, this.connection));
                }
            }
            case STATUS -> {
                if (this.server.acceptsStatusQuery()) {
                    File servericonsDir = new File("./server-icons/");
                    if (servericonsDir.listFiles()[0].exists()) {
                        Optional<File> optional = Optional.of(getRandomElement(Objects.requireNonNull(servericonsDir.listFiles()))).filter(File::isFile);

                        optional.ifPresent((file) -> {
                            try {
                                BufferedImage bufferedImage = ImageIO.read(file);
                                Validate.validState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide");
                                Validate.validState(bufferedImage.getHeight() == 64, "Must be 64 pixels high");
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
                                byte[] bs = Base64.getEncoder().encode(byteArrayOutputStream.toByteArray());
                                String var10001 = new String(bs, StandardCharsets.UTF_8);
                                server.getServerMetadata().setFavicon("data:image/png;base64," + var10001);
                            } catch (Exception var5) {
                                ServerListImageRandomizer.LOGGER.error("Couldn't load server icon", var5);
                            }

                        });
                    }
                    this.connection.setState(NetworkState.STATUS);
                    this.connection.setPacketListener(new ServerQueryNetworkHandler(this.server, this.connection));
                } else {
                    this.connection.disconnect(IGNORING_STATUS_REQUEST_MESSAGE);
                }
            }
            default -> throw new UnsupportedOperationException("Invalid intention " + packet.getIntendedState());
        }

    }
}

