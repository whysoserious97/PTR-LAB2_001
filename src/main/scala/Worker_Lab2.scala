import akka.actor.{Actor, ActorSelection}
import akka.event.{Logging, LoggingAdapter}

import scala.Console._
import scala.io.Source
import scala.util.Random

class Worker_Lab2 extends Actor{

  //val lines = Source.fromFile("src/main/scala/em.txt").getLines.toList
  var analyzer: ActorSelection = context.system.actorSelection("user/Analyzer")
  var agregator: ActorSelection = context.system.actorSelection("user/Agregator")
  val log: LoggingAdapter = Logging(context.system, this)
  var emotion: Map[String, Int] = Map[String, Int]()
//  for (line <- lines){
//    val splited = line.split("\t")
//    emotion = emotion + (splited(0) -> splited(1).toInt)
//  }

  def receive: Receive = {
    case tweet: Tweet => {
      val delay =Random.nextInt(450) + 50  // (0-450) + 50 = > 50 - 500 ms delay
      Thread.sleep(delay)
      if (!tweet.content.contains("panic")){
        //  println("From Worker 2")
        val data = ujson.read(tweet.content)

        if(data("message")("tweet").obj.contains("retweeted_status")){
          tweet.original_favorites = data("message")("tweet")("retweeted_status")("favorite_count").toString().toInt
          tweet.original_retweets = data("message")("tweet")("retweeted_status")("retweet_count").toString().toInt
          tweet.original_followers = data("message")("tweet")("retweeted_status")("user")("followers_count").toString().toInt
          tweet.original_engagement = if (tweet.original_followers != 0) (tweet.original_retweets + tweet.original_favorites)/tweet.original_followers else 0;
        }


        val favorites = data("message")("tweet")("favorite_count").toString().toInt
        val retweets = data("message")("tweet")("retweet_count").toString().toInt
        val followers = data("message")("tweet")("user")("followers_count").toString().toInt
        tweet.engagement = if (followers == 0) (retweets + favorites)/followers else 0;
        agregator ! tweet
      }
      else {
        log.info(s"${RED}Exception throwed" + s"${RESET}")
        sender ! "Success"
        throw new Exception("Panic!!!!!!!!!!!!")
      }
    }
  }
  override def postRestart(reason:Throwable){    // overriding preStart method
    log.info(s"${CYAN}I am restarted and reason is "+reason.getMessage + s"${RESET}")
  }

}
