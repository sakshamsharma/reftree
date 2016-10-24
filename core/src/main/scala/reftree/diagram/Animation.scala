package reftree.diagram

import reftree.core._

case class Animation(diagrams: Seq[Diagram]) {
  def addInSequence(that: Animation) = Animation(this.diagrams ++ that.diagrams)

  def addInParallel(that: Animation) = Animation {
    diagrams.zipAll(that.diagrams, Diagram.empty, Diagram.empty).map { case (a, b) ⇒ a + b }
  }

  def toNamespace(name: String) = copy(diagrams = diagrams.map(_.toNamespace(name)))
}

object Animation {
  def startWith[A: ToRefTree](start: A) = Builder(Vector(start))

  case class Builder[A: ToRefTree](frames: Vector[A]) {
    def iterate(iterations: (A ⇒ A)*) =
      Builder(iterations.foldLeft(frames)((current, step) ⇒ current :+ step(current.last)))

    def iterate(n: Int)(iteration: A ⇒ A) =
      Builder((1 to n).foldLeft(frames)((current, _) ⇒ current :+ iteration(current.last)))

    def iterateWithIndex(n: Int)(iteration: (A, Int) ⇒ A) =
      Builder((1 to n).foldLeft(frames)((current, i) ⇒ current :+ iteration(current.last, i)))

    def repeat(operations: (Builder[A] ⇒ Builder[A])*) =
      operations.foldLeft(this)((current, operation) ⇒ operation(current))

    def repeat(n: Int)(operation: Builder[A] ⇒ Builder[A]) =
      (1 to n).foldLeft(this)((current, _) ⇒ operation(current))

    def repeatWithIndex(n: Int)(operation: (Builder[A], Int) ⇒ Builder[A]) =
      (1 to n).foldLeft(this)((current, i) ⇒ operation(current, i))

    def build(diagram: (A, Int) ⇒ Diagram = (value, _) ⇒ Diagram.noLabel(value)) =
      Animation(frames.zipWithIndex.map(diagram.tupled))
  }
}