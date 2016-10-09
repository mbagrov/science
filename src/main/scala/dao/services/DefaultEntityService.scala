package dao.services

import dao.models.squeryl.SquerylSession
import dao.models.squeryl.query._
import org.squeryl.PrimitiveTypeMode._

import scala.collection.immutable
import scala.collection.immutable.Seq

/**
  * Created by Aleksey Voronets on 13.02.16.
  */
trait DefaultEntityService extends EntityType with FilterQueries {

  def getBy(id: EntityId): Entity

  def findBy(id: EntityId): Option[Entity]

  def findBy(id: Option[EntityId]): Option[Entity] = id.flatMap(findBy)

  def entities(ids: immutable.Seq[EntityId]): immutable.Seq[Entity]

  def list(filters: Filter*): immutable.Seq[Entity]
}

trait DefaultEntityServiceImpl extends DefaultEntityService with DefaultQueries {

  def getBy(id: EntityId): Entity = findBy(id).getOrElse(throw new Exception("not found " + id))

  def findBy(id: EntityId): Option[Entity] = tx(getByIdQuery(id).headOption)

  def tx[P](fun: => P): P

  //inefficient, TODO find out how to use 'in' condition polymorphically
  def entities(ids: immutable.Seq[EntityId]): Seq[Entity] = ids.flatMap(findBy)

  def list(filters: Filter*): Seq[Entity] = tx {filterQuery(filters.toSeq, defaultOrdering).toList}

  final protected def find(filters: Filter*) = tx {filterQuery(filters.toSeq, defaultOrdering).headOption}

  def defaultOrdering: Ordering = throw new RuntimeException("not implemented")
}


trait UpdateEntityService extends EntityType with Filters {

  def save(entity: Entity): Entity

  def updateEntity(entity: Entity)
}

trait UpdateEntityServiceImpl extends UpdateEntityService with MutationQueries with SquerylSession {

  def save(entity: Entity): Entity = tx {saveQuery(entity); entity}

  def updateEntity(entity: Entity): Unit = tx {updateQuery(entity)}

  def updateEntities(entities: Iterable[Entity]): Unit = tx {updateQuery(entities)}
}

trait DeletionEntityService extends EntityType with Filters {

  def delete(id: EntityId)

  def delete(entity: Entity)

  def delete(filters: Filter*)

  def clearTable()
}

trait DeletionEntityServiceImpl extends DeletionEntityService with DeletionQueries with SquerylSession {

  def delete(id: EntityId): Unit = tx {deleteQuery(id)}

  def delete(entity: Entity): Unit = delete(entity.typedId)

  def delete(filters: Filter*): Unit = tx {defaultTable.deleteWhere(combineFilters(filters.toSeq))}

  def clearTable(): Unit = tx {defaultTable.deleteWhere(e => 1 === 1)}
}