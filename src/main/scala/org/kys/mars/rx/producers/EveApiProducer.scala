package org.kys.mars.rx.producers

import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend, SttpBackendOptions}
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration
import scala.language.higherKinds

trait EveApiProducer {
  protected val rate: FiniteDuration
  protected implicit val sttpBackend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend(
    SttpBackendOptions.Default.copy(connectionTimeout = 5.seconds))
}
