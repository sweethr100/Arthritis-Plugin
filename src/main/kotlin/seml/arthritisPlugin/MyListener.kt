package seml.arthritisPlugin

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
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

        val key = NamespacedKey(plugin, "medicine")
        player.discoverRecipe(key)

        plugin.playerAge[player.uniqueId] = 10


    }

    @EventHandler
    fun onPlayerJump(event: PlayerStatisticIncrementEvent) {
        if (event.statistic == Statistic.JUMP) {
            val player = event.player
            val age = plugin.playerAge[player.uniqueId] ?: 10

            plugin.addPlayerArthritis(event.player,plugin.getJumpRunListFromMyValue()[(age/10)-1][0])
        }
    }

    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
        if (event.isSprinting) {
            val player = event.player
            val age = plugin.playerAge[player.uniqueId] ?: 10

            plugin.addPlayerArthritis(event.player,plugin.getJumpRunListFromMyValue()[(age/10)-1][1])
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            plugin.addPlayerArthritis(player,0)
            // 또는 원하는 효과 부여 코드
        }, 1L)

    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (item.type == Material.BEDROCK) {
            val meta = item.itemMeta
            if (meta != null && meta.displayName == "§c관절염 치료제") {
                // 설치 취소!
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item

        if (item.type == Material.BEDROCK) {

            val meta = item.itemMeta
            if (meta != null && meta.displayName == "§c관절염 치료제") {

                // 관절염 지수 초기화
                plugin.Arthritis.getScore(player).score = 0

                // 플레이어에게 효과 부여
                player.removePotionEffect(PotionEffectType.SLOWNESS)

                // 아이템 제거
                event.item.amount -= 1

                player.playSound(
                    player.location,
                    Sound.BLOCK_BREWING_STAND_BREW,
                    1f,
                    1f
                )
            }
        }
    }





}