package dev.franckyi.plugged

import com.mojang.blaze3d.systems.RenderSystem
import dev.franckyi.kbuttplug.api.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture
import kotlin.properties.Delegates

class DeviceListScreen : Screen(Text.literal("Plugged Device List")) {
    private var initialized: Boolean = false
    lateinit var deviceListWidget: PluggedDeviceListWidget
    private var connectButtonsWidth by Delegates.notNull<Int>()
    private var deviceButtonsWidth by Delegates.notNull<Int>()
    private var scanningButtonsWidth by Delegates.notNull<Int>()
    private var menuButtonsWidth by Delegates.notNull<Int>()
    private lateinit var connectButton: ButtonWidget
    private lateinit var scanningButton: ButtonWidget
    private lateinit var stopAllDevicesButton: ButtonWidget
    private lateinit var enableDeviceButton: ButtonWidget
    private lateinit var stopDeviceButton: ButtonWidget
    private lateinit var configureDeviceButton: ButtonWidget

    override fun init() {
        super.init()
        if (initialized) {
            deviceListWidget.updateSize(width, height, 84, this.height - 58)
        } else {
            initialized = true
            deviceListWidget = PluggedDeviceListWidget(mc, width, height, 84, height - 58, 30)
        }
        connectButtonsWidth = 200.coerceAtMost((width - 50 - 12) / 3)
        deviceButtonsWidth = 200.coerceAtMost((width - 50 - 12) / 5)
        scanningButtonsWidth = 200.coerceAtMost((width - 50 - 12) / 4)
        menuButtonsWidth = 200.coerceAtMost((width - 50 - 12) / 3)

        connectButton = addDrawableChild(ButtonWidget(width / 2 + 3, 32, connectButtonsWidth, 20, if (Connector.client.connected) DISCONNECT else CONNECT) { onClickConnect() })

        scanningButton = addDrawableChild(ButtonWidget(width / 2 - scanningButtonsWidth / 2 - 3, 58, scanningButtonsWidth, 20, if (Connector.client.scanning) STOP_SCANNING else START_SCANNING) { onClickScanning() })
        stopAllDevicesButton = addDrawableChild(ButtonWidget(width / 2 + scanningButtonsWidth / 2 + 3, 58, scanningButtonsWidth, 20, Text.literal("Stop all Devices")) { stopAllDevices() })

        addSelectableChild(deviceListWidget)

        enableDeviceButton = addDrawableChild(ButtonWidget(width / 2 - deviceButtonsWidth - 3, height - 52, deviceButtonsWidth, 20, Text.literal("Enable")) { onClickEnableDevice() })
        stopDeviceButton = addDrawableChild(ButtonWidget(width / 2 + 3, height - 52, deviceButtonsWidth, 20, Text.literal("Stop Device")) { stopDevice() })
        configureDeviceButton = addDrawableChild(ButtonWidget(width / 2 + deviceButtonsWidth + 9, height - 52, deviceButtonsWidth, 20, Text.literal("Configure...")) { })

        addDrawableChild(ButtonWidget(width / 2 - menuButtonsWidth - 3, height - 26, menuButtonsWidth, 20, Text.literal("Close")) { close() })
        addDrawableChild(ButtonWidget(width / 2 + 3, height - 26, menuButtonsWidth, 20, Text.of("Mod Configuration")) { mc.setScreen(ConfigScreen.build(this)) })

        updateButtons()
    }

    override fun render(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        val buttonWidthsTop = 200.coerceAtMost((width - 50 - 12) / 5)
        deviceListWidget.render(matrices, mouseX, mouseY, delta)
        DrawableHelper.drawCenteredTextWithShadow(matrices, mc.textRenderer, title.asOrderedText(), width / 2, 11, 0xFFFFFF)
        DrawableHelper.drawTextWithShadow(matrices, mc.textRenderer, if (Connector.client.connected) CONNECTED else DISCONNECTED, width / 2 - scanningButtonsWidth * 3 / 2 - 6, 38, 0xFFFFFF)
        DrawableHelper.drawTextWithShadow(matrices, mc.textRenderer, if (Connector.client.scanning) IS_SCANNING else NOT_SCANNING, width / 2 - scanningButtonsWidth * 3 / 2 - 6, 64, 0xFFFFFF)
        DrawableHelper.drawTextWithShadow(matrices, mc.textRenderer, Text.literal("Selected Device:"), width / 2 - buttonWidthsTop * 2 - 6, height - 46, 0xFFFFFF)
        super.render(matrices, mouseX, mouseY, delta)
    }

