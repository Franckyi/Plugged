package dev.franckyi.plugged

import net.fabricmc.api.ClientModInitializer

object PluggedFabricMod : ClientModInitializer {
    override fun onInitializeClient() = PluggedFabricLikeMod.init()
}