package eg
package main

import cats._, cats.implicits._, cats.data._

import org.atnos.eff._
import org.atnos.eff.addon.fs2._
import org.atnos.eff.addon.fs2.task._
import java.util.concurrent.CountDownLatch

final class SystemInteruption[S](implicit m: TimedTask |= S)
    extends eg.SystemInteruption[Eff[S, ?]] {

  // TODO add timeout for finished latch wait
  def setupShutdownHook(): Eff[S, ShutdownHook] = {
    val shutdownLatch = new CountDownLatch(1)
    val shutdownFinishedLatch = new CountDownLatch(1)

    for {
      _ <- taskDelay {
        sys.addShutdownHook {
          shutdownLatch.countDown() // let the other side know we're good
          shutdownFinishedLatch.await() // hold us up until the other side is done
        }
      }

      h <- Eff.pure(ShutdownHook(() => taskDelay(shutdownLatch.await()),
                                 () => taskDelay(shutdownFinishedLatch.countDown())))
    } yield h
  }
}
