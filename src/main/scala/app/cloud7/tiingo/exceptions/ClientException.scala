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

package app.cloud7.tiingo.exceptions

import scala.concurrent.duration.FiniteDuration

/**
 * Represents an error related to the Tiingo API.
 *
 * @see The [[ClientException]] companion object for the concrete types of errors.
 */
sealed trait ClientException extends Exception

object ClientException {

  /**
   * Error indicating that the API key was not provided.
   */
  case object ApiKeyNotProvided extends ClientException {
    def message = "API key not provided"
  }

  /**
   * Error indicating that the timeout is invalid.
   *
   * @param timeout the invalid timeout.
   */
  case class InvalidTimeoutDuration(timeout: FiniteDuration) extends ClientException {
    def message = s"Invalid timeout duration: $timeout"
  }

  /**
   * Error indicating that the pause is invalid.
   *
   * @param pause the invalid pause.
   */
  case class InvalidPauseDuration(pause: FiniteDuration) extends ClientException {
    def message = s"Invalid pause duration: $pause"
  }

  /**
   * Error indicating inability to parse header.
   *
   * @param key the key of the header.
   * @param value the value of the header.
   */
  case class HeaderParsingException(key: String, value: String) extends ClientException {
    def message = s"Invalid header: $key -> $value"
  }
}
