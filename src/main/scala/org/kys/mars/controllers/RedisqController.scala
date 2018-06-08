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

  def transformObservable(p: PackageBis, n: List[EsiName], d: Destination): List[CreateMessage[NotUsed]] = {
    val redColor = 0x990000
    val greenColor = 0x009900

    def getNameByOptId(id: Long): Option[String] = n.find(_.id == id).map(_.name)

    lazy val victim = p.killmail.victim

    lazy val generateTitle: String = {
      val entity: Option[String] = (victim.characterId, victim.corporationId) match {
        case (Some(ch), _) => getNameByOptId(ch)
        case (_, Some(cp)) => getNameByOptId(cp)
        case _ => None
      }
      val ship: Option[String] = victim.shipTypeId.flatMap(getNameByOptId)
      val loc = getNameByOptId(p.killmail.solarSystemId)

      s"${entity.getOrElse("Unknown")} | ${ship.getOrElse("Unknown")} | ${loc.getOrElse("Unknown")}"
    }

    lazy val generateZkillUrl: String = {
      val zkillBaseurl = marsConfig.getString("eve.zkbBaseUrl")
      s"$zkillBaseurl/kill/${p.killID}"
    }

    lazy val generateVictimImage: Option[String] = victim.shipTypeId.map(EsiUtils.getUrlByItemId)


    lazy val generateFooter: OutgoingEmbedFooter = {
      val finalBlow = p.killmail.attackers.find(_.finalBlow)
      val entityName: Option[String] = finalBlow.flatMap { b =>
        (b.characterId, b.corporationId) match {
          case (Some(ch), _) => getNameByOptId(ch)
          case (_, Some(cp)) => getNameByOptId(cp)
          case _ => None
        }
      }
      val entityGroup: Option[String] = finalBlow.flatMap { b =>
        (b.allianceId, b.corporationId) match {
          case (Some(al), _) => getNameByOptId(al)
          case (_, Some(cp)) => getNameByOptId(cp)
          case _ => None
        }
      }
      val footerThumbnail: Option[String] = finalBlow.flatMap(_.shipTypeId.map(EsiUtils.getUrlByItemId))
      val finalShipName: Option[String] = finalBlow.flatMap(_.shipTypeId.flatMap(getNameByOptId))
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

    lazy val generateEmbed: OutgoingEmbed = {
      def generateField(fieldName: String, fieldValue: String): EmbedField = {
        EmbedField(
          name = fieldName,
          value = fieldValue,
          inline = Some(true)
        )
      }

      val characterField = victim.characterId.flatMap(getNameByOptId).map(generateField("Character", _))
      val corporationField = victim.corporationId.flatMap(getNameByOptId).map(generateField("Corporation", _))
      val allianceField = victim.allianceId.flatMap(getNameByOptId).map(generateField("Alliance", _))
      val solarSystemField = getNameByOptId(p.killmail.solarSystemId).map(generateField("Location", _))
      val totalValueField = generateField("Total Value", p.zkb.totalValue.getOrElse(0D).formatted("%,.2f ISK"))

      val color = if (d.isFriendly(p)) redColor else greenColor

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

    d.discordChannelIds.map { id =>
      CreateMessage[NotUsed](ChannelId(id), CreateMessageData(content = "", embed = Some(generateEmbed)))
    }
  }
}
