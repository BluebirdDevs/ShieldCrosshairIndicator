package bluebird.shieldindicator.client.mixin;

import bluebird.shieldindicator.client.ModConfig;
import bluebird.shieldindicator.client.Shieldindicator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Shadow @Final private MinecraftClient client;

    @Unique
    private static final Identifier ATTACK_CROSSHAIR = Identifier.of("shieldindicator", "hud/layer/indicator_crosshair");

    @Unique
    private static final Identifier SHIELD_CROSSHAIR = Identifier.of("shieldindicator", "hud/layer/shield_crosshair");

    @Unique
    private static final Identifier ATTACK_CROSSHAIR_OVERRIDE = Identifier.of("shieldindicator", "hud/override/indicator_crosshair");

    @Unique
    private static final Identifier SHIELD_CROSSHAIR_OVERRIDE = Identifier.of("shieldindicator", "hud/override/shield_crosshair");


    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0))
    private void layerCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModConfig.INSTANCE.textureMode == ModConfig.TextureMode.LAYERED && this.client.targetedEntity instanceof LivingEntity entity && client.player != null) {
            boolean showShield = false;
            boolean showCrosshair = true;
            Identifier texture = ATTACK_CROSSHAIR_OVERRIDE;
            if (entity instanceof PlayerEntity player) {
                showShield = Shieldindicator.shouldShowShield(player, client);
                texture = showShield ? SHIELD_CROSSHAIR_OVERRIDE : ATTACK_CROSSHAIR_OVERRIDE;
            } else if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.PLAYERS) {
                showCrosshair = false;
            }

            if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.OFF && !showShield || !showCrosshair) {
            } else if (Shieldindicator.centeredCrosshair) {
                float scaleFactor = (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
                float scaledCenterX = (MinecraftClient.getInstance().getWindow().getFramebufferWidth() / scaleFactor) / 2f;
                float scaledCenterY = (MinecraftClient.getInstance().getWindow().getFramebufferHeight() / scaleFactor) / 2f;
                drawCenteredCrosshair(context, RenderPipelines.CROSSHAIR, texture, Math.round((scaledCenterX - 7.5f) * 4) / 4f, Math.round((scaledCenterY - 7.5f) * 4) / 4f, 15, 15);
            } else {
                context.drawGuiTexture(RenderPipelines.CROSSHAIR, texture, (context.getScaledWindowWidth() - 15) / 2, (context.getScaledWindowHeight() - 15) / 2, 15, 15);
            }
        }
    }

    @ModifyArg(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0), index = 1)
    private Identifier changeCrosshair(Identifier sprite) {
        if (ModConfig.INSTANCE.textureMode == ModConfig.TextureMode.OVERRIDE && this.client.targetedEntity instanceof LivingEntity) {
            boolean showShield = false;
            Identifier texture = ATTACK_CROSSHAIR_OVERRIDE;
            if (this.client.targetedEntity instanceof PlayerEntity player && client.player != null) {
                showShield = Shieldindicator.shouldShowShield(player, client);
                texture = showShield ? SHIELD_CROSSHAIR_OVERRIDE : ATTACK_CROSSHAIR_OVERRIDE;
            } else if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.PLAYERS) return sprite;

            if (ModConfig.INSTANCE.indicatorMode == ModConfig.IndicatorMode.OFF && !showShield) {
                return sprite;
            } else return texture;
        }
        return sprite;
    }

    private static void drawCenteredCrosshair(DrawContext instance, RenderPipeline pipeline, Identifier sprite, float x, float y, int w, int h) {
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

