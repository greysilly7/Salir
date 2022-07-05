package me.greysilly7.salir.mixin;

import me.greysilly7.salir.ServerListImageRandomizer;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;

    private static <T> T getRandomElement(T[] arr){
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }

    private static BufferedImage imageToBufferedImage(Image im) {
        BufferedImage bi = new BufferedImage
                (im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_RGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }


    @Inject(method = "onHandshake", at = @At("HEAD"))
    public void onHandshake(HandshakeC2SPacket packet, CallbackInfo ci) {
        File servericonsDir = new File("./server-icons/");
        if (Objects.requireNonNull(servericonsDir.listFiles())[0].exists()) {
            Optional<File> optional = Optional.of(getRandomElement(Objects.requireNonNull(servericonsDir.listFiles()))).filter(File::isFile);

            optional.ifPresent((file) -> {
                try {
                    BufferedImage bufferedImage = ImageIO.read(file);
                    Image scaled = bufferedImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(imageToBufferedImage(scaled), "PNG", byteArrayOutputStream);
                    byte[] bs = Base64.getEncoder().encode(byteArrayOutputStream.toByteArray());
                    String var10001 = new String(bs, StandardCharsets.UTF_8);
                    this.server.getServerMetadata().setFavicon("data:image/png;base64," + var10001);
                } catch (Exception var5) {
                    ServerListImageRandomizer.LOGGER.error("Couldn't load server icon", var5);
                }
            });
        }
    }
}
