package seml.arthritisPlugin

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerStatisticIncrementEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import kotlin.div
import kotlin.math.min
import org.bukkit.scoreboard.Scoreboard

class MyListener(private val plugin: ArthritisPlugin): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player


        if (plugin.Arthritis.getScore(player).score == null) {
            plugin.Arthritis.getScore(player).score = 0
        }

        plugin.playerAge[player.uniqueId] = 10
    }

    @EventHandler
    fun onPlayerJump(event: PlayerStatisticIncrementEvent) {
        if (event.statistic == Statistic.JUMP) {
            val player = event.player
            val age = plugin.playerAge[player.uniqueId] ?: 10

            addPlayerArthritis(event.player,plugin.getJumpRunListFromMyValue()[(age/10)-1][0])
        }
    }

    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
        if (event.isSprinting) {
            val player = event.player
            val age = plugin.playerAge[player.uniqueId] ?: 10

            addPlayerArthritis(event.player,plugin.getJumpRunListFromMyValue()[(age/10)-1][1])
        }
    }

    fun addPlayerArthritis(player: Player, diff: Int) {

        var stats = plugin.Arthritis.getScore(player).score

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

        plugin.Arthritis.getScore(player).score = stats
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            addPlayerArthritis(player,0)
            // 또는 원하는 효과 부여 코드
        }, 1L)

    }
}