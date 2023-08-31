package com.github.Ringoame196

import com.github.Ringoame196.Entity.Zombie
import com.github.Ringoame196.data.Data
import com.github.Ringoame196.data.GET
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class Hoe {
    fun clickEvent(player: Player, e: PlayerInteractEvent) {
        val action = e.action
        e.isCancelled = true
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            val offHandItem = player.inventory.itemInOffHand.itemMeta
            if (offHandItem == null || !offHandItem.displayName.contains("[ゾンビ召喚]")) {
                Player().errormessage("オフハンドにゾンビを持ってください", player)
                return
            }
            if (player.location.subtract(0.0, 1.0, 0.0).block.type != Material.GLASS && Data.DataManager.gameData.playMap == "map1") {
                Player().errormessage("${ChatColor.RED}ガラスの上で実行してください(ガラスの上に行くには、自陣の後ろにあるボタンをクリック)", player)
                return
            }

            val zombie = player.inventory.itemInOffHand
            Zombie().summonSorting(player, zombie)
            val teamName = GET().opposingTeamname(GET().teamName(player).toString())
            if (Data.DataManager.teamDataMap[teamName]?.zombieNotification == true) {
                Team().sound(Sound.ENTITY_ZOMBIE_AMBIENT, teamName!!)
            }
            removeOffHandItem(player)
            durable(player)
        }
    }
    fun removeOffHandItem(player: Player) {
        if (player.gameMode == GameMode.CREATIVE) { return }
        val itemInOffHand = player.inventory.itemInOffHand
        val oneItem = itemInOffHand.clone()
        oneItem.amount -= 1
        player.inventory.setItemInOffHand(oneItem)
    }
    fun durable(player: Player) {
        val itemInHand = player.inventory.itemInMainHand
        val damageableMeta = itemInHand.itemMeta as org.bukkit.inventory.meta.Damageable
        val currentDamage = damageableMeta.damage
        if (currentDamage < itemInHand.type.maxDurability) {
            damageableMeta.damage = currentDamage + 1
            itemInHand.itemMeta = damageableMeta

            // 耐久値が最大になったらアイテムを削除
            if (currentDamage + 1 >= itemInHand.type.maxDurability) {
                player.inventory.setItemInMainHand(null)
                player.playSound(player.location, Sound.ITEM_SHIELD_BREAK, 1f, 1f)
            }
        }
    }
    fun exclusion(player: Player, inventory: Inventory) {
        for (i in 0..8) {
            val item = inventory.getItem(i) ?: continue
            val itemName = item.itemMeta?.displayName
            if (itemName?.contains("${ChatColor.YELLOW}[ゾンビ召喚]") == true) { continue }
            player.inventory.addItem(item)
            inventory.setItem(i, ItemStack(Material.AIR))
        }
    }
}
