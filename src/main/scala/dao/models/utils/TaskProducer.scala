package dao.models.utils

/**
  * Created by mbagrov on 7/4/16.
  */
sealed trait TaskProducer

case object EQUser extends TaskProducer

case object EQUserRon extends TaskProducer

case object EQTimeout extends TaskProducer
