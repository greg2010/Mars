package org.kys.mars.util

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import org.kys.mars.models.Destination
import org.rogach.scallop.{ScallopConf, ScallopOption}
import pureconfig.module.enumeratum._


class ArgUtils(arguments: Seq[String]) extends ScallopConf(arguments) with LazyLogging {
  case class RootConfig(destinations: List[Destination])
  val config: ScallopOption[List[Destination]] =
    opt[Path](required = true).map { p =>
      pureconfig.loadConfigOrThrow[RootConfig](p).destinations
    }
  verify()
}