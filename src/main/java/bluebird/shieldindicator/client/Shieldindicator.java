package bluebird.shieldindicator.client;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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

    public static boolean shouldShowShield(Player defender, Minecraft client) {
        if (!defender.isBlocking() || !ModConfig.INSTANCE.shieldIndicator) {
            return false;
        }

        Player attacker = client.player;
        Vec3 defenderLook = defender.getLookAngle().normalize();

        Vec3 defenderEye = defender.getEyePosition();
        Vec3 attackerEye = attacker.getEyePosition();

        Vec3 toAttacker = attackerEye.subtract(defenderEye).normalize();

        return !(toAttacker.dot(defenderLook) <= 0.0D);
    }
}
