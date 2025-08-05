package seml.arthritisPlugin

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable.consumable
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Scoreboard
import kotlin.math.min
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.scoreboard.Objective
import net.kyori.adventure.text.Component

class ArthritisPlugin : JavaPlugin() {

    lateinit var scoreboard: Scoreboard
    lateinit var Arthritis: Objective
    lateinit var Ages: Objective

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(MyListener(this), this)

        scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        Arthritis = scoreboard.getObjective("arthritis") ?:
        scoreboard.registerNewObjective(
            "arthritis", "dummy"
        )
        Ages = scoreboard.getObjective("ages") ?:
        scoreboard.registerNewObjective(
            "ages", "dummy"
        )

        startArthritisActionBarTask()
        randomArthritisIncrease()
        addCustomRecipe()
    }

    fun getAgeList(): List<List<Int>> {
        val myvalue = mutableListOf<List<Int>>()
        val section = config.getConfigurationSection("ages") ?: return emptyList()
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

    // TODO : config.yml로 레시피 사용자 설정하기

    fun addCustomRecipe() {

        val consumable = consumable()
            .consumeSeconds(2f)
            .animation(ItemUseAnimation.EAT)
            .build()

        val bedrock = ItemStack(Material.BEDROCK)
        bedrock.setData(DataComponentTypes.CONSUMABLE, consumable)
        bedrock.setData(DataComponentTypes.CUSTOM_NAME, Component.text("§c관절염 치료제"))

        val key = NamespacedKey(this, "medicine")
        val recipe = ShapedRecipe(key, bedrock)
        recipe.shape(
            "ABC",
            "DEF",
            "GHI"
        )
        recipe.setIngredient('A', Material.PUFFERFISH)           // 첫째 줄 왼쪽
        recipe.setIngredient('B', Material.DIAMOND_BLOCK)        // 첫째 줄 가운데
        recipe.setIngredient('C', Material.POWDER_SNOW_BUCKET)   // 첫째 줄 오른쪽
        recipe.setIngredient('D', Material.END_CRYSTAL)          // 둘째 줄 왼쪽
        recipe.setIngredient('E', Material.HEART_OF_THE_SEA)  // 둘째 줄 가운데
        recipe.setIngredient('F', Material.BLAZE_ROD)            // 둘째 줄 오른쪽
        recipe.setIngredient('G', Material.RESIN_CLUMP)           // 셋째 줄 왼쪽
        recipe.setIngredient('H', Material.AMETHYST_SHARD)       // 셋째 줄 가운데
        recipe.setIngredient('I', Material.GLOW_INK_SAC) // 셋째 줄 오른쪽
        server.addRecipe(recipe)
    }
}
