package org.kys.mars.rx.producers

import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend}
import monix.execution.Cancelable
import monix.reactive.Observable
import monix.reactive.observers.Subscriber

import scala.concurrent.duration.FiniteDuration
import scala.language.higherKinds

trait EveApiProducer {
  protected val rate: FiniteDuration
  protected implicit val sttpBackend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
}
