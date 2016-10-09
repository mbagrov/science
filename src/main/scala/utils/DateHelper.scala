package utils

import java.sql.Timestamp
import java.text.{DateFormatSymbols, SimpleDateFormat}
import java.util._

import org.apache.commons.lang3.time.{DateFormatUtils, DateUtils}
import org.joda.time._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}


// scalastyle:off magic.number
// scalastyle:off method.name
// scalastyle:off null
object DateHelper extends DateHelper with DateFormatFunctions with DateArithmeticFunctions

trait DateHelper {

  final val MinutesInSecs = 1000 * 60
  final val HoursInSecs = MinutesInSecs * 60
  final val DaysInSecs = HoursInSecs * 24
  private val timeFormat = new SimpleDateFormat("HH:mm")

  def inLast24Hours(d: Date): Boolean = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.HOUR, -24)
    val activationCalendar = Calendar.getInstance()
    activationCalendar.setTime(d)
    calendar.before(activationCalendar)
  }

  def createCalendar(date: Date): Calendar = {
    val calendar: Calendar = new GregorianCalendar()
    calendar.setTime(date)
    calendar.setFirstDayOfWeek(Calendar.MONDAY)
    calendar
  }

  def datetime(date: Date): DateTime = new DateTime(date)

  def datetime(date: Timestamp): DateTime = new DateTime(date)

  def timestamp(d: Date): Timestamp = new Timestamp(d.getTime)

  def timestamp(date: DateTime): Timestamp = new Timestamp(date.toDate.getTime)

  def currentYear: Int = LocalDate.now().getYear

  def now: Timestamp = new Timestamp(new Date().getTime)

  def tomorrow: Timestamp = new Timestamp(DateTime.now.plusDays(1).toDate.getTime)

  def yesterday: Timestamp = new Timestamp(DateTime.now.minusDays(1).toDate.getTime)

  def isSameDay(date1: Date, date2: Date): Boolean = DateUtils.isSameDay(date1, date2)

  def localTime(time: Timestamp): LocalTime = new LocalTime(timeFormat.format(time))

  def isStartOfDay(time: LocalTime): Boolean = time.compareTo(new LocalTime(0, DateTimeZone.UTC)) == 0

  def moscowTimeZone: DateTimeZone = DateTimeZone.forID("Europe/Moscow")

  def startOfDay(time: Timestamp): Timestamp = timestamp(new DateTime(time).withTimeAtStartOfDay)
}

trait DateFormatFunctions {
  Self: DateHelper =>

  private lazy val standardDateFormat = DateTimeFormat.forPattern("dd.MM.yyyy")
  private lazy val fileDateTimeFormat = DateTimeFormat.forPattern("dd MMMM yyyy г. в HH:mm")
  private lazy val standartTimeFormat = DateTimeFormat.forPattern("HH:mm")
  private lazy val dateTimeFormatterMoscow = {
    val format = new SimpleDateFormat("dd.MM.yyyy HH:mm МСК")
    format.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
    format
  }
  private lazy val standardDateTimeFormat = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm")
  private lazy val birthdayFormat = createMonthsDateFormat("dd MMMM yyyy г.")
  private lazy val dayMonthDateFormat = createMonthsDateFormat("dd MMMM")
  private lazy val monthDatName = Array("января", "февраля", "марта", "апреля", "мая", "июня", "июля",
    "августа", "сентября", "октября", "ноября", "декабря")

  def formatDateTimeMoscow(timeString: Timestamp): String = dateTimeFormatterMoscow.format(timeString)

  def formatTime(dateTime: Timestamp, zone: TimeZone = TimeZone.getDefault): String = {
    standartTimeFormat.print(new DateTime(dateTime, DateTimeZone.forTimeZone(zone)))
  }

  def formatFileDateTime(dateTime: Timestamp, zone: TimeZone = TimeZone.getDefault): String = {
    fileDateTimeFormat.print(new DateTime(dateTime, DateTimeZone.forTimeZone(zone)))
  }

  def parseDate(dateStr: String, dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy")): Date = {
    dateFormat.parseLocalDateTime(dateStr).toDate
  }

