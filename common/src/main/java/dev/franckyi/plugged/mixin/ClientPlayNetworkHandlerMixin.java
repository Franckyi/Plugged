package dev.franckyi.plugged.mixin;

import dev.franckyi.plugged.EventHandler;
import dev.franckyi.plugged.Plugged;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onDeathMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void onDeathMessage(DeathMessageS2CPacket packet, CallbackInfo ci) {
        EventHandler.onDeath();
    }

    @Inject(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;addParticle(Lnet/minecraft/client/particle/Particle;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo ci, Entity entity, LivingEntity livingEntity) {
        if (livingEntity == MinecraftClient.getInstance().player) {
            if (entity instanceof ItemEntity) EventHandler.onPickupItem();
            else if (entity instanceof ExperienceOrbEntity orb) EventHandler.onPickupXp(orb.getExperienceAmount(), orb.getOrbSize());
        }
    }
}
