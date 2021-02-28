import akka.actor.{Actor, ActorSelection}
import akka.event.{Logging, LoggingAdapter}

import scala.Console._
import scala.io.Source
import scala.util.Random

import java.sql.DriverManager
import java.sql.Connection

class DatabaseManager extends Actor{

  val driver = "com.mysql.cj.jdbc.Driver" // com.mysql.jdbc.Driver
  val url = "jdbc:mysql://localhost:3306/classicmodels?useSSL=false" // rtp_001
  val username = "Andrei"
  val password = "andrei1997!!!"

  var connection:Connection = _
  try {
    // make the connection
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)

    // create the statement, and run the select query
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("SELECT city,phone FROM customers")
    while ( resultSet.next() ) {
      val host = resultSet.getString("city")
      val user = resultSet.getString("phone")
      println("host, user = " + host + ", " + user)
    }
  } catch {
    case e: Throwable => e.printStackTrace()
  }
  connection.close()

  def receive: Receive = {
    case str:String => {
    println("recieved" + str)
    }
  }
}
