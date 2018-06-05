package org.kys.mars


import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import org.kys.mars.models.ConfigSchema.Destinations
import org.kys.mars.producers.{NotificationProducer, RedisqProducer}

import scala.concurrent.Await
import org.rogach.scallop._


class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val config: ScallopOption[Path] = opt[Path](required = true)
  verify()
}

object Processor extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val argsConf = new Conf(args).config()
    val conf = pureconfig.loadConfig[Destinations](argsConf)
    val o = new RedisqProducer(1.millis)()
    //val o = new NotificationProducer(1.second, 210383611, "2WwvNhS8_vX_WUZ1_LDdjBvWL7JW9CoELCb_DUqVKRFEz7bZzgizxVwnXyeAFyMvcdKUZ4WRzUn4irJ-3REEoQ2")
    val f = o.map { x =>
      logger.info(s"Got notification with id ${x.killID}")
    }.runAsyncGetLast
    Await.result(f, Duration.Inf)
  }
}
