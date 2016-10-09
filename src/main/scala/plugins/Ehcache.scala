package plugins

import net.sf.ehcache.{CacheManager, Ehcache, Element}
import org.apache.commons.lang3.reflect.TypeUtils

import scala.reflect.ClassTag

/**
  * Created by Aleksey Voronets on 01.03.16.
  */

object EhcachePlugin {

    lazy val manager = {
        loaded = true
        CacheManager.create()
    }
    @volatile var loaded = false

    def cache(name: String = "ei"): ECacheApi = synchronized {
        new EhCacheImpl(if (manager.cacheExists(name)) manager.getEhcache(name) else newCache(name))
    }

    private def newCache(name: String): Ehcache = {
        manager.addCache(name)
        manager.getEhcache(name)
    }

    def removeCache(name: String): Unit = synchronized {manager.removeCache(name)}
}

class EhCacheImpl(val cache: Ehcache) extends ECacheApi {

    def set(key: String, value: Any, expiration: Int) {
        val element = new Element(key, value)
        if (expiration == 0) element.setEternal(true)
        element.setTimeToLive(expiration)
        cache.put(element)
    }

    def get(key: String): Option[Any] = Option(cache.get(key)).map(_.getObjectValue)

    def remove(key: String): Unit = cache.remove(key)

    def removeAll(): Unit = cache.removeAll()
}

trait ECacheApi {

    def set(key: String, value: Any, expiration: Int)

    def get(key: String): Option[Any]

    def getOrElse[A](key: String, expiration: Int = 0)(orElse: => A)(implicit ct: ClassTag[A]): A = {
        getAs[A](key).getOrElse {
            val value = orElse
            set(key, value, expiration)
            value
        }
    }

    def getAs[T](key: String)(implicit ct: ClassTag[T]): Option[T] = {
        get(key).map { item =>
            if (TypeUtils.isInstance(item, ct.runtimeClass)) Some(item.asInstanceOf[T]) else None
        }.getOrElse(None)
    }

    def remove(key: String)

    def removeAll()
}
