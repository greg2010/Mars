package org.kys.mars


import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import org.kys.mars.controllers.RedisqController
import org.kys.mars.rx.consumers.DiscordConsumer
import org.kys.mars.rx.producers.RedisqProducer
import org.kys.mars.util.{ArgUtils, DiscordUtils}

import scala.concurrent.Await
import scala.util.Try

object Processor extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val (client, loginFuture) = DiscordUtils.initClient(
      marsConfig.getString("discord.token"),
      Try(marsConfig.getString("discord.status")).toOption)
    lazy val argsConf = new ArgUtils(args)
    lazy val redisqProducer = new RedisqProducer(50.millis)()
    lazy val discordConsumer = new DiscordConsumer(client)
    lazy val redisqController = new RedisqController(redisqProducer, discordConsumer, argsConf.config())

    // Timeout after 1 minute if login didn't succeed
    Await.ready(loginFuture, 1.minute)
    val f = Task.gatherUnordered(redisqController.tasks).runAsync
    Await.ready(f, Duration.Inf)
  }
}
