package dao.models

/**
  * Created by Aleksey Voronets on 10.02.16.
  */
trait TypedId[T] {

  def raw: T
}
