package dev.franckyi.plugged

import dev.architectury.platform.forge.EventBuses
import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(Plugged.MOD_ID)
class PluggedForgeMod {
    init {
        EventBuses.registerModEventBus(Plugged.MOD_ID, MOD_BUS)
        Plugged.init()
        ModLoadingContext.get().activeContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { _, parent ->
                ConfigScreen.build(parent)
            }
        }
    }
}