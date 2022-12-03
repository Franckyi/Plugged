package dev.franckyi.plugged

import dev.franckyi.kbuttplug.api.ButtplugDevice
import dev.franckyi.kbuttplug.api.DeviceAttributeType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Text

val mc: MinecraftClient get() = MinecraftClient.getInstance()

fun showToast(text: Text) = SystemToast.add(mc.toastManager, SystemToast.Type.TUTORIAL_HINT, Text.translatable("plugged"), text)

val ButtplugDevice.canVibrate: Boolean get() = hasAttribute(DeviceAttributeType.VIBRATE)

val ButtplugDevice.hasBatteryLevel: Boolean get() = hasAttribute(DeviceAttributeType.BATTERY_LEVEL)

val ButtplugDevice.hasRSSILevel: Boolean get() = hasAttribute(DeviceAttributeType.RSSI_LEVEL)