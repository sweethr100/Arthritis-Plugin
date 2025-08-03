package seml.arthritisPlugin

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Scoreboard
import java.util.UUID

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

    fun startArthritisActionBarTask() {
        server.scheduler.runTaskTimer(this, Runnable {
            for (player in server.onlinePlayers) {
                // 관절염 지수 가져오기(예시)
                val value = Arthritis.getScore(player).score
                player.sendActionBar("§a당신의 관절염 지수: $value")
            }
        }, 0L, 1L) // 0틱 후 시작, 20틱(1초)마다 반복
    }
}
