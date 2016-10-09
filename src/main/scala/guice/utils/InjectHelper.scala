package guice.utils

import akka.actor.ActorSystem
import com.google.inject.{Guice, Module}

/**
  * Created by Aleksey Voronets on 09.02.16.
  */
trait InjectHelper {

  def inject[T](implicit mf: Manifest[T]): T = InjectHelper.injector.getInstance(mf.runtimeClass).asInstanceOf[T]

  def inject[A, B](implicit mfA: Manifest[A], mfB: Manifest[B]): (A, B) = (inject[A], inject[B])

  def getInstance[T](clazz: Class[T]): T = InjectHelper.injector.getInstance(clazz)

}

object InjectHelper extends InjectHelper {

  def classLoader: ClassLoader = classOf[ActorSystem].getClassLoader

  /*this.getClass.getClassLoader*/
  lazy val moduleClass = classLoader.loadClass("module.EQModule")
  lazy val moduleInstance = moduleClass.newInstance().asInstanceOf[Module]
  lazy val injector = {Guice.createInjector(moduleInstance)}
}
