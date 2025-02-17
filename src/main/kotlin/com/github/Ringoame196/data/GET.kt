package com.github.Ringoame196.data

import com.github.Ringoame196.Game.Scoreboard
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.inventory.Inventory

class GET {
    fun teamName(entity: Entity): String? {
        return if (entity is Player) {
            entity.scoreboard.teams.firstOrNull { it.hasEntry(entity.name) }?.name
        } else {
            if (entity.scoreboardTags.contains("red")) {
                "red"
            } else if (entity.scoreboardTags.contains("blue")) {
                "blue"
            } else { null }
        }
    }

    fun joinTeam(player: Player): Boolean {
        val jointeam = when (teamName(player)) {
            "red" -> true
            "blue" -> true
            else -> false
        }
        return jointeam
    }

    fun maxHP(shop: Villager): Double? {
        return shop.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value
    }

    fun hp(shop: Villager): Double {
        return shop.health
    }

    fun opposingTeamname(teamName: String): String? {
        val opoposingTeamname = when (teamName) {
            "red" -> "blue"
            "blue" -> "red"
            else -> null
        }
        return opoposingTeamname
    }

    fun status(): Boolean {
        return (Scoreboard().getValue("gameData", "status")) == 1
    }

    fun locationTitle(location: org.bukkit.Location?): String {
        if (location == null) {
            return "null"
        }

        val x = location.x.toInt()
        val y = location.y.toInt()
        val z = location.z.toInt()

        return "x:$x,y:$y,z:$z"
    }

    fun minutes(time: Int): String {
        val minutes = time / 60 + 0
        val seconds = time % 60 + 0
        return "${minutes}分${seconds}秒"
    }

    fun owner(entity: Entity): Player? {
        for (tag in entity.scoreboardTags) {
            if (!tag.contains("owner:")) {
                continue
            }
            val name = tag.replace("owner:", "")
            val player = Bukkit.getPlayer(name) ?: return null
            return player
        }
        return null
    }
    fun shop(entity: Entity): Boolean {
        return entity.scoreboardTags.contains("shop")
    }
    fun getNearestEntityOfType(location: Location, target: EntityType?, radius: Double, tag: String?): Entity? {
        var nearestEntity: Entity? = null
        var nearestDistanceSquared = Double.MAX_VALUE

        for (entity in location.world!!.getNearbyEntities(location, radius, radius, radius)) {
            if (entity.type == target) {
                if (tag != null && !entity.scoreboardTags.contains(tag)) { continue }
                if (entity is Player && entity.gameMode != GameMode.SURVIVAL) { continue }
                if (entity is Villager && !GET().shop(entity)) { continue }
                val distanceSquared = entity.location.distanceSquared(location)

                if (distanceSquared < nearestDistanceSquared) {
                    nearestDistanceSquared = distanceSquared
                    nearestEntity = entity
                }
            }
        }

        return nearestEntity
    }
    fun getTeamRevivalTime(teamName: String): Int {
        return Scoreboard().getValue(GET().getTeamSystemScoreName(teamName), "revivalTime") ?: 5
    }
    fun getTeamScoreName(teamName: String?): String {
        when (teamName) {
            "red" -> return "RedTeam"
            "blue" -> return "BlueTeam"
        }
        return null.toString()
    }
    fun getTeamSystemScoreName(teamName: String?): String {
        when (teamName) {
            "red" -> return "RedTeamSystem"
            "blue" -> return "BlueTeamSystem"
        }
        return null.toString()
    }
    fun getTeamshop(teamName: String): Villager? {
        val map = Scoreboard().getValue("gameData", "map")
        val location: Location? = when (teamName) {
            "red" -> when (map) {
                1 -> Data.DataManager.LocationData.redshop
                2 -> Data.DataManager.LocationData.mredshop
                3 -> Data.DataManager.LocationData.tmredshop
                else -> return null
            }
            "blue" -> when (map) {
                1 -> Data.DataManager.LocationData.blueshop
                2 -> Data.DataManager.LocationData.mblueshop
                3 -> Data.DataManager.LocationData.tmblueshop
                else -> return null
            }
            else -> return null
        }
        val shop = getNearestEntityOfType(location!!, EntityType.VILLAGER, 3.0, null)
        return if (shop is Villager) {
            shop
        } else {
            null
        }
    }
    fun teamChest(teamName: String): Inventory? {
        return when (teamName) {
            "red" -> Data.DataManager.gameData.redTeamChest
            "blue" -> Data.DataManager.gameData.blueTeamChest
            else -> null
        }
    }
    fun gameTime(): Int {
        return Scoreboard().getValue("gameData", "time")
    }
    fun cooltime(block: Material, teamName: String): Int {
        val fever = Scoreboard().getValue("gameData", "fever") == 1
        if (fever) {
            return -1
        }
        return when (block) {
            Material.DIAMOND_ORE -> 90
            Material.BEDROCK -> -1
            else -> GET().getTeamRevivalTime(teamName)
        }
    }
    fun orePoint(block: Material): Int? {
        return when (block) {
            Material.COAL_ORE -> 1
            Material.IRON_ORE -> 10
            Material.GOLD_ORE -> 20
            Material.DIAMOND_ORE -> 100
            Material.BEDROCK -> 10000
            else -> null
        }
    }
}
