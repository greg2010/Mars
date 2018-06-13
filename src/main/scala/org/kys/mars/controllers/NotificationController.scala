package org.kys.mars.controllers

import java.time.ZoneOffset

import akka.NotUsed
import cats.Id
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import monix.reactive.Observable
import net.katsstuff.ackcord.DiscordClient
import net.katsstuff.ackcord.data.{ChannelId, OutgoingEmbedFooter}
import net.katsstuff.ackcord.http.rest.{CreateMessage, CreateMessageData}
import org.kys.mars.models.EsiNotificationSenderType.Corporation
import org.kys.mars.models.EsiNotificationType.StructureUnderAttack
import org.kys.mars.models.Json.{EsiName, Notification}
import org.kys.mars.models.{EsiNotificationSenderType, NotificationDestination, RedisqDestination}
import org.kys.mars.rx.consumers.DiscordConsumer
import org.kys.mars.rx.producers.NotificationProducer
import org.kys.mars.util.DiscordUtils

import scala.concurrent.duration._
import org.kys.mars.util.EsiUtils._

import scala.concurrent.Future

class NotificationController(notificationProducers: List[NotificationProducer],
                             discordConsumer: DiscordConsumer) extends LazyLogging {

  lazy val tasks: List[Task[Unit]] = notificationProducers.map { producer =>
    producer
      .filter(n => isNotificationRelevant(n.`type`))
      .filter(_.`type` == StructureUnderAttack)
      .flatMap(p => Observable.fromIterable(transformObservable(p, producer.destination)))
      .mapTask(identity)
      .bufferTimedAndCounted(1.second, 5)
      .filter(_.nonEmpty)
      .consumeWith(discordConsumer)
  }



  def transformObservable(notification: Notification, d: NotificationDestination): List[Task[CreateMessage[NotUsed]]] = {
    def generateErrorAdminMessage(adminChannelId: Option[Long]): Option[CreateMessage[NotUsed]] = {
      adminChannelId.map { id =>
        val messageData = CreateMessageData(
          "Failed to generate embed for notification\n" +
            s"type:\n${notification.`type`}\n\n" +
            s"text:\n${notification.text}")
        CreateMessage[NotUsed](ChannelId(id), messageData)
      }
    }

    d.discordChannelIds.map { channelId =>
      val embed = notification.embed

      embed match {
        case Some(e) =>
          e match {
            case Left(err) =>
              logger.error("Notification decoding/parsing error " +
                s"notificationType=${notification.`type`} " +
                s"text=${notification.text} " +
                s"error=${err.getMessage}", err)
              generateErrorAdminMessage(d.adminDiscordChannelId).map(Task.now)
            case Right(em) =>
              Some(em.prettyPrintEmbed.map { e =>
                val footer = OutgoingEmbedFooter(notification.notificationId.toString)
                val newEmbed = e.copy(
                  footer = Some(footer),
                  timestamp = Some(notification.timestamp.atOffset(ZoneOffset.UTC)))
                CreateMessage[NotUsed](ChannelId(channelId), CreateMessageData(embed = Some(newEmbed)))
              })
          }
        case None =>
          logger.error(s"Failed to generate embed for a notification " +
            s"notificationType=${notification.`type`} " +
            s"text=${notification.text}")
          generateErrorAdminMessage(d.adminDiscordChannelId).map(Task.now)
      }
    }
  }.flatten
}