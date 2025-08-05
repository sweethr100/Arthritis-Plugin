package seml.arthritisPlugin

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import io.papermc.paper.registry.data.dialog.body.DialogBody.item
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerStatisticIncrementEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.potion.PotionEffectType

class MyListener(private val plugin: ArthritisPlugin): Listener {


    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (plugin.Ages.getScore(player).score == 0) {
            plugin.Ages.getScore(player).score = 10

            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                plugin.setPlayerAgeRandom(player)
            }, 20L)

        }

        player.discoverRecipe(NamespacedKey(plugin, "medicine"))
        player.discoverRecipe(NamespacedKey(plugin, "age_changer"))

    }

    @EventHandler
    fun onPlayerJump(event: PlayerStatisticIncrementEvent) {
        if (event.statistic == Statistic.JUMP) {
            val player = event.player
            val age = plugin.Ages.getScore(player).score

            plugin.addPlayerArthritis(event.player,plugin.AgeList[(age/10)-1][0])
        }
    }

    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
        if (event.isSprinting) {
            val player = event.player
            val age = plugin.Ages.getScore(player).score

            plugin.addPlayerArthritis(event.player,plugin.AgeList[(age/10)-1][1])
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

        if (item.type == Material.MILK_BUCKET) {
            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                plugin.addPlayerArthritis(player,0)
            }, 1L)
        }

        else if (item.type == Material.BEDROCK) {

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

        else if (item.type == Material.PAPER) {
            val meta = item.itemMeta
            if (meta != null && meta.displayName == "§b나이 랜덤 변경권") {
                plugin.setPlayerAgeRandom(player)
            }
        }
    }





}