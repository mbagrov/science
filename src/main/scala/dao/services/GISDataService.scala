package dao.services

import java.sql.DriverManager

import utils.IOUtils._
import com.typesafe.config.{Config, ConfigFactory}
import dao.models.{Feature, UuidHelper}
import utils.FeatureCollection

/**
  * Created by mbagrov on 10/16/16.
  */
trait GISDataService {

  def save(data: String): Unit

  def featureCollection: String

  def deleteAll(): Unit
}

class GisDataServiceImpl extends GISDataService {

  def config: Config = ConfigFactory.load()

  private lazy val importConnectionRoute = config.getString("db.default.url")
  private lazy val importUser = config.getString("db.default.user")
  private lazy val importPass = config.getString("db.default.password")
  private lazy val connection = DriverManager.getConnection(importConnectionRoute, importUser, importPass)

  def save(data: String): Unit =
    ensureClose(connection.prepareStatement(savePreparedStatement)){ ps => ps.setString(1, data); ps.execute() }

  def featureCollection: String = {
    ensureClose(connection.createStatement) { s =>
      val rs = s.executeQuery(listStatement)
      val result = for (_ <- 0 to rs.getFetchSize) yield { rs.next(); rs.getString(1)}
      result.mkString
    }
  }

  def deleteAll(): Unit = ensureClose(connection.createStatement) { s => s.execute(deleteAllStatement)}

  private val tableName = "GisTable"

  private val savePreparedStatement =
    s"""INSERT INTO "$tableName" VALUES ('${UuidHelper.randomUuid}', ST_SetSRID(ST_GeomFromGeoJSON(?), 4326));"""

  private val listStatement = {
    s"""SELECT row_to_json(fc)
      | FROM (SELECT 'FeatureCollection' AS type, array_to_json(array_agg(f)) AS features
      | FROM (SELECT 'Feature' AS type,
      |   ST_AsGeoJSON(lg.feature)::json AS geometry,
      |   row_to_json(lp) AS properties
      |FROM "$tableName" AS lg
      |INNER JOIN (SELECT id, feature FROM "$tableName") AS lp
      |  ON lg.id = lp.id) AS f) AS fc;""".stripMargin
  }

  private val deleteAllStatement = s"""DELETE FROM "$tableName";"""
}
