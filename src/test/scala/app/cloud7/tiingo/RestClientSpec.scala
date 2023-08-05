/*
 * Copyright 2023 crotodev
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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RestClientSpec extends AnyWordSpec with Matchers {

  implicit val system: ActorSystem = ActorSystem("test-system")


  "ClientConfig" when {
    "provided with a valid API key" should {
      "be able to be created" in {
        val config = ClientConfig(Some("token"))
        config should not be null
      }

      "contain the provided API key" in {
        val config = ClientConfig(Some("token"))
        config.apiKey shouldEqual "token"
      }

      "contain the Authorization header with correct token value" in {
        val config = ClientConfig(Some("token"))

        val header = config.headers.head

        (header.name -> header.value) shouldEqual ("Authorization" -> "Token token")
      }
    }

    "provided with additional headers" should {
      "contain the additional headers in the config" in {
        val headers = Map("User-Agent" -> "Akka HTTP")
        val config = ClientConfig(Some("token"), headers)
        val headerListMap = config.headers.map(h => (h.name, h.value)).toMap
        (headerListMap should contain).allOf("Authorization" -> "Token token",
          "User-Agent" -> "Akka HTTP"
        )
      }
    }

    "provided with invalid headers" should {
      "ignore the invalid headers and still create the config" in {
        val headers = Map("Invalid-Header" -> "Invalid Value")
        val config = ClientConfig(Some("token"), headers)
        config.headers should not contain ("Invalid-Header" -> "Invalid Value")
      }
    }
  }

}
