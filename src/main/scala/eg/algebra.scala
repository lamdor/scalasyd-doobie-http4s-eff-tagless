package eg

import cats.Monad
import cats.syntax.functor._
import cats.syntax.flatMap._
import java.time.LocalDate

case class BandId(id: Long) extends AnyVal
case class Band(name: String, started: LocalDate)

case class ArtistId(id: Long) extends AnyVal
case class Artist(name: String)

case class ArtistView(id: ArtistId, artist: Artist)
case class BandView(id: BandId, band: Band)

case class BandArtistView(band: Band, artists: Vector[Artist])

trait BandArtistRepository[F[_]] {
  def create(artist: Artist, band: Band): F[(ArtistId, BandId)]
  def createAritst(artist: Artist): F[ArtistId]
  def createBand(band: Band): F[BandId]

  def linkArtistToBand(artist: ArtistId, band: BandId): F[Unit]

  def findBandsWithArtists(): F[Vector[BandArtistView]]
}

trait WebInterface[F[_], W] {
  // TODO: better names... it's usually a braket
  def useWebInterface(port: Int,
                      onStart: W => F[Unit],
                      onShutdown: W => F[Unit])(implicit bandArtistRepository: BandArtistRepository[F]): F[Unit]
}

trait SystemInteruption[F[_]] {
  // TODO: can these hooks just be F[Unit]
  case class ShutdownHook(waitForShutdownRequested: () => F[Unit],
                          readyForShutdown: () => F[Unit])

  def setupShutdownHook(): F[ShutdownHook]
}


trait Program[F[_]] {
  def program(implicit M: Monad[F],
                       settingsRead: SettingsRead[F],
                       webInterface: WebInterface[F, _],
                       systemInteruption: SystemInteruption[F],
                       bandArtistRepository: BandArtistRepository[F]): F[Unit] = {
    for {
      settings <- settingsRead.readSettings
      shutdownHook <- systemInteruption.setupShutdownHook()
      web <- webInterface.useWebInterface(port = settings.web.port.value,
                                          onStart = _ => shutdownHook.waitForShutdownRequested(),
                                          onShutdown = _ => shutdownHook.readyForShutdown())
    } yield ()
  }
}
