import akka.http.scaladsl.model.StatusCodes
import utils._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import dao.services.FeatureService
import guice.utils.InjectHelper
import spray.json._

import scala.util.Try

trait ElectronicQueueMicroService extends BaseMicroService with StringHelper with DateHelper {

  private lazy val featureService = InjectHelper.inject[FeatureService]
  import DefaultJsonProtocol._
  import ServiceJsonFormatters.featureCollectionFormat

  val eqRoutes = logRequestResult("science-project") {
    path("") {
      getFromResource("web/index.html")
    } ~
    pathPrefix("") {
      getFromResourceDirectory("web")
    } ~
    path("show") {
      getFromResource("web/map.html")
    } ~
    path("edit") {
      getFromResource("web/editMap.html")
    } ~
    path("save") {
      (post & entity(as[JsObject])) { data =>
        val (point, lineStrings, polygons) = GISJsonParser.fromJson(data)
        featureService.saveWithDependencies(point, lineStrings, polygons)
        complete(StatusCodes.OK)
      }
    } ~
    path("delete") {
      get {
        featureService.deleteAll()
        complete(StatusCodes.OK)
      }
    } ~
    path("load") {
      get {
        val features = featureService.list()
        //FIXME: Сделать полную сериализацию фич в Json на стороне сервера
        complete(FeatureCollection("FeatureCollection", GISJsonParser.toSerializable(features)))
      }
    }
  }
}