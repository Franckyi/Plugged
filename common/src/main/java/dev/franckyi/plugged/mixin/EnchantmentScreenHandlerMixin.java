package dev.franckyi.plugged.mixin;

import dev.franckyi.plugged.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {
    @Shadow @Final public int[] enchantmentPower;

    @Inject(method = "onButtonClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"))
    private void onEnchant(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (player.world.isClient) EventHandler.onEnchant(id + 1, this.enchantmentPower[id]);
    }
}
