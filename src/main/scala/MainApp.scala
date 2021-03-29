import akka.actor.{ActorRef, ActorSystem, Props}

import java.net.InetSocketAddress

object MainApp{
        val actorSystem:ActorSystem=ActorSystem("ActorSystem")
        def main(args:Array[String]):Unit={

        val DS=actorSystem.actorOf(Props[DynamicSpawner],"DS")
        val DS2=actorSystem.actorOf(Props[DynamicSpawner_Lab2],"DS2")
        val Con=actorSystem.actorOf(Props[Connector],"C")
        val router=actorSystem.actorOf(Props[Router],"R")
        val autoScaler=actorSystem.actorOf(Props[AutoScaler],"AS")
        val dataAnalyser = actorSystem.actorOf(Props[ Analyzer ], "Analyzer")
        val databaseManager = actorSystem.actorOf(Props[DatabaseManager],"DBManager")
        val agregator = actorSystem.actorOf(Props[Agregator],"Agregator")
        val userEngagementActor = actorSystem.actorOf(Props[UserEngagement],"UE")
        Con!"test"
        dataAnalyser ! 10
        databaseManager ! "pull"
                val remote = new InetSocketAddress("localhost", 5005)
                val local = new InetSocketAddress("localhost", 5115)

                val udp: ActorRef = actorSystem.actorOf(ScheduledSenderActor(local, remote), name = "UDP")
        }
        }
