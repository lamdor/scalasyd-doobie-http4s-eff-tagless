package eg.main

import org.atnos.eff._
import org.atnos.eff.addon.fs2._
import org.atnos.eff.syntax.all._
import org.atnos.eff.syntax.addon.fs2.task._
import scala.concurrent.ExecutionContext
import fs2.Task
import cats.data.Reader
import cats._
import cats.arrow._

object ProgramStack {
  type S = Fx.fx2[TimedTask,
                  Safe]
}

object Program extends eg.Program[Eff[ProgramStack.S, ?]] {
  import ProgramStack._

  object Implicits {

    implicit val effToTask: Eff[S, ?] ~> Task = new FunctionK[Eff[S, ?], Task] {
      def apply[A](eff: Eff[S, A]): Task[A] = {
        eff
          .runSafe
          .runSequential.flatMap {
            case (Right(a), _) => Task.now(a)
            case (Left(t), _)  => Task.fail(t)
          }
      }
    }

    implicit val bandArtistRepository: eg.BandArtistRepository[Eff[S, ?]] = new eg.postgres.BandArtistRepository[S]
    implicit val webInterface: eg.WebInterface[Eff[S, ?], _]              = new eg.web.WebInterface[S]
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
        .runSafe
        .runSequential
        .unsafeRun
    result.fold(t => throw t, identity)
  }

}
