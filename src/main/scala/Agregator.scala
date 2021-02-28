import akka.actor.{Actor, ActorSelection}

import scala.collection.mutable.ListBuffer

class Agregator extends Actor{

  var dbManager: ActorSelection = context.system.actorSelection("user/DBManager")
  var tweets = ListBuffer[Tweet]()

  def receive: Receive = {
    case tweet:Tweet =>{
      if (tweet.engagement != -1 && tweet.isExecuted ){
        tweets.append(tweet)
      }
    }
    case integer: Int =>{
      var toSend = ListBuffer[Tweet]()
      while (tweets.nonEmpty && toSend.length < integer){
        toSend.append(tweets.remove(0))
      }
      dbManager ! toSend
    }
  }
}
