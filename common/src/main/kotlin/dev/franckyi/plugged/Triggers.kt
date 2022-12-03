package dev.franckyi.plugged

import dev.franckyi.karith.api.KthException
import dev.franckyi.karith.api.KthExpression
import dev.franckyi.karith.api.defaultContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import mu.KotlinLogging
import net.minecraft.text.Text
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}
private val kthCtx = defaultContext()

object Triggers {
    private val triggers = mutableMapOf<String, Trigger>()

    fun reload() {

    }
}

sealed interface Trigger

class SimpleTrigger(enabled: Boolean, intensity: String, duration: String, private vararg val durationVars: String) : ContinuousTrigger(enabled, intensity, "t", "ttot", "trem", "tprog", *durationVars) {
    private lateinit var _duration: String
    internal var durationInitialized = false
    var duration: String
        get() = _duration
        set(value) {
            try {
                durationExpr = kthCtx.expressionWith(value, *durationVars)
                _duration = value
                durationInitialized = true
            } catch (e: KthException) {
                logger.error(e) { "Error while parsing duration expression for trigger ${this.javaClass.simpleName}: $value" }
            }
        }
    lateinit var durationExpr: KthExpression

    init {
        this.duration = duration
    }
}

class ContinuousTrigger(var enabled: Boolean, intensity: String, private vararg val intensityVars: String) : Trigger {
    private lateinit var _intensity: String
    internal var intensityInitialized = false
    var intensity: String
        get() = _intensity
        set(value) {
            try {
                intensityExpr = kthCtx.expressionWith(value, *intensityVars)
                _intensity = value
                intensityInitialized = true
            } catch (e: KthException) {
                logger.error(e) { "Error while parsing intensity expression for trigger ${this.javaClass.simpleName}: $value" }
            }
        }
    lateinit var intensityExpr: KthExpression

