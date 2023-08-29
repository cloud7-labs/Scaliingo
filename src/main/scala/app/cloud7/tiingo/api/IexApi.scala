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

import akka.http.scaladsl.model.*
import akka.http.scaladsl.unmarshalling.Unmarshaller
import app.cloud7.tiingo.exceptions.EndpointException
import app.cloud7.tiingo.JsonProtocol.*
import app.cloud7.tiingo.RestClient
import cats.data.*
import akka.actor.ActorSystem
import app.cloud7.tiingo.ClientConfig

import scala.concurrent.ExecutionContext

import scala.concurrent.Future

/**
 * Represents the API for the IEX endpoint.
 */
trait IexApi extends MetaApi {

  import app.cloud7.tiingo.api.IexApi.*

  /**
   * Fetches latest price data for tickers.
   *
   * @param tickers      The tickers to fetch.
   * @param resampleFreq The resample frequency.
   * @return A future of the latest price data.
   */
  def fetchLatestPriceData(
      tickers: List[String] = List.empty,
      resampleFreq: Option[String] = None
  ): Future[List[LatestPriceData]] =
    LatestPriceDataEndpoint(tickers, resampleFreq, restClient).fetch

  /**
   * Fetches the historical price data for a ticker.
   *
   * @param ticker The ticker.
   * @param frequency The frequency.
   * @return A future list of the historical price data.
   */
  def fetchHistoricalPriceData(
      ticker: String,
      frequency: Option[String] = None
  ): Future[List[HistoricalPriceData]] =
    HistoricalPriceDataEndpoint(
      ticker,
      frequency,
      restClient
    ).fetch
}

/**
 * Companion object for the IexApi trait.
 */
object IexApi {

  def apply(
      config: ClientConfig
  )(implicit _system: ActorSystem, _ec: ExecutionContext): IexApi =
    new IexApi {
      implicit override val system: ActorSystem = _system
      implicit override val ec: ExecutionContext = _ec
      override val restClient: RestClient = RestClient(config)(_system)
      override val clientConfig: ClientConfig = config
    }

  /**
   * Represents an endpoint of the [[IexApi]].
   *
   * @tparam T The type of the data returned by the endpoint.
   */
  trait IexEndpoint[T] extends MetaEndpoint[T] {

    /**
     * Validates the resample frequency.
     *
     * @param resampleFreq The resample frequency.
     * @return A cats Validated of an endpoint exception or a string.
     */
    def validateResampleFreq(
        resampleFreq: String
    ): Validated[EndpointException, String] = {
      if (resampleFreq.matches("^[0-9]+(min|hour)$")) {
        Validated.Valid(resampleFreq)
      } else {
        Validated.Invalid(EndpointException.InvalidResampleFreq(resampleFreq))
      }
    }

    override val endpointPath: Uri.Path = Uri.Path("/iex/")
  }

  /**
   * Represents the latest price data for a ticker from the IEX endpoint.
   *
   * @param ticker            the ticker symbol.
   * @param timestamp         the timestamp of the data.
   * @param quoteTimestamp    the timestamp of the quote.
   * @param lastSaleTimestamp the timestamp of the last sale.
   * @param last              the last sale price.
   * @param lastSize          the size of the last sale.
   * @param tngoLast          the last price from Tiingo.
   * @param prevClose         the previous closing price.
   * @param open              the opening price.
   * @param high              the highest price.
   * @param low               the lowest price.
   * @param mid               the midpoint price.
   * @param volume            the volume of trades.
   * @param bidSize           the size of the bid.
   * @param bidPrice          the bid price.
   * @param askSize           the size of the ask.
   * @param askPrice          the ask price.
   */
  final case class LatestPriceData(
      ticker: String,
      timestamp: String,
      quoteTimestamp: String,
      lastSaleTimestamp: String,
      last: Double,
      lastSize: Option[Int],
      tngoLast: Double,
      prevClose: Double,
      open: Double,
      high: Double,
      low: Double,
      mid: Double,
      volume: Int,
      bidSize: Option[Int],
      bidPrice: Double,
      askSize: Option[Int],
      askPrice: Double
  ) {
    override def toString =
      s"LatestPriceData($ticker, $timestamp, $quoteTimestamp, $lastSaleTimestamp, $last, $lastSize, $tngoLast, $prevClose, $open, $high, $low, $mid, $volume, $bidSize, $bidPrice, $askSize, $askPrice)"
  }

  /**
   * Represents historical price data for a ticker from the IEX endpoint.
   *
   * @param date  the date of the data.
   * @param open  the opening price.
   * @param high  the highest price.
   * @param low   the lowest price.
   * @param close the closing price.
   */
  final case class HistoricalPriceData(
      date: String,
      open: Double,
      high: Double,
      low: Double,
      close: Double,
      volume: Option[Int]
  ) {
    override def toString =
      s"HistoricalPriceData($date, $open, $high, $low, $close)"
  }

  /**
   * Represents the latest price data endpoint.
   *
   * @param tickers      The list of tickers.
   * @param resampleFreq The resample frequency.
   * @param restClient   The REST client.
   * @param um           The unmarshaller.
   */
  final case class LatestPriceDataEndpoint(
      tickers: List[String] = List.empty,
      resampleFreq: Option[String],
      restClient: RestClient
  )(implicit val um: Unmarshaller[ResponseEntity, List[LatestPriceData]])
      extends IexEndpoint[List[LatestPriceData]] {

    override val query: Uri.Query = {
      val validatedResampleFreq: String = resampleFreq match {
        case Some(freq) =>
          validateResampleFreq(freq).fold(
            e => {
              logger.info(e.getMessage)
              "1min"
            },
            r => r
          )
        case None => "1min"
      }
      Uri.Query(
        "tickers" -> tickers.mkString(","),
        "resampleFreq" -> validatedResampleFreq,
        "format" -> "json"
      )
    }
  }

  /**
   * Represents the historical price data endpoint.
   *
   * @param ticker The ticker.
   * @param resampleFreq The resample frequency.
   * @param restClient The REST client.
   * @param um The unmarshaller.
   */
  final case class HistoricalPriceDataEndpoint(
      ticker: String,
      resampleFreq: Option[String],
      restClient: RestClient
  )(implicit val um: Unmarshaller[ResponseEntity, List[HistoricalPriceData]])
      extends IexEndpoint[List[HistoricalPriceData]] {

    override val query: Uri.Query = {
      val validatedResampleFreq: String = resampleFreq match {
        case Some(freq) =>
          validateResampleFreq(freq).fold(
            e => {
              logger.info(e.getMessage)
              "1min"
            },
            r => r
          )
        case None => "1min"
      }
      Uri.Query(
        "resampleFreq" -> validatedResampleFreq,
        "columns" -> "date,open,high,low,close,volume",
        "format" -> "json"
      )
    }

    override val endpointPath: Uri.Path = Uri.Path(s"/iex/$ticker/prices")
  }
}
