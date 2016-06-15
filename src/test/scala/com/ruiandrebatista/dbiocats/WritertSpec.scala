package com.ruiandrebatista.dbiocats

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import dbio._
import scala.concurrent.ExecutionContext.Implicits._
import slick.driver.H2Driver.api._
import cats.data.WriterT
import cats.std.all._

class WritertSpec extends FlatSpec with DatabaseSupport with Matchers with ScalaFutures {
  "DBIO instances" should "Work with writer t" in {
    def countQ(email: String) = (users.filter(_.email === email) join events).on(_.id === _.userId).length
    val wa = for {
      c1 <- WriterT.valueT[DBIO, Int, Int](countQ("rui.batista@example.com").result)
      _ <- WriterT.tell[DBIO, Int](c1)
      c2 <- WriterT.valueT[DBIO, Int, Int](countQ("pedro@example.com").result)
      _ <- WriterT.tell[DBIO, Int](c2)
    } yield ()
    val action = wa.written
    database.run(action).futureValue shouldBe 11
  }
}
