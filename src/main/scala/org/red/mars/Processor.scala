package org.red.mars

import com.softwaremill.sttp._

import scala.concurrent.duration._
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable

import scala.concurrent.Await

object Processor extends App {
  implicit val sttpBackend = HttpURLConnectionBackend()
  val r = sttp.get(uri"http://redisq.zkillboard.com/listen.php")
    .response(asString)
  val o = new RedisqProducer(100.millis)
  val f = o.map(x => println("lol" + x)).runAsyncGetFirst
  Await.result(f, Duration.Inf)
}
