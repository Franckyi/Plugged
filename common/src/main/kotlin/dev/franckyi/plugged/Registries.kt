package dev.franckyi.plugged

import net.minecraft.text.Text
import kotlin.reflect.KClass

private class ImmutableMapWrapper<K, V>(private val map: Map<K, V>) : Map<K, V> by map

object Registries {
    private val _triggers = mutableMapOf<String, TriggerEntry>()
    val triggers: Map<String, TriggerEntry> = ImmutableMapWrapper(_triggers)
    private val _transitions = mutableMapOf<String, TransitionEntry>()
    val transitions: Map<String, TransitionEntry> = ImmutableMapWrapper(_transitions)
    private val _presets = mutableMapOf<String, PresetEntry>()
    val presets: Map<String, PresetEntry> = ImmutableMapWrapper(_presets)

    fun registerTrigger(key: String, name: Text, type: Class<out Config.TriggerConfig>) {
        _triggers[key] = TriggerEntry(name, type)
    }

    fun registerTransition(key: String, name: Text, type: Class<out Config.TransitionConfig>) {
        _transitions[key] = TransitionEntry(name, type)
    }

    internal fun init() {
        registerTrigger("hurt", Config.SimpleTriggerConfig::class)
        registerTrigger("attack", Config.SimpleTriggerConfig::class)
        registerTrigger("death", Config.SimpleTriggerConfig::class)
        registerTrigger("kill", Config.SimpleTriggerConfig::class)
        registerTrigger("breakBlock", Config.SimpleTriggerConfig::class)
        registerTrigger("breakingBlock", Config.ContinuousTriggerConfig::class)
        registerTrigger("placeBlock", Config.SimpleTriggerConfig::class)
        registerTrigger("pickupItem", Config.SimpleTriggerConfig::class)
        registerTrigger("pickupXp", Config.SimpleTriggerConfig::class)
        registerTrigger("craft", Config.SimpleTriggerConfig::class)
        registerTrigger("smelt", Config.SimpleTriggerConfig::class)
        registerTrigger("enchant", Config.SimpleTriggerConfig::class)
        registerTrigger("repair", Config.SimpleTriggerConfig::class)
        registerTransition("linear", Config.LinearTransitionConfig::class)
        registerPreset("")
    }

    private fun registerTrigger(key: String, type: KClass<out Config.TriggerConfig>) {
        registerTrigger(key, Text.translatable("plugged.trigger.$key"), type.java)
    }

    private fun registerTransition(key: String, type: KClass<out Config.TransitionConfig>) {
        registerTransition(key, Text.translatable("plugged.transition.$key"), type.java)
    }

    private fun registerTransition(key: String, preset: Preset) {
        registerTransition(key, Text.translatable("plugged.transition.$key"), type.java)
    }

    data class TriggerEntry(val text: Text, val type: Class<out Config.TriggerConfig>)

    data class TransitionEntry(val text: Text, val type: Class<out Config.TransitionConfig>)

    data class PresetEntry(val text: Text, val preset: Preset)
}