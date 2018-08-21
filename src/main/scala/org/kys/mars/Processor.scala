package org.kys.mars


import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import org.kys.mars.controllers.{NotificationController, RedisqController}
import org.kys.mars.rx.consumers.DiscordConsumer
import org.kys.mars.rx.producers.{NotificationProducer, RedisqProducer}
import org.kys.mars.util.{ArgUtils, DiscordUtils, RedisUtils}

import scala.concurrent.Await
import scala.util.Try

object Processor extends LazyLogging {
  def main(args: Array[String]): Unit = {
    lazy val argsConf = new ArgUtils(args).config()
    val (client, loginFuture) = DiscordUtils.initClient(argsConf.discordBotToken, argsConf.discordStatus)
    lazy val notificationProducers = argsConf.notificationDestinations.map(new NotificationProducer(10.seconds, _))
    lazy val redisqProducer = new RedisqProducer(50.millis)("213")
    lazy val discordConsumer = new DiscordConsumer(client)
    lazy val redisqController = new RedisqController(redisqProducer, discordConsumer, argsConf.killmailDestinations)
    lazy val notificationController = new NotificationController(notificationProducers, discordConsumer)

    // Timeout after 1 minute if login didn't succeed
    Await.ready(loginFuture, 1.minute)
    val f = Task.gatherUnordered(redisqController.tasks).runAsync
    //val f = Task.gatherUnordered(notificationController.tasks).runAsync
    Await.ready(f, Duration.Inf)
  }
}
