package dao.models.utils

import org.squeryl.dsl.ast.LogicalBoolean

/**
  * Created by Aleksey Voronets on 10.02.16.
  */
trait ColumnFiltering[T] {

  val title: String
  val predicate: PartialFunction[(T, String), LogicalBoolean]
}

case class ColumnFilteringDefault[T](title: String, predicate: PartialFunction[(T, String), LogicalBoolean])
  extends ColumnFiltering[T]
