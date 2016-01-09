package rx

import java.util.concurrent.Executors
import rx.AsyncScheduler

import scala.concurrent.ExecutionContext
import utest._
import concurrent.duration._
import rx.ops.OPS._

object EventedTests extends TestSuite {

  implicit val todo = new AsyncScheduler(Executors.newSingleThreadScheduledExecutor(),ExecutionContext.Implicits.global)

  def tests = TestSuite {
    "debounce" - {
      "simple" - {
        val a = Var(10)
        val b = a.debounce(100.millis)
        a() = 5
        assert(b.now == 10)

        eventually {
          b.now == 5
        }

        a() = 2
        assert(b.now == 5)

        eventually {
          b.now == 2
        }

        a() = 1
        a() = 5
        a() = 42

        assert(b.now == 2)

        eventually {
          b.now == 42
        }
      }
    }
  }
}
