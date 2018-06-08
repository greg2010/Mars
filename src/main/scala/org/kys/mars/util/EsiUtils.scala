package org.kys.mars.util

import monix.eval.Task
import org.kys.mars.rx.producers.{EsiProducer, EveApiProducer}
import com.softwaremill.sttp._
import io.circe.syntax._
import com.softwaremill.sttp.circe._
import com.typesafe.scalalogging.LazyLogging
import org.kys.mars.models.Json.EsiName
import org.kys.mars.marsConfig

import scala.concurrent.ExecutionContext.Implicits.global
import retry.Success._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object EsiUtils extends EsiProducer with EveApiProducer with LazyLogging {
  val token = ""
  val rate: FiniteDuration = 1.second

  def resolveNamesTask(ids: List[Long]): Task[List[EsiName]] = {
    def retriableFuture: Future[Try[List[EsiName]]] = retry.Backoff().apply {
      Future {
        Try {
          if (ids.isEmpty) {
            List[EsiName]()
          } else {
            val url = uri"$baseUrl/universe/names/"
            val r = sttp.post(url).body(ids.toSet.asJson.noSpaces).response(asJson[List[EsiName]])
            val resp = r.send()
            resp.body match {
              case Right(Right(re)) => re
              case Left(ex) =>
                logger.error(s"Failed to obtain response from ESI API, error=$ex code=${resp.code}")
                throw new RuntimeException("Failed to obtain response from esi API")
              case Right(Left(ex)) =>
                logger.error("Failed to parse/decode response from ESI API", ex)
                throw ex
            }
          }
        }
      }
    }
    Task.defer {
      Task.fromFuture {
        retriableFuture.map {
          case Success(r) => r
          case Failure(ex) => throw ex
        }
      }
    }
  }

  def getUrlByItemId(id: Long): String = {
    val imgBaseUrl = marsConfig.getString("eve.imgBaseUrl")
    s"$imgBaseUrl/type/${id}_64.png"
  }

}
