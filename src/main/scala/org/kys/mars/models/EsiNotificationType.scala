package org.kys.mars.models

import enumeratum.EnumEntry.{Camelcase, CapitalWords}
import enumeratum._

import scala.collection.immutable

sealed trait EsiNotificationType extends EnumEntry

/** Post type enum definition. Uses [[enumeratum]] library for scala.
  * [[io.circe.Decoder]]-specific documentation: [[https://github.com/lloydmeta/enumeratum#usage-3]]
  */

case object EsiNotificationType extends Enum[EsiNotificationType] with CirceEnum[EsiNotificationType] {
  case object AllWarDeclaredMsg extends EsiNotificationType
  case object DeclareWar extends EsiNotificationType
  case object AllWarInvalidatedMsg extends EsiNotificationType
  case object AllyJoinedWarAggressorMsg extends EsiNotificationType
  case object CorpWarDeclaredMsg extends EsiNotificationType
  case object EntosisCaptureStarted extends EsiNotificationType
  case object SovCommandNodeEventStarted extends EsiNotificationType
  case object SovStructureDestroyed extends EsiNotificationType
  case object SovStructureReinforced extends EsiNotificationType
  case object StructureUnderAttack extends EsiNotificationType
  case object OwnershipTransferred extends EsiNotificationType
  case object StructureOnline extends EsiNotificationType
  case object StructureDestroyed extends EsiNotificationType
  case object StructureFuelAlert extends EsiNotificationType
  case object StructureAnchoring extends EsiNotificationType with CapitalWords
  case object StructureUnanchoring extends EsiNotificationType
  case object StructureServicesOffline extends EsiNotificationType
  case object StructureLostShields extends EsiNotificationType
  case object StructureLostArmor extends EsiNotificationType
  case object StructureWentLowPower extends EsiNotificationType
  case object StructureWentHighPower extends EsiNotificationType
  case object StructuresReinforcementChanged extends EsiNotificationType
  case object TowerAlertMsg extends EsiNotificationType
  case object TowerResourceAlertMsg extends EsiNotificationType
  case object StationServiceEnabled extends EsiNotificationType
  case object StationServiceDisabled extends EsiNotificationType
  case object OrbitalReinforced extends EsiNotificationType
  case object OrbitalAttacked extends EsiNotificationType
  case object SovAllClaimAquiredMsg extends EsiNotificationType
  case object SovStationEnteredFreeport extends EsiNotificationType
  case object AllAnchoringMsg extends EsiNotificationType
  case object InfrastructureHubBillAboutToExpire extends EsiNotificationType
  case object SovAllClaimLostMsg extends EsiNotificationType
  case object SovStructureSelfDestructRequested extends EsiNotificationType
  case object SovStructureSelfDestructFinished extends EsiNotificationType
  case object StationConquerMsg extends EsiNotificationType
  case object MoonminingExtractionStarted extends EsiNotificationType
  case object MoonminingExtractionCancelled extends EsiNotificationType
  case object MoonminingExtractionFinished extends EsiNotificationType
  case object MoonminingLaserFired extends EsiNotificationType
  case object MoonminingAutomaticFracture extends EsiNotificationType
  case object CorpAllBillMsg extends EsiNotificationType
  case object BillPaidCorpAllMsg extends EsiNotificationType
  case object CharAppAcceptMsg extends EsiNotificationType
  case object CorpAppNewMsg extends EsiNotificationType
  case object CharAppWithdrawMsg extends EsiNotificationType
  case object CharLeftCorpMsg extends EsiNotificationType
  case object CorpNewCEOMsg extends EsiNotificationType
  case object CorpVoteMsg extends EsiNotificationType
  case object CorpVoteCEORevokedMsg extends EsiNotificationType
  case object CorpTaxChangeMsg extends EsiNotificationType
  case object CorpDividendMsg extends EsiNotificationType
  case object BountyClaimMsg extends EsiNotificationType
  case object KillReportVictim extends EsiNotificationType
  case object KillReportFinalBlow extends EsiNotificationType
  case object BountyPlacedChar extends EsiNotificationType
  case object InsurancePayoutMsg extends EsiNotificationType
  case object InsuranceIssuedMsg extends EsiNotificationType
  case object NPCStandingsLost extends EsiNotificationType
  case object JumpCloneDeletedMsg2 extends EsiNotificationType
  case object AllianceCapitalChanged extends EsiNotificationType
  case object CloneActivationMsg2 extends EsiNotificationType
  case object CorpAppRejectCustomMsg extends EsiNotificationType
  case object StructureItemsDelivered extends EsiNotificationType
  case object CorpAppInvitedMsg extends EsiNotificationType

  val values: immutable.IndexedSeq[EsiNotificationType] = findValues
}