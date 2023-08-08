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
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.pattern.after
import app.cloud7.tiingo.api.Logging
import app.cloud7.tiingo.exceptions.ClientException
import app.cloud7.tiingo.exceptions.ClientException._
import cats.data.Validated
import cats.implicits._
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.concurrent.duration._

/**
 * Configuration for [[RestClient]].
 *
 * @param apiKey  The Tiingo API key.
 * @param headers Additional HTTP headers to include in the API requests.
 * @param pause   The duration to pause between consecutive API requests.
 * @param timeout The maximum duration to wait for an API request to complete.
 * @param baseUrl The base URL for the API requests.
 */
final case class ClientConfig(
    apiKey: String,
    headers: List[HttpHeader],
    pause: FiniteDuration,
    timeout: FiniteDuration,
    baseUrl: Uri
)

/**
 * Companion object for [[ClientConfig]].
 */
object ClientConfig {
  private val config = ConfigFactory.load()

  val baseUrl: Uri = "https://api.tiingo.com"

  def apply(
      apiKey: Option[String] = None,
      headers: Map[String, String] = Map.empty
  ): ClientConfig = {

    val k = resolveApiKey(apiKey).fold(
      err => throw err,
      v => v
    )

    val headersList =
      parseHeaders(
        headers + ("Authorization" -> s"Token $k")
      )

    val pause = FiniteDuration(config.getLong("api.pause"), "seconds")
    val timeout = FiniteDuration(config.getLong("api.timeout"), "seconds")

    val validatedTimeout = validateTimeoutDuration(timeout).fold(err => throw err, v => v)

    val validatedPause = validatePauseDuration(pause).fold(err => throw err, v => v)

    ClientConfig(k, headersList, validatedPause, validatedTimeout, baseUrl)
  }

  /**
   * Resolves the API key to be used for the requests.
   *
   * @param providedApiKey the API key provided by the user.
   * @return the API key to be used for the requests.
   */
  private def resolveApiKey(
      providedApiKey: Option[String]
  ): Validated[ClientException, String] =
    providedApiKey
      .orElse {
        val configApiKey = config.getString("api.key")
        if (configApiKey.equals("your-api-key-here"))
          sys.env.get("TIINGO_API_KEY")
        else Some(configApiKey)
      }
      .toValid(ApiKeyNotProvided)

  /**
   * Validates the timeout duration.
   *
   * @param timeout the timeout duration to validate.
   * @return A cats Validated instance containing the timeout duration if it is valid, or an invalid timeout duration exception otherwise.
   */
  private def validateTimeoutDuration(
      timeout: FiniteDuration
  ): Validated[ClientException, FiniteDuration] =
    if (timeout.toSeconds > 0) timeout.valid
    else InvalidTimeoutDuration(timeout).invalid

  /**
   * Validates the pause duration.
   *
   * @param pause the pause duration to validate.
   * @return A cats Validated instance containing the pause duration if it is valid, or an invalid pause duration exception otherwise.
   */
  private def validatePauseDuration(
      pause: FiniteDuration
  ): Validated[ClientException, FiniteDuration] =
    if (pause.toSeconds > 0) pause.valid
    else InvalidPauseDuration(pause).invalid

  /**
   * Parses a map of HTTP headers into a list of HttpHeader.
   *
   * @param headerMap The map of HTTP headers to parse.
   * @return The list of parsed HttpHeader. If a header fails to parse, an error message is printed and the header is omitted.
   */
  private def parseHeaders(
      headerMap: Map[String, String]
  ): List[HttpHeader] =
    headerMap.toList.flatMap { case (key, value) =>
      HttpHeader.parse(key, value) match {
        case HttpHeader.ParsingResult.Ok(parsedHeader, _) =>
          Some(parsedHeader)
        case HttpHeader.ParsingResult.Error(error) =>
          println(s"Failed to parse header '$key: $value': $error")
          None
      }
    }
}

