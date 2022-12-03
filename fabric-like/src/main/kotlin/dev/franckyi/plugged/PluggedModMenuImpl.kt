package dev.franckyi.plugged

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi

class PluggedModMenuImpl : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = ConfigScreenFactory(ConfigScreen::build)
}