    init {
        this.intensity = intensity
    }
}
/*
@Serializable(with = HurtTriggerSerializer::class)
class HurtTrigger(enabled: Boolean = true, intensity: String = "0.5", duration: String = "20") : SimpleTrigger(enabled, intensity, duration, "dmg")
private class HurtTriggerSerializer : SimpleTriggerSerializer<HurtTrigger>(::HurtTrigger, "0.5", "20")

@Serializable(with = AtttackTriggerSerializer::class)
class AttackTrigger(enabled: Boolean = true, intensity: String = "0.5", duration: String = "10") : SimpleTrigger(enabled, intensity, duration, "dmg")
private class AtttackTriggerSerializer : SimpleTriggerSerializer<AttackTrigger>(::AttackTrigger, "0.5", "10")

@Serializable(with = KillTriggerSerializer::class)
class KillTrigger(enabled: Boolean = true, intensity: String = "0.2", duration: String = "20") : SimpleTrigger(enabled, intensity, duration)
private class KillTriggerSerializer : SimpleTriggerSerializer<KillTrigger>(::KillTrigger, "0.2", "20")

@Serializable(with = BreakBlockTriggerSerializer::class)
class BreakBlockTrigger(enabled: Boolean = true, intensity: String = "0.2", duration: String = "10") : SimpleTrigger(enabled, intensity, duration, "hard", "res")
private class BreakBlockTriggerSerializer : SimpleTriggerSerializer<BreakBlockTrigger>(::BreakBlockTrigger, "0.2", "10")

@Serializable(with = BreakingBlockTriggerSerializer::class)
class BreakingBlockTrigger(enabled: Boolean = true, intensity: String = "0.1") : ContinuousTrigger(enabled, intensity, "progress")
private class BreakingBlockTriggerSerializer : ContinuousTriggerSerializer<BreakingBlockTrigger>(::BreakingBlockTrigger, "0.1")

@Serializable(with = PlaceBlockTriggerSerializer::class)
class PlaceBlockTrigger(enabled: Boolean = true, intensity: String = "0.1", duration: String = "10") : SimpleTrigger(enabled, intensity, duration, "hard", "res")
private class PlaceBlockTriggerSerializer : SimpleTriggerSerializer<PlaceBlockTrigger>(::PlaceBlockTrigger, "0.1", "10")

@Serializable(with = PickupItemTriggerSerializer::class)
class PickupItemTrigger(enabled: Boolean = true, intensity: String = "0.1", duration: String = "10") : SimpleTrigger(enabled, intensity, duration)
private class PickupItemTriggerSerializer : SimpleTriggerSerializer<PickupItemTrigger>(::PickupItemTrigger, "0.1", "10")

@Serializable(with = XpChangeTriggerSerializer::class)
class PickupXpTrigger(enabled: Boolean = true, intensity: String = "0.2", duration: String = "20") : SimpleTrigger(enabled, intensity, duration, "amount", "orbsize")
private class XpChangeTriggerSerializer : SimpleTriggerSerializer<PickupXpTrigger>(::PickupXpTrigger, "0.2", "20")

@Serializable(with = DeathTriggerSerializer::class)
class DeathTrigger(enabled: Boolean = true, intensity: String = "1.0", duration: String = "100") : SimpleTrigger(enabled, intensity, duration)
private class DeathTriggerSerializer : SimpleTriggerSerializer<DeathTrigger>(::DeathTrigger, "1.0", "100")

@Serializable(with = CraftTriggerSerializer::class)
class CraftTrigger(enabled: Boolean = true, intensity: String = "0.2", duration: String = "20") : SimpleTrigger(enabled, intensity, duration)
private class CraftTriggerSerializer : SimpleTriggerSerializer<CraftTrigger>(::CraftTrigger, "0.2", "20")

@Serializable(with = SmeltTriggerSerializer::class)
class SmeltTrigger(enabled: Boolean = true, intensity: String = "0.2", duration: String = "20") : SimpleTrigger(enabled, intensity, duration)
private class SmeltTriggerSerializer : SimpleTriggerSerializer<SmeltTrigger>(::SmeltTrigger, "0.2", "20")

@Serializable(with = EnchantTriggerSerializer::class)
class EnchantTrigger(enabled: Boolean = true, intensity: String = "0.2", duration: String = "20") : SimpleTrigger(enabled, intensity, duration, "cost", "lvl")
private class EnchantTriggerSerializer : SimpleTriggerSerializer<EnchantTrigger>(::EnchantTrigger, "0.2", "20")

@Serializable(with = RepairTriggerSerializer::class)
class RepairTrigger(enabled: Boolean = true, intensity: String = "0.2", duration: String = "20") : SimpleTrigger(enabled, intensity, duration, "cost")
private class RepairTriggerSerializer : SimpleTriggerSerializer<RepairTrigger>(::RepairTrigger, "0.2", "20")

@Serializable
private class SerializedTrigger(val enabled: Boolean, val intensity: String, val duration: String)

@Serializable
private class SerializedLiveTrigger(val enabled: Boolean, val intensity: String)

private abstract class SimpleTriggerSerializer<T : SimpleTrigger>(private val triggerFactory: (Boolean, String, String) -> T,
                                                                  private val defaultIntensity: String,
                                                                  private val defaultDuration: String) : KSerializer<T> {
    private val delegateSerializer = SerializedTrigger.serializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("Trigger", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeSerializableValue(delegateSerializer, SerializedTrigger(value.enabled, value.intensity, value.duration))

    override fun deserialize(decoder: Decoder): T {
        val res = decoder.decodeSerializableValue(delegateSerializer).let { triggerFactory(it.enabled, it.intensity, it.duration) }
        if (!res.durationInitialized) res.duration = defaultDuration
        if (!res.intensityInitialized) res.intensity = defaultIntensity
        return res
    }
}

private abstract class ContinuousTriggerSerializer<T : ContinuousTrigger>(private val triggerFactory: (Boolean, String) -> T,
                                                                          private val defaultIntensity: String) : KSerializer<T> {
    private val delegateSerializer = SerializedLiveTrigger.serializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("LiveTrigger", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeSerializableValue(delegateSerializer, SerializedLiveTrigger(value.enabled, value.intensity))

    override fun deserialize(decoder: Decoder): T {
        val res = decoder.decodeSerializableValue(delegateSerializer).let { triggerFactory(it.enabled, it.intensity) }
        if (!res.intensityInitialized) res.intensity = defaultIntensity
        return res
    }
}*/