/**
 * Represents a REST client for the Tiingo API.
 *
 * @param config the configuration for the client.
 * @param system the actor system to be used for the client.
 */
final case class RestClient(config: ClientConfig)(implicit
    system: ActorSystem
) extends Logging {

  implicit val ec: ExecutionContext = system.dispatcher

  /**
   * Sends a request to the specified URL and unmarshalls the response entity to type A.
   *
   * @param url the URL to which the request should be sent.
   * @param um the unmarshaller to convert the response entity to type A.
   * @tparam A the type to which the response entity should be unmarshalled.
   * @return A future of type A.
   */
  def sendRequest[A](url: Uri)(implicit
      um: Unmarshaller[ResponseEntity, A]
  ): Future[A] = {
    get(url, config.headers, config.pause, config.timeout)
  }

  /**
   *
   * @param url     the URL to which the request should be sent.
   * @param headers the headers to be included in the request.
   * @param pause   the duration for which the request should be paused before being sent.
   * @param timeout the duration after which the request should timeout.
   * @param um      the unmarshaller to convert the response entity to type A.
   * @tparam A the type to which the response entity should be unmarshalled.
   * @return A future of type A.
   */
  private def get[A](
      url: Uri,
      headers: List[HttpHeader],
      pause: FiniteDuration,
      timeout: FiniteDuration
  )(implicit um: Unmarshaller[ResponseEntity, A]): Future[A] =
    makeRequestWithMethod(
      HttpMethods.GET,
      url,
      headers,
      HttpEntity(ContentTypes.`application/json`, ""),
      pause,
      timeout
    )

  /**
   * Makes a HTTP request with the specified method, url, headers, and entity. The request will be paused for the specified duration
   * and will timeout after the specified duration.
   *
   * @param method  the HTTP method to be used for the request.
   * @param url     the URL to which the request should be sent.
   * @param headers the headers to be included in the request.
   * @param entity  the entity to be sent with the request.
   * @param pause   the duration for which the request should be paused before being sent.
   * @param timeout the duration after which the request should timeout.
   * @param um      the unmarshaller to convert the response entity to type A.
   * @param system  the actor system in which the request is made.
   * @return the response entity unmarshalled to type A.
   */
  private def makeRequestWithMethod[A](
      method: HttpMethod,
      url: Uri,
      headers: List[HttpHeader],
      entity: RequestEntity,
      pause: FiniteDuration,
      timeout: FiniteDuration
  )(implicit
      um: Unmarshaller[ResponseEntity, A],
      system: ActorSystem
  ): Future[A] = {

    val httpRequest =
      HttpRequest(method = method, uri = url, headers = headers, entity = entity)

    val responseFuture: Future[HttpResponse] = {
      val requestWithTimeout = Future.firstCompletedOf(
        Seq(
          Http().singleRequest(httpRequest),
          after(timeout, system.scheduler)(
            Future.failed(new TimeoutException("Request timed out"))
          )
        )
      )
      after(pause, system.scheduler)(requestWithTimeout)
    }

    responseFuture.flatMap { response =>
      logger.info(
        s"Status code: ${response.status.intValue}/Content-Type: ${response.entity.contentType}"
      )

      response.status match {
        // Handle redirect status codes
        case StatusCodes.MovedPermanently | StatusCodes.Found =>
          response.header[Location] match {
            case Some(location) =>
              logger.info(s"Redirecting to: ${location.uri}")
              makeRequestWithMethod(
                method,
                location.uri,
                headers,
                entity,
                pause,
                timeout
              ) // Recursive call
            case None =>
              Future.failed(new Exception("Redirect status without location header"))
          }
        case otherStatus if otherStatus.isFailure() =>
          Unmarshal(response.entity)
            .to[String]
            .flatMap(body => Future.failed(new Exception(s"Failed response body: $body")))
        case _ =>
          Unmarshal(response.entity).to[A]
      }
    }
  }
}
