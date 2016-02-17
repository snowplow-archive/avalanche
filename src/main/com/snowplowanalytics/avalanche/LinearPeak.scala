/*
 * Copyright (c) 2016 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.snowplowanalytics.avalanche

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Will create a Linear load increase on a Snowplow Pipeline.
 *
 * Simulation Breakdown Example:
 *
 * - SP_SIM_TIME=60
 * - Runtime: 60 minutes
 *
 * - SP_BASELINE_USERS=100
 * - Baseline: ~ 6000 events/minute
 *   - Runs for 60 minutes
 *
 * - SP_PEAK_USERS=10000
 * - Peak: ~ 600000 events/minute
 *   - Starts 5 minutes after baseline
 *   - Ramps up for 20 minutes
 *   - Full peak for 10 minutes
 *   - Ramps down for 20 minutes
 *   - Ends 5 minutes before baseline
 */
class LinearPeak extends Simulation {

  // Get ENV Vars for simulation
  val SimulationTime = sys.env("SP_SIM_TIME").toInt
  val BaselineUsers = sys.env("SP_BASELINE_USERS").toInt
  val PeakUsers = sys.env("SP_PEAK_USERS").toInt

  // Raw Events
  val PageViewEvent = "e=pv&page=DemoPageTitle&refr=DemoPageReferrer&url=DemoPageUrl&co=%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fcontexts%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%5B%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fmobile_context%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22deviceManufacturer%22%3A%22Apple%20Inc.%22%2C%22osVersion%22%3A%229.2%22%2C%22osType%22%3A%22ios%22%2C%22deviceModel%22%3A%22iPhone%22%2C%22networkType%22%3A%22wifi%22%2C%22appleIdfv%22%3A%22A229D70E-3C5F-4F75-994C-7AFC610FDCC4%22%7D%7D%2C%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fclient_session%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22previousSessionId%22%3A%2227f32674-c57c-4ad6-9a39-d43de062c748%22%2C%22firstEventId%22%3A%22ecadf00a-5fd3-4728-a305-a334353a0d24%22%2C%22sessionId%22%3A%2217d7d5dc-0ae5-44a3-af13-4f13962d57fb%22%2C%22userId%22%3A%221e6f3636-213f-41af-a7c6-cc6bb4b9455a%22%2C%22sessionIndex%22%3A7%2C%22storageMechanism%22%3A%22SQLITE%22%7D%7D%5D%7D"
  val StructuredEvent = "e=se&se_la=DemoLabel&se_pr=DemoProperty&se_ca=DemoCategory&se_va=5&se_ac=DemoAction&co=%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fcontexts%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%5B%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fmobile_context%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22deviceManufacturer%22%3A%22Apple%20Inc.%22%2C%22osVersion%22%3A%229.2%22%2C%22osType%22%3A%22ios%22%2C%22deviceModel%22%3A%22iPhone%22%2C%22networkType%22%3A%22wifi%22%2C%22appleIdfv%22%3A%22A229D70E-3C5F-4F75-994C-7AFC610FDCC4%22%7D%7D%2C%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fclient_session%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22previousSessionId%22%3A%2227f32674-c57c-4ad6-9a39-d43de062c748%22%2C%22firstEventId%22%3A%22ecadf00a-5fd3-4728-a305-a334353a0d24%22%2C%22sessionId%22%3A%2217d7d5dc-0ae5-44a3-af13-4f13962d57fb%22%2C%22userId%22%3A%221e6f3636-213f-41af-a7c6-cc6bb4b9455a%22%2C%22sessionIndex%22%3A7%2C%22storageMechanism%22%3A%22SQLITE%22%7D%7D%5D%7D"

  object Events {
    val baseline = during(SimulationTime minutes) {
      exec(http("PageView Event")
        .get("/i?" + PageViewEvent))
      .pause(1 seconds)
      .exec(http("Structured Event")
        .get("/i?" + StructuredEvent))
      .pause(1 seconds)
    }
    val peak = during((SimulationTime/2) minutes, "i") {
      exec(http("PageView Event")
        .get("/i?" + PageViewEvent))
      .pause(1 seconds)
      .exec(http("Structured Event")
        .get("/i?" + StructuredEvent))
      .pause(1 seconds)
    }
  }

  // HTTP Configuration
  val httpConf = http
    .baseURL(sys.env("SP_COLLECTOR_URL"))
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  // Create Users
  val baseline = scenario("Baseline").exec(Events.baseline)
  val peak = scenario("Peak").exec(Events.peak)

  // Simulation
  setUp(
    baseline.inject(atOnceUsers(BaselineUsers)),
    peak.inject(nothingFor((SimulationTime/12) minutes), rampUsers(PeakUsers) over ((SimulationTime/3) minutes))
  ).protocols(httpConf)
}
