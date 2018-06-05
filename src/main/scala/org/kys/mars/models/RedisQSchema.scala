package org.kys.mars.models

import io.circe.generic.auto._
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}

object RedisQSchema {
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  @ConfiguredJsonCodec case class Attacker(characterId: Option[Long],
                                           corporationId: Option[Long],
                                           allianceId: Option[Long],
                                           damageDone: Long,
                                           finalBlow: Boolean,
                                           securityStatus: Double,
                                           shipTypeId: Option[Long],
                                           weaponTypeId: Option[Long])

  case class Position(x: Double, y: Double, z: Double)

  @ConfiguredJsonCodec case class Item(flag: Long,
                                       itemTypeId: Long,
                                       quantityDropped: Option[Long],
                                       quantityDestroyed: Option[Long])

  @ConfiguredJsonCodec case class Victim(allianceId: Option[Long],
                                         characterId: Option[Long],
                                         corporationId: Option[Long],
                                         damageTaken: Long,
                                         items: List[Item],
                                         position: Position,
                                         shipTypeId: Option[Long])

  @ConfiguredJsonCodec case class Killmail(attackers: List[Attacker],
                                           killmailId: Long,
                                           killmailTime: String,
                                           solarSystemId: Long,
                                           victim: Victim)

  case class Zkb(locationID: Long,
                 hash: String,
                 fittedValue: Double,
                 totalValue: Double,
                 points: Long,
                 npc: Boolean,
                 solo: Boolean,
                 awox: Boolean,
                 href: String)

  case class PackageBis(killID: Long, killmail: Killmail, zkb: Zkb)

  case class R00tJsonObject(`package`: Option[PackageBis])
}
