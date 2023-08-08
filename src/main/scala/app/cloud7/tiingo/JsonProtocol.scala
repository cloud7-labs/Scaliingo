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

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import app.cloud7.tiingo.api._
import spray.json._

/**
 * Provides the JSON protocol for unmarshalling Tiingo API responses.
 */
object JsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val eodMetaDataFormat: RootJsonFormat[EodApi.MetaData] = jsonFormat6(
    EodApi.MetaData
  )

  implicit val eodPriceDataFormat: RootJsonFormat[EodApi.PriceData] = jsonFormat13(
    EodApi.PriceData
  )

  implicit val iexTobLastPriceFormat: RootJsonFormat[IexApi.TobLastPrice] = jsonFormat17(
    IexApi.TobLastPrice
  )

  implicit val iexHistoricalPriceDataFormat: RootJsonFormat[IexApi.HistoricalPriceData] =
    jsonFormat6(IexApi.HistoricalPriceData)

}
