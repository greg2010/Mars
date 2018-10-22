package org.kys.mars


import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import org.kys.mars.controllers.RedisqController
import org.kys.mars.models.Destination
import org.kys.mars.rx.consumers.DiscordConsumer
import org.kys.mars.rx.producers.RedisqProducer
import org.kys.mars.util.DiscordUtils
import pureconfig.module.enumeratum._

import scala.concurrent.Await
import scala.util.Try

object Processor extends App with LazyLogging {

  case class RootConfig(destinations: List[Destination])

  val fileConfigPath = Paths.get(marsConfig.getString("configFilePath"))
  val fileConfig = pureconfig.loadConfigOrThrow[RootConfig](fileConfigPath).destinations

  val (client, loginFuture) = DiscordUtils.initClient(
    marsConfig.getString("discord.token"),
    Try(marsConfig.getString("discord.status")).toOption)
  lazy val redisqProducer = new RedisqProducer(50.millis)()
  lazy val discordConsumer = new DiscordConsumer(client)
  lazy val redisqController = new RedisqController(redisqProducer, discordConsumer, fileConfig)

  // Timeout after 1 minute if login didn't succeed
  Await.ready(loginFuture, 1.minute)
  val f = Task.gatherUnordered(redisqController.tasks).runAsync
  Await.ready(f, Duration.Inf)

}
