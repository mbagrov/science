import akka.actor.ActorSystem
import akka.stream.Materializer
import org.slf4j.Logger

trait BaseMicroService {

  implicit val system: ActorSystem
  implicit val materializer: Materializer
  val logger: Logger
}
