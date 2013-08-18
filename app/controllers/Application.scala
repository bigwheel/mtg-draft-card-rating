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

  def index = Action {
    val names = accountConnection withSession {
      ( for( c <- Accounts; if c.name.length < 5 ) yield c.name ).list
      // or
      //Account.filter(_.name.length < 5).map(_.name).list
    }

    Ok("ok" + names.toString())
  }

  def sign_up = Action {
    Ok(views.html.sign_up())
  }

  /**
   * TODO: PUT化するべき
   * 実質PUTとして扱う
   */
  def account = Action { implicit request =>
    val loginForm = Form(
      tuple("name" -> text, "password" -> text)
    )

    // TODO: トランザクション処理がまったくない
    // トランザクション内でSELECT & INSERTするよう修正するべき
    val (name, password) = loginForm.bindFromRequest.get

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