    fun updateButtons() {
        connectButton.message = if (Connector.client.connected) DISCONNECT else CONNECT
        connectButton.active = !Plugged.embeddedServerRunning
        if (Connector.client.connected) {
            scanningButton.active = true
            scanningButton.message = if (Connector.client.scanning) STOP_SCANNING else START_SCANNING
            stopAllDevicesButton.active = true
            val entry = deviceListWidget.selectedOrNull
            if (entry != null && entry.device.canVibrate) {
                //configureDeviceButton.active = true
                enableDeviceButton.active = true
                stopDeviceButton.active = true
                enableDeviceButton.message = if (entry.enabled) DISABLE else ENABLE
            } else {
                //configureDeviceButton.active = false
                enableDeviceButton.active = false
                stopDeviceButton.active = false
                enableDeviceButton.message = Text.literal("Enable")
            }
            configureDeviceButton.active = false
        } else {
            scanningButton.active = false
            scanningButton.message = Text.literal("Start Scanning")
            stopAllDevicesButton.active = false
            enableDeviceButton.active = false
            enableDeviceButton.message = Text.literal("Enable")
            stopDeviceButton.active = false
            configureDeviceButton.active = false
        }
    }

    private fun onClickConnect() {
        connectButton.active = false
        val future = if (Connector.client.connected) Connector.disconnect() else Connector.connectWebsocket()
        future.whenComplete { _, _ ->
            connectButton.active = true
            updateButtons()
        }
    }

    private fun onClickEnableDevice() {
        deviceListWidget.selectedOrNull?.let {
            it.enabled = !it.enabled
            updateButtons()
        }
    }

    private fun stopDevice() {
        deviceListWidget.selectedOrNull?.let {
            stopDeviceButton.active = false
            it.device.stop().whenComplete { _, _ -> stopDeviceButton.active = true }
        }
    }

    private fun onClickScanning() {
        scanningButton.active = false
        val future = if (Connector.client.scanning) Connector.client.stopScanning() else Connector.client.startScanning()
        future.whenComplete { _, _ ->
            scanningButton.active = true
            updateButtons()
        }
    }

    private fun stopAllDevices() {
        stopAllDevicesButton.active = false
        Connector.client.stopAllDevices().whenComplete { _, _ -> stopAllDevicesButton.active = true }
    }

    companion object {
        val instance: DeviceListScreen? get() = if (mc.currentScreen is DeviceListScreen) mc.currentScreen as DeviceListScreen else null

        private val CONNECT: Text get() = Text.literal("Connect").formatted(Formatting.GREEN)
        private val DISCONNECT: Text get() = Text.literal("Disconnect").formatted(Formatting.RED)
        private val CONNECTED: Text get() = Text.literal("Server Status: ").append(Text.literal("Connected").formatted(Formatting.GREEN))
        private val DISCONNECTED: Text get() = Text.literal("Server Status: ").append(Text.literal("Disconnected").formatted(Formatting.RED))
        private val ENABLE: Text get() = Text.literal("Enable").formatted(Formatting.GREEN)
        private val DISABLE: Text get() = Text.literal("Disable").formatted(Formatting.RED)
        private val IS_SCANNING: Text get() = Text.literal("Status: Scanning...")
        private val NOT_SCANNING: Text get() = Text.literal("Status: Not Scanning")
        private val START_SCANNING: Text get() = Text.literal("Start Scanning").formatted(Formatting.GREEN)
        private val STOP_SCANNING: Text get() = Text.literal("Stop Scanning").formatted(Formatting.RED)
    }

