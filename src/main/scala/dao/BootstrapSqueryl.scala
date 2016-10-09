package dao

import guice.utils.ConfigHelper
import org.slf4j.Logger
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}

/**
  * Created by Aleksey Voronets on 09.02.16.
  */
object BootstrapSqueryl extends ConfigHelper {

  def bootstrap(logger: Logger): Unit = SessionFactory.concreteFactory = {
    Some(
      { () =>
        val s = getSession(SquerylConfig.dbDefaultAdapter)
        if (SquerylConfig.logSql) {
          s.setLogger(s => logger.debug(s))
        }
        s
      }
      //        Some(() => getSession(SquerylConfig.dbDefaultAdapter))
    )
  }

  private def getSession(adapter: DatabaseAdapter) = {
    val url = getString("db.default.url")
    val user = getString("db.default.user")
    val password = getString("db.default.password")
    Session.create(java.sql.DriverManager.getConnection(url, user, password), adapter)
  }
}
