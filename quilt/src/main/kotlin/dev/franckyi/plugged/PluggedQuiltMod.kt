package dev.franckyi.plugged

import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer

class PluggedQuiltMod : ClientModInitializer {
    override fun onInitializeClient(mod: ModContainer) = PluggedFabricLikeMod.init()
}