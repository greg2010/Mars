package org.red.mars.util

import java.sql.Timestamp

object RedisQSchema {
  case class Attacker(character_id: Option[Long],
                      corporation_id: Option[Long],
                      alliance_id: Option[Long],
                      damage_done: Long,
                      final_blow: Boolean,
                      security_status: Double,
                      ship_type_id: Option[Long],
                      weapon_type_id: Option[Long])

  case class Position(x: Double,
                      y: Double,
                      z: Double)

  case class Item(flag: Long, item_type_id: Long, quantity_dropped: Option[Long], quantity_destroyed: Option[Long])

  case class Victim(alliance_id: Option[Long],
                    character_id: Option[Long],
                    corporation_id: Option[Long],
                    damage_taken: Long,
                    items: List[Item],
                    position: Position,
                    ship_type_id: Option[Long])

  case class Killmail(attackers: List[Attacker],
                      killmail_id: Long,
                      killmail_time: String,
                      solar_system_id: Long,
                      victim: Victim)
  case class Zkb(
                  locationID: Long,
                  hash: String,
                  fittedValue: Double,
                  totalValue: Double,
                  points: Long,
                  npc: Boolean,
                  solo: Boolean,
                  awox: Boolean,
                  href: String
                )
  case class PackageBis(killID: Long,
                        killmail: Killmail,
                        zkb: Zkb)

  case class R00tJsonObject(`package`: PackageBis)
}
