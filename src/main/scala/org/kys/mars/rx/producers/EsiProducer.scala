package org.kys.mars.rx.producers
import com.softwaremill.sttp._
import org.kys.mars.marsConfig

trait EsiProducer {
  val token: String
  protected val baseUrl: String = marsConfig.getString("eve.esiBaseUrl")
  protected val baseRequest: RequestT[Empty, String, Nothing] = sttp.auth.bearer(token)
}
