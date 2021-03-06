import akka.actor.{Actor, ActorSelection}

import scala.collection.mutable.ListBuffer

class Agregator extends Actor {

  var dbManager : ActorSelection = context.system.actorSelection("user/DBManager")
  var tweets : Set[Tweet] = Set[Tweet]()
  var waitingRoom : Set[Tweet] = Set[Tweet]()

  def receive : Receive = {
    case tweetMap : Map[String, Any] => {

      var tweetSubset = waitingRoom.filter(t => t.id == tweetMap("id"))
      var tweet : Tweet = null
      if ( tweetSubset.isEmpty ) {
        tweet = new Tweet()
        waitingRoom += tweet
      }
      else {
        tweet = tweetSubset.head
      }
      tweet.recieved_count += 1

      if ( tweetMap("source") == "worker1" ) {
        tweet.id = tweetMap("id").toString
        tweet.user_name = tweetMap("user_name").toString
        tweet.message = tweetMap("message").toString
        tweet.result = tweetMap("result").toString.toInt
        tweet.scoredWords = tweetMap("scoredWords").asInstanceOf[ListBuffer[String]]
        //        println("Hello")
      }
      else if ( tweetMap("source") == "worker2" ) {
        tweet.engagement = tweetMap("engagement").toString.toInt
        tweet.followers = tweetMap("followers").toString.toInt
        tweet.retweets = tweetMap("retweets").toString.toInt
        tweet.favorites = tweetMap("favorites").toString.toInt

        if ( tweetMap.contains("retweeted") ) {
          tweet.original_engagement = tweetMap("original_engagement").toString.toFloat
          tweet.original_favorites = tweetMap("original_favorites").toString.toInt
          tweet.original_followers = tweetMap("original_followers").toString.toInt
          tweet.original_retweets = tweetMap("original_retweets").toString.toInt
          tweet.original_user_name = tweetMap("original_user_name").toString
          tweet.retweeted = tweetMap("retweeted").toString.toBoolean
          //     println("Retweeted")
        }
      }
      if ( tweet.recieved_count == 2 ) {
        tweets += tweet
        waitingRoom -= tweet
      }
      //      if (tweets.size == 10){
      //        println("here")
      //      }
    }
    case integer : Int => {
      var toSend = Set[Tweet]()
      toSend ++= tweets.take(integer)
      tweets --= toSend

      sender() ! toSend
    }
  }
}
