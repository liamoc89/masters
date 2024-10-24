package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class DashboardControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "DashboardController GET" should {

    "display the dashboard page from a new instance of controller" in {
      val controller = new DashboardController(stubControllerComponents())
      val dashboard = controller.showDashboard().apply(FakeRequest(GET, "/dashboard"))

      status(dashboard) mustBe OK
      contentType(dashboard) mustBe Some("text/html")
      contentAsString(dashboard) must include ("Dashboard")
      contentAsString(dashboard) must include ("My Golfers")
      contentAsString(dashboard) must include ("Leaderboard")
      contentAsString(dashboard) must include ("Player Insights")
      contentAsString(dashboard) must include ("My Account")
      contentAsString(dashboard) must include ("Log Out")
    }

  }
}
