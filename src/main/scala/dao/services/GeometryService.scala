package dao.services

import dao.ElectronicQueueSchema._
import dao.models._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table

trait GeometryService extends GeometryType with DefaultEntityService with UpdateEntityService {

  def delete(id: GeometryId): Int
}

class GeometryServiceImpl extends GeometryService with DefaultEntityServiceImpl with UpdateEntityServiceImpl {

  override protected val defaultTable: Table[Entity] = geometry

  override def defaultOrdering: Ordering = ci => ci.id asc

  def delete(id: GeometryId): Int = tx {
    defaultTable.deleteWhere(cit => cit.id === id.raw)
  }
}
