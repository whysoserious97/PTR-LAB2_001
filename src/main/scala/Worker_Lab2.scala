import akka.actor.{Actor, ActorSelection}
import akka.event.{Logging, LoggingAdapter}

import scala.Console._
import scala.util.Random

class Worker_Lab2 extends Actor{

  var analyzer: ActorSelection = context.system.actorSelection("user/Analyzer")
  var agregator: ActorSelection = context.system.actorSelection("user/Agregator")
  var userEngagementActor: ActorSelection = context.system.actorSelection("user/UE")
  val log: LoggingAdapter = Logging(context.system, this)
  var emotion: Map[String, Int] = Map[String, Int]()

  def receive: Receive = {
    case tweet: Map[String,String] => {
      val delay =Random.nextInt(450) + 50  // (0-450) + 50 = > 50 - 500 ms delay
      Thread.sleep(delay)
      if (!tweet("content").contains("panic")){
        var tweetMap = Map[String,Any]()
        tweetMap += ("id" -> tweet("id"))

        val data = ujson.read(tweet("content"))
        if(data("message")("tweet").obj.contains("retweeted_status")){
          tweetMap += ("retweeted" -> true)
          var original_user_name = data("message")("tweet")("retweeted_status")("user")("screen_name").toString()
          original_user_name = original_user_name.substring(1,original_user_name.length-1)
          tweetMap += ("original_user_name" -> original_user_name)

          tweetMap += ("original_favorites" ->
            data("message")("tweet")("retweeted_status")("favorite_count").toString().toInt)

          tweetMap += ("original_retweets" ->
            data("message")("tweet")("retweeted_status")("retweet_count").toString().toInt)

          tweetMap +=( "original_followers" ->
            data("message")("tweet")("retweeted_status")("user")("followers_count").toString().toInt)

          if(tweetMap("original_followers").toString.toInt != 0){
            tweetMap+=  ("original_engagement" ->
              (tweetMap("original_retweets").toString.toInt + tweetMap("original_favorites").toString.toInt/
                tweetMap("original_followers").toString.toInt))
          } else {
            tweetMap+=  ("original_engagement" -> 0)
          }
        }


        val favorites = data("message")("tweet")("favorite_count").toString().toInt
        val retweets = data("message")("tweet")("retweet_count").toString().toInt
        val followers = data("message")("tweet")("user")("followers_count").toString().toInt

        var user_name = data("message")("tweet")("user")("screen_name").toString()
        user_name = user_name.substring(1,user_name.length-1)


        tweetMap += ("user_name" -> user_name)
        tweetMap += ("favorites" -> favorites)
        tweetMap += ("retweets" -> retweets)
        tweetMap += ("followers" -> followers)

        tweetMap += ("engagement" -> (if (followers != 0) (retweets + favorites)/followers else 0))
        tweetMap += ("source" -> "worker2")


        tweetMap ++= tweet
        agregator ! tweetMap
        userEngagementActor ! tweetMap
      }
      else {
        log.info(s"${RED}Exception throwed" + s"${RESET}")
        throw new Exception("Panic!!!!!!!!!!!!")
      }
    }
  }
  override def postRestart(reason:Throwable){
    log.info(s"${CYAN}I am restarted and reason is "+reason.getMessage + s"${RESET}")
  }

}
