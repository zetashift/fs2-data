package fs2.data

package object mft {

  def dsl[InTag, OutTag](build: MFTBuilder[InTag, OutTag] => Unit): MFT[InTag, OutTag] = {
    val builder = new MFTBuilder[InTag, OutTag]
    build(builder)
    builder.build
  }

  def state[InTag, OutTag](args: Int, initial: Boolean = false)(implicit
      builder: MFTBuilder[InTag, OutTag]): builder.StateBuilder =
    builder.newState(args, initial)

  def any[InTag, OutTag](implicit builder: MFTBuilder[InTag, OutTag]): builder.PatternBuilder =
    builder.PatternBuilder.Any

  def node[InTag, OutTag](in: InTag)(implicit builder: MFTBuilder[InTag, OutTag]): builder.PatternBuilder =
    builder.PatternBuilder.Node(in)

  def anyNode[InTag, OutTag](implicit builder: MFTBuilder[InTag, OutTag]): builder.PatternBuilder =
    builder.PatternBuilder.AnyNode

  def leaf[InTag, OutTag](in: InTag)(implicit builder: MFTBuilder[InTag, OutTag]): builder.PatternBuilder =
    builder.PatternBuilder.Leaf(in)

  def anyLeaf[InTag, OutTag](implicit builder: MFTBuilder[InTag, OutTag]): builder.PatternBuilder =
    builder.PatternBuilder.AnyLeaf

  def epsilon[InTag, OutTag](implicit builder: MFTBuilder[InTag, OutTag]): builder.PatternBuilder =
    builder.PatternBuilder.Epsilon

  def eps: Rhs[Nothing] =
    Rhs.Epsilon

  def y(i: Int): Rhs[Nothing] =
    Rhs.Param(i)

  def x0: Forest =
    Forest.Self

  def x1: Forest =
    Forest.First

  def x2: Forest =
    Forest.Second

  def node[OutTag](out: OutTag, children: Rhs[OutTag]): Rhs[OutTag] =
    Rhs.Node(out, children)

  def copy[OutTag](children: Rhs[OutTag]): Rhs[OutTag] =
    Rhs.CopyNode(children)

  def leaf[OutTag](out: OutTag): Rhs[OutTag] =
    Rhs.Leaf(out)

  def copy: Rhs[Nothing] =
    Rhs.CopyLeaf

}
