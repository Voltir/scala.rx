package rx
import acyclic.file

object Util {

  //Var(2).map(a => a).map(a => a).map(a => a)
  //Var(1).mapM[Int](a => a).mapM[Int](a=>a).mapM[Int](a => 42 + a)
  Var(1).mapZ(a => a).mapZ(a=>a).mapZ(a => 42 + a)
  Var(2).mapZ(a => a).allZ.mapZZ(a => a + 34).allZ.mapZZ(a => a.map(3 + _))

  //Var(2).m
  /**
   * Generates a short dataflow graph for testing
   */
  def initGraph()(implicit ctx: RxCtx) = {
    val a = Var(1) // 3

    val b = Var(2) // 2

    val c = Rx { a() + b() } // 5
    val d = Rx{ c() * 5 } // 25
    val e = Rx{ c() + 4 } // 9
    val f = Rx{ d() + e() + 4 } // 25 + 9 + 4 =

   (a, b, c, d, e, f)
  }
}