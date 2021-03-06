import akka.actor.{Actor, ActorSelection}
import akka.event.{Logging, LoggingAdapter}

import scala.Console._
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random

class Worker extends Actor{

  val lines = Source.fromFile("src/main/scala/em.txt").getLines.toList
  var analyzer: ActorSelection = context.system.actorSelection("user/Analyzer")
  var agregator: ActorSelection = context.system.actorSelection("user/Agregator")
  val log: LoggingAdapter = Logging(context.system, this)
  var emotion: Map[String, Int] = Map[String, Int]()
  for (line <- lines){
    val splited = line.split("\t")
    emotion = emotion + (splited(0) -> splited(1).toInt)
  }

  def receive: Receive = {
    case tweet: Map[String,String] => {
      val delay =Random.nextInt(450) + 50  // (0-450) + 50 = > 50 - 500 ms delay
      Thread.sleep(delay)
      if (!tweet("content").contains("panic")){

        val data = ujson.read(tweet("content"))
        var user_name = data("message")("tweet")("user")("screen_name").toString()
          user_name = user_name.substring(1,user_name.length-1)
        var message = data("message")("tweet")("text").toString();
        message = message.substring(1,message.length-1)
        var chunks = message.split("[ ,.!?@\"]+")
        chunks=chunks.filter(_.nonEmpty)
        var scoredWords = ListBuffer[String]()
        var result = 0
        for (chunk <- chunks){
          if (emotion.contains(chunk)){
            scoredWords += chunk
            result = result + emotion(chunk)
          }
          if (chunk.startsWith("#")){
           analyzer ! chunk
          }
        }
     //   if(tweet.toExecute()){
//          log.info(s"Input"+message + s"${RESET}")
//          log.info(s"${GREEN_B}Scored Words"+scoredWords + s"${RESET}")
//          log.info(s"${WHITE_B}Result"+result + s"${RESET}")

        var resultMap = Map[String,Any]()
        resultMap += ("user_name" -> user_name)
        resultMap += ("message" -> message)
        resultMap += ("result" -> result)
        resultMap += ("scoredWords" -> scoredWords)
        resultMap += ("source" -> "worker1")
        resultMap ++= tweet


          agregator ! resultMap    // NOW ON WORKER2 NEED To continue
          //tweet.postExecution()
       // }
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
