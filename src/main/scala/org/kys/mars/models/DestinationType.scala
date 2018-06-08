package org.kys.mars.models

import enumeratum.EnumEntry.{Lowercase, Snakecase}
import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait DestinationType extends EnumEntry with Snakecase

case object DestinationType extends Enum[DestinationType] {
  case object Killmail  extends DestinationType with Lowercase
  case object Notification extends DestinationType with Lowercase

  val values: immutable.IndexedSeq[DestinationType] = findValues

}