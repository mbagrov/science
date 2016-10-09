package dao.models.utils

/**
  * Created by Aleksey Voronets on 10.02.16.
  */
case class FilterCondition(columnName: String, filterValue: String)

case class Paging(begin: Int, end: Int)