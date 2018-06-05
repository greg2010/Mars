package org.kys.mars.models

object ConfigSchema {
  case class Destinations(destinations: List[Destination])
  case class Destination(discord: Discord, entities: Entities)
  case class Discord(url: String, name: String, picture: String)
  case class Entities(charIds: List[Long], corpIds: List[Long], allianceIds: List[Long], all: Boolean = false)
}
