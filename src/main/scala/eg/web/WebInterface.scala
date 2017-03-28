package eg
package web

import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.syntax.all._
import org.atnos.eff.addon.fs2._
import org.atnos.eff.addon.fs2.task._
import org.atnos.eff.syntax.addon.fs2.task._

import org.http4s.HttpService
import org.http4s.server.Server
import cats._
import cats.implicits._
import cats.data.Reader
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.server.blaze._
import fs2.Task
import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._
import java.time.LocalDate

final class WebInterface[R](implicit m1: TimedTask |= R,
                                     m2: Safe <= R,
                                     effToTask: Eff[R, ?] ~> Task)
    extends eg.WebInterface[Eff[R, ?], Server] {

  // TODO: move me
  implicit val encodeLocalDate: Encoder[LocalDate] = Encoder.encodeString.contramap[LocalDate](_.toString)
  implicit val decodeLocalDate: Decoder[LocalDate] = Decoder.decodeString.emap { str =>
    Either.catchNonFatal(LocalDate.parse(str)).leftMap(_.getMessage)
  }

  def useWebInterface(port: Int,
                      onStart: Server => Eff[R, Unit],
                      onShutdown: Server => Eff[R, Unit])
                     (implicit bandArtistRepository: BandArtistRepository[Eff[R, ?]]): Eff[R, Unit] = {

    val read = HttpService {
      case GET -> Root / "bands" / "artists" =>
        val baavJson: Task[Json] = effToTask {
          bandArtistRepository.findBandsWithArtists().map { baav =>
            baav.asJson
          }
        }

        Ok(baavJson)
    }
    val write = HttpService {
      case req @ POST -> Root / "band"  =>
        val bandIdJson =
          req.as(jsonOf[Band]).flatMap { band =>
            effToTask(bandArtistRepository.createBand(band).map(_.asJson))
          }
        Ok(bandIdJson)
    }

    val service = read |+| write

    Http4sEff.startServerRootService(port, service, onStart, onShutdown)
  }

}


// reusable outside of this impl
object Http4sEff {

  // bracketed start and stop given service
  def startServerRootService[R](
    port: Int,
    service: HttpService,
    onStart: Server => Eff[R, Unit] = ((_: Server) => Eff.pure(())),
    onShutdown: Server => Eff[R, Unit] = ((_: Server) => Eff.pure(()))
  )(
    implicit m1: TimedTask |= R,
    m2: Safe /= R
  ): Eff[R, Unit] = { // can this not return server?
    val acquire: Eff[R, Server] = fromTask {
      BlazeBuilder
        .bindHttp(port)
        .mountService(service)
        .start
    }

    bracket(acquire)(s => onStart(s))(s => thenFinally(fromTask(s.shutdown), onShutdown(s)))

  }
}
