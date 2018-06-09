package org.kys.mars.util

import akka.Done
import cats.Id
import com.typesafe.scalalogging.LazyLogging
import net.katsstuff.ackcord.data.raw.RawActivity
import net.katsstuff.ackcord.{ClientSettings, DiscordClient}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object DiscordUtils extends LazyLogging {

  def initClient(token: String, status: Option[String] = None): (Future[DiscordClient[Id]], Future[Done]) = {
    val activity = status.map(RawActivity(_, 0, None, None, None, None, None, None, None))
    val clientSettings: ClientSettings = new ClientSettings(token = token, activity = activity)
    val client: Future[DiscordClient[Id]] = clientSettings.build()
    val loginFuture = client.flatMap(_.login())
    (client, loginFuture)
  }

  def stopClient(client: Future[DiscordClient[Id]]): Future[Boolean] = {
    client.flatMap(_.logout())
  }
}
