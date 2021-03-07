import akka.actor.{Actor, ActorSelection}
import akka.event.{Logging, LoggingAdapter}

import scala.Console._
import scala.collection.mutable.ListBuffer

class UserEngagement extends Actor {

  var tweets : Set[Tweet] = Set[Tweet]()
  var userEngagementMap = collection.mutable.Map[String, Float]().withDefaultValue(0)
  val log: LoggingAdapter = Logging(context.system, this)

  def receive : Receive = {
    case tweetMap : Map[String, Any] => {

      val key = tweetMap("user_name").toString
      val engagement = tweetMap("engagement").toString.toFloat
      val value = userEngagementMap(key)  + engagement
      userEngagementMap.update(key, value)
      log.info(s"${MAGENTA_B}User " + key + " has "+ value + s"engagement points${RESET}")

      if (tweetMap.contains("retweeted")){
        val key = tweetMap("original_user_name").toString
        val engagement = tweetMap("original_engagement").toString.toFloat
        val value = userEngagementMap(key)  + engagement
        userEngagementMap.update(key, value)
        log.info(s"${YELLOW_B}User " + key + " has "+ value + s"engagement points${RESET}")
      }

    }
    case _ =>
  }
}
