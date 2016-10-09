package utils

import scala.util.Try

/**
  * Created by Aleksey Voronets on 11.02.16.
  */

// scalastyle:off magic.number
// scalastyle:off method.name
// scalastyle:off null
object StringHelper extends StringHelper

trait StringHelper {

    val DefaultZeroNumber = 4

    def nonEmptySafe(os: Option[String]): Boolean = os.exists(_.trim.nonEmpty)

    def isEmptySafe(s: String): Boolean = !nonEmptySafe(s)

    def nonEmptySafe(s: String): Boolean = Option(s).exists(_.trim.nonEmpty)

    def takeSafe(s: String, l: Int): Option[String] = trimToOption(s).map(_.take(l))

    def padZeroes(number: Int, zeros: Int = DefaultZeroNumber): String = padZeroes(number.toString, zeros)

    def padZeroes(str: String, zeros: Int): String = "0" * (zeros - str.length) + str

    def removeMultiSpaces(str: String): String = str.replaceAll("\\s+", " ")

    def decapitalize(str: String): String = {
        val (firstLetter, other) = str.splitAt(1)
        firstLetter.toLowerCase ++ other
    }

    def hash(str: String): Int = (0 until str.length).foldLeft(7)((acc, indx) => acc * 31 + str.charAt(indx))

    def takeWithDots(s: String, l: Int): String = trimToOption(s).map { trimmed =>
        if (trimmed.length > 3 && trimmed.length > l) trimmed.take(l).substring(0, l - 3) + "..."
        else trimmed
    }.getOrElse("")

    def trimToOption(s: String): Option[String] = trimToOption(Option(s))

    def trimToOption(opt: Option[String]): Option[String] = opt.map(_.trim).filter(
        str => str.nonEmpty && str != "undefined")

    def trimSafe(s: String): String = stripToEmpty(s).trim

    def stripToEmpty(s: String): String = if (s == null) "" else s

    def handleIfNotNull(str: String, fn: String => String,
        default: String = ""): String = if (str != null) fn(str) else default

    implicit def toLabeled[T](o: T)(implicit formatter: LabelFormatter[T]): Labeled = new Labeled() {
        def label = formatter.label(o)
    }

    implicit def toStringHelperWrapper(obj1: AnyRef): AnyRef = new {
        def +?+(obj2: AnyRef) = withDelimiter(obj1, obj2, " ")
    }

    def withDelimiter(obj1: AnyRef, obj2: AnyRef,
        delimiter: String): String = Seq(obj1, obj2).flatMap(toStringSafe).mkString(delimiter)

    def toStringSafe(o: Any): Option[String] = Option(o).map(_.toString)

    def externalUrl(url: String): String = {
        val urlPattern = "^(http|https|news|ftp)://.+".r
        url match {
            case urlPattern(u) => url
            case _ => "http://" + url
        }
    }

    def coordinateWithNumeral(k: Int, form1: String, form2: String,
        form5: String): String = (Math.abs(k) % 100, Math.abs(k) % 10) match {
        case (n, _) if n > 10 && n < 20 => form5
        case (_, n) if n > 1 && n < 5 => form2
        case (_, 1) => form1
        case (_, _) => form5
    }

    def levenshteinDistance(s1: String, s2: String): Int = {
        def minimum(i1: Int, i2: Int, i3: Int) = math.min(math.min(i1, i2), i3)

        val dist = Array.tabulate(s2.length + 1, s1.length + 1) { (j, i) => if (j == 0) i else if (i == 0) j else 0 }

        for (j <- 1 to s2.length; i <- 1 to s1.length) {
            dist(j)(i) = {
                if (s2(j - 1) == s1(i - 1)) dist(j - 1)(i - 1)
                else minimum(dist(j - 1)(i) + 1, dist(j)(i - 1) + 1, dist(j - 1)(i - 1) + 1)
            }
        }

        dist(s2.length)(s1.length)
    }

    trait Labeled {

        def label: String
    }
}

trait BooleanHelper {

    implicit def attachOrFalse(boolOpt: Option[Boolean]): AnyRef = new {
        def orFalse = boolOpt.getOrElse(false)
    }

    def parseBool(str: String): Option[Boolean] = str match {
        case "true" => Some(true)
        case "false" => Some(false)
        case _ => None
    }
}

trait LongHelper {

    def parseLong(str: String): Option[Long] = {
        try Some(str.toLong)
        catch {case e: Exception => None}
    }
}

object DoubleHelper extends DoubleHelper

trait DoubleHelper {

    def parseDouble(str: String): Option[Double] = {
        try Some(str.replaceAll(",", ".").toDouble)
        catch {case e: Exception => None}
    }

    def doubleToString(d: Double): String = if (d.toInt == d) d.toInt.toString else d.toString
}

trait LabelFormatter[T] {

    def label(o: T): String
}

object ArrayHelper extends ArrayHelper

trait ArrayHelper {

    def getArrayItem[T](array: Array[T])(index: Int)(default: T): T = Try(array(index)).toOption.getOrElse(default)
}

object OptionHelper extends OptionHelper

trait OptionHelper {

    implicit def strToOpt(str: String): Option[String] = Some(str)
}
