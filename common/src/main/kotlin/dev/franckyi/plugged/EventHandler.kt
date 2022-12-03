package dev.franckyi.plugged

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.PlayerEvent
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.minecraft.block.BlockState

object EventHandler {
    private val entityHitByPlayer = Int2IntOpenHashMap()
    
    fun trigger(name: String, vararg args: Pair<String, Number>) {
        TODO("Not yet implemented")
    }

    fun tick() {
        val iterator = entityHitByPlayer.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.setValue(entry.value - 1)
            if (entry.value <= 0) iterator.remove()
        }
    }

    fun init() {
        EntityEvent.LIVING_HURT.register { entity, source, damage ->
            if (entity.world.isClient && damage > 0) {
                if (entity == mc.player) trigger("hurt", "dmg" to damage)
                else if (source.attacker == mc.player) {
                    trigger("attack", "dmg" to damage)
                    entityHitByPlayer[entity.id] = 5
                }
            }
            EventResult.pass()
        }
        EntityEvent.LIVING_DEATH.register { entity, _ ->
            if (entity.world.isClient && entity.id in entityHitByPlayer) {
                entityHitByPlayer -= entity.id
                trigger("kill")
            }
            EventResult.pass()
        }
        PlayerEvent.CRAFT_ITEM.register { player, _, _ ->
            if (player.world.isClient) trigger("craft")
        }
        PlayerEvent.SMELT_ITEM.register { player, _ ->
            if (player.world.isClient) trigger("smelt")
        }
    }

    private fun mixinTrigger(name: String, vararg args: Pair<String, Number>) {
        if (Plugged.modInitialized) trigger(name, *args)
    }

    @JvmStatic
    fun onDeath() = mixinTrigger("death")

    @JvmStatic
    fun onBlockBreak(state: BlockState) = mixinTrigger("breakBlock", "hard" to state.block.hardness, "res" to state.block.blastResistance)

    @JvmStatic
    fun onBlockBreaking(progress: Int) {
        /*if (Plugged.modInitialized) {
            if (progress < 0 || progress >= 10) Controller.removeContinuousEvent(Config.instance.triggers.breakingBlock)
            else Controller.updateContinuousEvent(
                Config.instance.triggers.breakingBlock,
                "progress" to (progress + 1) / 10.0
            )
        }*/
    }

    @JvmStatic
    fun onBlockPlaced(state: BlockState) = mixinTrigger("placeBlock", "hard" to state.block.hardness, "res" to state.block.blastResistance)

    @JvmStatic
    fun onPickupItem() = mixinTrigger("pickupItem")

    @JvmStatic
    fun onPickupXp(amount: Int, orbSize: Int) = mixinTrigger("pickupXp", "amount" to amount, "orbsize" to orbSize)

    @JvmStatic
    fun onEnchant(cost: Int, level: Int) = mixinTrigger("enchant", "cost" to cost, "lvl" to level)

    @JvmStatic
    fun onRepair(cost: Int) = mixinTrigger("repair", "cost" to cost)
}