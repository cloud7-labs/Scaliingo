# Scaliingo

Scaliingo is a Scala client for interfacing with the Tiingo API. This robust library is designed to connect effortlessly with the Tiingo financial data platform. Currently, Scaliingo only supports Tiingo's end-of-day (EOD) API endpoints. Support for other Tiingo's API endpoints such as news, crypto, forex, and IEX will be included in future releases.

## Installation

To use Scaliingo in your SBT project, add the following line to your `build.sbt` file:

    ``` scala
    libraryDependencies += "app.cloud7" %% "scaliingo_2.12_1.0" % "0.1.0"
    ```

## License

This project is licensed under the Apache License, Version 2.0. For more information, please refer to the `LICENSE` file in the project root.

## Usage

``` scala
// Import the TiingoClient
import dev.cloud9.tiingo._

// Create a new TiingoClient instance
val client = TiingoClient(Some("YOUR_API_KEY"))

// Access the EodApi
val eodApi = client.getEod

// Get a ticker's metadata
val metadata = eodApi.fetchMetaData("GOOGL")

// Get a ticker's end-of-day data
val priceData = eodApi.fetchPriceData(
    ticker = "GOOGL", 
    startDate = Some("2020-01-01"), 
    endDate = Some("2020-01-31"), 
    frequency = Some("daily")
    )
```

## Contributions

We welcome contributions from the community. Please refer to the `CONTRIBUTING.md` file for more information.

## Disclaimer

Please note that this project, Scaliingo, is an independent project developed for interacting with the Tiingo API and is not officially affiliated with, endorsed by, or directly related to Tiingo Inc. or their official API. The use of this project is at your own discretion and risk. Please ensure you abide by Tiingo's API usage policies when using Scaliingo.