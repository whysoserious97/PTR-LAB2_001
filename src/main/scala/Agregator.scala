import akka.actor.{Actor, ActorSelection}

import scala.collection.mutable.ListBuffer

class Agregator extends Actor{

  var dbManager: ActorSelection = context.system.actorSelection("user/DBManager")
  var tweets: Set[Tweet] = Set[Tweet]()

  def receive: Receive = {
    case tweet:Tweet =>{
      tweet.recieved_count +=1
      if (tweet.recieved_count == 2 ){
        tweets += tweet
      }
      if (tweets.size == 10){
        println("here")
      }
    }
    case integer: Int =>{
      var toSend = Set[Tweet]()
        toSend ++=  tweets.take(integer)
      tweets --= toSend

      dbManager ! toSend
    }
  }
}
