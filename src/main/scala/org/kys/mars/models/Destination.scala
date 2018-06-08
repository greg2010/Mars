package org.kys.mars.models

import com.typesafe.scalalogging.LazyLogging
import org.kys.mars.models.Json._

case class Destination(`type`: DestinationType,
                       characterIds: List[Long],
                       corporationIds: List[Long],
                       allianceIds: List[Long],
                       all: Boolean,
                       discordChannelIds: List[Long],
                       name: String) extends LazyLogging {
  private def containsOptId(xs: List[Long], id: Option[Long]): Boolean = id.exists(i => xs.contains(i))

  private def isRelevantCharacterEntity(characterEntity: CharacterEntity): Boolean = {
    lazy val ch = containsOptId(characterIds, characterEntity.characterId)
    lazy val cp = containsOptId(corporationIds, characterEntity.corporationId)
    lazy val al = containsOptId(allianceIds, characterEntity.allianceId)
    ch || cp || al
  }

  def isFriendly(kmPackage: PackageBis): Boolean = !all || isRelevantCharacterEntity(kmPackage.killmail.victim)

  def isRelevant(kmPackage: PackageBis): Boolean = {
    val relevant = all || kmPackage.killmail.attackers.map(isRelevantCharacterEntity).reduce(_ || _)
    logger.debug(s"Determined killmail ${kmPackage.killID} relevant=$relevant destination=$name")
    relevant
  }

  override def toString: String = name
}
