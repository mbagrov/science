package dao.models.squeryl

import dao.SquerylConfig
import liquibase.change.custom.CustomTaskChange
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.ValidationErrors
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Session

/**
  * Created by alex on 10.03.16.
  */
trait SquerylCustomTaskChange extends CustomTaskChange {

  def validate(database: Database): ValidationErrors = new ValidationErrors()

  def performMigration(session: Session)

  def execute(database: Database) {
    val conn = database.getConnection.asInstanceOf[JdbcConnection].getUnderlyingConnection
    val session = Session.create(conn, SquerylConfig.dbDefaultAdapter)
    val oldAutoCommit = conn.getAutoCommit
    try {
      conn.setAutoCommit(false)
      using(session) {performMigration(session)}
      conn.commit()
    }
    catch {
      case e: Throwable =>
        conn.rollback()
        throw e
    }
    finally conn.setAutoCommit(oldAutoCommit)
  }
}
