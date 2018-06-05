package org.kys.mars.producers

import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._
import com.typesafe.scalalogging.LazyLogging
import io.circe.DecodingFailure
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import org.kys.mars.models.RedisQSchema._

import scala.concurrent.duration.{FiniteDuration, _}
import io.circe.parser._
import io.circe.generic.auto._
import monix.reactive.subjects.ReplaySubject

class RedisqProducer(override val rate: FiniteDuration)(key: String = "kys-km-producer1")
  extends Observable[PackageBis] with EveApiProducer with LazyLogging {
  new ReplaySubject[]()
  private var eTag: String = ""

  val r = sttp.get(uri"http://redisq.zkillboard.com/listen.php?queueID=$key&ttw=1")
    .response(asString)

  override def unsafeSubscribeFn(subscriber: Subscriber[PackageBis]): Cancelable = {
    subscriber.scheduler.scheduleAtFixedRate(0.seconds, rate) {
      val rawResp = r.send()
      rawResp.body match {
        case Right(succResp) =>
          parse(succResp).map(_.as[R00tJsonObject]) match {
            case Right(Right(res)) =>
              res.`package` match {
                case Some(pkg) => subscriber.onNext(pkg)
                case _ => ()
              }
            case Left(err) =>
              logger.error(s"Failed to parse response from RedisQ, " +
                s"error=${err.message} " +
                s"rawMessage=$succResp")

            case Right(Left(err)) =>
              logger.info(s"Failed to decode response from RedisQ, " +
                s"error=${err.message} " +
                s"rawMessage=$succResp")
          }
        case Left(err) =>
          rawResp.code match {
            case 429 =>
              val delay = rawResp.header("Retry-After").map(_.toLong).getOrElse(10000L)
              logger.warn(s"Got 429, delaying by delay=$delay")
              Thread.sleep(delay)
            case _ => logger.error(s"Got http error response from RedisQ, code=${rawResp.code} error=$err")
          }
      }
    }
  }
}
