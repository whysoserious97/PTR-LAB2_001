import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

class ScheduledSenderActor(local: InetSocketAddress, remote: InetSocketAddress) extends Actor with ActorLogging {

  import context.system

  IO(Udp) ! Udp.Bind(self, local)

  //val scheduleCancellable: Cancellable = system.scheduler.schedule(0.seconds, 1.second, self, "hello")


  def receive = {
    case Udp.Bound(_) ⇒{
      context.become(ready(sender()))
      var connectPub = new ConnectPub()
      connectPub.addTopic("users")
      self ! connectPub.stringify()
      //self ! "PublishUnsubscribe"
    }
  }

  def ready(send: ActorRef): Receive = {
    case msg: String ⇒{
      send ! Udp.Send(ByteString(msg), remote)
      println("Sender: send:"+msg+ "remote:"+remote)
    }
    case tweet:Tweet=>{
      println("tweet is ready");
      var msg = tweet.stringifyTweet()
      send ! Udp.Send(ByteString(msg), remote)
      msg = tweet.stringifyUser()
      send ! Udp.Send(ByteString(msg), remote)
    }
     // send ! Udp.Send(ByteString("from ready"), remote)
     // println("From ready" + msg)

    case Udp.Received(data, remoteAddress) ⇒
      val ipAddress = remoteAddress.getAddress.getHostAddress
      val port = remoteAddress.asInstanceOf[InetSocketAddress].getPort
      if (data.utf8String.startsWith("ACK")){
        println(data.utf8String)
      }
      //log.info(s"we received ${data.utf8String} from IP Address: $ipAddress and port number: $port")
  }
}

object ScheduledSenderActor {
  def apply(local: InetSocketAddress, remote: InetSocketAddress) = Props(classOf[ScheduledSenderActor], local, remote)
}