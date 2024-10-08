package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

class SignUpControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

    "SignUpController GET" should {

        "display the sign up page from the router" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentType(result) mustBe Some("text/html")
            contentAsString(result) must include("Sign Up")
        }

        "include a First Name input box in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="text" id="firstName" name="firstName" required>""")
        }

        "include a Surname input box in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="text" id="surname" name="surname" required>""")
        }

        "include an Email input box in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="email" id="email" name="email" required>""")
        }

        "include a Password input box in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="password" id="password" name="password" required>""")
        }
    }
}
