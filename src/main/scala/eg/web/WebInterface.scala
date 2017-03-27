package eg
package web

import org.atnos.eff._
import org.atnos.eff.safe._
import org.atnos.eff.reader._
import org.atnos.eff.syntax.safe._
import org.atnos.eff.syntax.reader._
import org.atnos.eff.addon.fs2._
import org.atnos.eff.addon.fs2.task._
import org.atnos.eff.syntax.addon.fs2.task._

import org.http4s.HttpService
import org.http4s.server.Server
import cats.syntax.semigroup._
import cats.syntax.functor._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.server.blaze._
import fs2.interop.cats._
import fs2.{Scheduler, Task}
import scala.concurrent.ExecutionContext
import io.circe._, io.circe.generic.auto._, io.circe.syntax._
import java.time.LocalDate
import cats.data.Reader

final class WebInterface[R](implicit m1: TimedTask |= R,
                               m2: Safe /= R,
                               m3: Reader[fs2.Strategy, ?] |= R, // should be tagged so they are are global
                               m4: Reader[fs2.Scheduler, ?] |= R)
    extends eg.WebInterface[Eff[R, ?], Server] {

  // move me
  implicit val encodeLocalDate: Encoder[LocalDate] = new Encoder[LocalDate] {
    def apply(date: LocalDate): Json = Json.fromString(date.toString)
  }

  def useWebInterface(port: Int,
                      onStart: Server => Eff[R, Unit],
                      onShutdown: Server => Eff[R, Unit])
                     (implicit bandArtistRepository: BandArtistRepository[Eff[R, ?]]): Eff[R, Unit] = {
    ask[R, fs2.Strategy] flatMap { implicit S: fs2.Strategy =>
      ask[R, fs2.Scheduler] flatMap { implicit scheduler: fs2.Scheduler =>

        val read = HttpService {
          case GET -> Root / "bands" / "artists" =>
            val baavJson: Eff[R, Json] = bandArtistRepository.findBandsWithArtists().map { baav =>
              baav.asJson
            }

            // val (result: Either[Throwable,Unit], _: List[Throwable]) =
            //   .runReader(S).runReader(scheduler).runSafe.runSequential.unsafeRun
            Ok("ok")
        }
        val write = HttpService {
          case POST -> Root / "band"  => Ok("ok!")
        }

        val service = read |+| write

        Http4sEff.startServerRootService(port, service, onStart, onShutdown)
      }
    }

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
