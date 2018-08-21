package org.kys.mars.util

import monix.eval.Task
import org.kys.mars.rx.producers.{EsiProducer, EveApiProducer}
import com.softwaremill.sttp._
import io.circe.syntax._
import com.softwaremill.sttp.circe._
import com.typesafe.scalalogging.LazyLogging
import org.kys.mars.models.Json.EsiName
import org.kys.mars.marsConfig
import org.kys.mars.models.EsiNotificationType
import org.kys.mars.models.EsiNotificationType._

import scala.concurrent.ExecutionContext.Implicits.global
import retry.Success._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object EsiUtils extends EsiProducer with EveApiProducer with LazyLogging {
  val token = ""
  val rate: FiniteDuration = 1.second

  def resolveNamesTask(ids: List[Long]): Task[Either[Throwable, List[EsiName]]] = {
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
        retriableFuture.flatMap {
          case Success(r) => Future.successful(r)
          case Failure(ex) => Future.failed(ex)
        }
      }.attempt
    }
  }

  def getUrlByItemId(id: Long): String = {
    val imgBaseUrl = marsConfig.getString("eve.imgBaseUrl")
    s"$imgBaseUrl/type/${id}_64.png"
  }

  def getDotlanRangeUrlBySystemName(name: String): String = {
    s"${marsConfig.getString("eve.dotlanBaseUrl")}/range/Avatar,5/$name"
  }

  def isCitadelAttackedAlert(t: EsiNotificationType): Boolean = t match {
    case StructureUnderAttack => true
    case StructureLostShields => true
    case StructureLostArmor => true
    case StructureDestroyed => true
    case _ => false
  }

  def isPosAttackedAlert(t: EsiNotificationType): Boolean = t match {
    case TowerAlertMsg => true
    case _ => false
  }

  def isFuelAlert(t: EsiNotificationType): Boolean = t match {
    case TowerResourceAlertMsg => true
    case StructureFuelAlert => true
    case _ => false
  }

  def isStructureDirectorAlert(t: EsiNotificationType): Boolean = t match {
    case StructureOnline => true
    case StructureServicesOffline => true
    case StructuresReinforcementChanged  => true
    case StructureWentHighPower => true
    case StructureAnchoring => true
    case StructureUnanchoring => true
    case OwnershipTransferred => true
    case _ => false
  }

  def isMiscDirectorAlert(t: EsiNotificationType): Boolean = t match {
    case CorpAllBillMsg => true
    case CorpAppNewMsg => true
    case CorpNewCEOMsg => true
    case CorpTaxChangeMsg => true
    case CharLeftCorpMsg => true
    case CharAppWithdrawMsg => true
    case CharAppAcceptMsg => true
    case _ => false
  }

  def isNotificationRelevant(t: EsiNotificationType): Boolean = {
    isCitadelAttackedAlert(t) ||
    isPosAttackedAlert(t) ||
    isFuelAlert(t) ||
    isStructureDirectorAlert(t) ||
    isMiscDirectorAlert(t)
  }
}
