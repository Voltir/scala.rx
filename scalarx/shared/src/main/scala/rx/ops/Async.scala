package rx.ops

import rx.{RxCtx, Var, Rx}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{Deadline, FiniteDuration}
import scala.util.Failure

object OPS {

  implicit class FutureCombinators[T](val src: Future[T]) extends AnyVal {

    def toRx(initial: T)(implicit ec: ExecutionContext, ctx: RxCtx): Rx[T] = {
      @volatile var completed: T = initial
      val ret = Rx.build { inner => completed }(ctx)
      src.map { done =>
        completed = done
        ret.recalc()
      }
      ret
    }

  }

  implicit class AsyncCombinators[T](val src: rx.Node[T]) extends AnyVal {

    def debounce(interval: FiniteDuration)(implicit scheduler: Scheduler, ctx: RxCtx): Rx[T] = {
      @volatile var npt = Deadline.now
      lazy val ret: Rx[T] = Rx.build { inner =>
        src.Internal.addDownstream(inner)
        if(Deadline.now >= npt) {
          npt = Deadline.now + interval
          src.now
        } else {
          scheduler.scheduleOnce(npt - Deadline.now) {
            ret.propagate()
          }
          ret()(inner)
        }
      }(ctx)
      ret
    }
  }

}
