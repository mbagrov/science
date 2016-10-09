package dao.models

/**
  * Created by Aleksey Voronets on 13.02.16.
  */
trait UuidHelper {

  def randomUuid: String = java.util.UUID.randomUUID.toString.replaceAll("-", "")
}

object UuidHelper extends UuidHelper