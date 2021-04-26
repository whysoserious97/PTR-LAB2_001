
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorPath, ActorRef, ActorSelection, ActorSystem, OneForOneStrategy, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.util.Timeout
import scala.language.postfixOps

import scala.Console._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt

class DynamicSpawner extends Actor {
  implicit  val system : ActorSystem = context.system;
  var router: ActorSelection = system.actorSelection("user/R")
  implicit val timeout: Timeout = Timeout(5 seconds)

  val log: LoggingAdapter = Logging(context.system, this)
  val workers : ListBuffer[ActorRef] = ListBuffer[akka.actor.ActorRef]()
  val workerPaths : ListBuffer[ActorPath] = ListBuffer[akka.actor.ActorPath]()
  for (_ <- 1 to 10){
    val worker = context.actorOf(Props[Worker])
    workers += worker
    workerPaths += worker.path
  }
  override val supervisorStrategy : OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
    case _: Exception     => Restart
  }

  def receive : Receive = {
    case "workers" => {
      val children = workerPaths
      sender() ! children
    }
    case integer: Integer => {
        if (integer > 1.2 * workers.length ){
          val more = integer - workers.length
          for (_ <- 1 to more){
            val worker = context.actorOf(Props[Worker])
            workers += worker
            workerPaths += worker.path
          }
          router ! workerPaths
          log.info(s"${GREEN}Increased to"+ workerPaths.length + s"${RESET}")
        }
        else if (integer <  0.8 *workers.length){
          val difference =  workers.length - integer
          for (_ <- 1 to difference){
            workers -= workers.last
            workerPaths -= workerPaths.last
          }
          router ! workerPaths
          log.info(s"${GREEN}Reduced to"+ workerPaths.length + s"${RESET}")
        }
    }

  }
}