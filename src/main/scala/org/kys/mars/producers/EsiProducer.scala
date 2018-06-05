package org.kys.mars.producers
import com.softwaremill.sttp._

trait EsiProducer {
  val token: String
  protected val baseUrl: String = "https://esi.evetech.net/latest"
  protected val baseRequest: RequestT[Empty, String, Nothing] = sttp.auth.bearer(token)
}
