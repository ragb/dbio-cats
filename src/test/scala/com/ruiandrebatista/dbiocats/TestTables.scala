package com.ruiandrebatista.dbiocats
import slick.driver.H2Driver
import H2Driver.api._

trait TestTables {
  case class UserRow(id: Long, name: String, email: String)
  case class EventRow(id: Long, userId: Long, event: String, time: Long)
  class UsersTable(tag: Tag) extends Table[UserRow](tag, "users") {
    def * = (id, name, email) <> (UserRow.tupled, UserRow.unapply _)
    def id = column[Long]("id", O.PrimaryKey)
    def name = column[String]("name")
    def email = column[String]("email")
    def emailIndex = index("email_idx", email, true)
  }
  val users = TableQuery[UsersTable]

  class EventsTable(tag: Tag) extends Table[EventRow](tag, "events") {
    def * = (id, userId, event, time) <> (EventRow.tupled, EventRow.unapply _)
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def user = foreignKey("user_kf", userId, users)(_.id)
    def event = column[String]("event")
    def time = column[Long]("time")
    def forInsert = (userId, event, time)
  }
  val events = TableQuery[EventsTable]

  def schema = users.schema ++ events.schema
}