package plugins

import java.sql.{Connection, DriverManager}

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.{ClassLoaderResourceAccessor, CompositeResourceAccessor, FileSystemResourceAccessor}
import utils.IOUtils._

/**
  * Created by Aleksey Voronets on 27.02.16.
  */
object LiquibasePlugin {

    lazy val config = ConfigFactory.load()

    def performMigrations: Unit = {

        val changeLog = config.getString("liquibase.changelog")
        val url = config.getString("db.default.url")
        val userName = config.getString("db.default.user")
        val password = config.getString("db.default.password")

        ensureClose(DriverManager.getConnection(url, userName, password)) { connection =>
            val liqui = try {getLiquibase(changeLog, connection)} catch {case e: Exception => throw new Exception("Liquibase can't be instantiated")}
            liqui.update("dev")
        }
    }

    def getLiquibase(changeLogFilePath: String, connection: Connection): Liquibase = {
        val fileAccessor = new FileSystemResourceAccessor()
        val classLoaderAccessor = new ClassLoaderResourceAccessor(classLoader)
        val fileOpener = new CompositeResourceAccessor(fileAccessor, classLoaderAccessor)
        new Liquibase(changeLogFilePath, fileOpener, new JdbcConnection(connection))
    }

    def classLoader: ClassLoader = classOf[ActorSystem].getClassLoader
}
