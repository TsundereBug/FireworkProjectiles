package com.tsunderebug.fireworkprojectiles

import java.io.FileNotFoundException
import java.util
import java.util.UUID

import org.bukkit.command.{Command, CommandSender}
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.{EntityType, Firework, Player}
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.{Bukkit, Color, FireworkEffect, Material, OfflinePlayer}

import scala.collection.JavaConverters._
import scala.collection.mutable

class FireworkProjectiles extends JavaPlugin {

  var fireworksMap: mutable.Map[OfflinePlayer, (Boolean, TraversableOnce[FireworkEffect])] = mutable.Map()

  override def onEnable(): Unit = {
    super.onEnable()
    ConfigurationSerialization.registerClass(classOf[PlayerFireworkSetup])
    val c = getConfig
    try {
      c.load("fireworkprojectiles.yml")
    } catch {
      case _: FileNotFoundException =>
        c.set("fireworks", Array(PlayerFireworkSetup(getServer.getOfflinePlayer(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")), true, Seq(FireworkEffect.builder.`with`(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).build()))))
        c.save("fireworkprojectiles.yml")
        c.load("fireworkprojectiles.yml")
    }
    val setups = c.getList("fireworks").asInstanceOf[util.List[PlayerFireworkSetup]]
    fireworksMap = mutable.Map(setups.asScala.map((s) => (s.p, (s.e.booleanValue(), s.f))):_*)
    this.getCommand("tfw").setExecutor((commandSender: CommandSender, command: Command, s: String, strings: Array[String]) => {
      commandSender match {
        case player: Player =>
          if (fireworksMap.contains(player)) {
            val fw = player.getWorld.spawnEntity(player.getLocation, EntityType.FIREWORK).asInstanceOf[Firework]
            val fwm = fw.getFireworkMeta
            val effects = fireworksMap(player)
            fwm.addEffects(effects._2.toSeq:_*)
            fwm.setPower(0)
            fw.setFireworkMeta(fwm)
            Bukkit.getScheduler.runTaskLaterAsynchronously(this, new Runnable {
              override def run(): Unit = fw.detonate()
            }, 0l)
            true
          } else {
            false
          }
        case _ => false
      }
    })
    this.getCommand("fws").setExecutor((commandSender: CommandSender, command: Command, s: String, strings: Array[String]) => {
      commandSender match {
        case player: Player =>
          if(player.getInventory.getItemInMainHand.getType == Material.FIREWORK) {
            val fwm = player.getInventory.getItemInMainHand.getItemMeta.asInstanceOf[FireworkMeta]
            fireworksMap += (player -> (true, fwm.getEffects.asScala))
            player.sendRawMessage("\u00a7d[FireworkProjectiles] Enabled and set the firework in your hand")
            true
          } else {
            false
          }
        case _ => false
      }
    })
    getServer.getPluginManager.registerEvents(new FireworkProjectilesListener, this)
  }

  def saveFireworks(): Unit = synchronized({
    val c = getConfig
    c.set("fireworks", fireworksMap.map((t) => PlayerFireworkSetup(t._1, t._2._1, t._2._2)).toArray)
    c.save("fireworkprojectiles.yml")
  })

}

object FireworkProjectiles {

  def apply(): FireworkProjectiles = Bukkit.getServer.getPluginManager.getPlugin("FireworkProjectiles").asInstanceOf[FireworkProjectiles]

}
