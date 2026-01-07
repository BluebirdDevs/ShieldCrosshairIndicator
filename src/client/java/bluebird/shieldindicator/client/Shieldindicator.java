package bluebird.shieldindicator.client;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shieldindicator implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("shieldindicator");

    public static boolean centeredCrosshairLoaded;

    @Override
    public void onInitializeClient() {
        centeredCrosshairLoaded = FabricLoader.getInstance().isModLoaded("centered-crosshair");
        ModConfig.init();
    }

    public static boolean shouldShowShield(PlayerEntity defender, MinecraftClient client) {
        if (!defender.isBlocking() || !ModConfig.INSTANCE.shieldIndicator) {
            return false;
        }

        PlayerEntity attacker = client.player;
        Vec3d defenderLook = defender.getRotationVector().normalize();

        Vec3d defenderEye = defender.getEyePos();
        Vec3d attackerEye = attacker.getEyePos();

        Vec3d toAttacker = attackerEye.subtract(defenderEye).normalize();

        return !(toAttacker.dotProduct(defenderLook) <= 0.0D);
    }
}
