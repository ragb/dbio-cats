package com.ruiandrebatista.dbiocats

import cats._
import slick.dbio._
import scala.concurrent.ExecutionContext
import scala.util.{ Success, Failure }

trait DBIOInstances {
  implicit def dbioMonadError[S <: NoStream, N <: Effect](implicit ec: ExecutionContext) = new MonadError[DBIOAction[?, NoStream, Effect], Throwable] with CoflatMap[DBIOAction[?, NoStream, Effect]] {
    override def pure[A](a: A) = DBIO.successful(a)
    override def flatMap[A, B](fa: DBIOAction[A, NoStream, Effect])(f: A => DBIOAction[B, NoStream, Effect]) = fa.flatMap(f)
    override def coflatMap[A, B](fa: DBIOAction[A, NoStream, Effect])(f: DBIOAction[A, NoStream, Effect] => B): DBIOAction[B, NoStream, Effect] = DBIO.successful(f(fa))
    override def handleErrorWith[A](fa: DBIOAction[A, NoStream, Effect])(h: Throwable => DBIOAction[A, NoStream, Effect]) = fa.asTry flatMap {
      case Success(a) => DBIO.successful(a)
      case Failure(t) => h(t)
    }
    override def raiseError[A](e: Throwable): DBIOAction[A, NoStream, Effect] = DBIO.failed(e)
    override def map[A, B](fa: DBIOAction[A, NoStream, Effect])(f: A => B) = fa map f
    override def ap[A, B](ff: DBIOAction[A => B, NoStream, Effect])(fa: DBIOAction[A, NoStream, Effect]) = (ff zip fa) map { case (f, a) => f(a) }
    override def product[A, B](fa: DBIOAction[A, NoStream, Effect], fb: DBIOAction[B, NoStream, Effect]) = fa zip fb
  }
}

