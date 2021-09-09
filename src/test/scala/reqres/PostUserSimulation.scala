package reqres

import io.gatling.core.Predef._
import io.gatling.core.feeder.BatchableFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration.DurationInt

class PostUserSimulation extends Simulation {

  val httpConf: HttpProtocolBuilder = http.baseUrl("https://reqres.in/api/")
    .header("Accept", "application/json")

  val csvFeeder: BatchableFeederBuilder[String]#F = csv("data/newUsers.csv").circular

  val scn: ScenarioBuilder = scenario("Create user")
    .repeat(3) {
      feed(csvFeeder)
        .exec(http("Post user")
          .post("users")
          .body(ElFileBody("bodies/User.json")).asJson
          .check(jsonPath("$.name").is("${name}"))
          .check(jsonPath("$.job").is("${job}"))
          .check(status.is(201)))
        .pause(1.seconds)
    }

  setUp(
    scn.inject(
      atOnceUsers(1),
      rampUsersPerSec(1) to 5 during 10.seconds
    ).protocols(httpConf)
  ).maxDuration(10.seconds)
    .assertions(
      global.successfulRequests.percent.gt(95),
      global.responseTime.max.lt(5000)
    )
}
