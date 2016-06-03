package com.ruiandrebatista.dbiocats
import org.scalatest._
import cats.data._
import cats._
import cats.std.option._
import cats.syntax.option._
import dbio._
import slick.driver.H2Driver
import H2Driver.api._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits._
import org.scalatest.concurrent.ScalaFutures

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
    val ot = for {
      userId <- optiont("rui.batista@example.com")
      c <- OptionT.liftF[DBIO, Int](events.filter(_.userId === userId).length.result)
    } yield (c)
    val action = ot.value
    database.run(action).futureValue shouldBe 1.some
  }
}
