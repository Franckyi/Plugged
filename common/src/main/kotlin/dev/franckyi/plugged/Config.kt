package dev.franckyi.plugged

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dev.architectury.platform.Platform
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.lang.reflect.Type
import java.util.concurrent.CompletableFuture
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val logger = KotlinLogging.logger {}

data class Config(val general: General = General(),
                  val triggerPreset: String = "chill",
                  val customTriggerPreset: TriggerPreset = TriggerPreset(),
                  /*val transitions: Transitions = Transitions(),
                  val triggers: MutableMap<String, TriggerConfig> = defaultTriggers()*/) {

    data class General(var useEmbeddedServer: Boolean = false,
                       var websocketServerUrl: String = "ws://localhost:12345",
                       var autoConnectAfterJoiningWorld: Boolean = true,
                       var autoScanDevicesAfterConnect: Boolean = true,
                       var autoControlDevices: Boolean = true)


    data class TriggerPreset(var transition: TransitionConfig? = LinearTransitionConfig(),
                             var triggers: TriggerMap = TriggerMap())

    sealed interface TransitionConfig

    data class LinearTransitionConfig(var increasingStep: Double = 0.1, var decreasingStep: Double = 0.05) : TransitionConfig

    /*@Serializable
    data class Triggers(val hurt: HurtTrigger = HurtTrigger(),
                        val attack: AttackTrigger = AttackTrigger(),
                        val death: DeathTrigger = DeathTrigger(),
                        val kill: KillTrigger = KillTrigger(),
                        val breakBlock: BreakBlockTrigger = BreakBlockTrigger(),
                        val breakingBlock: BreakingBlockTrigger = BreakingBlockTrigger(),
                        val placeBlock: PlaceBlockTrigger = PlaceBlockTrigger(),
                        val pickupItem: PickupItemTrigger = PickupItemTrigger(),
                        val pickupXp: PickupXpTrigger = PickupXpTrigger(),
                        val craft: CraftTrigger = CraftTrigger(),
                        val smelt: SmeltTrigger = SmeltTrigger(),
                        val enchant: EnchantTrigger = EnchantTrigger(),
                        val repair: RepairTrigger = RepairTrigger()
    )*/

    interface TriggerConfig {
        fun createTrigger(): Trigger
    }

    data class ContinuousTriggerConfig(val intensity: String) : TriggerConfig {
        override fun createTrigger(): Trigger {
            TODO("Not yet implemented")
        }
    }

    data class SimpleTriggerConfig(val intensity: String, val duration: String) : TriggerConfig {
        override fun createTrigger(): Trigger {
            TODO("Not yet implemented")
        }
    }

    class TriggerMap : LinkedHashMap<String, TriggerConfig>()

    companion object {
        private val gson = GsonBuilder().run {
            setPrettyPrinting()
            registerTypeAdapter(TriggerMap::class.java, TriggerMapSerializer())
            registerTypeAdapter(TransitionConfig::class.java, TransitionConfigSerializer())
            create()
        }

        lateinit var instance: Config

        fun load(): CompletableFuture<Void> = CompletableFuture.runAsync {
            logger.info { "Loading configuration" }
            Platform.getConfigFolder().resolve("plugged.json").let { path ->
                instance = if (path.exists()) {
                    path.readText().let { gson.fromJson(it, Config::class.java) }
                } else {
                    logger.info { "Creating default configuration" }
                    Config()
                }
            }
            save().get()
        }.thenRun { logger.info { "Configuration loaded" } }

        fun save(): CompletableFuture<Void> = CompletableFuture.runAsync {
            logger.info { "Saving configuration" }
            Platform.getConfigFolder().resolve("plugged.json").writeText(gson.toJson(instance))
        }.thenRun { logger.info { "Configuration saved" } }

        private class TriggerMapSerializer : JsonDeserializer<TriggerMap> {
            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): TriggerMap = TriggerMap().apply {
                json.asJsonObject.entrySet().forEach { (key, value) ->
                    Registries.triggers[key]?.let { this[key] = context.deserialize(value, it.type) }
                        ?: logger.warn { "Unknown trigger type $key" }
                }
            }
        }

        private class TransitionConfigSerializer : JsonDeserializer<Config.TransitionConfig> {
            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): Config.TransitionConfig? {
                json.asJsonObject.let { obj ->
                    Registries.transitions[obj["type"].asString]?.let { return context.deserialize(json, it.type) }
                        ?: logger.warn { "Unknown transition ${obj["type"].asString}" }
                }
                return null
            }
        }
    }


}
