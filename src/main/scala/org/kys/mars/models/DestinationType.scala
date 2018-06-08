package org.kys.mars.models

import enumeratum.EnumEntry.{Lowercase, Snakecase}
import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed trait DestinationType extends EnumEntry

/** Enum that represents possible types of destinations.
  * Currently expects the values in lowercase (killmail)
  */
case object DestinationType extends Enum[DestinationType] {
  case object Killmail  extends DestinationType with Lowercase
  case object Notification extends DestinationType with Lowercase

  val values: immutable.IndexedSeq[DestinationType] = findValues

}