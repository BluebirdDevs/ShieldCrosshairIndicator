package bluebird.shieldindicator.client.mixin;

import bluebird.shieldindicator.client.ModConfig;
import bluebird.shieldindicator.client.Shieldindicator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Gui.class)
public class MixinInGameHud {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static final Identifier ATTACK_CROSSHAIR = Identifier.fromNamespaceAndPath("shieldindicator", "hud/indicator_crosshair");

    @Unique
    private static final Identifier SHIELD_CROSSHAIR = Identifier.fromNamespaceAndPath("shieldindicator", "hud/shield_crosshair");


    @Inject(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
    private void shieldindicator$layerCrosshair(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (ModConfig.INSTANCE.textureMode == ModConfig.TextureMode.LAYERED && this.minecraft.crosshairPickEntity instanceof LivingEntity entity && minecraft.player != null) {
            boolean showShield = false;
            boolean showCrosshair = true;
            if (entity instanceof Player player) {
                showShield = Shieldindicator.shouldShowShield(player, minecraft);
            } else if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.PLAYERS) {
                showCrosshair = false;
            }

            Identifier texture = showShield ? SHIELD_CROSSHAIR : ATTACK_CROSSHAIR;

            if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.OFF && !showShield || !showCrosshair) {
            } else if (Shieldindicator.centeredCrosshairLoaded) {
                float scaleFactor = (float) Minecraft.getInstance().getWindow().getGuiScale();
                float scaledCenterX = (Minecraft.getInstance().getWindow().getWidth() / scaleFactor) / 2f;
                float scaledCenterY = (Minecraft.getInstance().getWindow().getHeight() / scaleFactor) / 2f;
                drawCenteredCrosshair(context, RenderPipelines.CROSSHAIR, texture, Math.round((scaledCenterX - 7.5f) * 4) / 4f, Math.round((scaledCenterY - 7.5f) * 4) / 4f, 15, 15);
            } else {
                context.blitSprite(RenderPipelines.CROSSHAIR, texture, (context.guiWidth() - 15) / 2, (context.guiHeight() - 15) / 2, 15, 15);
            }
        }
    }

    @ModifyArg(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0), index = 1)
    private Identifier shieldindicator$changeCrosshair(Identifier sprite) {
        if (ModConfig.INSTANCE.textureMode == ModConfig.TextureMode.OVERRIDE && this.minecraft.crosshairPickEntity instanceof LivingEntity) {
            boolean showShield = false;
            Identifier texture = ATTACK_CROSSHAIR;
            if (this.minecraft.crosshairPickEntity instanceof Player player && minecraft.player != null) {
                showShield = Shieldindicator.shouldShowShield(player, minecraft);
                texture = showShield ? SHIELD_CROSSHAIR : ATTACK_CROSSHAIR;
            } else if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.PLAYERS) return sprite;

            if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.OFF && !showShield) {
                return sprite;
            } else return texture;
        }
        return sprite;
    }

    @Unique
    private static void drawCenteredCrosshair(GuiGraphicsExtractor instance, RenderPipeline pipeline, Identifier sprite, float x, float y, int w, int h) {
        try {
            Method m = instance.getClass().getMethod(
                    "centered_crosshair$drawGuiTexture",
                    pipeline.getClass(),
                    sprite.getClass(),
                    float.class,
                    float.class,
                    int.class,
                    int.class
            );

            m.invoke(instance, pipeline, sprite, x, y, w, h);
        } catch (Throwable t) {
            Shieldindicator.LOGGER.error(t.toString());
        }
    }
}

