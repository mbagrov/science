import akka.http.scaladsl.model.StatusCodes
import utils._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import dao.services.{FeatureService, GISDataService}
import guice.utils.InjectHelper
import spray.json._

import scala.util.Try

trait ElectronicQueueMicroService extends BaseMicroService with StringHelper with DateHelper {

  private lazy val featureService = InjectHelper.inject[FeatureService]
  private lazy val gisDataService = InjectHelper.inject[GISDataService]
  import DefaultJsonProtocol._
  import ServiceJsonFormatters.featureCollectionFormat

  val eqRoutes = logRequestResult("science-project") {
    path("") {
      getFromResource("web/index.html")
    } ~
    pathPrefix("") {
      getFromResourceDirectory("web")
    } ~
    pathPrefix("relational") {
      path("show") {
        getFromResource("web/templates/relational.html")
      } ~
      path("save") {
        (post & entity(as[JsObject])) { data =>
          val (point, lineStrings, polygons) = GISJsonParser.fromJson(data)
          featureService.saveWithDependencies(point, lineStrings, polygons)
          complete(StatusCodes.OK)
        }
      } ~
      path("load") {
        get {
          val features = featureService.list()
          //FIXME: Сделать полную сериализацию фич в Json на стороне сервера
          FeatureCollection("FeatureCollection", GISJsonParser.toSerializable(features))
          complete(FeatureCollection("FeatureCollection", GISJsonParser.toSerializable(features)))
        }
      }
    } ~
    pathPrefix("gis") {
      path("show") {
        getFromResource("web/templates/gis.html")
      } ~
      path("save") {
        (post & entity(as[JsObject])) { data =>
          //TODO: Вынести логику в парсер
          data.getFields("features") match {
            case Seq(JsArray(jsObjects)) => {
              jsObjects.flatMap(_.asJsObject.getFields("geometry")).map(_.toString).foreach(a => gisDataService.save(a))
              complete(StatusCodes.OK)
            }
          }
        }
      } ~
      path("load") {
        get {
          val a = gisDataService.featureCollection
          complete(a)
        }
      }
    } ~
    pathPrefix("nosql") {
      path("show") {
        getFromResource("web/templates/nosql.html")
      } ~
      path("save") {
        (post & entity(as[String])) { data =>
          complete(StatusCodes.NotImplemented)
        }
      } ~
      path("load") {
        get {
//          complete(GeoDataHepler.data(dbType))
          ???
        }
      }
    } ~
    path("delete") {
      (get & parameter('dbType)) { dbType =>
        dbType match {
          case "relational" => featureService.deleteAll()
          case "gis" => gisDataService.deleteAll()
        }

        complete(StatusCodes.OK)
      }
    }/* ~
    path("load") {
      (get & parameter('dbType)) { dbType =>
        complete(GeoDataHepler.data(dbType))
      }
    }*/
  }
}