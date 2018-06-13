package org.kys.mars.rx.producers
import com.softwaremill.sttp._
import org.kys.mars.marsConfig

trait EsiProducer {
  protected val baseUrl: String = marsConfig.getString("eve.esiBaseUrl")
  protected def baseRequest(token: String): RequestT[Empty, String, Nothing] = sttp.auth.bearer(token)
}
