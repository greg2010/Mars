package org.red.mars

import com.softwaremill.sttp._
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import org.red.mars.util._
import io.circe.parser._
import io.circe.generic.auto._
import com.softwaremill.sttp.circe._
import io.circe

import scala.concurrent.duration._
import org.red.mars.util.RedisQSchema._

import scala.concurrent.duration.FiniteDuration

class RedisqProducer(rate: FiniteDuration, key: String = "kys-km-producer") extends Observable[R00tJsonObject] {
  private implicit val sttpBackend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()

  val r = sttp.get(uri"http://redisq.zkillboard.com/listen.php")
    .response(asJson[R00tJsonObject])

  override def unsafeSubscribeFn(subscriber: Subscriber[R00tJsonObject]): Cancelable = {
    import monix.execution.Scheduler.Implicits.{global => s}
    s.scheduleAtFixedRate(0.seconds, rate) {
      r.send()
    }
  }
}
