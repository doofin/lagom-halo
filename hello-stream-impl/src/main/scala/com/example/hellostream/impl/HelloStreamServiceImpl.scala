package com.example.hellostream.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.example.hellostream.api.HelloStreamService
import com.example.hello.api.HelloService
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Directive0, Directive1, Route}
import javax.inject.Inject

import scala.concurrent.Future

@Inject
class HelloStreamServiceImpl(helloService: HelloService, syst: ActorSystem)
    extends HelloStreamService {

  /*
  val ctx = new AkkaTypedCtx()
  import ctx._
  val bindingFuture: Future[Http.ServerBinding] =
    Http().bindAndHandle(path("")(complete("ok")), "0.0.0.0", 8080)
   */

  1 to 5 foreach { _ =>
    println("HelloStreamServiceImpl run ! ")
    println("sys: " + syst.startTime)
  }

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] =
    ServiceCall { hellos =>
      Future.successful(hellos.mapAsync(8)(helloService.hello(_).invoke()))
    }
}
