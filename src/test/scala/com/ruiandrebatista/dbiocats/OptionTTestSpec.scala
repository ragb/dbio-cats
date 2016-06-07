package com.ruiandrebatista.dbiocats

import scala.concurrent.ExecutionContext.Implicits._

import cats.data._
import cats.syntax.option._
import dbio._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import slick.driver.H2Driver.api._

class OptionTTestSpec extends FlatSpec with DatabaseSupport with ScalaFutures with Matchers {
  def optiont(email: String) = for {
    user <- OptionT[DBIO, UserRow](users.filter(_.email === email).result.headOption)
  } yield (user.id)

  "DBIOcats instances" should "Allow usage of OptionT in for compreensions" in {
    val action1 = optiont("rui.batista@example.com").value
    database.run(action1).futureValue shouldBe 1.some
    val action2 = optiont("nomail").value
    database.run(action2).futureValue shouldBe None
  }

  it should "Allow lifting of dbio actions to OptionT" in {
    def ot(email: String) = for {
      userId <- optiont(email)
      c <- OptionT.liftF[DBIO, Int](events.filter(_.userId === userId).length.result)
    } yield (c)
    val action1 = ot("rui.batista@example.com").value
    database.run(action1).futureValue shouldBe 1.some
    val action2 = ot("godiva@example.com").value
    database.run(action2).futureValue shouldBe 0.some
  }
}
