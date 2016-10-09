package dao.services

import dao.ElectronicQueueSchema._
import dao.models.utils.EmptyExpression
import dao.models.{Feature, FeatureId, FeatureType}
import guice.utils.InjectHelper
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table
import utils.{FeatureLineString, FeaturePoint, FeaturePolygon}

trait FeatureService extends FeatureType with DefaultEntityService with UpdateEntityService {

  def delete(id: FeatureId): Int

  def saveWithDependencies(points: Seq[FeaturePoint], lineStrings: Seq[FeatureLineString], polygons: Seq[FeaturePolygon]): Unit

  def deleteAll(): Unit
}

class FeatureServiceImpl extends FeatureService with DefaultEntityServiceImpl with UpdateEntityServiceImpl {

  override protected val defaultTable: Table[Entity] = feature
  private lazy val geometryService = InjectHelper.inject[GeometryService]

  override def defaultOrdering: Ordering = ci => ci.id asc

  def delete(id: FeatureId): Int = tx {
    defaultTable.deleteWhere(cit => cit.id === id.raw)
  }

  override def saveWithDependencies(points: Seq[FeaturePoint], lineStrings: Seq[FeatureLineString],
      polygons: Seq[FeaturePolygon]): Unit = tx {

    points.map(Feature(_)).foreach{ case (f, g) => geometryService.save(g); super.save(f)}
    lineStrings.map(Feature(_)).foreach{ case (f, g) => geometryService.save(g); super.save(f)}
    polygons.map(Feature(_)).foreach{ case (f, g) => geometryService.save(g); super.save(f)}
  }

  def deleteAll(): Unit = tx {
    defaultTable.deleteWhere(e => 1 === 1)
    geometry.deleteWhere(e => 1 === 1)
  }
}
