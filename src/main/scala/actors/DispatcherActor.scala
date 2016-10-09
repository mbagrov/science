package actors

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern._
import akka.util.Timeout
import org.slf4j.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Aleksey Voronets on 24.06.16.
  */
trait DispatcherActor extends Actor {

  def logger: Logger

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1 minute) { case e: Throwable =>
    logger.error(e.getMessage, e); Resume
  }

  protected def resendMessage[T <: Actor](nameTokes: String*)(message: Any)
    (implicit classTag: scala.reflect.ClassTag[T], logger: Logger) = {
    val name = nameTokes.mkString("_")
    context.child(name).getOrElse(context.actorOf(Props[T], name)) ! message
  }

  protected def askMessage[T <: Actor, T1](nameTokes: String*)(message: Any)
    (implicit classTag: scala.reflect.ClassTag[T], classTag1: scala.reflect.ClassTag[T1], timeout: Timeout) = {
    val name = nameTokes.mkString("_")
    val result = (context.child(name).getOrElse(context.actorOf(Props[T], name)) ? message).mapTo[T1]
    result pipeTo sender
  }
}