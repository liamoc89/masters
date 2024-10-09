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

        "include a link to the home page" in {
            val controller = new SignUpController(stubControllerComponents())
            val home = controller.showSignUpForm().apply(FakeRequest(GET, "/"))

            contentAsString(home) must include("""<a href="/">Home</a>""")
        }

        "include a Sign Up button in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<button type="submit">Sign Up</button>""")
        }

    }

    "SignUpController POST" should {

        "handle form submission for sign up" in {
            val request = FakeRequest(POST, "/signup")
                .withFormUrlEncodedBody(
                    "firstName" -> "John",
                    "surname" -> "Doe",
                    "email" -> "john.doe@example.com",
                    "password" -> "password123"
                )

            val result = route(app, request).get

            status(result) mustBe SEE_OTHER // Expecting a redirect after form submission
            redirectLocation(result) mustBe Some("/") // Expecting to be redirected to the home page
            flash(result).get("success") mustBe Some("Sign up successful!") // Assuming a success message in flash scope
        }
    }
}
