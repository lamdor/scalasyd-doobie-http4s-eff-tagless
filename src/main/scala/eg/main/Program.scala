package eg.main

import org.atnos.eff._
import org.atnos.eff.addon.fs2._
import org.atnos.eff.syntax.all._
import org.atnos.eff.syntax.addon.fs2.task._
import scala.concurrent.ExecutionContext
import cats.data.Reader

object ProgramStack {
  type S = Fx.fx4[Reader[fs2.Strategy, ?],
                  Reader[fs2.Scheduler, ?],
                  TimedTask,
                  Safe]
}

object Program extends eg.Program[Eff[ProgramStack.S, ?]] {
  import ProgramStack._

  object Implicits {
    implicit val bandArtistRepository: eg.BandArtistRepository[Eff[S, ?]] = new eg.postgres.BandArtistRepository[S]
    implicit val webInterface: eg.WebInterface[Eff[S, ?], _]   = new eg.web.WebInterface[S]
    implicit val settingsRead: eg.SettingsRead[Eff[S, ?]]                 = new eg.main.SettingsRead[S]
    implicit val systemInteruption: eg.SystemInteruption[Eff[S, ?]]       = new eg.main.SystemInteruption[S]
    implicit val executionContext: ExecutionContext                       = ExecutionContext.Implicits.global
    implicit val fs2Strategy: fs2.Strategy                                = fs2.Strategy.fromExecutionContext(executionContext)
    implicit val fs2Scheduler: fs2.Scheduler                              = fs2.Scheduler.fromFixedDaemonPool(1)
  }

  def main(args: Array[String]): Unit = {
    import Implicits._

    val (result: Either[Throwable,Unit], finalizerExceptions: List[Throwable]) =
      program
        .runReader(fs2Strategy)
        .runReader(fs2Scheduler)
        .runSafe
        .runSequential
        .unsafeRun
    result.fold(t => throw t, identity)
  }

}
