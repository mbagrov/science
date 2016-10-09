package dao.models.squeryl

import java.sql.SQLException

import org.squeryl.dsl.QueryDsl
import org.squeryl.internals.Utils
import org.squeryl.{Session, SessionFactory}
import plugins.{ECacheApi, EhcachePlugin}

import scala.runtime.NonLocalReturnControl

/**
  * Created by Aleksey Voronets on 09.02.16.
  */
trait SquerylSession {

  import ExtendedPrimitiveTypeMode._

  def tx[P](fun: => P): P = customTransaction(fun)

  def onTxSuccess[P](fun: => P)(successFun: P => Unit): P = customTransaction(fun, Some(successFun))
}

object ExtendedPrimitiveTypeMode extends org.squeryl.PrimitiveTypeMode with ExtendedQueryDsl

trait ExtendedQueryDsl extends QueryDsl {

  def cache: ECacheApi = EhcachePlugin.cache("squeryl")

  def customTransaction[A](action: => A, successAction: Option[A => _] = None): A =
    if (!Session.hasCurrentSession) {
      executeTransaction(action, successAction)
    } else {
      val res = action
      val s = Session.currentSession
      successAction.foreach(sa => updateActionList(s.toString, TxSuccessAction(res, sa)))
      res
    }

  def updateActionList(key: String, action: TxSuccessAction[_]): Unit = {
    val actionList: Seq[TxSuccessAction[_]] = cache.getAs[Seq[TxSuccessAction[_]]](key).getOrElse(Nil)
    cache.set(key, actionList :+ action, 0)
  }

  private def executeTransaction[A](action: => A, successAction: Option[A => _]): A = {

    val s = SessionFactory.newSession

    val c = s.connection

    val originalAutoCommit = c.getAutoCommit
    if (originalAutoCommit) c.setAutoCommit(false)

    var txOk = false
    try {
      val res = using(s)(action)
      txOk = true
      successAction.foreach(sa => updateActionList(s.toString, TxSuccessAction(res, sa)))
      res
    }
    catch {
      case e: NonLocalReturnControl[_] =>
        txOk = true
        throw e
    }
    finally {
      try {
        if (txOk) {
          c.commit()
          val txSuccessActions: Seq[TxSuccessAction[_]] = cache.getAs[Seq[TxSuccessAction[_]]](s.toString).getOrElse(Nil)
          txSuccessActions.foreach(_.execute)
          cache.remove(s.toString)
        }
        else c.rollback()

        if (originalAutoCommit != c.getAutoCommit) c.setAutoCommit(originalAutoCommit)
      }
      catch {
        case e: SQLException =>
          Utils.close(c)
          if (txOk) throw e // if an exception occured b4 the commit/rollback we don't want to obscure the original exception
      }
      try {c.close()}
      catch {
        case e: SQLException =>
          if (txOk) throw e // if an exception occured b4 the close we don't want to obscure the original exception
      }
    }
  }

}

case class TxSuccessAction[A](txResult: A, successAction: A => _) {

  def execute: Any = successAction(txResult)

}
