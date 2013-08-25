package controllers

import play.api.mvc._
import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession
import play.api.data._
import play.api.data.Forms._

object Accounts extends Table[(String, String)]("ACCOUNTS") {
  def name = column[String]("NAME", O.PrimaryKey)
  def password = column[String]("PASSWORD")
  def * = name ~ password
}

object Application extends Controller {

  def accountConnection = Database.forURL(
    "jdbc:mysql://localhost/test1?user=root&password=",
    driver = "com.mysql.jdbc.Driver"
  )

  def index = Action { request =>
    Ok(views.html.index(request.session.get("name")))
  }

  def sign_up = Action {
    Ok(views.html.sign_up())
  }

  private[this] val accountForm = Form(
    tuple("name" -> text, "password" -> text)
  )

  /**
   * TODO: PUT化するべき
   * 実質PUTとして扱う
   */
  def login = Action { implicit request =>
    val (name, password) = accountForm.bindFromRequest.get

    // TODO: トランザクション処理がまったくない
    // トランザクション内でSELECT & INSERTするよう修正するべき
    accountConnection withSession {
      val result = ( for(a <- Accounts; if a.name === name && a.password === password) yield a.name ).list

      if (result.length == 0) {
        Ok("name: " + name + "\npassword: " + password).withSession(
          session + ("name" -> name)
        )
      } else {
        Forbidden("that name is already existed")
      }
    }
  }

  /**
   * TODO: PUT化するべき
   * 実質PUTとして扱う
   */
  def account = Action { implicit request =>
    val (name, password) = accountForm.bindFromRequest.get

    // TODO: トランザクション処理がまったくない
    // トランザクション内でSELECT & INSERTするよう修正するべき
    accountConnection withSession {
      val result = ( for(a <- Accounts; if a.name === name) yield a.name ).list

      println(result.toString)

      if (result.length == 0) {
        Accounts.insert(name, password)
        Ok("name: " + name + "\npassword: " + password)
      } else {
        Forbidden("that name is already existed")
      }
    }
  }
}