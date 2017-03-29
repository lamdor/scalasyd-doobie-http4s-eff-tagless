package eg

import java.io.File
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean._
import eu.timepit.refined.numeric._
import eu.timepit.refined.collection._

case class WebSettings(
  port: Int Refined And[Positive, LessEqual[W.`65535`.T]]
)

case class DatabaseSettings(
  jdbcConnectionString: String Refined NonEmpty,
  driver: String Refined NonEmpty,
  username: String Refined NonEmpty,
  password: String
)

case class Settings(
  web: WebSettings,
  database: DatabaseSettings
)

trait SettingsRead[F[_]] {
  def readSettings(): F[Settings]
}
