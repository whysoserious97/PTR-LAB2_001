
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorSelection, OneForOneStrategy, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.util.Timeout

import scala.Console._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt

class DynamicSpawner extends Actor {
  implicit  val system = context.system;
  var router: ActorSelection = system.actorSelection("user/R")
  implicit val timeout: Timeout = Timeout(5 seconds)

  val log: LoggingAdapter = Logging(context.system, this)
  val workers = ListBuffer[akka.actor.ActorRef]()
  val workerPaths = ListBuffer[akka.actor.ActorPath]()
  for (a <- 1 to 10){
    val worker = system.actorOf(Props[Worker])
    workers += worker
    workerPaths += worker.path
  }
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
    case _: Exception     => Restart
  }


  // TO USE STATE ON READING AND CHANGING
  def receive = {
    case "workers" => {
      val children = workerPaths
      sender() ! children
    }
    case integer: Integer => {
        if (integer > 1.2 * workers.length ){
          val more = integer - workers.length
          for (a <- 1 to more){
            val worker = system.actorOf(Props[Worker])
            workers += worker
            workerPaths += worker.path
          }
          var changed =  router ? workerPaths
          log.info(s"${GREEN}Increased to"+ workerPaths.length + s"${RESET}")
        }
        else if (integer <  0.8 *workers.length){
          val difference =  workers.length - integer
          for (_ <- 1 to difference){
            workers -= workers.last
            workerPaths -= workerPaths.last
          }
         var changed =  router ? workerPaths
          log.info(s"${GREEN}Reduced to"+ workerPaths.length + s"${RESET}")
        }
    }

  }
}