/*
 * Copyright 2023 cloud7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.cloud7.tiingo

import akka.actor.ActorSystem
import app.cloud7.tiingo.api.EodApi
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

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
