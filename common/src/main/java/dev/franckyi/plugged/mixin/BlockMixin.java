package dev.franckyi.plugged.mixin;

import dev.franckyi.plugged.EventHandler;
import dev.franckyi.plugged.Plugged;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "onBreak", at = @At("HEAD"))
    private void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (world.isClient && player == MinecraftClient.getInstance().player) EventHandler.onBlockBreak(state);
    }

    @Inject(method = "onPlaced", at = @At("HEAD"))
    private void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (world.isClient && placer == MinecraftClient.getInstance().player) EventHandler.onBlockPlaced(state);
    }
}
