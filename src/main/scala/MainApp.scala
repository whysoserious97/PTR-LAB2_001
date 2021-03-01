import akka.actor.{ActorSystem, Props}

object MainApp{
        val actorSystem:ActorSystem=ActorSystem("ActorSystem")
        def main(args:Array[String]):Unit={

        var DS=actorSystem.actorOf(Props[DynamicSpawner],"DS")
        var DS2=actorSystem.actorOf(Props[DynamicSpawner_Lab2],"DS2")
        val Con=actorSystem.actorOf(Props[Connector],"C")
        var router=actorSystem.actorOf(Props[Router],"R")
        var autoScaler=actorSystem.actorOf(Props[AutoScaler],"AS")
        var dataAnalyser =actorSystem.actorOf(Props[Analyzer],"Analyzer")
        var databaseManager = actorSystem.actorOf(Props[DatabaseManager],"DBManager")
        var agregator = actorSystem.actorOf(Props[Agregator],"Agregator")
        Con!"test"
        dataAnalyser ! 10
        databaseManager ! "pull"

        }
        }
