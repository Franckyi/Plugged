package dev.franckyi.plugged.mixin;

import dev.franckyi.plugged.EventHandler;
import dev.franckyi.plugged.Plugged;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {
    @Shadow public abstract int getLevelCost();

    @Inject(method = "onTakeOutput", at = @At(value = "HEAD"))
    private void onRepair(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player.world.isClient) EventHandler.onRepair(this.getLevelCost());
    }
}
