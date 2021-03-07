import akka.actor.{ActorSystem, Props}

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

        }
        }
