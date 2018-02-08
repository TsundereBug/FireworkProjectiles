package com.tsunderebug.fireworkprojectiles

import org.bukkit.{Bukkit, Material}
import org.bukkit.entity.{EntityType, Firework, Player}
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.{EventHandler, Listener}

class FireworkProjectilesListener extends Listener {

  val projectileItems = Seq(
    Material.ENDER_PEARL,
    Material.BOW,
    Material.SNOW_BALL,
    Material.EGG,
    Material.SPLASH_POTION,
    Material.LINGERING_POTION,
    Material.EXP_BOTTLE
  )

  @EventHandler
  def onAction(e: PlayerInteractEvent): Unit = {
    if(e.getPlayer.isSneaking && (e.getAction == Action.LEFT_CLICK_AIR || e.getAction == Action.LEFT_CLICK_BLOCK) && projectileItems.contains(e.getPlayer.getInventory.getItemInMainHand.getType)) {
      val map = FireworkProjectiles().fireworksMap
      map.get(e.getPlayer) match {
        case Some((enabled, f)) =>
          map += (e.getPlayer -> (!enabled, f))
          if(enabled) {
            e.getPlayer.sendRawMessage("\u00a7d[FireworkProjectiles] Disabled Firework Projectiles")
          } else {
            e.getPlayer.sendRawMessage("\u00a7d[FireworkProjectiles] Enabled Firework Projectiles")
          }
          FireworkProjectiles().saveFireworks()
        case None =>
          e.getPlayer.sendRawMessage("\u00a7d[FireworkProjectiles] You don't have a firework set up! Hold a firework in your hand and type /fws to set one.")
      }
    }
  }

  @EventHandler
  def onProjectileLand(e: ProjectileHitEvent): Unit = {
    e.getEntity.getShooter match {
      case p: Player =>
        val map = FireworkProjectiles().fireworksMap
        map.get(p) match {
          case Some((enabled, f)) =>
            if(enabled) {
              val fw = p.getWorld.spawnEntity(e.getEntity.getLocation, EntityType.FIREWORK).asInstanceOf[Firework]
              val fwm = fw.getFireworkMeta
              fwm.addEffects(f.toSeq:_*)
              fwm.setPower(0)
              fw.setFireworkMeta(fwm)
              Bukkit.getScheduler.runTaskLaterAsynchronously(FireworkProjectiles(), new Runnable {
                override def run(): Unit = fw.detonate()
              }, 0l)
            }
          case None =>
        }
      case _ =>
    }
  }

}
