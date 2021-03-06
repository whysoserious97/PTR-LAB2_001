
import akka.actor.{Actor, ActorSelection, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.util.Timeout

import scala.Console._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Random, Success}
import java.util.UUID.randomUUID

class Router extends Actor{

  implicit  val system: ActorSystem = context.system;
  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  implicit val timeout: Timeout = Timeout(0.6 seconds)

  val log: LoggingAdapter = Logging(context.system, this)
  var ds: ActorSelection = system.actorSelection("user/DS")
  var ds2: ActorSelection = system.actorSelection("user/DS2")
  var paths: ListBuffer[akka.actor.ActorPath] = ListBuffer[akka.actor.ActorPath]()
  var paths2: ListBuffer[akka.actor.ActorPath] = ListBuffer[akka.actor.ActorPath]() // Used for Worker2

  var future: Future[Any] = ds ? "workers"
  future.foreach(f => paths = f.asInstanceOf[ListBuffer[akka.actor.ActorPath]])

  var future2: Future[Any] = ds2 ? "workers"
  future2.foreach(f => paths2 = f.asInstanceOf[ListBuffer[akka.actor.ActorPath]])

  def receive = {
    case str: String => {
      val worker = system.actorSelection(paths.head)
      val worker2 = system.actorSelection(paths2.head)
      //val tweet = new Tweet(str,worker,worker2)
      var tweetMap: Map[String,String] = Map("id" -> randomUUID().toString,"content" -> str)
      val response =  worker ? tweetMap
      worker2 ! tweetMap
      response.onComplete{
        case Success(_) =>{}
        case Failure(f) => {
//          self ! tweetMap
//          log.warning(s"${RED_B}Speculative execution started${RESET}")

        }
      }
        paths += paths.head
        paths.remove(0)

        paths2 += paths2.head
        paths2.remove(0)
    }
    case workers: ListBuffer[akka.actor.ActorPath]  => {
       if (workers.head.parent.toString.contains("DS2")){  //workers.head.getClass.toString.contains("_Lab2")
         paths2 = workers.clone()
       }
       else {
         paths = workers.clone()
       }

    }
    case tweet: Tweet => {
      val worker = system.actorSelection(paths(Random.nextInt(paths.length)))
   //   val worker2 = system.actorSelection(paths2(Random.nextInt(paths2.length)))
      if ( !tweet.isExecuted){
        val response =  worker ? tweet
       // worker2 ! tweet
        response.onComplete{
          case Success(_) =>{}
         case Failure(f) => {
           self ! tweet
           log.warning(s"${RED_B}Speculative execution started${RESET}")
          }
        }
      }
    }
    case z => log.info(z.toString)
  }
}