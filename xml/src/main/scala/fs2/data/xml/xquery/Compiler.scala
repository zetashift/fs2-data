package fs2.data
package xml
package xquery

import mft._

import cats.{Defer, MonadError}
import cats.syntax.all._

class Compiler[F[_]](implicit F: MonadError[F, Throwable] with Defer[F]) {

  /** Compiles an XQuery expression, that can be reused over several streams. */
  def compile(query: Query): F[CompiledQuery[F]] = {

    def loop(query: Query,
             context: Map[String, Int],
             q: Int,
             acc: Map[Int, Rules[XmlLeaf, XmlLeaf]]): MFT[XmlLeaf, XmlLeaf] = ???

    loop(query, Map("input" -> 0), 2, Compiler.initialRules).esp[F].map(new CompiledQuery(_))
  }

}

object Compiler {

  private val initialRules: Map[Int, Rules[XmlLeaf, XmlLeaf]] =
    Map(
      0 -> Rules(
        Nil,
        List(
          EventSelector.AnyNode -> Rhs.Call(
            2,
            Forest.First,
            List(Rhs.Concat(Rhs.CopyNode(Rhs.Call(1, Forest.First, Nil)), Rhs.Call(1, Forest.Second, Nil)))),
          EventSelector.AnyLeaf -> Rhs.Call(2, Forest.First, List(Rhs.CopyNode(Rhs.Call(1, Forest.First, Nil)))),
          EventSelector.Epsilon -> Rhs.Call(2, Forest.First, List(Rhs.Epsilon))
        )
      ),
      1 -> Rules(
        Nil,
        List(
          EventSelector.AnyNode -> Rhs.Concat(Rhs.CopyNode(Rhs.Call(1, Forest.First, Nil)),
                                              Rhs.Call(1, Forest.Second, Nil)),
          EventSelector.AnyLeaf -> Rhs.CopyLeaf,
          EventSelector.Epsilon -> Rhs.Epsilon
        )
      )
    )

}
