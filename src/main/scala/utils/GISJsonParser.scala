package utils

import constants.ERConstants
import dao.models.{FeatureSerializable, Geometry, GeometrySerializable}
//import spray.json.{DefaultJsonProtocol, JsArray, JsObject}
import spray.json
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import dao.models.Feature
import utils._

import scala.util.Try

/**
  * Created by mbagrov on 10/8/16.
  */

object ServiceJsonFormatters extends DefaultJsonProtocol {

  implicit val geometryPolygonFormat = jsonFormat2(GeometryPolygon)
  implicit val featurePolygonFormat = jsonFormat3(FeaturePolygon)

  implicit val geometryLineStringFormat = jsonFormat2(GeometryLineString)
  implicit val featureLineStringFormat = jsonFormat3(FeatureLineString)

  implicit val geometryPointFormat = jsonFormat2(GeometryPoint)
  implicit val featurePointFormat = jsonFormat3(FeaturePoint)

  implicit val geometryFormat = jsonFormat2(GeometrySerializable)
  implicit val featureFormat = jsonFormat3(FeatureSerializable.apply)
  implicit val featureCollectionFormat = jsonFormat2(FeatureCollection)
}

object GISJsonParser {

  import ServiceJsonFormatters.{featureCollectionFormat => _, _}

  type Features = (Seq[FeaturePoint], Seq[FeatureLineString], Seq[FeaturePolygon])

  def fromJson(json: JsObject): Features = {
    json.getFields(ERConstants.GeoJSON.featuresField) match {
      case Seq(JsArray(jsObjects)) => {
        jsObjects.foldLeft[Features]((Nil, Nil, Nil)) { (accumulator, f) =>
          val points = {
            val point = Try(f.convertTo[FeaturePoint]).toOption
            point.fold(accumulator._1)(p => accumulator._1 :+ p)
          }
          val lineStrings = {
            val lineString = Try(f.convertTo[FeatureLineString]).toOption
            lineString.fold(accumulator._2)(ls => accumulator._2 :+ ls)
          }
          val polygons = {
            val polygon = Try(f.convertTo[FeaturePolygon]).toOption
            polygon.fold(accumulator._3)(p => accumulator._3 :+ p)
          }
          (points, lineStrings, polygons)
        }
      }
    }
  }

  def toSerializable(features: Seq[Feature]) = features.map(FeatureSerializable(_))
}

trait FeatureCollectionBase {

  val features: Seq[FeatureBase]
}

trait FeatureBase {

  val geometry: GeometryBase
}

trait GeometryBase {

  val `type`: String
}

case class FeaturePolygon(`type`: String, geometry: GeometryPolygon, properties: Map[String, String]) extends FeatureBase

case class GeometryPolygon(`type`: String, coordinates: Seq[Seq[Seq[Double]]]) extends GeometryBase

case class FeatureLineString(`type`: String, geometry: GeometryLineString, properties: Map[String, String]) extends FeatureBase

case class GeometryLineString(`type`: String, coordinates: Seq[Seq[Double]]) extends GeometryBase

case class FeaturePoint(`type`: String, geometry: GeometryPoint, properties: Map[String, String]) extends FeatureBase

case class GeometryPoint(`type`: String, coordinates: Seq[Double]) extends GeometryBase

case class FeatureCollection(`type`: String, features: Seq[FeatureSerializable])

