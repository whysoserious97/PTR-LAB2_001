
import akka.NotUsed
import akka.actor.{Actor, ActorSelection, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ThrottleMode
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class Connector extends Actor {


  implicit  val system: ActorSystem = context.system;
  implicit val dispatcher = context.dispatcher

  var router: ActorSelection = system.actorSelection("user/R")
  var autoScaler: ActorSelection = system.actorSelection("user/AS")

  def receive: Receive = {
    case "test" => {

      val send: HttpRequest => Future[HttpResponse] =
        Http().singleRequest(_)
      val eventSource: Source[ServerSentEvent, NotUsed] = EventSource(
        uri = Uri(s"http://localhost:4000/tweets/1"),
        send,
        initialLastEventId = None,
        retryDelay = 1.second
      )
      val eventSource2: Source[ServerSentEvent, NotUsed] = EventSource(
        uri = Uri(s"http://localhost:4000/tweets/2"),
        send,
        initialLastEventId = None,
        retryDelay = 1.second
      )
      while (true){
        val future = eventSource.throttle(1, 1.milliseconds, 1, ThrottleMode.Shaping).take(20).runWith(Sink.seq)
        val future2 = eventSource2.throttle(1, 1.milliseconds, 1, ThrottleMode.Shaping).take(20).runWith(Sink.seq)
        future.foreach(se => se.foreach(
          ev => {
           val event = ev.getData();

            router ! event
            autoScaler ! event
          }))
        future2.foreach(se => se.foreach(
          ev => {
            val event = ev.getData();

            router ! event
            autoScaler ! event
          }))
        while (!future.isCompleted || !future2.isCompleted){}
      }

    }

  }
}