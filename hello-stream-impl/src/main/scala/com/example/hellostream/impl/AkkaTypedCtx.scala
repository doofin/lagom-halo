package com.example.hellostream.impl

import akka.actor
import akka.actor.typed.scaladsl._
import akka.stream.typed.scaladsl.ActorMaterializer
import akka.actor.typed.{ActorSystem, Behavior, PostStop, PreRestart}
import com.typesafe.config.ConfigFactory
import akka.actor.typed.scaladsl.adapter._
import akka.stream._

import scala.concurrent.ExecutionContext

import AkkaTypedCtx._

class AkkaTypedCtx(init: ActorContext[Unit] => Unit = _ => (),
                   port: String = "0") {
  val systemT: ActorSystem[Unit] =
    ActorSystem(
      AkkaTypedCtx.main(init),
      clusterSystemName,
      ConfigFactory.parseString(akkaClusterConf {
        val res = "255" + port
        println("bind to " + res)
        res
      })
    )
  implicit val system_untyped: actor.ActorSystem = systemT.toUntyped
  implicit val mat: Materializer = ActorMaterializer(
    Some(ActorMaterializerSettings(system_untyped).withSupervisionStrategy(
      decider)))(systemT)
  implicit val ec: ExecutionContext = systemT.executionContext

  systemT ! ()
}

object AkkaTypedCtx {
  val main: (ActorContext[Unit] => Unit) => Behavior[Unit] = f =>
    Behaviors.setup[Unit] { ctx: ActorContext[Unit] ⇒
      f(ctx)
      Behaviors
        .receiveMessage { msg: Unit =>
          println("AkkaTypedCtx Cluster ok!")
          Behaviors.same[Unit]
        }
        .receiveSignal {
          case (_, PostStop) =>
            Behaviors.same[Unit]
          case (_, PreRestart) =>
            Behaviors.same[Unit]
        }
  }
  // ok
  val decider: Supervision.Decider = { x: Throwable ⇒
    x.printStackTrace()
    println(x.getMessage)
    Supervision.Resume
  }

  val clusterSystemName = "ClusterSystem"
  val host = "127.0.0.1"
  val seeds: String =
    s"""["akka.tcp://$clusterSystemName@$host:2550",
         |"akka.tcp://$clusterSystemName@$host:2551"]""".stripMargin

  val akkaClusterConf: String => String = x => s"""
         |akka {
         |  loglevel = "DEBUG"
         |  loggers = ["akka.event.slf4j.Slf4jLogger"]
         |  actor {
         |    provider = "akka.cluster.ClusterActorRefProvider"
         |  }
         |  remote {
         |    enabled-transports = ["akka.remote.netty.tcp"]
         |    log-remote-lifecycle-events = off
         |    netty.tcp {
         |      hostname = "$host"
         |      port = $x
         |    }
         |  }
         |  cluster {
         |    seed-nodes = $seeds
         |  }
         |}
    """.stripMargin

}
//  implicit val mat: Materializer =
//    ActorMaterializer[Unit]()(systemT)
