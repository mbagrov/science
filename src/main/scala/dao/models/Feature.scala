package dao.models

import dao.models.squeryl.query.TypedIdentifiable
import _root_.utils.{FeatureLineString, FeaturePoint, FeaturePolygon}
import dao.services.GeometryService
import guice.utils.InjectHelper
import org.squeryl.annotations.Transient

case class Feature(
    id: String,
    `type`: String,
    geometryId: String,
    properties: String)
  extends TypedIdentifiable[String, FeatureId] {

  override def typedId: FeatureId = FeatureId(id)

  @Transient lazy val geometryService = InjectHelper.inject[GeometryService]
  @Transient lazy val geometry = geometryService.getBy(GeometryId(geometryId))
}

case class FeatureId(raw: String) extends TypedId[String]

trait FeatureType {

  type IdType = String
  type EntityId = FeatureId
  type Entity = Feature
}

object Feature extends UuidHelper {

  type FeatureWithGeometry = (Feature, Geometry)

  def apply(point: FeaturePoint): FeatureWithGeometry = {
    val geometry = Geometry(point.geometry)
    val feature = Feature(randomUuid, point.`type`, geometry.id, point.properties.toStringJson)

    (feature, geometry)
  }

  def apply(polygon: FeaturePolygon): FeatureWithGeometry = {
    val geometry = Geometry(polygon.geometry)
    val feature = Feature(randomUuid, polygon.`type`, geometry.id, polygon.properties.toStringJson)

    (feature, geometry)
  }

  def apply(lineString: FeatureLineString): FeatureWithGeometry = {
    val geometry = Geometry(lineString.geometry)
    val feature = Feature(randomUuid, lineString.`type`, geometry.id, lineString.properties.toStringJson)

    (feature, geometry)
  }

  private implicit class Map2JsonMap(map: Map[String, String]) {

    def toStringJson: String = map.map { case (k, v) =>
      val result = s"""""$k": "$v""""
      result
    }.mkString("{", ", ", "}")
  }

}

case class FeatureSerializable(`type`: String, geometry: GeometrySerializable, properties: String)

object FeatureSerializable {

  def apply(feature: Feature): FeatureSerializable = {
    import feature._; FeatureSerializable(`type`, geometry, properties)
  }

  private implicit def geometry2Serializable(geometry: Geometry): GeometrySerializable = {
    import geometry._; GeometrySerializable(`type`, coordinates)
  }
}
