package dev.franckyi.plugged

import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object ConfigScreen {
    fun build(parent: Screen): Screen = config("plugged.config") {
        parentScreen = parent
        setSavingRunnable { Config.save() }
        category("general") {
            boolean("use_embedded_server", Config.instance.general.useEmbeddedServer) {
                setTooltip(*tooltip(8))
                setSaveConsumer { Config.instance.general.useEmbeddedServer = it }
            }
            boolean("auto_scan_devices_after_connect", Config.instance.general.autoScanDevicesAfterConnect) {
                setTooltip(*tooltip(2))
                setSaveConsumer { Config.instance.general.autoScanDevicesAfterConnect = it }
            }
            boolean("auto_control_devices", Config.instance.general.autoControlDevices) {
                setTooltip(*tooltip(2))
                setSaveConsumer { Config.instance.general.autoControlDevices = it }
            }
        }
        category("connection") {
            string("websocket_server_url", Config.instance.connection.websocketServerUrl) {
                setTooltip(*tooltip(4))
                setSaveConsumer { Config.instance.connection.websocketServerUrl = it }
            }
            boolean("auto_connect_after_joining_world", Config.instance.connection.autoConnectAfterJoiningWorld) {
                setTooltip(*tooltip(2))
                setSaveConsumer { Config.instance.connection.autoConnectAfterJoiningWorld = it }
            }
        }
        category("transitions") {
            boolean("enabled", Config.instance.transitions.enabled) {
                setTooltip(*tooltip(1))
                setSaveConsumer { Config.instance.transitions.enabled = it }
            }
            double("increasing_step", Config.instance.transitions.increasingStep) {
                setTooltip(*tooltip(4))
                setSaveConsumer { Config.instance.transitions.increasingStep = it }
            }
            double("decreasing_step", Config.instance.transitions.decreasingStep) {
                setTooltip(*tooltip(4))
                setSaveConsumer { Config.instance.transitions.decreasingStep = it }
            }
        }
        /*category("triggers") {
            simpleTrigger("hurt", Config.instance.triggers.hurt)
            simpleTrigger("attack", Config.instance.triggers.attack)
            simpleTrigger("death", Config.instance.triggers.death)
            simpleTrigger("kill", Config.instance.triggers.kill)
            simpleTrigger("break_block", Config.instance.triggers.breakBlock)
            continuousTrigger("breaking_block", Config.instance.triggers.breakingBlock)
            simpleTrigger("place_block", Config.instance.triggers.placeBlock)
            simpleTrigger("pickup_item", Config.instance.triggers.pickupItem)
            simpleTrigger("pickup_xp", Config.instance.triggers.pickupXp)
            simpleTrigger("craft", Config.instance.triggers.craft)
            simpleTrigger("smelt", Config.instance.triggers.smelt)
            simpleTrigger("enchant", Config.instance.triggers.enchant)
            simpleTrigger("repair", Config.instance.triggers.repair)
        }*/
    }

    private lateinit var builder: ConfigEntryBuilder

    private val ctx: MutableList<String> = mutableListOf()

    private val ctxName: String get() = ctx.joinToString(".")

    private fun config(name: String, block: ConfigBuilder.() -> Unit): Screen = ConfigBuilder.create().applyCtx {
        ctx.clear()
        title = name.ctx
        builder = entryBuilder()
        block()
    }.build()

    private fun ConfigBuilder.category(name: String, block: ConfigCategory.() -> Unit): ConfigCategory =
        getOrCreateCategory(name.ctx).applyCtx(block)

    private fun ConfigCategory.boolean(name: String, default: Boolean, block: BooleanToggleBuilder.() -> Unit) =
        addEntry(builder.startBooleanToggle(name.ctx, default).applyCtx(block).build())

    private fun ConfigCategory.string(name: String, default: String, block: StringFieldBuilder.() -> Unit) =
        addEntry(builder.startStrField(name.ctx, default).applyCtx(block).build())

    private fun ConfigCategory.double(name: String, default: Double, block: DoubleFieldBuilder.() -> Unit) =
        addEntry(builder.startDoubleField(name.ctx, default).applyCtx(block).build())

    private fun ConfigCategory.int(name: String, default: Int, block: IntFieldBuilder.() -> Unit) =
        addEntry(builder.startIntField(name.ctx, default).applyCtx(block).build())

    private fun ConfigCategory.subCategory(name: String, block: SubCategoryBuilder.() -> Unit) =
        addEntry(builder.startSubCategory(name.ctx).applyCtx(block).build())

    private fun SubCategoryBuilder.boolean(name: String, default: Boolean, block: BooleanToggleBuilder.() -> Unit) =
        add(builder.startBooleanToggle(name.ctx, default).applyCtx(block).build())

    private fun SubCategoryBuilder.string(name: String, default: String, block: StringFieldBuilder.() -> Unit) =
        add(builder.startStrField(name.ctx, default).applyCtx(block).build())

    private fun SubCategoryBuilder.double(name: String, default: Double, block: DoubleFieldBuilder.() -> Unit) =
        add(builder.startDoubleField(name.ctx, default).applyCtx(block).build())

    private fun SubCategoryBuilder.int(name: String, default: Int, block: IntFieldBuilder.() -> Unit) =
        add(builder.startIntField(name.ctx, default).applyCtx(block).build())

    private fun ConfigCategory.simpleTrigger(name: String, trigger: SimpleTrigger) = subCategory(name) {
        setTooltip(*tooltip(1))
        simpleTriggerEntries(trigger)
    }

    private fun ConfigCategory.continuousTrigger(name: String, trigger: ContinuousTrigger) = subCategory(name) {
        setTooltip(*tooltip(1))
        continuousTriggerEntries(trigger)
    }

    private fun SubCategoryBuilder.simpleTriggerEntries(trigger: SimpleTrigger) {
        continuousTriggerEntries(trigger)
        add(builder.startStrField(Text.translatable("plugged.config.triggers.duration"), trigger.duration)
            .setTooltip(Text.translatable("plugged.config.triggers.duration.0"))
            .setSaveConsumer { trigger.duration = it }
            .build())
    }

    private fun SubCategoryBuilder.continuousTriggerEntries(trigger: ContinuousTrigger) {
        add(builder.startBooleanToggle(Text.translatable("plugged.config.triggers.enabled"), trigger.enabled)
            .setTooltip(Text.translatable("plugged.config.triggers.enabled.0"))
            .setSaveConsumer { trigger.enabled = it }
            .build())
        add(builder.startStrField(Text.translatable("plugged.config.triggers.intensity"), trigger.intensity)
            .setTooltip(Text.translatable("plugged.config.triggers.intensity.0"))
            .setSaveConsumer { trigger.intensity = it }
            .build())
    }

    private fun tooltip(lines: Int): Array<Text> = Array(lines) { Text.translatable("$ctxName.$it") }

    private val String.ctx: Text
        get() {
            this@ConfigScreen.ctx += this
            return Text.translatable(ctxName)
        }

    private fun <T> T.applyCtx(block: T.() -> Unit): T {
        block()
        ctx.removeLast()
        return this
    }
}