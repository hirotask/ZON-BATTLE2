package com.github.Ringoame196.Game

import com.github.Ringoame196.Block
import com.github.Ringoame196.Entity.Shop
import com.github.Ringoame196.GUI
import com.github.Ringoame196.Give
import com.github.Ringoame196.ParticipatingPlayer
import com.github.Ringoame196.RandomChest
import com.github.Ringoame196.Sign
import com.github.Ringoame196.Team
import com.github.Ringoame196.data.Data
import com.github.Ringoame196.data.GET
import com.github.Ringoame196.data.Gamedata
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

class GameSystem {
    fun gameSettingGUIClick(plugin: Plugin, player: Player, item: ItemStack, e: InventoryClickEvent) {
        e.isCancelled = true
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f)
        val displayName = item.itemMeta?.displayName
        when (displayName) {
            "${ChatColor.AQUA}ゲームスタート" -> start(plugin, player)
            "${ChatColor.RED}終了" -> stop(player)
            "${ChatColor.YELLOW}ショップ召喚" -> Shop().summon(player.location, null)
            "${ChatColor.GREEN}参加" -> ParticipatingPlayer().inAndout(player)
            "${ChatColor.YELLOW}実験所へ" -> Bukkit.dispatchCommand(player, "mvtp jikken")
            "${ChatColor.YELLOW}ロビーへ" -> Bukkit.dispatchCommand(player, "mvtp world")
            "${ChatColor.YELLOW}バトルへ" -> Bukkit.dispatchCommand(player, "mvtp BATTLE")
            "${ChatColor.YELLOW}トレジャーバトル" -> Bukkit.dispatchCommand(player, "mvtp TreBAT")
            "${ChatColor.BLUE}プレイヤー" -> {
                GUI().joinPlayers(player)
                return
            }
            "${ChatColor.GREEN}座標設定" -> {
                Map().selectworld(player)
                return
            }
            "${ChatColor.RED}強制再生" -> {
                if (!e.isShiftClick) { return }
                reproduction(plugin)
            }
        }
        if (item.type == Material.CHEST && displayName?.contains("マップ") == true) {
            Map().settingOpenGUI(displayName, player)
            return
        }
        if (item.type == Material.ENDER_EYE && e.isShiftClick) {
            setlocation(item, player)
            e.isCancelled = true
        } else if (item.type == Material.REDSTONE) {
            if (GET().status()) {
                com.github.Ringoame196.Entity.Player().errormessage("ゲーム最中に変更はできません", player)
                return
            }
            Scoreboard().add("gameData", "map", 1)
            Map().resetMapName()
            val playMap = Map().getMapName()
            player.sendMessage("${ChatColor.AQUA}${playMap}を選択しました")
            e.isCancelled = true
            GUI().gamesettingGUI(player)
            return
        }
        player.closeInventory()
    }
    fun setlocation(item: ItemStack, player: Player) {
        when (item.itemMeta?.displayName) {
            "${ChatColor.RED}shop" -> Data.DataManager.LocationData.redshop = player.location
            "${ChatColor.BLUE}shop" -> Data.DataManager.LocationData.blueshop = player.location
            "${ChatColor.RED}spawn" -> Data.DataManager.LocationData.redspawn = player.location
            "${ChatColor.BLUE}spawn" -> Data.DataManager.LocationData.bluespawn = player.location
            "${ChatColor.YELLOW}ランダムチェスト" -> Data.DataManager.LocationData.randomChest = player.location.block.location

            "${ChatColor.RED}mshop" -> Data.DataManager.LocationData.mredshop = player.location
            "${ChatColor.BLUE}mshop" -> Data.DataManager.LocationData.mblueshop = player.location
            "${ChatColor.RED}mspawn" -> Data.DataManager.LocationData.mredspawn = player.location
            "${ChatColor.BLUE}mspawn" -> Data.DataManager.LocationData.mbluespawn = player.location
            "${ChatColor.YELLOW}mランダムチェスト1" -> Data.DataManager.LocationData.mrandomChest1 = player.location.block.location
            "${ChatColor.YELLOW}mランダムチェスト2" -> Data.DataManager.LocationData.mrandomChest2 = player.location.block.location
            "${ChatColor.RED}mspawnZombie1" -> Data.DataManager.LocationData.mredZombiespawn1 = player.location
            "${ChatColor.RED}mspawnZombie2" -> Data.DataManager.LocationData.mredZombiespawn2 = player.location
            "${ChatColor.BLUE}mspawnZombie1" -> Data.DataManager.LocationData.mblueZombiespawn1 = player.location
            "${ChatColor.BLUE}mspawnZombie2" -> Data.DataManager.LocationData.mblueZombiespawn2 = player.location

            "${ChatColor.RED}tmshop" -> Data.DataManager.LocationData.tmredshop = player.location
            "${ChatColor.BLUE}tmshop" -> Data.DataManager.LocationData.tmblueshop = player.location
            "${ChatColor.RED}tmspawn" -> Data.DataManager.LocationData.tmredspawn = player.location
            "${ChatColor.BLUE}tmspawn" -> Data.DataManager.LocationData.tmbluespawn = player.location
            "${ChatColor.YELLOW}tmランダムチェスト" -> Data.DataManager.LocationData.tmrandomChest = player.location.block.location
            "${ChatColor.YELLOW}tmZombie1" -> Data.DataManager.LocationData.tmZombiespawn1 = player.location.block.location
            "${ChatColor.YELLOW}tmZombie2" -> Data.DataManager.LocationData.tmZombiespawn2 = player.location
            "${ChatColor.YELLOW}tmZombie3" -> Data.DataManager.LocationData.tmZombiespawn3 = player.location
        }
        player.sendMessage("${ChatColor.AQUA}座標設定完了")

        val filePath = "plugins/ZON-BATTLE2/location_data.yml"
        Data.DataManager.LocationData.saveToFile(filePath)
    }
    fun settingScoreboard() {
        Scoreboard().make("point", "Point")
        Scoreboard().setTeamScore()
    }

    fun start(plugin: Plugin, player: Player) {
        if (Scoreboard().getSize("participatingPlayer") == 0) { return }
        if (Scoreboard().getValue("gameData", "map") == 0) {
            com.github.Ringoame196.Entity.Player().errormessage("マップ選択してください", player)
            return
        }
        if (GET().status()) {
            player.sendMessage("${ChatColor.RED}既にゲームはスタートしています")
            return
        }
        Scoreboard().set("gameData", "status", 1)
        reset()
        Data.DataManager.LocationData.let {
            if (it.redshop == null || it.blueshop == null || it.redspawn == null || it.bluespawn == null) {
                NotSet(player)
                return
            }
        }
        Map().mapSetting()
        if (Bukkit.getScoreboardManager()?.mainScoreboard?.getTeam("red") == null) { Team().make("red", ChatColor.RED, "${ChatColor.RED}[赤チーム]") }
        if (Bukkit.getScoreboardManager()?.mainScoreboard?.getTeam("blue") == null) { Team().make("blue", ChatColor.BLUE, "${ChatColor.BLUE}[青チーム]") }
        Timer().feverSet()
        settingScoreboard()

        var c = 5
        object : BukkitRunnable() {
            override fun run() {
                if (c > 0) {
                    ParticipatingPlayer().message("${ChatColor.GREEN}[カウントダウン]開始まで$c")
                    ParticipatingPlayer().sound(Sound.BLOCK_DISPENSER_FAIL)
                    c--
                } else {
                    Team().division()
                    Sign().numberdisplay("ゲーム進行中")
                    Bukkit.broadcastMessage("${ChatColor.GREEN}攻防戦ゲームスタート！！")
                    ParticipatingPlayer().sound(Sound.ENTITY_ENDER_DRAGON_AMBIENT)
                    Timer().GameTimer(plugin)
                    this.cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 20L) // 0Lは遅延時間、20Lは繰り返し間隔（1秒=20tick
    }
    fun NotSet(player: Player) {
        player.sendMessage("${ChatColor.RED}未設定の項目があります")
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f)
    }

    fun stop(player: Player) {
        if (!GET().status()) {
            player.sendMessage("${ChatColor.RED}ゲームは開始していません")
            return
        }
        gameEndSystem("${ChatColor.RED}攻防戦ゲーム強制終了！！", null)
    }

    fun gameend(winTeam: String) {
        gameEndSystem("${ChatColor.RED}攻防戦ゲーム終了！！", winTeam)
    }
    fun gameEndSystem(message: String, winTeam: String?) {
        Data.DataManager.gameData.bossBar.removeAll()
        Bukkit.broadcastMessage("${ChatColor.YELLOW}[攻防戦]$message")
        for (loopPlayer in Bukkit.getOnlinePlayers()) {
            val join = Scoreboard().getValue("participatingPlayer", loopPlayer.name) ?: 0
            if (join != 0) {
                loopPlayer.sendMessage("${ChatColor.AQUA}[ゲーム時間]${GET().minutes(GET().gameTime())}")
                loopPlayer.sendMessage("${ChatColor.RED}${winTeam}チームの勝利")
                loopPlayer.playSound(loopPlayer.location, Sound.BLOCK_ANVIL_USE, 1f, 1f)
                loopPlayer.inventory.clear()
                loopPlayer.walkSpeed = 0.2f
                if (loopPlayer.isOp) {
                    loopPlayer.inventory.addItem(Give().gameSetting())
                }
                for (effect in loopPlayer.activePotionEffects) {
                    loopPlayer.removePotionEffect(effect.type)
                }
                Bukkit.getWorld("world")?.let { loopPlayer.teleport(it.spawnLocation) }
                if (winTeam == null) { continue }
                if (winTeam == GET().teamName(loopPlayer)) {
                    for (i in 1..5) {
                        loopPlayer.inventory.addItem(Give().coin())
                    }
                    Scoreboard().add("RankingData_win", loopPlayer.uniqueId.toString(), 1)
                } else {
                    loopPlayer.inventory.addItem(Give().coin())
                }
            }
        }
        for (player in Bukkit.getWorld("BATTLE")?.players!!) {
            if (player.gameMode != GameMode.CREATIVE) {
                Bukkit.getWorld("world")?.let { player.teleport(it.spawnLocation) }
            }
        }
        Sign().numberdisplay("参加者:0人")
        Scoreboard().set("gameData", "status", 0)
        reset()
        Team().make("red", ChatColor.RED, "${ChatColor.RED}[赤チーム]")
        Team().make("blue", ChatColor.BLUE, "${ChatColor.BLUE}[青チーム]")
        Ranking().updateRankingScoreboard()
        Scoreboard().make("participatingPlayer", "ParticipatingPlayer")
    }
    fun resetGameData() {
        Scoreboard().make("gameData", "GameData")
        Scoreboard().set("gameData", "map", 0)
        Scoreboard().set("gameData", "status", 0)
        Scoreboard().set("gameData", "magnification", 1)
    }

    fun adventure(e: org.bukkit.event.Event, player: Player) {
        if (player.gameMode == GameMode.CREATIVE) { return }
        if (e is Cancellable) { e.isCancelled = true }
    }
    fun reset() {
        Block().deleteRevival()
        Scoreboard().set("gameData", "reload", 0)
        for (entity in Bukkit.getWorld("BATTLE")?.entities!!) {
            if (entity !is Player) {
                entity.remove()
            }
        }
        Team().delete()
        RandomChest().reset()
        if (GET().status()) { return }
        Data.DataManager.gameData = Gamedata() // gameData を新しい Gamedata インスタンスに置き換える
        resetGameData()
    }
    fun playersJoin(playerName: String, sender: Player) {
        val player = Bukkit.getPlayer(playerName.replace("${ChatColor.YELLOW}", "")) ?: return
        val join = Scoreboard().getValue("participatingPlayer", playerName) ?: 0
        if (join == 0 || join == 3) {
            ParticipatingPlayer().inAndout(player)
            player.sendMessage("${ChatColor.RED}※${sender.displayName}があなたのゲーム参加を操作しました")
        } else {
            Scoreboard().set("participatingPlayer", playerName, join + 1)
        }
        GUI().joinPlayers(sender)
    }
    fun reproduction(plugin: Plugin) {
        Timer().GameTimer(plugin)
        Block().deleteRevival()
        Scoreboard().set("gameData", "reload", 0)
        for (loopPlayer in Bukkit.getOnlinePlayers()) {
            val join: Int = Scoreboard().getValue("participatingPlayer", loopPlayer.name) ?: 0
            if (join != 0) {
                Data.DataManager.gameData.bossBar.addPlayer(loopPlayer)
                loopPlayer.sendMessage("${ChatColor.RED}強制続行します")
                loopPlayer.playSound(loopPlayer, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1f, 1f)
            }
        }
    }
}
