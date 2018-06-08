package org.kys.mars.rx.consumers

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import cats.Id
import com.typesafe.scalalogging.LazyLogging
import monix.eval.Callback
import monix.execution.Ack.{Continue, Stop}
import monix.execution._
import monix.execution.cancelables.AssignableCancelable
import monix.reactive.Consumer
import monix.reactive.observers.Subscriber
import net.katsstuff.ackcord.http.requests.{RequestError, RequestResponse}
import net.katsstuff.ackcord.http.rest.CreateMessage
import net.katsstuff.ackcord.{ClientSettings, DiscordClient}

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class DiscordConsumer(client: Future[DiscordClient[Id]])
  extends Consumer[Seq[CreateMessage[NotUsed]], Unit] with LazyLogging {
  override def createSubscriber(cb: Callback[Unit], s: Scheduler):
  (Subscriber[Seq[CreateMessage[NotUsed]]], AssignableCancelable) = {
    val subscriber: Subscriber[Seq[CreateMessage[NotUsed]]] = new Subscriber[Seq[CreateMessage[NotUsed]]] {
      override implicit def scheduler: Scheduler = s

      override def onNext(elem: Seq[CreateMessage[NotUsed]]): Future[Ack] = {
        client.flatMap { client =>
          implicit val mat: Materializer = client.requests.mat
          logger.debug(s"Posting ${elem.length} messages to discord")
          val f = Source.fromIterator(() => elem.toIterator).via(client.requests.flow).runWith(Sink.seq)
          f.onComplete {
            case Success(r) =>
              r.foreach {
                case RequestError(_, e, uri, rawUri) =>
                  logger.error(s"Got bad response from discord API, route=$rawUri", e)
                case x => x.map(r => logger.debug(s"Sent message to channelId=${r.channelId} messageId=${r.id}"))
              }
            case Failure(ex) =>
              logger.error(s"Failed to send message to channelIds=${elem.map(_.channelId.longValue).mkString(",")}", ex)
          }
          f.map(_ => Continue)
        }
      }

      override def onError(ex: Throwable): Unit = {
        logger.error("Exception in DiscordConsumer", ex)
        cb.onError(ex)
      }

      override def onComplete(): Unit = {
        logger.info("DiscordConsumer reached .onComplete, closing")
        cb.onSuccess(Unit)
      }
    }
    (subscriber, AssignableCancelable.single())
  }
}
