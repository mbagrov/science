package utils

import spray.json.{JsString, JsonParser}
import utils.StringHelper._

import scala.util.Try

/**
  * Created by Aleksey Voronets on 16.02.16.
  */
trait MultiLanguageHelper {

  val RU_LOCALE = "RU"
  val EN_LOCALE = "EN"
  val MAIN_LOCALE = RU_LOCALE
}

object MultiLanguageHelper extends MultiLanguageHelper

trait MultiLanguageField {

  val values: Iterable[LanguageValue]

  def valueRU: Option[String] = valueByLocale(MultiLanguageHelper.RU_LOCALE)

  def valueEN: Option[String] = valueByLocale(MultiLanguageHelper.EN_LOCALE)

  def valueByLocale(locale: String): Option[String] = values.find(_.languageCode == locale).flatMap(lv => trimToOption(lv.value))

  def value: String = valueByLocale(MultiLanguageHelper.RU_LOCALE).getOrElse(defaultValue)

  def defaultValue: String = defaultValueMaybe.getOrElse("")

  def defaultValueMaybe: Option[String] =
    values.find(lv => lv.languageCode == MultiLanguageHelper.RU_LOCALE).map(_.value)
}

case class MultiLanguageJsonField(json: String, checkCorrectness: Boolean = false) extends MultiLanguageField {

  /*
  * тут нужно парсить json
  * если не удалось распарсить, считаем русским названием
  * */
  override val values: Iterable[LanguageValue] = trimToOption(json).fold(Seq.empty[LanguageValue]) { json =>
    def parseValues = JsonParser(json).asJsObject.fields.map { case (attr, JsString(value)) =>
      LanguageValue(attr, value)
    }.toList

    if (checkCorrectness) parseValues
    else Try(parseValues).toOption.getOrElse(Seq(LanguageValue(MultiLanguageHelper.RU_LOCALE, json)))
  }

  override def toString: String = defaultValue
}

case class MultiLanguageFieldWrapper(objectId: String, fieldName: String, values: Seq[LanguageValue])
  extends MultiLanguageField

case class LanguageValue(languageCode: String, value: String)
