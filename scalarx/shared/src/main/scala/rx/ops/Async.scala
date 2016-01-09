package rx.ops

import rx.{RxCtx, Rx}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{Deadline, FiniteDuration}
import scala.util.Failure

object OPS {

  implicit class FutureCombinators[T](val f: Future[T]) extends AnyVal {
    def toRx(initial: T)(implicit ec: ExecutionContext, ctx: RxCtx): Rx[T] = {
      var completed: T = initial
      val ret = Rx.build { inner => completed }(ctx)
      f.map { v => completed = v ; ret.recalc() }
      ret
    }
  }

  implicit class AsyncCombinators[T](val n: rx.Node[T]) extends AnyVal {
    def debounce(interval: FiniteDuration)(implicit scheduler: Scheduler, ctx: RxCtx): Rx[T] = {
      var npt = Deadline.now
      lazy val ret: Rx[T] = Rx.build { inner =>
        n.Internal.addDownstream(inner)
        if(Deadline.now >= npt) {
          npt = Deadline.now + interval
          n.now
        } else {
          scheduler.scheduleOnce(npt - Deadline.now) {
            ret.propagate()
          }
          ret()(inner)
        }
      }(ctx)
      ret
    }

    def delay(duration: FiniteDuration)(implicit scheduler: Scheduler, ctx: RxCtx): Rx[T] = ???

    //https://github.com/MetaStack-pl/MetaRx/commit/4cc7930e7b0c9b423d33a94868d19be5cf6f1a58
    def throttle(interval: FiniteDuration)(implicit scheduler: Scheduler, ctx: RxCtx): Rx[T] = ???
  }

  object Timer {
    def apply(interval: FiniteDuration, delay: FiniteDuration): Rx[Long] = ???
  }

}
