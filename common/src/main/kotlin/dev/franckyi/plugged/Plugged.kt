package dev.franckyi.plugged

import dev.architectury.event.EventResult
import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.client.ClientPlayerEvent
import dev.architectury.event.events.client.ClientTickEvent
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.platform.Platform
import dev.architectury.registry.client.keymappings.KeyMappingRegistry
import dev.architectury.utils.Env
import it.unimi.dsi.fastutil.ints.Int2IntMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import mu.KotlinLogging
import net.minecraft.block.BlockState
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

object Plugged {
    private val openMenuKey = KeyBinding(
        "plugged.key.open_menu",
        InputUtil.Type.KEYSYM,
        InputUtil.GLFW_KEY_M,
        "plugged"
    )

    const val MOD_ID = "plugged"

    var embeddedServerRunning = false
    var modInitialized = false

    fun init() {
        try {
            if (Platform.getEnvironment() == Env.SERVER) return
            logger.info("Initializing Plugged")
            Connector.setup()
            Config.load().get(10, TimeUnit.SECONDS)

            KeyMappingRegistry.register(openMenuKey)

            ClientTickEvent.CLIENT_PRE.register { Controller.tick() }
            ClientTickEvent.CLIENT_LEVEL_PRE.register {
                EventHandler.tick()
                if (openMenuKey.wasPressed()) mc.setScreen(DeviceListScreen())
            }
            ClientPlayerEvent.CLIENT_PLAYER_JOIN.register {
                if (!embeddedServerRunning && Config.instance.general.autoConnectAfterJoiningWorld) Connector.connectWebsocket()
                    .whenComplete { _, e -> if (e == null) showToast(Text.translatable("plugged.toast.connected")) }
            }
            ClientPlayerEvent.CLIENT_PLAYER_QUIT.register { if (!embeddedServerRunning) Connector.disconnect() }
            ClientLifecycleEvent.CLIENT_STOPPING.register { Connector.close() }

            EventHandler.init()

            if (Config.instance.general.useEmbeddedServer) {
                Connector.connectEmbedded()
                embeddedServerRunning = true
            }

            modInitialized = true
            logger.info { "Plugged initialization complete" }
        } catch (e: Throwable) {
            logger.error(e) { "Error while loading Plugged, the mod has been disabled" }
        }
    }
}
