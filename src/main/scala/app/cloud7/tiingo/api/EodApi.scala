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
import app.cloud7.tiingo.exceptions._
import app.cloud7.tiingo.JsonProtocol._
import app.cloud7.tiingo.RestClient
import app.cloud7.tiingo.exceptions.EndpointException._
import cats.data.Validated

import scala.concurrent.{ExecutionContext, Future}

/**
 * Represents the Tiingo End-of-Day API.
 */
trait EodApi extends MetaApi {

  import app.cloud7.tiingo.api.EodApi._

  /**
   * Fetches the metadata of a ticker.
   *
   * @param ticker The ticker symbol.
   * @return A future of the metadata.
   */
  def fetchMetaData(ticker: String): Future[MetaData] =
    MetaDataEndpoint(ticker, restClient).fetch

  /**
   * Fetches the price data of a ticker.
   *
   * @param ticker The ticker symbol.
   * @param startDate The start date of the ticker.
   * @param endDate The end date of the ticker.
   * @param frequency The frequency of the data.
   * @return A future sequence of the price data.
   */
  def fetchPriceData(
      ticker: String,
      startDate: Option[String] = None,
      endDate: Option[String] = None,
      frequency: Option[String] = None
  ): Future[Seq[PriceData]] =
    PriceDataEndpoint(ticker, startDate, endDate, frequency, restClient).fetch
}

/**
 * Companion object for the Tiingo End-of-Day API.
 */
object EodApi {

  import app.cloud7.tiingo.ClientConfig

  def apply(config: ClientConfig)(implicit _system: ActorSystem, _ec: ExecutionContext): EodApi =
    new EodApi {
      override implicit val system: ActorSystem = _system
      override implicit val ec: ExecutionContext = _ec
      override val restClient: RestClient = RestClient(config)(system)
      override val clientConfig: ClientConfig = config

    }

  /**
   * Represents the metadata of a ticker.
   *
   * @param ticker       The ticker symbol.
   * @param name         The name of the ticker.
   * @param exchangeCode The code of the exchange.
   * @param startDate    The start date of the ticker.
   * @param endDate      The end date of the ticker.
   * @param description  The description of the ticker.
   */
  final case class MetaData(
      ticker: String,
      name: String,
      exchangeCode: String,
      startDate: String,
      endDate: String,
      description: String
  ) {
    override def toString =
      s"MetaData($ticker, $name, $exchangeCode, $startDate, $endDate, $description)"
  }

  /**
   * Represents the end of day price data for a ticker.
   *
   * @param date        The date of the data.
   * @param close       The closing price.
   * @param high        The highest price.
   * @param low         The lowest price.
   * @param open        The opening price.
   * @param volume      The volume of the trades.
   * @param adjClose    The adjusted closing price.
   * @param adjHigh     The adjusted highest price.
   * @param adjLow      The adjusted lowest price.
   * @param adjOpen     The adjusted opening price.
   * @param adjVolume   The adjusted volume of the trades.
   * @param divCash     The dividend cash.
   * @param splitFactor The split factor.
   */
  final case class PriceData(
      date: String,
      close: Double,
      high: Double,
      low: Double,
      open: Double,
      volume: Long,
      adjClose: Double,
      adjHigh: Double,
      adjLow: Double,
      adjOpen: Double,
      adjVolume: Long,
      divCash: Double,
      splitFactor: Double
  ) {
    override def toString =
      s"PriceData($date, $close, $high, $low, $open, $volume, $adjClose, $adjHigh, $adjLow, $adjOpen, $adjVolume, $divCash, $splitFactor)"
  }

  /**
   * Represents an endpoint of the [[EodApi]].
   *
   * @tparam T The type of the data returned by the endpoint.
   */
  sealed trait EodEndpoint[T] extends MetaEndpoint[T] {
    private val validResampleFreqs =
      List("daily", "weekly", "monthly", "quarterly", "annually")

    /**
     * Validates the resample frequency.
     *
     * @param resampleFreq The resample frequency.
     * @return A cats Validated of or a string.
     */
    def validateResampleFreq(
        resampleFreq: String
    ): Validated[EndpointException, String] = {
      if (validResampleFreqs.contains(resampleFreq)) {
        Validated.Valid(resampleFreq)
      } else {

        Validated.Invalid(InvalidResampleFreq(resampleFreq))
      }
    }
  }

  /**
   * Represents the endpoint for the end of day metadata for a ticker.
   *
   * @param ticker     The ticker symbol.
   * @param restClient The REST client.
   * @param um         The unmarshaller.
   */
  final case class MetaDataEndpoint(ticker: String, restClient: RestClient)(implicit
      val um: Unmarshaller[ResponseEntity, EodApi.MetaData]
  ) extends EodEndpoint[EodApi.MetaData] {
    override val query: Uri.Query = Uri.Query.Empty

    override val endpointPath: Uri.Path = Uri.Path(s"/tiingo/daily/$ticker")

  }

  /**
   * Represents the endpoint for the end of day price data for a ticker.
   *
   * @param ticker The ticker symbol.
   * @param startDate The start date.
   * @param endDate The end date.
   * @param resampleFreq The resample frequency.
   * @param restClient The REST client.
   * @param um The unmarshaller.
   */
  final case class PriceDataEndpoint(
      ticker: String,
      startDate: Option[String],
      endDate: Option[String],
      resampleFreq: Option[String],
      restClient: RestClient
  )(implicit val um: Unmarshaller[ResponseEntity, List[PriceData]])
      extends EodEndpoint[List[PriceData]] {

    override val query: Uri.Query = {
      def validatedDate(dateOpt: Option[String]): Option[String] =
        dateOpt.flatMap { date =>
          validateDateFormat(date).fold(
            e => {
              logger.info(e.getMessage)
              None
            },
            date => Some(date)
          )
        }

      val validatedStart = validatedDate(startDate)
      val validatedEnd = validatedDate(endDate)

      val queryMap = (validatedStart, validatedEnd) match {
        case (Some(s), Some(e)) =>
          validateDateRange(s, e).fold(
            e => {
              logger.info(e.getMessage)
              Map.empty[String, String]
            },
            _ => Map("startDate" -> s, "endDate" -> e)
          )
        case (Some(s), None) => Map("startDate" -> s)
        case (None, Some(e)) => Map("endDate" -> e)
        case _               => Map.empty[String, String]
      }

      val withResampleFreq = resampleFreq.flatMap { r =>
        validateResampleFreq(r).fold(
          exception => {
            logger.info(exception.getMessage)
            None
          },
          freq => Some(queryMap + ("resampleFreq" -> freq))
        )
      }

      Uri.Query(withResampleFreq.getOrElse(queryMap))
    }

    override val endpointPath = Uri.Path(s"/tiingo/daily/$ticker/prices")

  }
}