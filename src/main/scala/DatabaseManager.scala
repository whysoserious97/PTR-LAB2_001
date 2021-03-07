import akka.actor.{Actor, ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout

import scala.Console._
import scala.util.{Failure, Random, Success}
import java.sql.{Connection, DriverManager, PreparedStatement, Statement}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationDouble

class DatabaseManager extends Actor {

  var connection : Connection = _

  createTable()
  var con : Connection = getConnection()
  var statement : Statement = con.createStatement
  var count = 0;
  var MAX_BATCH_SIZE = 128
  var agregator : ActorSelection = context.system.actorSelection("user/Agregator")

  var pstmt_user:PreparedStatement = con.prepareStatement(
    "INSERT INTO tweeter_user (user_name) " +
      " VALUES(?)");
  var pstmt_tweet:PreparedStatement = con.prepareStatement("INSERT INTO tweet (message,userID,engagement,is_original) " +
    "VALUES(?,(SELECT id FROM tweeter_user WHERE user_name = ? LIMIT 1),?,?)")

  implicit val system : ActorSystem = context.system;
  implicit val timeout : Timeout = Timeout(6 seconds)
  implicit val dispatcher : ExecutionContextExecutor = context.dispatcher
  self ! "execute"

  def receive : Receive = {
    case "pull" => {
      val response = agregator ? (Random.nextInt(50) + 10)  // Request a random int between [10 , 60)
      response.onComplete {
        case Success(tweets) => self ! tweets
        case Failure(f) =>
      }

      while (!response.isCompleted) {}
      Thread.sleep(1000)
      self ! "pull"
    }
    case "execute" => {
      if (count != 0) {
        count = 0
        pstmt_user.executeBatch()
        pstmt_tweet.executeBatch()
     //   println("Executed")
      }
      Thread.sleep(1000)
      println("I will repeat")
      self ! "execute"
    }
    case tweets : Set[Tweet] => {
      tweets.foreach(
        tweet => {
          pstmt_user.setString(1,tweet.user_name)
          pstmt_user.addBatch()

          pstmt_tweet.setString(1,tweet.message)
          pstmt_tweet.setString(2,tweet.user_name)
          pstmt_tweet.setFloat(3,tweet.engagement)
          pstmt_tweet.setBoolean(4, false)
          pstmt_tweet.addBatch()

          if(tweet.retweeted){
            pstmt_user.setString(1,tweet.original_user_name)
            pstmt_user.addBatch()

            pstmt_tweet.setString(1,tweet.message)
            pstmt_tweet.setString(2,tweet.original_user_name)
            pstmt_tweet.setFloat(3,tweet.original_engagement)
            pstmt_tweet.setBoolean(4, true)
            pstmt_tweet.addBatch()
            count += 1
          }
          count += 1
        }
      )

      if (count >= MAX_BATCH_SIZE) {
        count = 0
        pstmt_user.executeBatch()
        pstmt_tweet.executeBatch()
      }
    }
  }

  def createTable() : Unit = {
    try {
      val con : Connection = getConnection()
      var create = con.prepareStatement("CREATE TABLE IF NOT EXISTS " +
        "tweeter_user(id int NOT NULL AUTO_INCREMENT primary key,user_name varchar(255))")
      create.executeUpdate();

      create = con.prepareStatement("CREATE TABLE IF NOT EXISTS " +
        "tweet(id int NOT NULL AUTO_INCREMENT primary key,message varchar(255),userID int,engagement FLOAT(3)," +
        "is_original BOOLEAN," +
        "FOREIGN KEY (userID) REFERENCES tweeter_user(id))")
      create.executeUpdate();
    } catch {
      case e : Exception => println(e.printStackTrace())
    }
    finally {
      connection.close()
    }
  }

  def getConnection() : Connection = {
    try {
      val driver = "com.mysql.cj.jdbc.Driver" // com.mysql.jdbc.Driver
      val url = "jdbc:mysql://localhost:3306/rtp_001?useSSL=false" // rtp_001
      val username = "Andrei"
      val password = "andrei1997!!!"
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      return connection
    } catch {
      case e : Throwable => e.printStackTrace()
    }
    null
  }
}
