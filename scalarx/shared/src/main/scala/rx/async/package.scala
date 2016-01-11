package rx

import rx.async.Scheduler

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{Deadline, FiniteDuration}

package object async {

  implicit class FutureCombinators[T](val f: Future[T]) extends AnyVal {
    def toRx(initial: T)(implicit ec: ExecutionContext, ctx: RxCtx): Rx[T] = {
      @volatile var completed: T = initial
      val ret = Rx.build { inner => completed }(ctx)
      f.map { v => completed = v ; ret.recalc() }
      ret
    }
  }

  implicit class AsyncCombinators[T](val n: rx.Node[T]) extends AnyVal {
    def debounce(interval: FiniteDuration)(implicit scheduler: Scheduler, ctx: RxCtx): Rx[T] = {
      @volatile var npt = Deadline.now
      @volatile var task = Option.empty[Cancelable]
      lazy val ret: Rx[T] = Rx.build { inner =>
        n.Internal.addDownstream(inner)
        if(Deadline.now >= npt) {
          npt = Deadline.now + interval
          n.now
        } else {
          task.foreach(_.cancel())
          task = Some(scheduler.scheduleOnce(npt - Deadline.now) {
            ret.propagate()
          })
          ret()(inner)
        }
      }(ctx)
      ret
    }

    def delay(amount: FiniteDuration)(implicit scheduler: Scheduler, ctx: RxCtx): Rx[T] = {
      @volatile var fired = Deadline.now - amount
      @volatile var waiting = 0
      val next: Var[T] = Var(n.now)
      n.foreach { i =>
        if(Deadline.now >= fired + amount) {
          fired = Deadline.now
          next() = i
        } else {
          waiting += 1
          scheduler.scheduleOnce(fired + (amount*waiting) - Deadline.now) {
            waiting -= 1
            require(waiting >= 0)
            next() = i
          }
        }
      }
      Rx.build { inner => next.Internal.addDownstream(inner); next.now }(ctx)
    }
  }

  object Timer {
    def apply(interval: FiniteDuration, delay: FiniteDuration): Rx[Long] = ???
  }
}
