package dao.models

import dao.models.squeryl.query.TypedIdentifiable
import _root_.utils.{GeometryPoint, GeometryLineString, GeometryPolygon}

case class Geometry(
    id: String,
    `type`: String,
    coordinates: String)
  extends TypedIdentifiable[String, GeometryId] {

  override def typedId: GeometryId = GeometryId(id)
}

case class GeometryId(raw: String) extends TypedId[String]

trait GeometryType {

  type IdType = String
  type EntityId = GeometryId
  type Entity = Geometry
}

object Geometry extends UuidHelper {

  def apply(geometry: GeometryPoint): Geometry =
    Geometry(randomUuid, geometry.`type`, geometry.coordinates.map(_.toString).toStringJson)

  def apply(geometry: GeometryLineString): Geometry =
    Geometry(randomUuid, geometry.`type`, geometry.coordinates.map(_.map(_.toString).toStringJson).toStringJson)

  def apply(geometry: GeometryPolygon): Geometry =
    Geometry(randomUuid, geometry.`type`, geometry.coordinates.map(_.map(_.map(_.toString).toStringJson).toStringJson).toStringJson)

  private implicit class Seq2JsonString(seq: Seq[String]) {

    def toStringJson: String = seq.mkString("[", ", ", "]")
  }

}

case class GeometrySerializable(`type`: String, coordinates: String)