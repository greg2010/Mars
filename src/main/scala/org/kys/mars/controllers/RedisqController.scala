package org.kys.mars.controllers

import java.time.OffsetDateTime

import akka.NotUsed
import monix.eval.Task
import net.katsstuff.ackcord.data.{ChannelId, EmbedField, OutgoingEmbed, OutgoingEmbedFooter, OutgoingEmbedThumbnail}
import net.katsstuff.ackcord.http.rest.{CreateMessage, CreateMessageData}
import org.kys.mars.models.Json._
import org.kys.mars.rx.consumers.DiscordConsumer
import org.kys.mars.rx.producers.RedisqProducer
import org.kys.mars.util.EsiUtils
import monix.execution.Scheduler.Implicits.global
import monix.reactive.{Observable, Pipe}
import org.kys.mars.marsConfig

import scala.concurrent.duration._
import org.kys.mars.models.{Destination, DestinationType}


/** RedisController is a class that provides [[Task]] that does processes [[RedisqProducer]] with [[DiscordConsumer]].
  *
  * @param redisqProducer   Instance of [[RedisqProducer]] events from which are processed
  * @param discordConsumer  Instance of [[DiscordConsumer]] that processes the events
  * @param destinations     List of destinations configured through a `.conf` file
  */
class RedisqController(redisqProducer: RedisqProducer,
                       discordConsumer: DiscordConsumer,
                       destinations: List[Destination]) {


  lazy val tasks: List[Task[Unit]] = {
    val sharedObservable = redisqProducer.share
    destinations
      .filter(_.`type` == DestinationType.Killmail)
      .map { destination =>
        sharedObservable
          .filter(destination.isRelevant)
          .mapTask(p => EsiUtils.resolveNamesTask(p.getIds).map(n => (p, n)))
          .map(p => Observable.fromIterable(transformObservable(p._1, p._2, destination)))
          .flatten
          .bufferTimedWithPressure(1.second, 50)
          .filter(_.nonEmpty)
          .consumeWith(discordConsumer)
      }
  }

  /** Auxillary function that takes a RedisQ package ([[PackageBis]]) and produces a [[CreateMessage]] instance.
    * That is, this function takes a `Zkillboard` message and produces a `Discord` message
    * @param p  RedisQ Package
    * @param n  List of resolved names for ids contained in `p`
    * @param d  Destination for which package is being produced
    * @return   Discord message with [[ChannelId]] attached
    */
  def transformObservable(p: PackageBis, n: List[EsiName], d: Destination): List[CreateMessage[NotUsed]] = {
    /*
     * Default constants for friendly and hostile killmails
     */
    val redColor = 0x990000
    val greenColor = 0x009900

    /** A helper function that resolves eve name by its' id
      *
      * @param id   id to be resolved
      * @return     Name ([[String]]) if id is resolved, [[None]] otherwise
      */
    def getNameById(id: Long): Option[String] = n.find(_.id == id).map(_.name)

    lazy val victim = p.killmail.victim

    /*
     * Title for the embed of a form "Nero Solo | Crow | O94U-A"
     */
    lazy val generateTitle: String = {
      val entity: Option[String] = (victim.characterId, victim.corporationId) match {
        case (Some(ch), _) => getNameById(ch)
        case (_, Some(cp)) => getNameById(cp)
        case _ => None
      }
      val ship: Option[String] = victim.shipTypeId.flatMap(getNameById)
      val loc = getNameById(p.killmail.solarSystemId)

      s"${entity.getOrElse("Unknown")} | ${ship.getOrElse("Unknown")} | ${loc.getOrElse("Unknown")}"
    }

    /*
     * A link to the killmail on zKillboard (https://zkillboard.com/kill/70478414/)
     */
    lazy val generateZkillUrl: String = {
      val zkillBaseurl = marsConfig.getString("eve.zkbBaseUrl")
      s"$zkillBaseurl/kill/${p.killID}"
    }

    /*
     * A link to the victim ship thumbnail
     */
    lazy val generateVictimImage: Option[String] = victim.shipTypeId.map(EsiUtils.getUrlByItemId)


    /*
     * Footer for the embed ("Final blow by Dorio Whitney (DARKNESS.) in Muninn (4 involved)•Today at 5:26 PM")
     */
    lazy val generateFooter: OutgoingEmbedFooter = {
      val finalBlow = p.killmail.attackers.find(_.finalBlow)
      val entityName: Option[String] = finalBlow.flatMap { b =>
        (b.characterId, b.corporationId) match {
          case (Some(ch), _) => getNameById(ch)
          case (_, Some(cp)) => getNameById(cp)
          case _ => None
        }
      }
      val entityGroup: Option[String] = finalBlow.flatMap { b =>
        (b.allianceId, b.corporationId) match {
          case (Some(al), _) => getNameById(al)
          case (_, Some(cp)) => getNameById(cp)
          case _ => None
        }
      }
      val footerThumbnail: Option[String] = finalBlow.flatMap(_.shipTypeId.map(EsiUtils.getUrlByItemId))
      val finalShipName: Option[String] = finalBlow.flatMap(_.shipTypeId.flatMap(getNameById))
      val involvedCount: Int = p.killmail.attackers.length
      val entityGroupText = if (entityGroup.nonEmpty && entityName.exists(_ != entityGroup.get)) {
        s"(${entityGroup.get}) "
      } else {
        " "
      }

      val footerText = s"Final blow by ${entityName.getOrElse("Unknown")} " +
        s"$entityGroupText" +
        s"in ${finalShipName.getOrElse("Unknown")} " +
        s"($involvedCount involved)"
      OutgoingEmbedFooter(footerText, footerThumbnail)
    }

    /*
     * Embed to be sent in the discord message
     */
    lazy val generateEmbed: OutgoingEmbed = {
      /** A helper function that produces discord embed fields
        *
        * @param fieldName    Name of the field to be generated
        * @param fieldValue   Title of the field to be generated
        * @param inline       Specifies if the field should be inline (defaults to true)
        * @return             Instance of [[EmbedField]] that contains field information
        */
      def generateField(fieldName: String, fieldValue: String, inline: Boolean = true): EmbedField = {
        EmbedField(
          name = fieldName,
          value = fieldValue,
          inline = Some(inline)
        )
      }

      val characterField = victim.characterId.flatMap(getNameById).map(generateField("Character", _))
      val corporationField = victim.corporationId.flatMap(getNameById).map(generateField("Corporation", _))
      val allianceField = victim.allianceId.flatMap(getNameById).map(generateField("Alliance", _))
      val solarSystemField = getNameById(p.killmail.solarSystemId).map(generateField("Location", _))
      val totalValueField = generateField("Total Value", p.zkb.totalValue.getOrElse(0D).formatted("%,.2f ISK"))

      val color = if (d.isFriendly(p)) redColor else greenColor

      /*
       * Putting it all together
       */
      OutgoingEmbed(
        title = Some(generateTitle),
        url = Some(generateZkillUrl),
        timestamp = Some(OffsetDateTime.parse(p.killmail.killmailTime)),
        color = Some(color),
        footer = Some(generateFooter),
        thumbnail = generateVictimImage.map(OutgoingEmbedThumbnail),
        fields = Seq(characterField, corporationField, allianceField, solarSystemField, Some(totalValueField)).flatten
      )
    }

    /*
     * For every channel in destination generate a message
     */
    d.discordChannelIds.map { id =>
      CreateMessage[NotUsed](ChannelId(id), CreateMessageData(content = "", embed = Some(generateEmbed)))
    }
  }
}
