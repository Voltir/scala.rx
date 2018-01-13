package rx

import utest._

import collection.mutable

object CombinatorTests extends TestSuite {
  import Ctx.Owner.Unsafe._

  def tests = utest.Tests {
    "bleh" - {
      val v = Var(Var(1))
      val a = v.flatMap(_.map("a"*_))
      pprint.log(a.now)
      assert(a.now == "a")
      v.now() = 5
      pprint.log(a.now)
      assert(a.now == "a"*5)
    }

    "nooooo" - {
      val rxa = Var(2)
      val rxb = rxa.map(_ + 1)
      val rxc = rxa.map(_ + 1)

      val flatMapTriggered = mutable.ArrayBuffer.empty[(Int, Int)]
      var nestedRxTriggered = mutable.ArrayBuffer.empty[(Int, Int)]
      var rxTriggered = mutable.ArrayBuffer.empty[(Int, Int)]
      val forTriggered = mutable.ArrayBuffer.empty[(Int, Int)]

      Rx {
        val b = rxb()
        val c = rxc()
        rxTriggered += ((b, c))
      }

      // flatMap should expand to
      // rxb.flatMap(b => rxc.map(c => bla))
      Rx {
        val m = rxc.map(c => nestedRxTriggered += ((rxb(), c)))
        m()
      }

      rxb.flatMap(b => rxc.map(c => flatMapTriggered += ((b, c))))

      for {
         b <- rxb
         c <- rxc
       } yield { forTriggered += ((b,c)) }


      assert(rxTriggered.toList == List((3, 3)))
      assert(nestedRxTriggered.toList == List((3, 3)))
      assert(flatMapTriggered.toList == List((3, 3)))
      assert(forTriggered.toList == List((3, 3)))

      rxa() = 12
      assert(rxTriggered.toList == List((3, 3), (13, 13)))
      assert(nestedRxTriggered.toList == List((3, 3), (13, 13)))
      assert(flatMapTriggered.toList == List((3, 3), (13, 13)))
      assert(forTriggered.toList == List((3, 3), (13, 13)))

      rxa() = 22
      assert(rxTriggered.toList == List((3, 3), (13, 13), (23, 23)))
      assert(nestedRxTriggered.toList == List((3, 3), (13, 13), (23, 23)))
      assert(flatMapTriggered.toList == List((3, 3), (13, 13), (23, 23)))
      assert(forTriggered.toList == List((3, 3), (13, 13), (23, 23)))

      rxa() = 32
      assert(rxTriggered.toList == List((3, 3), (13, 13), (23, 23), (33, 33)))
      assert(nestedRxTriggered.toList == List((3, 3), (13, 13), (23, 23), (33, 33)))
      assert(flatMapTriggered.toList == List((3, 3), (13, 13), (23, 23), (33, 33)))
      assert(forTriggered.toList == List((3, 3), (13, 13), (23, 23), (33, 33)))
    }
  }
}
