package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.Helpers._
import play.api.test._

class LoginControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

    "LoginController GET" should {

        "display the login page from the router" in {
            val request = FakeRequest(GET, "/login")
            val result = route(app, request).get

            status(result) mustBe OK
            contentType(result) mustBe Some("text/html")
            contentAsString(result) must include("Login")
        }


        "include an Email input box in the login page" in {
            val request = FakeRequest(GET, "/login")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="email" id="email" name="email" required>""")
        }

        "include a Password input box in the login page" in {
            val request = FakeRequest(GET, "/login")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="password" id="password" name="password" required>""")
        }

        "include a link to the home page" in {
            val controller = new LoginController(stubControllerComponents())
            val home = controller.showLoginForm().apply(FakeRequest(GET, "/"))

            contentAsString(home) must include("""<a href="/">Home</a>""")
        }

        "include a Login button in the login page" in {
            val request = FakeRequest(GET, "/login")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<button type="submit">Login</button>""")
        }
    }

    "LoginController POST" should {

        "handle form submission for login" in {
            val request = FakeRequest(POST, "/login")
                .withFormUrlEncodedBody(
                    "email" -> "john.doe@example.com",
                    "password" -> "password123"
                )

            val result = route(app, request).get

            status(result) mustBe SEE_OTHER // Expecting a redirect after form submission
            redirectLocation(result) mustBe Some("/") // Expecting to be redirected to the home page
            flash(result).get("success") mustBe Some("Login successful!") // Assuming a success message in flash scope
        }
    }
}
