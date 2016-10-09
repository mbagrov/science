package dao.models.utils

// scalastyle:off magic.number
object WeekDays extends Enumeration {

  val monday = Value("понедельник")
  val tuesday = Value("вторник")
  val wednesday = Value("среда")
  val thursday = Value("четверг")
  val friday = Value("пятница")
  val saturday = Value("суббота")
  val sunday = Value("воскресенье")

  val intToWeekDay = Map(1 -> monday, 2 -> tuesday, 3 -> wednesday, 4 -> thursday, 5 -> friday, 6 -> saturday, 7 -> sunday)
  val weekDayToInt = intToWeekDay.map { case (key, value) => value -> key }
}

object WeekDaysNumbers {

  import WeekDays._

  def weekDayWithNumber(code: WeekDays.Value): WeekDay = code match {
    case `monday` => WeekDay(monday, 1)
    case `tuesday` => WeekDay(tuesday, 2)
    case `wednesday` => WeekDay(wednesday, 3)
    case `thursday` => WeekDay(thursday, 4)
    case `friday` => WeekDay(friday, 5)
    case `saturday` => WeekDay(saturday, 6)
    case `sunday` => WeekDay(sunday, 0)
  }

  val wholeWeekDayNumbers = (0 to 6).toList
}

case class WeekDay(name: WeekDays.Value, number: Int)
