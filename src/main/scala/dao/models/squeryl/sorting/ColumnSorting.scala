package dao.models.squeryl.sorting

import java.util.Date

import org.squeryl.PrimitiveTypeMode
import org.squeryl.dsl.StringExpression
import org.squeryl.dsl.ast.OrderByExpression

/**
  * Created by Aleksey Voronets on 10.02.16.
  */
trait ColumnSorting[T] {

  type ColumnType

  def ascending: Boolean

  def column: (T => ColumnType)

  def title: String

  def direction: String = if (ascending) "asc" else "desc"

  def oppositeDirection: ColumnSorting[T]

  def orderingQuery: (T => OrderByExpression)

  def nextSorting(sorting: ColumnSorting[T]): ColumnSorting[T] =
    if (title == sorting.title) sorting.oppositeDirection else this
}

case class IntColumnSorting[T](column: (T => Int), ascending: Boolean, title: String) extends ColumnSorting[T] {

  type ColumnType = Int

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) = if (ascending) (entity: T) => column(entity).asc
  else (entity: T) => column(entity).desc

  def oppositeDirection: IntColumnSorting[T] = copy(ascending = !ascending)
}

case class IntOptionColumnSorting[T](column: (T => Option[Int]), ascending: Boolean, title: String)
  extends ColumnSorting[T] {

  type ColumnType = Option[Int]

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) = if (ascending) (entity: T) => column(entity).asc
  else (entity: T) => column(entity).desc

  def oppositeDirection: IntOptionColumnSorting[T] = copy(ascending = !ascending)
}

case class SumColumnSorting[T](column: (T => Int), ascending: Boolean, title: String) extends ColumnSorting[T] {

  type ColumnType = Int

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) =
    if (ascending) (entity: T) => sum(column(entity)).asc else (entity: T) => sum(column(entity)).desc

  def oppositeDirection: SumColumnSorting[T] = copy(ascending = !ascending)
}

case class FloatColumnSorting[T](column: (T => Double), ascending: Boolean, title: String) extends ColumnSorting[T] {

  type ColumnType = Double

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) = if (ascending) (entity: T) => column(entity).asc
  else (entity: T) => column(entity).desc

  def oppositeDirection: FloatColumnSorting[T] = copy(ascending = !ascending)
}

case class DateColumnSorting[T](column: (T => Date), ascending: Boolean, title: String) extends ColumnSorting[T] {

  type ColumnType = Date

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) = if (ascending) (entity: T) => column(entity).asc
  else (entity: T) => column(entity).desc

  def oppositeDirection: DateColumnSorting[T] = copy(ascending = !ascending)
}

case class StringColumnSorting[T](column: (T => String), ascending: Boolean, title: String) extends ColumnSorting[T] {

  type ColumnType = String

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) = if (ascending) (entity: T) => column(entity).asc
  else (entity: T) => column(entity).desc

  def oppositeDirection: StringColumnSorting[T] = copy(ascending = !ascending)
}

case class StringOptionColumnSorting[T](column: (T => Option[String]), ascending: Boolean, title: String)
  extends ColumnSorting[T] {

  type ColumnType = Option[String]

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) = if (ascending) (entity: T) => column(entity).asc
  else (entity: T) => column(entity).desc

  def oppositeDirection: StringOptionColumnSorting[T] = copy(ascending = !ascending)
}

case class BooleanColumnSorting[T](column: (T => Boolean), ascending: Boolean, title: String) extends ColumnSorting[T] {

  type ColumnType = Boolean

  import org.squeryl.PrimitiveTypeMode._

  def orderingQuery: (T => OrderByExpression) = if (ascending) (entity: T) => column(entity).asc
  else (entity: T) => column(entity).desc

  def oppositeDirection: BooleanColumnSorting[T] = copy(ascending = !ascending)
}

case class StringExpressionOptionColumnSorting[T](column: (T => StringExpression[Option[PrimitiveTypeMode.StringType]]),
    ascending: Boolean, title: String) extends ColumnSorting[T] {

  import PrimitiveTypeMode._

  type ColumnType = StringExpression[Option[PrimitiveTypeMode.StringType]]

  def orderingQuery: (T => OrderByExpression) =
    if (ascending) (entity: T) => column(entity).asc else (entity: T) => column(entity).desc

  def oppositeDirection: StringExpressionOptionColumnSorting[T] = copy(ascending = !ascending)
}
