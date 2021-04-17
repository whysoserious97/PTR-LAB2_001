
class TweetManipulation {
  def stringify(tweet: Tweet):String = {
    var result:String = ""
    val topic = "tweet"
    result = s"Tweet{topic:$topic|" +
      s"engagement:${tweet.engagement}|" +
      s"isOriginal:${!tweet.retweeted}|" +
      s"""message:"${tweet.message}"""" +
      s"}";

    result
  }
}
