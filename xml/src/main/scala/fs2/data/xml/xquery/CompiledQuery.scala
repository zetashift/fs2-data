package fs2
package data
package xml
package xquery

import esp.ESP

private sealed trait XmlLeaf
private object XmlLeaf {}

class CompiledQuery[F[_]](mft: ESP[F, XmlLeaf, XmlLeaf]) {

  def pipe: Pipe[F, XmlEvent, XmlEvent] = ???

}
