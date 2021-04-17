class Unsubscribe {
  var topics : Array[String ] = Array[String]()
  def stringify():String = {
    var result:String = ""
    result = s"Unsubscribe{topics:["
    for (i <- topics.indices){
      result += topics(i)
      if(i != topics.length - 1){
        result += ','
      }
    }
    result += s"]}"


    result
  }
}


