import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import dao.BootstrapSqueryl
import org.slf4j.{Logger, LoggerFactory}
import plugins.{EhcachePlugin, LiquibasePlugin}

object ElectronicQueue extends App with ElectronicQueueMicroService {

  lazy val config = ConfigFactory.load()
  implicit val system = ActorSystem("ElectronicQueueSystem")
  implicit val materializer = ActorMaterializer()

  val cache = EhcachePlugin.manager

  val logger: Logger = LoggerFactory.getLogger(getClass)
  LiquibasePlugin.performMigrations

  BootstrapSqueryl.bootstrap(logger)

  val eqServicePort = ConfigFactory.load().getString("http-service-port-eq").toInt
  val serviceHost = ConfigFactory.load().getString("http-service-host")
  val bindingFuture = Http().bindAndHandle(eqRoutes, serviceHost, eqServicePort)
}
