package module

import com.google.inject.multibindings.Multibinder
import com.google.inject.{Binder, Scopes}
import com.tzavellas.sse.guice.ScalaModule

/**
  * Created by Aleksey Voronets on 09.02.16.
  */
trait CoreModule extends ScalaModule {

    //workaround for http://stackoverflow.com/questions/17557057/how-to-solve-implementation-restriction-trait-accesses-protected-method
    override def binder: Binder = super.binder()

    protected def bindSet[T](classes: Iterable[Class[_ <: T]], elementClass: Class[T])(implicit binder: Binder) {
        classes.foreach(d => Multibinder.newSetBinder(binder, elementClass).addBinding().to(d).in(Scopes.SINGLETON))
    }

    protected def bindSingleton[T, C <: T](implicit tM: Manifest[T], tC: Manifest[C]) {
        bind(tM.runtimeClass.asInstanceOf[Class[T]]).to(tC.runtimeClass.asInstanceOf[Class[C]]).in(Scopes.SINGLETON)
    }

    protected def bindSingletonSet(classes: Iterable[Class[_]]) {
        classes.foreach(bind(_).in(Scopes.SINGLETON))
    }

}
