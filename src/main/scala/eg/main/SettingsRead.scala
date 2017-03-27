package eg
package main

import org.atnos.eff._
import java.io.File
import fs2.Task
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import eu.timepit.refined.pureconfig._
import org.atnos.eff.addon.fs2._
import org.atnos.eff.addon.fs2.task._
import com.typesafe.config.{Config, ConfigFactory}
import scala.util.control.NoStackTrace


case class SettingsReadConfigFailures(failures: ConfigReaderFailures) extends RuntimeException with NoStackTrace

final class SettingsRead[S](implicit member: TimedTask |= S)
    extends eg.SettingsRead[Eff[S, ?]] {

  def readSettings(): Eff[S, Settings] = for {
    config <- taskDelay[S, Config](ConfigFactory.load())
    settings <- loadConfig[Settings](config, "eg").fold(
      failures => taskFailed[S, Settings](SettingsReadConfigFailures(failures)),
      settings => taskDelay[S, Settings](settings)
    )
  } yield settings

}
