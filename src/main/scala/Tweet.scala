import akka.actor.ActorSelection

import scala.collection.mutable.ListBuffer

class Tweet (var content:String,var worker: ActorSelection){
  var workers: ListBuffer[ActorSelection] = ListBuffer[ActorSelection]().appended(worker)
  var isExecuted = false
  var input :String = ""
  var scoredWords :ListBuffer[String] = ListBuffer[String]()
  var result: Int  = 0

  def addWorker(worker: ActorSelection): Unit ={
    workers.appended(worker)
  }

  def toExecute(): Boolean ={
    this.synchronized{
      if (!isExecuted){
        isExecuted = true
        true
      }
      else {
        false
      }
    }

  }
}
