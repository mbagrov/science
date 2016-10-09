package dao.models.utils

import dao.models.squeryl.sorting.ColumnSorting

import scala.collection.immutable
import scala.collection.immutable.Seq

/**
  * Created by Aleksey Voronets on 10.02.16.
  */
trait SortingHelper[T] {

  def defaultSorting: ColumnSorting[T]

  def sortingList: immutable.Seq[ColumnSorting[T]]

  private def getSortWithDirection(sort: ColumnSorting[T], direction: String) =
    if (sort.direction == direction) sort else sort.oppositeDirection


  def getSortFromString(sorting: String, direction: String): ColumnSorting[T] = {
    val sort = sortingList.find(_.title == sorting).getOrElse(defaultSorting)
    direction match {
      case "asc" => getSortWithDirection(sort, "asc")
      case "desc" => getSortWithDirection(sort, "desc")
      case _ => sort
    }
  }

  def getMultipleSortsFromString(sorting: String, direction: String): Seq[ColumnSorting[T]] = {
    val sort = sortingList.filter(_.title == sorting)
    if (sort.isEmpty) sortingList.filter(_.title == defaultSorting.title)
    else {
      val result = direction match {
        case "asc" => sort.map(getSortWithDirection(_, "asc"))
        case "desc" => sort.map(getSortWithDirection(_, "desc"))
        case _ => sort
      }
      result :+ defaultSorting
    }
  }
}