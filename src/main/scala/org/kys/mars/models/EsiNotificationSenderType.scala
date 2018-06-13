package org.kys.mars.models

import enumeratum.EnumEntry.{Camelcase, Lowercase}
import enumeratum._

import scala.collection.immutable

sealed trait EsiNotificationSenderType extends EnumEntry



case object EsiNotificationSenderType extends Enum[EsiNotificationSenderType] with CirceEnum[EsiNotificationSenderType] {
  case object Character extends EsiNotificationSenderType with Lowercase
  case object Corporation extends EsiNotificationSenderType with Lowercase
  case object Alliance extends EsiNotificationSenderType with Lowercase
  case object Faction extends EsiNotificationSenderType with Lowercase

  val values: immutable.IndexedSeq[EsiNotificationSenderType] = findValues

}