    inner class PluggedDeviceListWidget(
        client: MinecraftClient,
        width: Int,
        height: Int,
        top: Int,
        bottom: Int,
        itemHeight: Int
    ) : AlwaysSelectedEntryListWidget<PluggedDeviceListWidget.PluggedDeviceEntry>(client, width, height, top, bottom, itemHeight) {
        private val deviceEntryMap: MutableMap<Int, PluggedDeviceEntry> = mutableMapOf()

        init {
            Connector.client.devices.values.forEach { addEntry(it) }
            (1..10).forEach { addEntry(FakeButtplugDevice(it, "Fake Device $it")) }
        }

        override fun setSelected(entry: PluggedDeviceEntry?) {
            super.setSelected(entry)
            updateButtons()
        }

        fun addEntry(device: ButtplugDevice) {
            PluggedDeviceEntry(device).apply {
                deviceEntryMap[device.index] = this
                addEntry(this)
            }
        }

        fun removeEntry(device: ButtplugDevice) {
            removeEntry(deviceEntryMap[device.index])
        }

        inner class PluggedDeviceEntry(val device: ButtplugDevice) : Entry<PluggedDeviceEntry>() {
            private var _enabled = Controller.enabledDevices.contains(device.index)
            var enabled: Boolean
                get() = _enabled
                set(value) {
                    _enabled = value
                    if (value) Controller.enabledDevices += device.index else Controller.enabledDevices -= device.index
                }

            override fun render(matrices: MatrixStack, index: Int, y: Int, x: Int, entryWidth: Int, entryHeight: Int, mouseX: Int, mouseY: Int, hovered: Boolean, tickDelta: Float) {
                val enabledText = if (enabled) Text.translatable("plugged.gui.device.enabled") else Text.translatable("plugged.gui.device.disabled")
                drawTextWithShadow(matrices, mc.textRenderer, Text.literal(device.name), x + 4, y + 4, 0xFFFFFF)
                if (!device.canVibrate) drawTextWithShadow(matrices, mc.textRenderer, Text.translatable("plugged.gui.device.not_supported"), x + 4, y + 17, 0xFF5555)
                drawTextWithShadow(matrices, mc.textRenderer, enabledText, x + entryWidth - mc.textRenderer.getWidth(enabledText) - 25, y + 9, 0xFFFFFF)

                RenderSystem.setShader(GameRenderer::getPositionTexShader)
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
                RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE)
                drawTexture(matrices, x + entryWidth - 15, y + 7, 0f, (176 + 0 * 8).toFloat(), 10, 8, 256, 256)
            }

            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                this@PluggedDeviceListWidget.setSelected(this)
                return true
            }

            override fun getNarration(): Text = Text.literal(device.name)
        }
    }
}

private class FakeButtplugDevice(override val index: Int, override val name: String) : ButtplugDevice {
    override val attributes: Map<DeviceAttributeType, DeviceAttributeData> = if (index % 2 == 0) mapOf() else mapOf(DeviceAttributeType.VIBRATE to DeviceAttributeData(0, emptyList(), emptyList()))

    override fun close() {}

    override fun fetchBatteryLevel(): CompletableFuture<Double> = CompletableFuture.completedFuture(1.0)

    override fun fetchRSSILevel(): CompletableFuture<Int> = CompletableFuture.completedFuture(0)

    override fun linear(component: LinearData): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun linear(duration: Int, position: Double): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun linear(components: Iterable<LinearData>): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun linear(components: Map<Int, LinearData>): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun rotate(component: RotateData): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun rotate(speed: Double, clockwise: Boolean): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun rotate(components: Iterable<RotateData>): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun rotate(components: Map<Int, RotateData>): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun stop(): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun vibrate(speed: Double): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun vibrate(speeds: Iterable<Double>): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun vibrate(speeds: Map<Int, Double>): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun rawRead(endpoint: Endpoint, expectedLength: Int, timeout: Int): CompletableFuture<ByteArray> = CompletableFuture.completedFuture(
        ByteArray(0)
    )

    override fun rawWrite(endpoint: Endpoint, data: ByteArray, writeWithResponse: Boolean): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun rawSubscribe(endpoint: Endpoint, callback: EndpointCallback): CompletableFuture<Void> = CompletableFuture.completedFuture(null)

    override fun rawUnsubscribe(endpoint: Endpoint): CompletableFuture<Void> = CompletableFuture.completedFuture(null)
}
