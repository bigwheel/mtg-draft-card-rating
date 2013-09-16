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

  def before = new WithApplication {
    Application.accountConnection withSession {
      Query(Accounts).delete
    }
  }

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "about login" should {
      "cant login before to creating account" in new WithApplication {
        val home = route(FakeRequest(POST, "/login").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

        status(home) must equalTo(FORBIDDEN)
      }

      "can login after creating account" in new WithApplication {
        status(route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get) must equalTo(OK)
        status(route(FakeRequest(POST, "/login").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get) must equalTo(OK)
      }
    }

    "about logout" should {
      "can logout before login" in new WithApplication { // TODO: 本来はログイン前にログアウトできるべきではな気がする
        status(route(FakeRequest(GET, "/logout")).get) must equalTo(OK)
      }

      "can logout after login" in new WithApplication {
        route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get
        route(FakeRequest(POST, "/login").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

        status(route(FakeRequest(GET, "/logout")).get) must equalTo(OK)
      }
    }

    "about create account" in {
      "can create an account" in new WithApplication {
        val home = route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

        status(home) must equalTo(OK)
      }

      "cant create an account with existing account name" in new WithApplication {
        val result1 = route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get
        val result2 = route(FakeRequest(POST, "/account").withFormUrlEncodedBody("name" -> "abc", "password" -> "def")).get

        if (status(result1) == OK)
          status(result2) must equalTo(FORBIDDEN)
        else {
          status(result1) must equalTo(FORBIDDEN)
          status(result2) must equalTo(OK)
        }
      }
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentAsString(home) must contain ("アカウント名")
    }
  }
}