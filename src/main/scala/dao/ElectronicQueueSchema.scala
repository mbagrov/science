package dao

import dao.models._
import org.squeryl.Schema

object ElectronicQueueSchema extends Schema {

  val feature = table[Feature]("Feature")
  val geometry = table[Geometry]("Geometry")
}
