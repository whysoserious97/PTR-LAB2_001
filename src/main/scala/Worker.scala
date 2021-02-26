import akka.actor.{Actor, ActorSelection}
import akka.event.{Logging, LoggingAdapter}

import scala.Console._
import scala.io.Source
import scala.util.Random

class Worker extends Actor{

  val lines = Source.fromFile("src/main/scala/em.txt").getLines.toList
  var analyzer: ActorSelection = context.system.actorSelection("user/Analyzer")
  val log: LoggingAdapter = Logging(context.system, this)
  var emotion: Map[String, Int] = Map[String, Int]()
  for (line <- lines){
    val splited = line.split("\t")
    emotion = emotion + (splited(0) -> splited(1).toInt)
  }

  def receive: Receive = {
    case tweet: Tweet => {
      val delay =Random.nextInt(450) + 50  // (0-450) + 50 = > 50 - 500 ms delay
      Thread.sleep(delay)
      if (!tweet.content.contains("panic")){
        val data = ujson.read(tweet.content)
        tweet.input = data("message")("tweet")("text").toString();
        var chunks = tweet.input.split("[ ,.!?@\"]+")
        chunks=chunks.filter(_.nonEmpty)

        for (chunk <- chunks){
          if (emotion.contains(chunk)){
            tweet.scoredWords += chunk
            tweet.result = tweet.result + emotion(chunk)
          }
          if (chunk.startsWith("#")){
           analyzer ! chunk
          }
        }
        if(tweet.toExecute()){
          log.info(s"Input"+tweet.input + s"${RESET}")
          log.info(s"${GREEN_B}Scored Words"+tweet.scoredWords + s"${RESET}")
          log.info(s"${WHITE_B}Result"+tweet.result + s"${RESET}")
          //tweet.postExecution()
        }
        sender() ! "Success"

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
