import akka.actor.ActorSelection

import scala.collection.mutable.ListBuffer

class Tweet (){
  //var workers: ListBuffer[ActorSelection] = ListBuffer[ActorSelection]().appended(worker)
  var isExecuted = false
  var message :String = ""
  var scoredWords :ListBuffer[String] = ListBuffer[String]()
  var result: Int  = 0
  var engagement: Float = -1

  var original_favorites:Int = 0
  var original_retweets:Int = 0
  var original_followers:Int = 0
  var original_engagement:Float = -1 // if there is retweeted_status, this will be computed

  var followers = 0
  var favorites = 0
  var retweets = 0

  var user_name = ""
  var original_user_name = ""

  var recieved_count = 0
  var id:String = null
  var map:Map[String,Any] = Map[String,Any]()

  var retweeted = false

//  def addWorker(worker: ActorSelection): Unit ={
//    workers.appended(worker)
//  }

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
