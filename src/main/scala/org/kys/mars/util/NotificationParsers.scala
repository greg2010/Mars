package org.kys.mars.util

import io.circe.generic.auto._
import org.kys.mars.models.EsiNotificationType._
import org.kys.mars.models.Json.Notification
import org.kys.mars.models.notifications.ParsedNotificationLike
import io.circe.yaml.parser
import org.kys.mars.models.notifications.ParsedNotification._

object NotificationParsers {

  def parse(n: Notification): Option[Either[io.circe.Error, ParsedNotificationLike]] = n.`type` match {
    case StructureUnderAttack => Some(parser.parse(n.text).flatMap(_.as[StructureUnderAttackText]))
    case StructureLostShields => Some(parser.parse(n.text).flatMap(_.as[StructureLostShieldsText]))
    case StructureLostArmor => Some(parser.parse(n.text).flatMap(_.as[StructureLostArmorText]))
    case StructureDestroyed => None
    case TowerAlertMsg => Some(parser.parse(n.text).flatMap(_.as[TowerAlertMsgText]))
    case TowerResourceAlertMsg => Some(parser.parse(n.text).flatMap(_.as[TowerResourceAlertMsgText]))
    case StructureFuelAlert => Some(parser.parse(n.text).flatMap(_.as[StructureFuelAlertText]))
    case StructureOnline => None
    case StructureServicesOffline => Some(parser.parse(n.text).flatMap(_.as[StructureServicesOfflineText]))
    case StructuresReinforcementChanged  => None
    case StructureWentHighPower => Some(parser.parse(n.text).flatMap(_.as[StructureWentHighPowerText]))
    case StructureAnchoring => None
    case StructureUnanchoring => None
    case OwnershipTransferred => Some(parser.parse(n.text).flatMap(_.as[OwnershipTransferredText]))
    case CorpAllBillMsg => Some(parser.parse(n.text).flatMap(_.as[CorpAllBillMsgText]))
    case CorpAppNewMsg => Some(parser.parse(n.text).flatMap(_.as[CorpAppNewMsgText]))
    case CorpNewCEOMsg => None
    case CorpTaxChangeMsg => Some(parser.parse(n.text).flatMap(_.as[CorpTaxChangeMsgText]))
    case CharLeftCorpMsg => Some(parser.parse(n.text).flatMap(_.as[CharLeftCorpMsgText]))
    case CharAppWithdrawMsg => None
    case CharAppAcceptMsg => Some(parser.parse(n.text).flatMap(_.as[CharAppAcceptMsgText]))
    case _ => None
  }
}
