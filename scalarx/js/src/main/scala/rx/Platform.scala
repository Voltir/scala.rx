package rx

import rx.ops.Scheduler

object Platform  {
  implicit lazy val DefaultScheduler: Scheduler = new AsyncScheduler
}
