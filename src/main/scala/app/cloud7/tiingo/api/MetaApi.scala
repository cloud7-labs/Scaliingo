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
import akka.http.scaladsl.model.{ResponseEntity, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshaller
import app.cloud7.tiingo.{ClientConfig, RestClient}
import cats.data.Validated
import org.slf4j.{Logger, LoggerFactory}

import java.text.{ParseException, SimpleDateFormat}
import scala.concurrent.{ExecutionContext, Future}

/**
 * A trait that provides a logger.
 */
trait Logging {
  protected val logger: Logger = LoggerFactory.getLogger(getClass)
}

/**
 * Represents the API which all other API interfaces extend.
 */
trait MetaApi extends Logging {

  implicit val system: ActorSystem

  implicit val ec: ExecutionContext

  val baseUrl: Uri = "https://api.tiingo.com"

  val clientConfig: ClientConfig

  protected val restClient: RestClient
}

/**
 * Represents an endpoint of the API.
 *
 * @tparam T The type of the data returned by the endpoint.
 */
trait MetaEndpoint[T] extends Logging {

  import app.cloud7.tiingo.exceptions.EndpointException.{InvalidDateFormat, InvalidDateRange}

  implicit val um: Unmarshaller[ResponseEntity, T]

  val restClient: RestClient
  val query: Uri.Query = Uri.Query.Empty
  val endpointPath: Uri.Path = Uri.Path.Empty

  /**
   * Endpoint of the API.
   *
   * @return A Akka HTTP Uri.
   */
  protected def endpoint: Uri =
    restClient.config.baseUrl.withPath(endpointPath).withQuery(query)

  /**
   * Fetches the data from the endpoint.
   *
   * @return A future of type T.
   */
  def fetch: Future[T] = {
    logger.info(s"Fetching data from ${endpoint.toString}")
    restClient.sendRequest[T](endpoint)(um)
  }

  /**
   * Validates a date range.
   *
   * @param startDate The start date.
   * @param endDate   The end date.
   * @return A cats Validated of type [[app.cloud7.tiingo.exceptions.EndpointException.InvalidDateRange]] or a tuple of strings.
   */
  def validateDateRange(
      startDate: String,
      endDate: String
  ): Validated[InvalidDateRange, (String, String)] = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    if (format.parse(startDate).after(format.parse(endDate))) {
      Validated.invalid(InvalidDateRange(startDate, endDate))
    } else {
      Validated.valid((startDate, endDate))
    }
  }

  /**
   * Validates a date.
   *
   * @param date The date to validate.
   * @return A cats Validated of type [[app.cloud7.tiingo.exceptions.EndpointException.InvalidDateFormat]] or string.
   */
  protected def validateDateFormat(date: String): Validated[InvalidDateFormat, String] = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    Validated
      .catchOnly[ParseException](format.parse(date))
      .map(_ => date)
      .leftMap(_ => InvalidDateFormat(date))
  }
}
