package dev.franckyi.plugged

import mu.KotlinLogging
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

private val logger = KotlinLogging.logger {}

object Controller {
    private var lastIntensity: Double = 0.0
    private val ongoingEvents = mutableListOf<PluggedEvent>()
    internal val enabledDevices = mutableSetOf<Int>()

    fun tick() {
        ongoingEvents.forEach(PluggedEvent::tick)
        ongoingEvents.removeIf { it is PluggedSimpleEvent && it.remainingDuration < 0 }
        if (Connector.client.connected) {
            val targetIntensity = ongoingEvents.maxOfOrNull(PluggedEvent::computeIntensity) ?: 0.0
            if (targetIntensity != lastIntensity) {
                val intensity = if (Config.instance.transitions.enabled) {
                    if (lastIntensity > targetIntensity) max(targetIntensity, lastIntensity - Config.instance.transitions.decreasingStep)
                    else min(targetIntensity, lastIntensity + Config.instance.transitions.increasingStep)
                } else targetIntensity
                val roundedIntensity = intensity.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                logger.info { "Changing vibration speed to $roundedIntensity" }
                Connector.client.devices
                    .filterKeys { it in enabledDevices }
                    .forEach {
                        logger.info { "Changing vibration speed to $roundedIntensity for device ${it.value.name}" }
                        it.value.vibrate(roundedIntensity)
                    }
                lastIntensity = roundedIntensity
            }
        }
    }

    fun triggerEvent(trigger: SimpleTrigger, vararg args: Pair<String, Number>) {
        if (!trigger.enabled || !Connector.client.connected) return
        val duration = trigger.durationExpr.intResultWith(*args).coerceAtLeast(0)
        if (duration > 0) {
            ongoingEvents.add(PluggedSimpleEvent(trigger, duration, args))
            logger.info { "Added event from trigger ${trigger.javaClass.simpleName} with duration $duration" }
        }
    }

    fun updateContinuousEvent(trigger: ContinuousTrigger, vararg args: Pair<String, Number>) {
        if (!trigger.enabled || !Connector.client.connected) return
        ongoingEvents.find { it.trigger == trigger }?.let {
            (it as PluggedLiveEvent).args = args
        } ?: run {
            ongoingEvents += PluggedLiveEvent(trigger, args)
            logger.info { "Added live event from trigger ${trigger.javaClass.simpleName}" }
        }
    }

    fun removeContinuousEvent(trigger: ContinuousTrigger) {
        if (!trigger.enabled || !Connector.client.connected) return
        ongoingEvents.find { it.trigger == trigger }?.let {
            logger.info { "Removed live event from trigger ${trigger.javaClass.simpleName}" }
            ongoingEvents.remove(it)
        }
    }

    sealed interface PluggedEvent {
        val trigger: Trigger
        fun computeIntensity(): Double
        fun tick()
    }

    private class PluggedSimpleEvent(
        override val trigger: SimpleTrigger,
        val totalDuration: Int = 0,
        val args: Array<out Pair<String, Number>>,
        var remainingDuration: Int = totalDuration
    ) : PluggedEvent {
        override fun computeIntensity(): Double {
            val duration = totalDuration - remainingDuration
            return trigger.intensityExpr.resultWith(
                "t" to duration,
                "ttot" to totalDuration,
                "trem" to remainingDuration,
                "tprog" to duration.toDouble() / totalDuration,
                *args
            )
        }

        override fun tick() { remainingDuration-- }
    }

    private class PluggedLiveEvent(
        override val trigger: ContinuousTrigger,
        var args: Array<out Pair<String, Number>>,
        var duration: Int = 0
    ) : PluggedEvent {
        override fun computeIntensity(): Double = trigger.intensityExpr.resultWith("t" to duration, *args)

        override fun tick() { duration++ }
    }
}