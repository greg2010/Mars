package org.kys

import com.typesafe.config.{Config, ConfigFactory}
//import org.kys.mars.models.ConfigSchema.Destinations
import pureconfig.error.ConfigReaderFailures

package object mars {
  val rawConfig: Config = ConfigFactory.load()
  val marsConfig: Config = rawConfig.getConfig("mars")
}