  def parseTime(timeStr: String, dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")): LocalTime =
    dateFormat.parseLocalTime(timeStr)

  def formatDate(date: Date, zone: TimeZone = TimeZone.getDefault): String = {
    standardDateFormat.print(new DateTime(date, DateTimeZone.forTimeZone(zone)))
  }

  // Йода падает когда время попадает на летний/зимний переход времени.
  // Поэтому сделано на SimpleDateFormat. Он добовляет переход автоматом.
  def parseDateTime(dateTimeStr: String, zone: TimeZone = TimeZone.getDefault,
      dateFormat: SimpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm")): Timestamp = {
    dateFormat.setTimeZone(zone)
    new Timestamp(dateFormat.parse(dateTimeStr).getTime)
  }

  def formatDateTime(dateTime: Timestamp, zone: TimeZone = TimeZone.getDefault,
      format: DateTimeFormatter = standardDateTimeFormat): String = {
    format.print(new DateTime(dateTime, DateTimeZone.forTimeZone(zone)))
  }

  def ddMMMM(date: Date): String = format(dayMonthDateFormat, date)

  private def format(formatter: SimpleDateFormat, date: Date) = if (date == null) "" else formatter.format(date)

  def ddMMyyyy(date: Date): String = DateFormatUtils.format(date, "dd.MM.yyyy")

  def ddMMyy(date: Date): String = DateFormatUtils.format(date, "dd.MM.yy")

  def yyMMdd(date: Date): String = DateFormatUtils.format(date, "yy.MM.dd")

  def ddMMyyyyHHmm(date: Date): String = DateFormatUtils.format(date, "dd.MM.yyyy HH:mm")

  def ddMMMMyyyyDoc(date: Date): String =
    DateFormatUtils.format(date, s"«dd» ${monthDatName(createCalendar(date).get(Calendar.MONTH))} yyyy г.")

  def HHmm(date: Date): String = DateFormatUtils.format(date, "HH:mm")

  private val dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm")

  def formatDateTimeNoTimezone(timeString: Timestamp, formatter: SimpleDateFormat = dateTimeFormatter): String =
    formatter.format(timeString)

  def formatBirthday(date: Date): String = ddMMMMyyyy(date)

  def ddMMMMyyyy(date: Date): String = format(birthdayFormat, date)

  def formatWithoutTimeZone(date: Date): String = date.toString

  def formatYear(date: Date): String = if (date == null) "" else createCalendar(date).get(Calendar.YEAR).toString

  def formatDay(date: Date): String = format(dayFormat, date)

  private def dayFormat = new SimpleDateFormat("dd")

  def formatMonth(date: Date): String = format(monthFormat, date)

  private def monthFormat = new SimpleDateFormat("MMMM")

  def formatTimeInterval(msec: Long): String = {
    val days = msec / DaysInSecs
    val hours = msec / HoursInSecs - days * 24
    val minutes = msec / MinutesInSecs - hours * 60 - days * 24 * 60
    val seconds = msec / 1000 - minutes * 60 - hours * 3600 - days * 24 * 3600

    "%d дней %d часов %02d минут %02d секунд".format(days, hours, minutes, seconds)
  }

  def nowMoscow: Timestamp = {
    val ddMMyyyyHHmmssSSS = "dd.MM.yyyy HH:mm:ss.SSS"
    val nowDateTimeStr = DateTimeFormat.forPattern(ddMMyyyyHHmmssSSS).print(new DateTime(moscowTimeZone))
    parseDateTime(nowDateTimeStr, dateFormat = new SimpleDateFormat(ddMMyyyyHHmmssSSS))
  }

  private def createMonthsDateFormat(formatString: String) = {
    val format = new SimpleDateFormat(formatString)
    val symbols = new DateFormatSymbols()
    symbols.setMonths(monthDatName)
    format.setDateFormatSymbols(symbols)
    format
  }
}

trait DateArithmeticFunctions {
  Self: DateHelper =>

  /**
    * Разница в месяцах, между date1 и date2.
    */
  def getDiffInMonths(date1: Date, date2: Date): Int = math.abs(Months.monthsBetween(
    new DateTime(date1.getTime).withDayOfMonth(1),
    new DateTime(date2.getTime).withDayOfMonth(1)).getMonths)

  def getDiffInDays(date1: Date, date2: Date): Int =
    Days.daysBetween(new DateTime(date1).toDateMidnight(), new DateTime(date2).toDateMidnight()).getDays()

  def diffInMinutes(date1: Date, date2: Date): Int = math.abs(Minutes.minutesBetween(
    new DateTime(date1.getTime),
    new DateTime(date2.getTime)).getMinutes)

  def plusDays(date: Date, days: Int): Timestamp = timestamp(datetime(date).plusDays(days))

  def plusDays(date: Timestamp, days: Int): Timestamp = timestamp(datetime(date).plusDays(days))

  def minutesAgo(minutes: Int): Date = {
    val now = new DateTime()
    val r = now.minus(Minutes.minutes(minutes))
    r.toDate
  }

  def secondsAgo(seconds: Int): Date = new DateTime().minus(Seconds.seconds(seconds)).toDate

  def tsHourBefore: Timestamp = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.HOUR, -1)
    new Timestamp(calendar.getTimeInMillis)
  }

  def tsDayBefore: Timestamp = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DATE, -1)
    new Timestamp(calendar.getTimeInMillis)
  }

  def floor(date: Date): Timestamp = {
    val calendar = createCalendar(date)
    calendar.set(Calendar.HOUR, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    new Timestamp(calendar.getTimeInMillis)
  }

  def ceiling(date: Date): Timestamp = {
    val calendar = createCalendar(date)
    calendar.set(Calendar.HOUR, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)

    new Timestamp(calendar.getTimeInMillis)
  }

  def curDateYearsBefore(years: Int): Date = {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -years)
    calendar.getTime
  }

  def absBeginByYear(year: Int): DateTime = new DateTime(year, 1, 1, 0, 0, 0, 0)

  def absEndByYear(year: Int): DateTime = new DateTime(year, 12, 31, 23, 59, 59, 999)

  def daysOfWeekNumsWithSundayEqSeven(days: Seq[Int]): Seq[Int] = days.map(d => if (d == 0) 7 else d)

  //возращает задержку в миллисекундах
  def fromNowBeforeTimeInMillis(time: LocalTime, timeZone: TimeZone = TimeZone.getDefault): Int = {
    val curTime = LocalTime.now(DateTimeZone.forTimeZone(timeZone))

    if (curTime.isAfter(time)) DaysInSecs - curTime.getMillisOfDay + time.getMillisOfDay
    else time.getMillisOfDay - curTime.getMillisOfDay
  }

  def max(d1: DateTime, d2: DateTime): DateTime = if(d1.isAfter(d2)) d1 else d2
}