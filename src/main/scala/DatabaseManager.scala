import akka.actor.{Actor, ActorSelection, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.util.Timeout

import scala.Console._
import scala.io.Source
import scala.util.{Failure, Random, Success}
import java.sql.{Connection, DriverManager, Statement}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationDouble

class DatabaseManager extends Actor{

  var connection:Connection = _

  createTable()
  var con: Connection = getConnection()
  var statement: Statement = con.createStatement
  var count = 0;
  var MAX_BATCH_SIZE = 128
  var agregator: ActorSelection = context.system.actorSelection("user/Agregator")

  implicit  val system: ActorSystem = context.system;
  implicit val timeout: Timeout = Timeout(6 seconds)
  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  //  self ! "Andrei"
//  self ! "Ion"
//
//  self ! "Gheorghe"
//  self ! "Luca"
//
//  self ! "Petru"
//  self ! "Radu"
//  self ! "execute"


  def receive: Receive = {
    case "pull" =>{

      val response = agregator ? 30
      response.onComplete{
        case Success(tweets) =>{
          println(tweets)
        }
        case Failure(f) => {

        }
          self ! "pull"
      }
    }
    case "execute" =>{
      if(count != 0){
        count=0
        statement.executeBatch()
        println("Executed")
      }
      Thread.sleep(1000)
      println("I will repeat")
      self ! "execute"
    }
    case user_name:String => {
    statement.addBatch("INSERT INTO tweeter_user(user_name) VALUES('"+user_name+"')")
    count += 1
      if (count == MAX_BATCH_SIZE ){
        count = 0
        statement.executeBatch()
      }
    }
  }

  def createTable(): Unit ={
    try {
      var con:Connection = getConnection()
      var create = con.prepareStatement("CREATE TABLE IF NOT EXISTS tweeter_user(id int NOT NULL AUTO_INCREMENT primary key,user_name varchar(255))")
      create.executeUpdate();
    }catch{
      case e:Exception => println(e.printStackTrace())
    }
    finally{
      connection.close()
    }
  }


  def getConnection() : Connection ={
    try {
      val driver = "com.mysql.cj.jdbc.Driver" // com.mysql.jdbc.Driver
      val url = "jdbc:mysql://localhost:3306/rtp_001?useSSL=false" // rtp_001
      val username = "Andrei"
      val password = "andrei1997!!!"
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      // create the statement, and run the select query
     return connection
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    return null
  }
}
