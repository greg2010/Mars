package org.kys.mars.models

import java.time.Instant

import io.circe
import io.circe.generic.auto._
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import monix.eval.Task
import org.kys.mars.models.notifications.ParsedNotificationLike
import org.kys.mars.util.NotificationParsers

/** All models used to render from/to JSON are listed here.
  * All models expect fields in JSON to be named in snake_case.
  */
object Json {
  import io.circe.java8.time._
  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames

  @ConfiguredJsonCodec case class EsiName(category: String, id: Long, name: String)

  @ConfiguredJsonCodec case class Notification(notificationId: Long,
                                               senderId: Long,
                                               senderType: EsiNotificationSenderType,
                                               timestamp: Instant,
                                               text: String,
                                               `type`: EsiNotificationType) {
    lazy val embed: Option[Either[circe.Error, ParsedNotificationLike]] = NotificationParsers.parse(this)
  }


  trait CharacterEntity {
    val characterId: Option[Long]
    val corporationId: Option[Long]
    val allianceId: Option[Long]
  }

  @ConfiguredJsonCodec case class Attacker(characterId: Option[Long],
                                           corporationId: Option[Long],
                                           allianceId: Option[Long],
                                           damageDone: Long,
                                           finalBlow: Boolean,
                                           securityStatus: Double,
                                           shipTypeId: Option[Long],
                                           weaponTypeId: Option[Long]) extends CharacterEntity

  case class Position(x: Double, y: Double, z: Double)

  @ConfiguredJsonCodec case class Item(flag: Long,
                                       itemTypeId: Long,
                                       quantityDropped: Option[Long],
                                       quantityDestroyed: Option[Long])

  @ConfiguredJsonCodec case class Victim(characterId: Option[Long],
                                         corporationId: Option[Long],
                                         allianceId: Option[Long],
                                         damageTaken: Long,
                                         items: List[Item],
                                         position: Position,
                                         shipTypeId: Option[Long]) extends CharacterEntity

  @ConfiguredJsonCodec case class Killmail(attackers: List[Attacker],
                                           killmailId: Long,
                                           killmailTime: String,
                                           solarSystemId: Long,
                                           victim: Victim)

  @ConfiguredJsonCodec case class Zkb(locationID: Long,
                 hash: String,
                 fittedValue: Double,
                 totalValue: Option[Double],
                 points: Long,
                 npc: Boolean,
                 solo: Boolean,
                 awox: Boolean,
                 href: String)

  @ConfiguredJsonCodec case class R00tJsonObject(`package`: Option[PackageBis])

  @ConfiguredJsonCodec case class PackageBis(killID: Long, killmail: Killmail, zkb: Zkb) {
    def getIds: List[Long] = {
      val victimIds: List[Option[Long]] = List(
        killmail.victim.characterId,
        killmail.victim.corporationId,
        killmail.victim.allianceId,
        killmail.victim.shipTypeId)

      val solarSystemId: List[Option[Long]] = List(Some(killmail.solarSystemId))

      val attackerIds: List[Option[Long]] = killmail.attackers
        .flatMap(x => List(x.characterId, x.corporationId, x.allianceId, x.shipTypeId))
      (victimIds ++ solarSystemId ++ attackerIds).flatten
    }
  }
}
