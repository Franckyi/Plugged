package dev.franckyi.plugged.mixin;

import dev.franckyi.plugged.EventHandler;
import dev.franckyi.plugged.Plugged;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "setBlockBreakingInfo", at = @At("HEAD"))
    private void onBlockBreaking(int entityId, BlockPos pos, int progress, CallbackInfo ci) {
        if (entityId == MinecraftClient.getInstance().player.getId()) EventHandler.onBlockBreaking(progress);
    }
}
