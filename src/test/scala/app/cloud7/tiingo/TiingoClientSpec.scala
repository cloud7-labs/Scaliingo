package app.cloud7.tiingo

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import app.cloud7.tiingo.api.EodApi

class TiingoClientSpec extends AnyWordSpec with Matchers {

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  val clientConfig = ClientConfig()
  val restClient = RestClient(clientConfig)
  val mockTicker = "AAPL"

  "TiingoClient" should {
    "be able to be created" in {
      val client = TiingoClient(Some("token"))
      client should not be null
    }
    "be able to get the EOD API" in {
      val client = TiingoClient(Some("token"))
      val eodApi = client.getEod
      eodApi should not be null
      eodApi shouldBe a[EodApi]
    }
  }
}
