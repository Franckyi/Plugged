package dev.franckyi.plugged

import dev.franckyi.kbuttplug.api.ButtplugClient
import dev.franckyi.kbuttplug.api.ButtplugDevice
import dev.franckyi.kbuttplug.api.ButtplugException
import dev.franckyi.kbuttplug.api.ButtplugLogHandler
import mu.KotlinLogging
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

private val logger = KotlinLogging.logger {}

object Connector {
    lateinit var logHandler: ButtplugLogHandler
    lateinit var client: ButtplugClient

    fun setup() {
        logHandler = ButtplugLogHandler.createSlf4jLogger()
        logger.info { "Buttplug Log Handler initialized" }
        client = ButtplugClient.create("Plugged Minecraft Mod").apply {
            onDeviceAdded = ::onDeviceAdded
            onDeviceRemoved = ::onDeviceRemoved
            onError = ::onErrorReceived
            onScanningFinished = ::onScanningFinished
            onDisconnect = ::onServerDisconnect
        }
        logger.info { "Buttplug Client initialized" }
    }

    fun connectEmbedded() {
        logger.info { "Connecting to Embedded Buttplug Server" }
        client.connectLocal().whenComplete { _, e -> onConnected(e) }
    }

    fun connectWebsocket(): CompletableFuture<Void> {
        if (client.connected) return CompletableFuture.completedFuture(null)
        logger.info { "Connecting to Websocket Buttplug Server at address ${Config.instance.connection.websocketServerUrl}" }
        return client.connectWebsocket(Config.instance.connection.websocketServerUrl).whenComplete { _, e ->
            if (e != null) showToast(Text.translatable("plugged.toast.connection_error"))
            onConnected(e)
        }
    }

    fun disconnect(): CompletableFuture<Void> {
        if (!client.connected) return CompletableFuture.completedFuture(null)
        logger.info { "Disconnecting" }
        return client.disconnect().whenComplete { _, error ->
            if (error != null) {
                logger.error(error) { "Error while disconnecting" }
            } else {
                logger.info { "Disconnected" }
            }
        }
    }

    fun close() {
        client.close()
        logger.info { "Buttplug Client closed" }
        logHandler.close()
        logger.info { "Buttplug Log Handler closed" }
    }

    private fun onConnected(error: Throwable?) {
        if (error != null) {
            logger.error(error) { "Error while connecting" }
        } else {
            logger.info { "Connected to Buttplug Server" }
            if (Config.instance.general.autoScanDevicesAfterConnect) {
                client.startScanning().whenComplete { _, error1 ->
                    if (error1 != null) logger.error(error1) { "Error while scanning for devices" }
                    DeviceListScreen.instance?.updateButtons()
                }
            }
        }
    }

    private fun onDeviceAdded(device: ButtplugDevice) {
        logger.info { "Device added: ${device.name}" }
        if (Config.instance.general.autoControlDevices && device.canVibrate) Controller.enabledDevices += device.index
        DeviceListScreen.instance?.deviceListWidget?.addEntry(device)
    }

    private fun onDeviceRemoved(device: ButtplugDevice) {
        logger.info { "Device removed: ${device.name}" }
        Controller.enabledDevices -= device.index
        DeviceListScreen.instance?.deviceListWidget?.removeEntry(device)
    }

    private fun onErrorReceived(e: ButtplugException) {
        logger.error(e) { "Error received: ${e.message}" }
        mc.player!!.sendMessage(
            Text.literal("[Plugged] Error received: ${e.message}, check logs for more info").formatted(Formatting.RED)
        )
    }

    private fun onScanningFinished() {
        logger.info { "Scanning finished" }
        DeviceListScreen.instance?.updateButtons()
    }

    private fun onServerDisconnect() {
        logger.info { "Server disconnected" }
        DeviceListScreen.instance?.let {
            it.deviceListWidget.children().clear()
            it.updateButtons()
        } ?: run {
            showToast(Text.translatable("plugged.toast.disconnected"))
        }
    }
}
