package dao

import guice.utils.ConfigHelper
import org.squeryl.adapters.PostgreSqlAdapter

/**
  * Created by Aleksey Voronets on 09.02.16.
  */
object SquerylConfig extends ConfigHelper {

  lazy val dbDefaultAdapter = getString("db.default.driver") match {
    case "org.postgresql.Driver" => new PostgreSqlAdapter
    case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
  }

  lazy val dbSeAdapter = getString("db.se.driver") match {
    case "org.postgresql.Driver" => new PostgreSqlAdapter
    case _ => sys.error("Database driver must be org.postgresql.Driver")
  }

  lazy val logSql = getBoolean("log-sql")
}