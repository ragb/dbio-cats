package com.ruiandrebatista.dbiocats

import cats._
import slick.dbio._
import scala.concurrent.ExecutionContext
import scala.util.{ Success, Failure }

trait DBIOInstances {
  implicit def dbioMonadError(implicit ec: ExecutionContext) = new MonadError[DBIO, Throwable] with CoflatMap[DBIO] {
    override def pure[A](a: A) = DBIO.successful(a)
    override def flatMap[A, B](fa: DBIO[A])(f: A => DBIO[B]) = fa.flatMap(f)
    override def coflatMap[A, B](fa: DBIO[A])(f: DBIO[A] => B): DBIO[B] = DBIO.successful(f(fa))
    override def handleErrorWith[A](fa: DBIO[A])(h: Throwable => DBIO[A]) = fa.asTry flatMap {
      case Success(a) => DBIO.successful(a)
      case Failure(t) => h(t)
    }
    override def raiseError[A](e: Throwable): DBIO[A] = DBIO.failed(e)
    override def map[A, B](fa: DBIO[A])(f: A => B) = fa map f
    override def ap[A, B](ff: DBIO[A => B])(fa: DBIO[A]) = (ff zip fa) map { case (f, a) => f(a) }
    override def product[A, B](fa: DBIO[A], fb: DBIO[B]) = fa zip fb
  }
}
