package test

import org.specs2.mutable._

import scala.slick.driver.MySQLDriver.simple._
import Database.threadLocalSession
import play.api.test._
import play.api.test.Helpers._
import org.specs2.specification.BeforeExample
import controllers.{Accounts, Application}
import scala.slick.lifted.Query

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification with BeforeExample {

  sequential

  def before = {
    running(FakeApplication()) {
      Application.accountConnection withSession {
        Query(Accounts).delete
      }
    }
  }

  "Application" should {

    "send 404 on a bad request" in {
      running(FakeApplication()) {
        route(FakeRequest(GET, "/boum")) must beNone
      }
    }

    "about login" should {
      "cant login before to creating account" in {
        running(FakeApplication()) {
          val home = route(FakeRequest(POST, "/login").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

          status(home) must equalTo(FORBIDDEN)
        }
      }

      "can login after creating account" in {
        running(FakeApplication()) {
          val r = route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get
          status(r) must equalTo(OK)
          Thread.sleep(1000) // なぜかないと403になる
          val result = route(FakeRequest(POST, "/login").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

          status(result) must equalTo(OK)
        }
      }
    }

    "about logout" should {
      "can logout before login" in { // TODO: 本来はログイン前にログアウトできるべきではな気がする
        running(FakeApplication()) {
          status(route(FakeRequest(GET, "/logout")).get) must equalTo(OK)
        }
      }

      "can logout after login" in {
        running(FakeApplication()) {
          route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get
          Thread.sleep(1000) // なぜかないと403になる
          route(FakeRequest(POST, "/login").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

          status(route(FakeRequest(GET, "/logout")).get) must equalTo(OK)
        }
      }
    }

    "about create account" in {
      "can create an account" in {
        running(FakeApplication()) {
          val home = route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

          status(home) must equalTo(OK)
        }
      }

      "cant create an account with existing account name" in {
        running(FakeApplication()) {

          val r = route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get
          status(r) must equalTo(FORBIDDEN)
          Thread.sleep(1000) // なぜかないと403になる
          val result = route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

          status(result) must equalTo(FORBIDDEN)
        }
      }
    }

    "render the index page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get

        status(home) must equalTo(OK)
        contentAsString(home) must contain ("アカウント名")
      }
    }
  }
}