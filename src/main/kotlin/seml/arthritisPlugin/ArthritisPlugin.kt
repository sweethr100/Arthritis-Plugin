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
import org.bukkit.inventory.ShapelessRecipe

class ArthritisPlugin : JavaPlugin() {

    lateinit var scoreboard: Scoreboard
    lateinit var Arthritis: Objective
    lateinit var Ages: Objective
    lateinit var AgeList: List<List<Int>>

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

        AgeList = getAgeListFromConfig()
        startArthritisActionBarTask()
        randomArthritisIncrease()
        addCustomRecipes()
    }

    fun getAgeListFromConfig(): List<List<Int>> {
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

    fun addCustomRecipes() {

        val consumable = consumable()
            .consumeSeconds(2f)
            .animation(ItemUseAnimation.EAT)
            .build()



        val medicine = ItemStack(Material.BEDROCK)
        medicine.setData(DataComponentTypes.CONSUMABLE, consumable)
        medicine.setData(DataComponentTypes.CUSTOM_NAME, Component.text("§c관절염 치료제"))

        val medicineIngredients = config.getStringList("medicineRecipe")

        val medicineRecipe = ShapelessRecipe(NamespacedKey(this, "medicine"), medicine)

        for (ingredient in medicineIngredients) {
            val material = Material.getMaterial(ingredient.uppercase())
            if (material != null) {
                medicineRecipe.addIngredient(material)
            } else {
                logger.warning("Invalid material in recipe: $ingredient")
            }
        }

        server.addRecipe(medicineRecipe)

        val ageChanger = ItemStack(Material.PAPER)
        ageChanger.setData(DataComponentTypes.CONSUMABLE, consumable)
        ageChanger.setData(DataComponentTypes.CUSTOM_NAME, Component.text("§b나이 랜덤 변경권"))

        val ageChangerIngredients = config.getStringList("ageChangerRecipe")

        val ageChangerRecipe = ShapelessRecipe(NamespacedKey(this, "age_changer"), ageChanger)

        for (ingredient in ageChangerIngredients) {
            val material = Material.getMaterial(ingredient.uppercase())
            if (material != null) {
                ageChangerRecipe.addIngredient(material)
            } else {
                logger.warning("Invalid material in recipe: $ingredient")
            }
        }

        server.addRecipe(ageChangerRecipe)

    }

    fun setPlayerAgeRandom(player: Player) {
        val random = (1..AgeList.size).random()
        Ages.getScore(player).score = random * 10

        player.sendTitle("§a당신의 나이 : ", "§f${random * 10}세", 10, 70, 20)
    }

}
