import akka.actor.Actor
import akka.event.{Logging, LoggingAdapter}

import java.util.Calendar
import scala.Console._
import scala.collection.mutable.ListBuffer

class Analyzer extends Actor{
  val log: LoggingAdapter = Logging(context.system, this)

  var hashtags = new ListBuffer[HashtagRecord]()
  var hashtagMap = collection.mutable.Map[String,Int]().withDefaultValue(0)

  override def receive = {

    case str:String => {
      val time = Calendar.getInstance().getTime.getTime
        hashtags += new HashtagRecord(str,time)
    }

    case topX: Int => {
         if(hashtags.nonEmpty) {
            var index = hashtags.length
            var currentHashTag = hashtags.last
            var difference = Calendar.getInstance().getTime.getTime - currentHashTag.time
            while (index > 0 && difference < 10000) {
                index -= 1
                currentHashTag = hashtags(index)
              val key = currentHashTag.hashTag
                hashtagMap.update(key , hashtagMap(key) + 1)
                difference = Calendar.getInstance().getTime.getTime - currentHashTag.time
            }

            val seq = hashtagMap.toSeq.sortWith(_._2 > _._2)

            var elements = "Top Hashtags: "
            val number = Math.min(topX,seq.size)
                 for (i <- 0 until number){
                   elements += seq(i)._1 + "("+seq(i)._2+") "
                 }

                 log.info(s"${CYAN_B}" + elements + s"${RESET}")
    }
      Thread.sleep(2000)
      self ! topX
    }

    case _ => {}
  }
}
class HashtagRecord(var hashTag:String, var time:Long){
  var count = 0
}