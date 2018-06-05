package org.kys

import com.typesafe.config.{Config, ConfigFactory}
import org.kys.mars.models.ConfigSchema.Destinations
import pureconfig.error.ConfigReaderFailures

package object mars {
  val config: Config = ConfigFactory.load()
  val marsProcessorConfig: Either[ConfigReaderFailures, Destinations] = pureconfig.loadConfig[Destinations]("mars.config")

}
