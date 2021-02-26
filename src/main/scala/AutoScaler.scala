
import akka.actor.{Actor, ActorSelection}

import java.util.Calendar
import scala.collection.mutable.ListBuffer

class AutoScaler extends Actor{
  implicit  val system = context.system;


  var ds: ActorSelection = system.actorSelection("user/DS")
  var events = new ListBuffer[Event]()
  var periodicity = 100
  var messagesPerTime = 0;

  def receive: Receive = {
    case str:String => {
      val time1 = Calendar.getInstance().getTime
      events += new Event(str, time1.getTime)
      periodicity -= 1
      if (periodicity == 0){
        autoscale()
        periodicity = 100
      }
    }
  }
  def autoscale(): Unit ={
    messagesPerTime = 0
    var index = events.length
    var currentEvent = events.last
    var difference =Calendar.getInstance().getTime.getTime - currentEvent.time
    while(index > 0 && difference < 10000){
      index -=1
      currentEvent = events(index)
      messagesPerTime += 1
      difference =Calendar.getInstance().getTime.getTime - currentEvent.time
    }
    ds ! messagesPerTime
  }
}
class Event(var message:String, var time:Long)
