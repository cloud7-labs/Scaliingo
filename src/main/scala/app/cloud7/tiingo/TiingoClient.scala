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
import app.cloud7.tiingo.api.EodApi

import scala.concurrent.ExecutionContext

/**
 * Client for interfacing with the Tiingo API.
 *
 * @param apiKey The Tiingo API key.
 * @param headers Additional HTTP headers to include in the API requests.
 * @param system The actor system.
 * @param ec    The execution context.
 */
final case class TiingoClient(
    apiKey: Option[String] = None,
    headers: Map[String, String] = Map.empty
)(implicit val system: ActorSystem, val ec: ExecutionContext) {

  val config: ClientConfig = ClientConfig(apiKey, headers)

  /**
   * Get the EOD API.
   *
   * @return The EOD API.
   */
  def getEod: EodApi = EodApi(config)
}
