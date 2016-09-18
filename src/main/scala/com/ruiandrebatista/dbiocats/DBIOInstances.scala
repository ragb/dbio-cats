package com.ruiandrebatista.dbiocats

import cats._, cats.data._
import cats.syntax.xor._
import slick.dbio._
import scala.concurrent.ExecutionContext
import scala.util.{ Success, Failure }
import scala.util.control.NonFatal

trait DBIOInstances extends DBIOInstances1 {
  implicit def dbioMonadError(implicit ec: ExecutionContext) = new MonadError[DBIO, Throwable] with CoflatMap[DBIO] {
    override def pure[A](a: A) = DBIO.successful(a)
    override def flatMap[A, B](fa: DBIO[A])(f: A => DBIO[B]) = fa.flatMap(f)
    override def tailRecM[A, B](a: A)(f: A => DBIO[Either[A, B]]) = defaultTailRecM(a)(f)
    override def coflatMap[A, B](fa: DBIO[A])(f: DBIO[A] => B): DBIO[B] = DBIO.successful(f(fa))
    override def handleErrorWith[A](fa: DBIO[A])(h: Throwable => DBIO[A]) = fa.asTry flatMap {
      case Success(a) => DBIO.successful(a)
      case Failure(t) => h(t)
    }
    override def attempt[A](fa: DBIO[A]): DBIO[Throwable Xor A] = handleError(fa map (_.right: Throwable Xor A)) { case NonFatal(t) => t.left }
    override def raiseError[A](e: Throwable): DBIO[A] = DBIO.failed(e)

    override def map[A, B](fa: DBIO[A])(f: A => B) = fa map f
    override def ap[A, B](ff: DBIO[A => B])(fa: DBIO[A]) = (ff zip fa) map { case (f, a) => f(a) }
    override def product[A, B](fa: DBIO[A], fb: DBIO[B]) = fa zip fb
  }
}

private[dbiocats] trait DBIOInstances1 extends DBIOInstances2 {
  implicit def dbioMonoid[A](implicit ma: Monoid[A], executionContext: ExecutionContext): Monoid[DBIO[A]] = new DBIOSemiGroup[A] with Monoid[DBIO[A]] {
    override val semigroup = ma
    override val empty = DBIO.successful(ma.empty)
  }
}

private[dbiocats] trait DBIOInstances2 {
  implicit def dbioSemigroup[A](implicit sa: Semigroup[A], executionContext: ExecutionContext): Semigroup[DBIO[A]] = new DBIOSemiGroup[A] {
    override val semigroup = sa
  }
}

private[dbiocats] abstract class DBIOSemiGroup[A](implicit executionContext: ExecutionContext) extends Semigroup[DBIO[A]] {
  val semigroup: Semigroup[A]
  def combine(dl: DBIO[A], dr: DBIO[A]) = (dl zip dr) map { case (l, r) => semigroup.combine(l, r) }
}
