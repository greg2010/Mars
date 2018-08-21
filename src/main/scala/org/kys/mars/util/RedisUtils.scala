package org.kys.mars.util

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import org.kys.mars.marsConfig
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object RedisUtils extends LazyLogging {
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()

  val client = RedisClient(
    host = marsConfig.getString("redis.host"),
    port = marsConfig.getInt("redis.port")
  )

  def readNotificationIdAndWriteIfNeeded(channelId: Long, notificationId: Long): Task[Boolean] = Task.defer {
    val key = s"channel_id/$channelId"
    def lrange: Future[List[Long]] = client.lrange[String](key, 0, -1).map(_.toList.map(_.toLong))
    def set: Future[Boolean] = client.lpush(s"channel_id/$channelId", notificationId).map(_ => true)
    Task.fromFuture(
      for {
        lr <- lrange
        shouldBeSet <- Future(!lr.contains(notificationId))
        set <- {
          logger.debug(s"Determined id needs to be written to redis " +
            s"written=$shouldBeSet " +
            s"channelId=$channelId " +
            s"notificationId=$notificationId")
          if (shouldBeSet) set
          else Future(false)
        }
      } yield shouldBeSet
    )
  }
}
