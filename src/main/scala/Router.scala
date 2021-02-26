
import akka.actor.{Actor, ActorSelection, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.util.Timeout

import scala.Console._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Random, Success}

class Router extends Actor{

  implicit  val system: ActorSystem = context.system;
  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  implicit val timeout: Timeout = Timeout(0.6 seconds)

  val log: LoggingAdapter = Logging(context.system, this)
  var ds: ActorSelection = system.actorSelection("user/DS")
  var paths: ListBuffer[akka.actor.ActorPath] = ListBuffer[akka.actor.ActorPath]()

  var future: Future[Any] = ds ? "workers"
  future.foreach(f => paths = f.asInstanceOf[ListBuffer[akka.actor.ActorPath]])

  def receive = {
    case str: String => {
      val worker = system.actorSelection(paths.head)
      val tweet = new Tweet(str,worker)
      val response =  worker ? tweet
      response.onComplete{
        case Success(_) =>{}
        case Failure(f) => {
          self ! tweet
          log.warning(s"${RED_B}Speculative execution started${RESET}")

        }
      }
        paths += paths.head
        paths.remove(0)
    }
    case workers: ListBuffer[akka.actor.ActorPath] => {
      paths = workers.clone()
    }
    case tweet: Tweet => {
      val worker = system.actorSelection(paths(Random.nextInt(paths.length)))
      if ( !tweet.isExecuted){
        val response =  worker ? tweet
        response.onComplete{
          case Success(_) =>{}
         case Failure(f) => {
           self ! tweet
           log.warning(s"${RED_B}Speculative execution started${RESET}")
          }
        }
      }
    }
    case _ => log.info("received unknown message")
  }
}