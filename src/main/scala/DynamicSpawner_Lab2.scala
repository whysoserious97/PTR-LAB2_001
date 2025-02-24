
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorPath, ActorRef, ActorSelection, ActorSystem, OneForOneStrategy, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.ask
import akka.util.Timeout
import scala.language.postfixOps

import scala.Console._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt

class DynamicSpawner_Lab2 extends Actor {
  implicit  val system : ActorSystem = context.system;
  var router: ActorSelection = system.actorSelection("user/R")
  implicit val timeout: Timeout = Timeout(5 seconds)

  val log: LoggingAdapter = Logging(context.system, this)
  val workers2 : ListBuffer[ActorRef] = ListBuffer[akka.actor.ActorRef]()
  val workerPaths2 : ListBuffer[ActorPath] = ListBuffer[akka.actor.ActorPath]()
  for (a <- 1 to 10){
    val worker2 = context.actorOf(Props[Worker_Lab2])
    workers2 += worker2
    workerPaths2 += worker2.path
  }
  override val supervisorStrategy : OneForOneStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute){
    case _: Exception     => Restart
  }


  def receive : Receive = {
    case "workers" => {
      val children = workerPaths2
      sender() ! children
    }
    case integer: Integer => {
        if (integer > 1.2 * workers2.length ){
          val more = integer - workers2.length
          for (_ <- 1 to more){
            val worker2 = context.actorOf(Props[Worker_Lab2])
            workers2 += worker2
            workerPaths2 += worker2.path
          }
          router ! workerPaths2
          log.info(s"${GREEN}Increased to"+ workerPaths2.length + s"${RESET}")
        }
        else if (integer <  0.8 *workers2.length){
          val difference =  workers2.length - integer
          for (_ <- 1 to difference){
            workers2 -= workers2.last
            workerPaths2 -= workerPaths2.last
          }
          router ! workerPaths2
          log.info(s"${GREEN}Reduced to"+ workerPaths2.length + s"${RESET}")
        }
    }

  }
}