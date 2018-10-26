package com.example.hellostream.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.example.hellostream.api.HelloStreamService
import com.example.hello.api.HelloService

import scala.concurrent.Future

/**
  * Implementation of the HelloStreamService.
  */
class HelloStreamServiceImpl(helloService: HelloService) extends HelloStreamService {
  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]] = ServiceCall { hellos =>

    Future.successful(hellos.mapAsync(8)(helloService.hello(_).invoke()))
  }
}
