package fs2.data.mft

import scala.collection.mutable.ListBuffer

class MFTBuilder[InTag, OutTag] private[mft] {
  self =>

  private var initial = 0

  private val states = new ListBuffer[StateBuilder]

  final class StateBuilder private[mft] (val q: Int, val nargs: Int) {
    var rules = new ListBuffer[(EventSelector[InTag], Rhs[OutTag])]
    def apply(pat: PatternBuilder): RuleBuilder =
      new RuleBuilder(this, pat)

    def apply(f: Forest, args: Rhs[OutTag]*): Rhs[OutTag] =
      Rhs.Call(q, f, args.toList)
  }

  sealed trait PatternBuilder
  private[mft] object PatternBuilder {
    case object Any extends PatternBuilder
    case class Node(in: InTag) extends PatternBuilder
    case object AnyNode extends PatternBuilder
    case class Leaf(in: InTag) extends PatternBuilder
    case object AnyLeaf extends PatternBuilder
    case object Epsilon extends PatternBuilder
  }

  class RuleBuilder private[mft] (q: StateBuilder, pat: PatternBuilder) {

    def ->(rhs: Rhs[OutTag]): Unit =
      pat match {
        case PatternBuilder.Node(in) => q.rules += (EventSelector.Node(in) -> rhs)
        case PatternBuilder.AnyNode  => q.rules += (EventSelector.AnyNode -> rhs)
        case PatternBuilder.Leaf(in) => q.rules += (EventSelector.Leaf(in) -> rhs)
        case PatternBuilder.AnyLeaf  => q.rules += (EventSelector.AnyLeaf -> rhs)
        case PatternBuilder.Epsilon  => q.rules += (EventSelector.Epsilon -> rhs)
        case PatternBuilder.Any =>
          q.rules += (EventSelector.AnyNode -> rhs) += (EventSelector.AnyLeaf -> rhs) += (EventSelector.Epsilon -> rhs)
      }

  }

  def newState(nargs: Int = 0, initial: Boolean): StateBuilder = {
    val q = states.size
    if (initial)
      self.initial = q
    val st = new StateBuilder(q, nargs)
    states += st
    st
  }

  def build: MFT[InTag, OutTag] =
    new MFT(initial, states.map { st => st.q -> Rules(List.range(0, st.nargs), st.rules.result()) }.toMap)

}
