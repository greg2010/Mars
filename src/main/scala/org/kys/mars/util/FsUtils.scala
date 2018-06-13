package org.kys.mars.util

import com.typesafe.scalalogging.LazyLogging
import monix.eval.Task
import org.kys.mars.marsConfig

import scala.reflect.io.{File, Path}
import scala.util.Try

object FsUtils extends LazyLogging {
  private val tempFolder = marsConfig.getString("fs.tempFolder")

  Path(tempFolder).createDirectory()

  def writeLastNotificationId(channelId: Long, notificationId: Long): Task[Unit] = Task {
    File(tempFolder + s"/$channelId").writeAll(notificationId.toString)
  }

  def readLastNotificationId(channelId: Long): Task[Option[String]] = Task {
    File(tempFolder + s"/$channelId").safeSlurp()
  }
}
