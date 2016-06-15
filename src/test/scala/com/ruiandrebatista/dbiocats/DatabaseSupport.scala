package com.ruiandrebatista.dbiocats
import org.scalatest._
import slick.driver.H2Driver
import H2Driver.api._
import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

trait DatabaseSupport extends TestTables with BeforeAndAfter {
  this: Suite =>
  val database = Database.forURL("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
  before {
    val action = DBIO.seq(
      schema.create,
      users ++= Seq(
        UserRow(1, "rui", "rui.batista@example.com"),
        UserRow(2, "godiva", "godiva@example.com"),
        UserRow(3, "pedro", "pedro@example.com")
      ),
      events.map(_.forInsert) += (1, "loggedin", System.currentTimeMillis()),
      events.map(_.forInsert) ++= (1 to 10) map { i => (3l, "event" + i, System.currentTimeMillis()) }
    ).transactionally
    Await.result(database.run(action), 5 seconds)
  }
  after {
    Await.result(database.run(schema.drop), 5 seconds)
  }
}
