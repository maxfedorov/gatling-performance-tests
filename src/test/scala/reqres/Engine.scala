package reqres

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

  System.setProperty("gatling.conf.file", "configs/gatling.conf")

  val props = new GatlingPropertiesBuilder()

  Gatling.fromMap(props.build)
}
