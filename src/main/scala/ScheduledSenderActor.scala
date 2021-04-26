import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString

class ScheduledSenderActor(local: InetSocketAddress, remote: InetSocketAddress) extends Actor with ActorLogging {

  import context.system

  IO(Udp) ! Udp.Bind(self, local)



  def receive = {
    case Udp.Bound(_) ⇒{
      context.become(ready(sender()))
      var connectPub = new ConnectPub()
      connectPub.addTopic("users")
      connectPub.addTopic("tweet")
      self ! connectPub.stringify()
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


    case Udp.Received(data, remoteAddress) ⇒
      val ipAddress = remoteAddress.getAddress.getHostAddress
      val port = remoteAddress.asInstanceOf[InetSocketAddress].getPort
      if (data.utf8String.startsWith("ACK")){
        println(data.utf8String)
      }
  }
}

object ScheduledSenderActor {
  def apply(local: InetSocketAddress, remote: InetSocketAddress) = Props(classOf[ScheduledSenderActor], local, remote)
}