package com.tsunderebug.fireworkprojectiles

import java.util

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.{FireworkEffect, OfflinePlayer}

import scala.collection.JavaConverters._

case class PlayerFireworkSetup(
                                p: OfflinePlayer,
                                e: java.lang.Boolean,
                                f: TraversableOnce[FireworkEffect]
                              ) extends ConfigurationSerializable {

  def this(map: util.Map[String, AnyRef]) {
    this(map.get("player").asInstanceOf[OfflinePlayer], map.get("enabled").asInstanceOf[java.lang.Boolean], map.get("effects").asInstanceOf[util.List[FireworkEffect]].asScala)
  }

  override def serialize(): util.Map[String, AnyRef] = {
    Map("player" -> p, "enabled" -> e, "effects" -> f.toArray).asJava
  }

}
