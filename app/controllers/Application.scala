package controllers

import play.api._
import play.api.mvc._
import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession

object Coffees extends Table[(String, Double)]("COFFEES") {
  def name = column[String]("COF_NAME", O.PrimaryKey)
  def price = column[Double]("PRICE")
  def * = name ~ price
}

object Application extends Controller {
  
  def index = Action {
    Database.forURL("jdbc:mysql://localhost/test1?user=root&password=", driver = "com.mysql.jdbc.Driver") withSession {
      ( for( c <- Coffees; if c.price < 10.0 ) yield c.name ).list
      // or
      Coffees.filter(_.price < 10.0).map(_.name).list
    }

    Ok("test")
  }

  def sign_up = Action {
    Ok(views.html.sign_up())
  }

  def account = Action {
    Ok("post ok")
  }
}