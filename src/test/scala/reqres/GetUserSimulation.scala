package reqres

import io.gatling.core.Predef._
import io.gatling.core.feeder.BatchableFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.DurationInt

class GetUserSimulation extends Simulation {

  val httpConf: HttpProtocolBuilder = http.baseUrl("https://reqres.in/api/")
    .header("Accept", "application/json")

  val csvFeeder: BatchableFeederBuilder[String]#F = csv("data/users.csv").circular

  val scn: ScenarioBuilder = scenario("Get user")
    .exec(http("Get users list")
      .get("users?page=1")
      .check(jsonPath("$.total").is("12"))
      .check(status.is(200)))
    .pause(5)
    .during(10.seconds) {
      feed(csvFeeder)
        .exec(http("Get user")
          .get("users/${id}")
          .check(jsonPath("$.data.first_name").is("${name}"))
          .check(status.is(200)))
    }

  setUp(
    scn.inject(
      atOnceUsers(1),
      rampUsers(10) during 5.seconds
    ).protocols(httpConf)
  ).maxDuration(10.seconds)
    .assertions(
      global.successfulRequests.percent.gt(95),
      global.responseTime.max.lt(5000)
    )
}
