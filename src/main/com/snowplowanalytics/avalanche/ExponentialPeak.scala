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
 * Will create an Exponential load increase on a Snowplow Pipeline.
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
 *   - 1. Add 10000/500  Users over 20 minutes ~ 20
 *   - 2. Add 10000/250  Users over 18 minutes ~ 40
 *   - 3. Add 10000/125  Users over 16 minutes ~ 80
 *   - 4. Add 10000/62   Users over 14 minutes ~ 160
 *   - 5. Add 10000/31   Users over 12 minutes ~ 320
 *   - 6. Add 10000/16   Users over 10 minutes ~ 625
 *   - 7. Add 10000/8    Users over 8 minutes  ~ 1250
 *   - 8. Add 10000/4    Users over 6 minutes  ~ 2500
 *   - 9. Add 10000/2    Users over 4 minutes  ~ 5000
 *
 * - All users will be active at the ~25 minute mark and will run concurrently for 10 minutes
 */
class ExponentialPeak extends Simulation {

  // Get ENV Vars for simulation
  val SimulationTime = sys.env("SP_SIM_TIME").toInt
  val BaselineUsers = sys.env("SP_BASELINE_USERS").toInt
  val PeakUsers = sys.env("SP_PEAK_USERS").toInt

  // Raw Events
  val PageViewEvent = "e=pv&page=DemoPageTitle&refr=DemoPageReferrer&url=DemoPageUrl&co=%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fcontexts%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%5B%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fmobile_context%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22deviceManufacturer%22%3A%22Apple%20Inc.%22%2C%22osVersion%22%3A%229.2%22%2C%22osType%22%3A%22ios%22%2C%22deviceModel%22%3A%22iPhone%22%2C%22networkType%22%3A%22wifi%22%2C%22appleIdfv%22%3A%22A229D70E-3C5F-4F75-994C-7AFC610FDCC4%22%7D%7D%2C%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fclient_session%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22previousSessionId%22%3A%2227f32674-c57c-4ad6-9a39-d43de062c748%22%2C%22firstEventId%22%3A%22ecadf00a-5fd3-4728-a305-a334353a0d24%22%2C%22sessionId%22%3A%2217d7d5dc-0ae5-44a3-af13-4f13962d57fb%22%2C%22userId%22%3A%221e6f3636-213f-41af-a7c6-cc6bb4b9455a%22%2C%22sessionIndex%22%3A7%2C%22storageMechanism%22%3A%22SQLITE%22%7D%7D%5D%7D"
  val StructuredEvent = "e=se&se_la=DemoLabel&se_pr=DemoProperty&se_ca=DemoCategory&se_va=5&se_ac=DemoAction&co=%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fcontexts%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%5B%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fmobile_context%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22deviceManufacturer%22%3A%22Apple%20Inc.%22%2C%22osVersion%22%3A%229.2%22%2C%22osType%22%3A%22ios%22%2C%22deviceModel%22%3A%22iPhone%22%2C%22networkType%22%3A%22wifi%22%2C%22appleIdfv%22%3A%22A229D70E-3C5F-4F75-994C-7AFC610FDCC4%22%7D%7D%2C%7B%22schema%22%3A%22iglu%3Acom.snowplowanalytics.snowplow%5C%2Fclient_session%5C%2Fjsonschema%5C%2F1-0-1%22%2C%22data%22%3A%7B%22previousSessionId%22%3A%2227f32674-c57c-4ad6-9a39-d43de062c748%22%2C%22firstEventId%22%3A%22ecadf00a-5fd3-4728-a305-a334353a0d24%22%2C%22sessionId%22%3A%2217d7d5dc-0ae5-44a3-af13-4f13962d57fb%22%2C%22userId%22%3A%221e6f3636-213f-41af-a7c6-cc6bb4b9455a%22%2C%22sessionIndex%22%3A7%2C%22storageMechanism%22%3A%22SQLITE%22%7D%7D%5D%7D"

  object Events {
    val baseline = during(SimulationTime minutes, "n") {
      exec(http("PageView Event")
        .get("/i?" + "&" + PageViewEvent))
      .pause(1 seconds)
      .exec(http("Structured Event")
        .get("/i?" + "&" + StructuredEvent))
      .pause(1 seconds)
    }
    val peak = during((SimulationTime/2) minutes, "n") {
      exec(http("PageView Event")
        .get("/i?" + "&" + PageViewEvent))
      .pause(1 seconds)
      .exec(http("Structured Event")
        .get("/i?" + "&" + StructuredEvent))
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
  val peak1 = scenario("Peak 1").exec(Events.peak)
  val peak2 = scenario("Peak 2").exec(Events.peak)
  val peak3 = scenario("Peak 3").exec(Events.peak)
  val peak4 = scenario("Peak 4").exec(Events.peak)
  val peak5 = scenario("Peak 5").exec(Events.peak)
  val peak6 = scenario("Peak 6").exec(Events.peak)
  val peak7 = scenario("Peak 7").exec(Events.peak)
  val peak8 = scenario("Peak 8").exec(Events.peak)
  val peak9 = scenario("Peak 9").exec(Events.peak)

  // Divide the Sim Time into 30 parts
  val TimeSegment: Int = Utils.getPositiveCount(SimulationTime, 30)

  // Simulation
  setUp(
    baseline.inject(atOnceUsers(BaselineUsers)),
    peak1.inject(nothingFor((TimeSegment * 2)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 500)) over ((TimeSegment * 10) minutes)),
    peak2.inject(nothingFor((TimeSegment * 3)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 250)) over ((TimeSegment * 9)  minutes)),
    peak3.inject(nothingFor((TimeSegment * 4)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 125)) over ((TimeSegment * 8)  minutes)),
    peak4.inject(nothingFor((TimeSegment * 5)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 62))  over ((TimeSegment * 7)  minutes)),
    peak5.inject(nothingFor((TimeSegment * 6)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 31))  over ((TimeSegment * 6)  minutes)),
    peak6.inject(nothingFor((TimeSegment * 7)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 16))  over ((TimeSegment * 5)  minutes)),
    peak7.inject(nothingFor((TimeSegment * 8)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 8))   over ((TimeSegment * 4)  minutes)),
    peak8.inject(nothingFor((TimeSegment * 9)  minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 4))   over ((TimeSegment * 3)  minutes)),
    peak9.inject(nothingFor((TimeSegment * 10) minutes), rampUsers(Utils.getPositiveCount(PeakUsers, 2))   over ((TimeSegment * 2)  minutes))
  ).protocols(httpConf)
}

object Utils {

  /**
   * Ensures we do not get a zero value for any division.
   *
   * @param total The total to divide by
   * @param divisor The count to divide by
   * @return either the result or 1
   */
  def getPositiveCount(total: Double, divisor: Double): Int = {
    val Count: Double = (total/divisor) match {
      case c if c < 1 => 1
      case c => c
    }
    return Count.toInt
  }
}
