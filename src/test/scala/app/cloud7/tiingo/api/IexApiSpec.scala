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

package app.cloud7.tiingo.api

import akka.actor.ActorSystem
import app.cloud7.tiingo.{ClientConfig, RestClient}
import app.cloud7.tiingo.JsonProtocol._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration._

class IexApiSpec extends AnyWordSpec with Matchers {
  implicit val system: ActorSystem = ActorSystem()
  val clientConfig = ClientConfig()
  val restClient = RestClient(clientConfig)
  val mockTicker1 = "AAPL"
  val mockTicker2 = "GOOGL"
  val mockTickers = List(mockTicker1, mockTicker2)

  "TobLastPriceEndpoint" should {
    "be able to be created" in {
      val endpoint = IexApi.TobLastPriceEndpoint(mockTickers, None, restClient)
      endpoint should not be null
    }

    "be able to fetch data" in {
      val endpoint = IexApi.TobLastPriceEndpoint(mockTickers, None, restClient)
      val data = endpoint.fetch
      data should not be null
      val result = Await.result(data, 5.seconds)
      result.head.ticker shouldBe mockTickers.head
    }
  }

  "HistoricalPriceDataEndpoint" should {
    "be able to be created" in {
      val endpoint = IexApi.HistoricalPriceDataEndpoint(mockTicker1, None, restClient)
      endpoint should not be null
    }

    "be able to fetch data" in {
      val endpoint = IexApi.HistoricalPriceDataEndpoint(mockTicker1, None, restClient)
      val data = endpoint.fetch
      data should not be null
      val result = Await.result(data, 5.seconds)
      result should not be null
    }
  }
}
