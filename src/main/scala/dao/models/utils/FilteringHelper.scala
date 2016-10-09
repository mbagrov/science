package dao.models.utils

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.ast.LogicalBoolean
import org.squeryl.internals.StatementWriter

/**
  * Created by Aleksey Voronets on 10.02.16.
  */
trait FilteringHelper[T] {

  def filterTypesList: Seq[ColumnFiltering[T]]

  def getPredicate(t: T, filters: Seq[FilterCondition] = Nil): LogicalBoolean = {
    val result = filters.map { case (condition) => filterTypesList
      .filter(_.title == condition.columnName)
      .map(_.predicate(t, condition.filterValue))
      .reduceLeftOption { (l, r) => l or r }
    }
    result.flatten.reduceLeftOption { (ll, rr) => ll and rr }.getOrElse(1 === 1)
  }

  protected def columnFilter(filterName: String)(predicate: PartialFunction[(T, String), LogicalBoolean]) =
    ColumnFilteringDefault[T](filterName, predicate)

  protected def isNullFilter(filterName: String)(f: T => String) =
    columnFilter(filterName) { case (data, _) => f(data) isNull }

  protected def isNullOptFilter(filterName: String)(f: T => Option[String]) =
    columnFilter(filterName) { case (data, _) => f(data) isNull }

  protected def boolToIntFilter(filterName: String)(f: T => Int) = columnFilter(filterName) { case (data, filter) =>
    filter match {
      case "true" => f(data) === 1
      case "false" => f(data) === 0
      case _ => EmptyExpression
    }
  }

  protected def strFilter(filterName: String)(f: T => String) =
    columnFilter(filterName) { case (data, filter) => f(data) === filter }

  protected def strOptFilter(filterName: String)(f: T => Option[String]) =
    columnFilter(filterName) { case (data, filter) => f(data) === Some(filter) }

  protected def strLikeFilter(filterName: String)(f: T => String) =
    columnFilter(filterName) { case (data, filter) => f(data) like s"%$filter%" }
}

/**
  * Заглушка, чтобы не писать (1 === 1).inhibitWhen(inhibited = true)
  */
object EmptyExpression extends LogicalBoolean {

  def doWrite(sw: StatementWriter) {}

  override def inhibited: Boolean = true
}
