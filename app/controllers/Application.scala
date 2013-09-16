package controllers

import play.api.mvc._
import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play._
import org.mindrot.jbcrypt.BCrypt
import play.api.Logger

object Accounts extends Table[(String, String)]("ACCOUNTS") {
  def name = column[String]("NAME", O.PrimaryKey)
  def password = column[String]("PASSWORD")
  def * = name ~ password
}

object Application extends Controller {

  def accountConnection = Database.forDataSource(DB.getDataSource("default"))

  private[this] val accountForm = Form(
    tuple("name" -> text, "password" -> text)
  )

  def index = Action { request =>
    Ok(views.html.index(request.session.get("name"), accountForm.fill(("abc", ""))))
  }

  def sign_up = Action {
    Ok(views.html.sign_up(accountForm.fill(("abc", ""))))
  }

  def login = Action { implicit request =>
    val (name, plainPassword) = accountForm.bindFromRequest.get

    val result = accountConnection withSession {
      ( for(a <- Accounts; if a.name === name) yield a.password ).list
    }

    if (result.length == 0 || !BCrypt.checkpw(plainPassword, result(0))) {
      Forbidden("アカウント名またはパスワードが違います")
    } else {
      Ok("name: " + name + "\npassword: " + plainPassword).withSession(
        session + ("name" -> name)
      )
    }
  }

  /**
   * TODO: PUT化するべき
   * 実質PUTとして扱う
   */
  def account = Action { implicit request =>
    val (name, plainPassword) = accountForm.bindFromRequest.get

    // TODO: 本当はsaltを設定するべきなのでこの標準実装はよくない。
    // 下記URLを参考にしつつ自分でsltを使用した実装をするべき
    // http://www.atmarkit.co.jp/fsecurity/special/165pswd/01.html
    val password = BCrypt.hashpw(plainPassword, BCrypt.gensalt())

    // TODO: トランザクション処理がまったくない
    // トランザクション内でSELECT & INSERTするよう修正するべき
    accountConnection withSession {
      val result = ( for(a <- Accounts; if a.name === name) yield a.name ).list

      if (result.length == 0) {
        Accounts.insert(name, password)
        Ok("name: " + name + "\npassword: " + password)
      } else {
        Forbidden("そのアカウント名はすでに使用されています")
      }
    }
  }

  def logout = Action { implicit request =>
    Ok("ログアウトしました").withNewSession
  }
}