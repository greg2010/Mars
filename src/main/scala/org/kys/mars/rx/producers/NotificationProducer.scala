package org.kys.mars.rx.producers

import com.softwaremill.sttp._
import com.typesafe.scalalogging.LazyLogging
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import io.circe.parser._
import io.circe.generic.auto._
import org.kys.mars.models.{NotificationDestination, RedisqDestination}
import org.kys.mars.models.Json.Notification

import scala.concurrent.duration._


/** TBD
  *
  * @param rate
  * @param characterId
  * @param token
  */
class NotificationProducer(override val rate: FiniteDuration, val destination: NotificationDestination)
  extends Observable[Notification] with EveApiProducer with EsiProducer with LazyLogging {
  private var eTag: String = ""
  def getRequest: RequestT[Id, String, Nothing] =
    baseRequest(destination.esiRefreshToken).get(uri"$baseUrl/characters/${destination.characterId}/notifications")
      .header("If-None-Match", eTag)
      .response(asString)

  override def unsafeSubscribeFn(subscriber: Subscriber[Notification]): Cancelable = {
    subscriber.scheduler.scheduleAtFixedRate(0.seconds, rate) {
      val rawResp = getRequest.send()
      rawResp.body match {
        case Right(resp) =>
          parse(resp).map(_.as[List[Notification]]) match {
            case Right(Right(res)) =>
              rawResp.header("Etag") match {
                case Some(etag) => eTag = etag.replace("\"", "")
                case None => ()
              }
              res.sortBy(x => x.notificationId).foreach(subscriber.onNext)
            case Right(Left(err)) =>
              logger.error(s"Failed to decode ESI response, raw response was $resp", err)
            case Left(err) =>
              logger.error(s"Failed to parse ESI reponse, raw response was $resp", err)

          }
        case Left(err) =>
          if (!rawResp.isSuccess) {
            logger.error(s"Failed to obtain data from ESI, error code=${rawResp.code} message=$err")
          } else if (rawResp.code == 301) {
            logger.debug(s"Got 301 from ESI, content not changed code=${rawResp.code}")
           } else {
            logger.debug(s"Got non-200 from ESI, but not error code=${rawResp.code}")
          }
      }
    }
  }
}
