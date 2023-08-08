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

class EodApiSpec extends AnyWordSpec with Matchers with Logging {
  implicit val system: ActorSystem = ActorSystem()
  val clientConfig = ClientConfig()
  val restClient = RestClient(clientConfig)
  val mockTicker = "AAPL"

  "MetaDataEndpoint" should {
    "be able to be created" in {
      val endpoint = EodApi.MetaDataEndpoint(mockTicker, restClient)
      endpoint should not be null
    }

    "be able to fetch data" in {
      val endpoint = EodApi.MetaDataEndpoint(mockTicker, restClient)
      val data = endpoint.fetch
      val result = Await.result(data, 5.seconds)
      result.ticker shouldBe mockTicker
      logger.info(s"result: $result")
    }
  }

  "PriceDataEndpoint" should {
    "be able to be created" in {
      val endpoint = EodApi.PriceDataEndpoint(mockTicker, None, None, None, restClient)
      endpoint should not be null
    }

    "be able to fetch data" in {
      val endpoint = EodApi.PriceDataEndpoint(mockTicker, None, None, None, restClient)
      val data = endpoint.fetch
      val result = Await.result(data, 5.seconds)
      result should not be null
      logger.info(s"result: $result")
    }

    "be able to fetch data with start date" in {
      val endpoint =
        EodApi.PriceDataEndpoint(mockTicker, Some("2020-01-01"), None, None, restClient)
      val data = endpoint.fetch
      val result = Await.result(data, 5.seconds)
      result should not be null
      logger.info(s"result: $result")
    }
  }
}
