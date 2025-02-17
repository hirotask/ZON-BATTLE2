package com.github.Ringoame196

import com.github.Ringoame196.Entity.Shop
import com.github.Ringoame196.Entity.TNT
import com.github.Ringoame196.Game.Point
import com.github.Ringoame196.Game.Scoreboard
import com.github.Ringoame196.data.PetData
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class Item {
    fun clickSystem(player: Player, item: ItemStack?, block: Block?, e: PlayerInteractEvent, plugin: Plugin) {
        val itemName = item?.itemMeta?.displayName.toString()
        val itemType = item?.type
        if (itemName.contains("[ペット召喚]")) {
            e.isCancelled = true
            PetData().switch(itemName.replace("[ペット召喚]", ""), player, block, null)
        } else if (itemName.contains("[捕獲]")) {
            e.isCancelled = true
            PetData().switch(itemName.replace("[捕獲]", ""), player, block, item?.itemMeta?.lore?.get(0)?.toDouble())
        } else {
            when {
                itemType == Material.EMERALD -> {
                    money(player, itemName)
                    return
                }

                itemType == Material.COMMAND_BLOCK && itemName == "ゲーム設定" -> {
                    e.isCancelled = true
                    if (!player.isOp) {
                        player.inventory.setItemInMainHand(ItemStack(Material.AIR))
                    } else {
                        GUI().gamesettingGUI(player)
                    }
                    return
                }

                block?.type == Material.OAK_WALL_SIGN -> {
                    e.isCancelled = true
                    Sign().click(player, block, plugin)
                    return
                }

                itemName == "${ChatColor.GREEN}リモートショップ" -> {
                    Shop().gui(player)
                    e.isCancelled = true
                }

                itemName == "${ChatColor.YELLOW}チャット" -> {
                    GUI().messageBook(player)
                    return
                }

                itemName == "${ChatColor.GOLD}ファイヤボール" -> {
                    val playerLocation: Location = player.location.add(0.0, 0.5, 0.0)
                    val direction: org.bukkit.util.Vector = playerLocation.direction
                    val spawnLocation: Location = playerLocation.add(direction.multiply(2.0))
                    val fireball: Fireball = player.world.spawn(spawnLocation, Fireball::class.java)
                    fireball.velocity = direction.multiply(0.25) // 速度を調整できます
                    fireball.yield = 2.5F
                    fireball.setIsIncendiary(false)
                }

                itemName == "${ChatColor.RED}TNT" -> TNT().summon(player, plugin)

                itemName == "アイテムドロップ" -> inventoryDrop(player)
                else -> return
            }
        }
        removeitem(player)
    }
    fun money(player: Player, itemName: String) {
        val pointString = itemName.replace("${ChatColor.GREEN}", "").replace("p", "")
        val point = pointString.toIntOrNull() ?: return
        Point().add(player, point, false)
        removeitem(player)
    }
    fun removeitem(player: org.bukkit.entity.Player) {
        if (player.gameMode == GameMode.CREATIVE) { return }
        val itemInHand = player.inventory.itemInMainHand
        val oneItem = itemInHand.clone()
        oneItem.amount = 1
        player.inventory.removeItem(oneItem)
    }
    fun inventoryDrop(player: Player) {
        for (slot in 0 until 36) { // メインのインベントリスロットは0から35まで
            val item = player.inventory.getItem(slot) ?: continue
            player.inventory.removeItem(item)
            player.location.world?.dropItemNaturally(player.location.add(0.0, 2.0, 0.0), item)
        }
    }
    fun capture(entity: Entity, player: Player) {
        if (entity.scoreboardTags.contains("redPet")) {
            Scoreboard().remove("RedTeamSystem", "petCount", 1)
        } else if (entity.scoreboardTags.contains("bluePet")) {
            Scoreboard().remove("BlueTeamSystem", "petCount", 1)
        }
        val item = ItemStack(Material.DRAGON_EGG)
        val meta = item.itemMeta
        meta?.setDisplayName("[捕獲]${entity.customName}")
        val hp = mutableListOf<String>()
        if (entity is LivingEntity) {
            hp.add(entity.health.toString())
        } else {
            hp.add("0.0")
        }
        meta?.lore = hp
        item.setItemMeta(meta)
        player.inventory.setItemInMainHand(item)

        entity.remove()
    }
}
