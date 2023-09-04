# Scaliingo

## Overview
Scaliingo serves as a premier Scala-based client tailored for the Tiingo API. Its aim is to offer users an efficient and reliable bridge to the expansive financial data resources available on the Tiingo platform.

## Features
- **Seamless Connection**: Designed to integrate effortlessly with the Tiingo API, ensuring smooth data retrieval.
- **Current Support**: Focused on Tiingo's End-of-Day (EOD) and IEX API endpoints, providing businesses with crucial daily financial data.
- **Forward-Thinking**: Anticipate enhanced support for Tiingo's diverse API endpoints, including news, crypto, and forex, in our upcoming releases.

## Installation

To use Scaliingo in your SBT project, add the following line to your `build.sbt` file:

``` scala
libraryDependencies += "app.cloud7" % "scaliingo_2.12_1.0" % "0.1.1"
```

## Usage

``` scala
// Import the TiingoClient
import dev.cloud7.tiingo._

// Create a new TiingoClient instance
val client = TiingoClient(Some("YOUR_API_KEY"))

// Get a ticker's metadata
val metadata = client.fetchTickerMetaData("GOOGL")

// Get a ticker's end-of-day data
val priceData = client.fetchTickerPriceData(
    ticker = "GOOGL", 
    startDate = Some("2020-01-01"), 
    endDate = Some("2020-01-31"), 
    frequency = Some("daily")
    )
```

## Contributions

Contributions are welcome from the community. Please refer to the `CONTRIBUTING.md` file for more information.

## License

This project is licensed under the Apache License, Version 2.0. For more information, please refer to the `LICENSE` file in the project root.

## Disclaimer

Please note that this project, Scaliingo, is an independent project developed for interacting with the Tiingo API and is not officially affiliated with, endorsed by, or directly related to Tiingo Inc. or their official API. The use of this project is at your own discretion and risk. Please ensure you abide by Tiingo's API usage policies when using Scaliingo.