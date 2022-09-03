package fs2.data.xml
package xquery

import cats.data.NonEmptyList

sealed trait Tree
sealed trait Query extends Tree
sealed trait Clause extends Query

case class Str(s: String) extends Tree

case class Element(name: QName, children: List[Tree]) extends Query

case class ForClause(variable: String, source: Ordpath, result: Query) extends Clause

case class LetClause(variable: String, query: Query, result: Query) extends Clause

case class ListClause(queries: NonEmptyList[Query]) extends Clause

case class Ordpath(variable: String, path: List[PathStep]) extends Clause

case class PathStep(axis: Axis, test: NodeTest, predicate: Option[Predicate])

sealed trait Axis
object Axis {
  case object Child extends Axis
  case object Descendant extends Axis
  case object FollowingSibling extends Axis
}

sealed trait NodeTest
object NodeTest {
  case class ElementName(name: QName) extends NodeTest
  case object Wildcard extends NodeTest
  case object Text extends NodeTest
  case object Node extends NodeTest
}

sealed trait Predicate
object Predicate {
  case class Exists(steps: List[PathStep]) extends Predicate
  case class Empty(steps: List[PathStep]) extends Predicate
  case class Eq(steps: List[PathStep], value: String) extends Predicate
  case class NEq(steps: List[PathStep], value: String) extends Predicate

  case class And(left: Predicate, right: Predicate) extends Predicate
}
