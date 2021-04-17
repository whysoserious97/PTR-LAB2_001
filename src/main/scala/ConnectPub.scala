class ConnectPub() {
  var topics = Array[String]()

  def addTopic(str:String): Unit ={
     topics = topics :+ str
  }
    def stringify():String = {
      var result:String = ""
        result = s"ConnectPub{topics:["
          for (i <- topics.indices){
        result += topics(i)
            if(i != topics.length - 1){
              result += ','
            }
      }
        result += s"]}"
//      result = s"ConnectPub{topic:$topic|" +
//        s"engagement:${tweet.engagement}|" +
//        s"isOriginal:${!tweet.retweeted}|" +
//        s"""message:"${tweet.message}"""" +
//        s"}";

      result
    }
  }
