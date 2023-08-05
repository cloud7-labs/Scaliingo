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

package app.cloud7.tiingo.exceptions

sealed trait EndpointException extends Exception

object EndpointException {

  /**
   * Exception thrown when an invalid resample frequency is provided.
   *
   * @param invalidFreq The invalid resample frequency.
   */
  case class InvalidResampleFreq(invalidFreq: String) extends EndpointException {
    def message = s"Invalid resample frequency: $invalidFreq"
  }

  /**
   * Exception thrown when an invalid date range is provided.
   *
   * @param startDate The start date.
   * @param endDate   The end date.
   */
  case class InvalidDateRange(startDate: String, endDate: String)
    extends EndpointException {
    def message = s"Invalid date range: $startDate - $endDate"
  }

  /**
   * Exception thrown when an invalid date format is provided.
   *
   * @param date The invalid date.
   */
  case class InvalidDateFormat(date: String) extends EndpointException {
    def message = s"Invalid date format: $date. Must be yyyy-MM-dd"
  }
}
