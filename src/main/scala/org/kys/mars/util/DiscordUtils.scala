package org.kys.mars.util

import akka.Done
import cats.Id
import com.typesafe.scalalogging.LazyLogging
import net.katsstuff.ackcord.{ClientSettings, DiscordClient}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object DiscordUtils extends LazyLogging {

  def initClient(token: String): (Future[DiscordClient[Id]], Future[Done]) = {
    val clientSettings: ClientSettings = new ClientSettings(token)
    val client: Future[DiscordClient[Id]] = clientSettings.build()
    val loginFuture = client.flatMap(_.login())
    (client, loginFuture)
  }

  def stopClient(client: Future[DiscordClient[Id]]): Future[Boolean] = {
    client.flatMap(_.logout())
  }
}
