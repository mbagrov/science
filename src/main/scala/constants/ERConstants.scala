package constants

import com.typesafe.config.ConfigFactory

/**
  * Created by alex on 10.03.16.
  */
object ERConstants {

  lazy val configuration = ConfigFactory.load()

  object GeoJSON {

    val featuresField = "features"
  }

}
