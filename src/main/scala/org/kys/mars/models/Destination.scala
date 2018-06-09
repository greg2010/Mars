package org.kys.mars.models

import com.typesafe.scalalogging.LazyLogging
import org.kys.mars.models.Json._

/** This model represents destinations for the bot.
  * Example of this encoded in `.conf` can be found in `mars.conf.example` in field `destinations`
  * @param `type`             Enum that represents what this destination describes [ [[[Killmail]] [[Notification]] ]
  * @param characterIds       List of Eve character ids relevant to the destination
  * @param corporationIds     List of Eve corporation ids relevant to the destination
  * @param allianceIds        List of Eve alliance ids relevant to the destination
  * @param all                A boolean value that determines if the destination is _catch-all_.
  * @param discordChannelIds  List of Discord channel ids that determines where messages should be forwarded
  * @param name               Name for the config entry. Purely used for logging purposes.
  */
case class Destination(`type`: DestinationType,
                       characterIds: List[Long],
                       corporationIds: List[Long],
                       allianceIds: List[Long],
                       all: Boolean,
                       discordChannelIds: List[Long],
                       name: String) extends LazyLogging {
  /** Helper function that determines if [[Option]] of [[Long]] is contained in a [[List]] of [[Long]]
    * @param xs   List to be searched in
    * @param id   Id to be searched in List
    * @return     [[Boolean]] True if id is contained in `xs`, false otherwise (or if id is [[None]])
    */
  private def containsOptId(xs: List[Long], id: Option[Long]): Boolean = id.exists(i => xs.contains(i))

  /** Determines if [[CharacterEntity]] is relevant (that is, present in characterIds/corporationIds/allianceIds)
    * @param characterEntity  Entity to be processed
    * @return                 True if entity is relevant to the destination, false otherwise
    */
  private def isRelevantCharacterEntity(characterEntity: CharacterEntity): Boolean = {
    lazy val ch = containsOptId(characterIds, characterEntity.characterId)
    lazy val cp = containsOptId(corporationIds, characterEntity.corporationId)
    lazy val al = containsOptId(allianceIds, characterEntity.allianceId)
    ch || cp || al
  }

  /** Determines if killmail is friendly (the victim in the package is relevant to the package)
    * @param kmPackage    Package to be processed
    * @return             True if package is friendly, false otherwise
    */
  def isFriendly(kmPackage: PackageBis): Boolean = !all && isRelevantCharacterEntity(kmPackage.killmail.victim)

  /** Determines if [[PackageBis]] is relevant to the destination
    * @param kmPackage    Package to be processed
    * @return             True if package is relevant to the destination, false otherwise
    */
  def isRelevant(kmPackage: PackageBis): Boolean = {
    val relevant =
      all ||
        kmPackage.killmail.attackers.map(isRelevantCharacterEntity).reduce(_ || _) ||
        isRelevantCharacterEntity(kmPackage.killmail.victim)
    logger.debug(s"Determined killmail ${kmPackage.killID} relevant=$relevant destination=$name")
    relevant
  }

  /*
   * Nice printing for logs
   */
  override def toString: String = name
}
