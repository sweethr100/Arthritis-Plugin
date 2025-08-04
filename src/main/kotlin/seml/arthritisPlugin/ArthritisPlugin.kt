package seml.arthritisPlugin

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.players
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID
import kotlin.math.min
import org.bukkit.Sound

class ArthritisPlugin : JavaPlugin() {

    lateinit var scoreboard: Scoreboard
    lateinit var Arthritis: org.bukkit.scoreboard.Objective

    val playerAge = mutableMapOf<UUID, Int>()

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(MyListener(this), this)

        scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        Arthritis = scoreboard.getObjective("arthritis") ?:
        scoreboard.registerNewObjective(
            "arthritis", "dummy"
        )

        startArthritisActionBarTask()
        randomArthritisIncrease()
    }

    fun getJumpRunListFromMyValue(): List<List<Int>> {
        val myvalue = mutableListOf<List<Int>>()
        val section = config.getConfigurationSection("myvalue") ?: return emptyList()
        for (key in section.getKeys(false)) {
            val jump = section.getInt("$key.jump", 0)
            val run = section.getInt("$key.run", 0)
            myvalue.add(listOf(jump, run))
        }
        return myvalue
    }

    fun addPlayerArthritis(player: Player, diff: Int) {

        var stats = Arthritis.getScore(player).score

        if (stats == 100 && diff > 0) {
            player.addPotionEffect(PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0, false, false))
        }

        stats = min(stats + diff , 100)

        if (stats >= 50) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, -1, 0, false, false))
        }
        if (stats >= 100) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, -1, 1, false, false))
        }

        Arthritis.getScore(player).score = stats
    }

    fun startArthritisActionBarTask() {
        server.scheduler.runTaskTimer(this, Runnable {
            for (player in server.onlinePlayers) {
                // 관절염 지수 가져오기(예시)
                val value = Arthritis.getScore(player).score
                player.sendActionBar("§a당신의 관절염 지수: $value")
            }
        }, 0L, 1L) // 0틱 후 시작, 20틱(1초)마다 반복
    }

    fun randomArthritisIncrease() {
        server.scheduler.runTaskTimer(this, Runnable {

            val players = server.onlinePlayers

            if (players.isNotEmpty()) {

                var stackCount = 0
                for (player in players) {
                    stackCount = stackCount + player.inventory.contents
                        .count { it != null && it.type != Material.AIR }
                }

                if (stackCount / players.size >= 15) {
                    val randomPlayer = players.random()
                    randomPlayer.sendMessage("다리를 삐끗했어요!")
                    randomPlayer.playSound(
                        randomPlayer.location,
                        Sound.ENTITY_PLAYER_HURT,
                        1f,
                        1f
                    )
                    addPlayerArthritis(randomPlayer, 5)
                }

            }
        }, 0L, 1200L)

    }
}
