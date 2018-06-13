package org.kys.mars.util

import java.nio.file.Path

import com.typesafe.scalalogging.LazyLogging
import org.kys.mars.models.{MarsConfFile}
import org.rogach.scallop.{ScallopConf, ScallopOption}
import pureconfig.module.enumeratum._


class ArgUtils(arguments: Seq[String]) extends ScallopConf(arguments) with LazyLogging {
  val config: ScallopOption[MarsConfFile] =
    opt[Path](required = true).map { p =>
      pureconfig.loadConfigOrThrow[MarsConfFile](p)
    }
  verify()
}