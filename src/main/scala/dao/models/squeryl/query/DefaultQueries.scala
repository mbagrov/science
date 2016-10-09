package dao.models.squeryl.query

import java.sql.Timestamp

import dao.models.TypedId
import dao.models.utils.Paging
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.Measures
import org.squeryl.dsl.ast.{EqualityExpression, LogicalBoolean, OrderByExpression, TypedExpressionNode}
import org.squeryl.{KeyedEntity, Query, Table}

/**
  * Created by Aleksey Voronets on 10.02.16.
  */
trait Aliased {

  def alias: String
}

trait TypedIdentifiable[T, +ID <: TypedId[T]] extends KeyedEntity[T] {

  def typedId: ID
}

trait UserTypedIdentifiable[T, +ID <: TypedId[T]] extends TypedIdentifiable[T, ID] {

  def typedId: ID

  def user: Long
}

trait Lifecycle extends WithMakeRemoved {

  def created: Timestamp

  def modified: Timestamp

  def removalDate: Option[Timestamp]
}

trait EntityType {

  type IdType
  type EntityId <: TypedId[IdType]
  type Entity <: TypedIdentifiable[IdType, EntityId]
}

trait UserEntityType extends EntityType {

  type IdType
  type EntityId <: TypedId[IdType]
  type Entity <: UserTypedIdentifiable[IdType, EntityId]
}

trait RemovableEntity {

  def removed: Boolean
}

trait WithMakeRemoved extends RemovableEntity {

  def makeRemoved: WithMakeRemoved
}

trait RestorableEntity extends RemovableEntity {

  def restore: RestorableEntity
}

trait WithDefaultTable extends EntityType {

  protected val defaultTable: Table[Entity]
}

trait WithIdFun extends WithDefaultTable {

  def idFun(e: Entity): TypedExpressionNode[_]
}

trait DefaultQueries extends WithDefaultTable {

  def getByIdQuery(id: EntityId): Option[Entity] = defaultTable.lookup(id.raw)

  def allQuery: Seq[Entity] = defaultTable.toSeq

  //TODO move somewhere
  def notRemoved(removable: RemovableEntity): EqualityExpression = removable.removed.~ === false.~

}

trait MutationQueries extends WithDefaultTable {

  def saveQuery(entity: Entity): EntityId = defaultTable.insert(entity).typedId

  def updateQuery(entity: Entity): Unit = defaultTable.update(entity)

  def updateQuery(entities: Iterable[Entity]): Unit = defaultTable.update(entities)
}

trait DeletionQueries extends WithDefaultTable {

  def deleteQuery(id: EntityId) {defaultTable.delete(id.raw)}
}

trait Filters extends EntityType {

  def combineFilters(filters: Seq[Filter]): Filter =
    if (filters.nonEmpty) filters.reduceLeft((l, r) => (e: Entity) => l(e) and r(e)) else defaultFilter

  type Filter = Entity => LogicalBoolean

  type Ordering = Entity => OrderByExpression

  //TODO found out how to do it better
  val defaultFilter = (entity: Entity) => 1 === 1
}

trait FilterQueries extends DefaultQueries with Filters {

  def filterQuery(filterFuns: Seq[Filter], orderFun: Ordering): Query[Entity] =
    filterQuery(combineFilters(filterFuns), orderFun)

  def filterQuery(filterFun: Filter, orderFun: Ordering): Query[Entity] =
    from(defaultTable)(s => where(filterFun(s)).select(s).orderBy(orderFun(s)))

  def defaultOrdering: Ordering
}

trait CountEntitiesQueries extends WithIdFun with Filters {

  def countQuery(filterFuns: Seq[Filter]): Query[Measures[Long]] = {
    from(defaultTable)(s => where(combineFilters(filterFuns)(s)).compute(countDistinct(idFun(s))))
  }
}

trait CountRemovableEntitiesQueries extends CountEntitiesQueries {

  type Entity <: TypedIdentifiable[IdType, EntityId] with RemovableEntity

  def removedFilter(removed: Boolean): (Entity) => EqualityExpression = (_: Entity).removed === removed

  def countNotRemovedQuery(filterFuns: Seq[Filter]): Query[Measures[Long]] = countQuery(filterFuns :+ removedFilter(false))

  def countRemovedQuery(filterFuns: Seq[Filter]): Query[Measures[Long]] = countQuery(filterFuns :+ removedFilter(true))
}

trait PagingQueries extends FilterQueries with CountEntitiesQueries with PagingQueriesHelper {

  def idFun(e: Entity): TypedExpressionNode[_]

  def filteredPageQuery(filterFuncs: Seq[Filter], orderFun: Ordering, pagingOpt: Option[Paging]): Query[Entity] =
    filteredPageQuery(combineFilters(filterFuncs), orderFun, pagingOpt)

  def filteredPageQuery(filterFuncs: Filter, orderFun: Ordering, pagingOpt: Option[Paging]): Query[Entity] = {
    val query = filterQuery(filterFuncs, orderFun)
    addOptionalPaging(query, pagingOpt)
  }
}

trait PagingQueriesHelper {

  def addPaging[T](query: Query[T], p: Paging): Query[T] = query.page(p.begin, p.end - p.begin)

  def addOptionalPaging[T](query: Query[T], p: Option[Paging]): Query[T] = p.map(addPaging(query, _)).getOrElse(query)
}