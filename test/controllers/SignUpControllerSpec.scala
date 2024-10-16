package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test._
import play.api.test.Helpers._
import repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

class SignUpControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val mockUserRepository: UserRepository = mock[UserRepository]
    val signUpController = new SignUpController(stubControllerComponents(), mockUserRepository)

    val validSignUpData: SignUpData = SignUpData("John", "Doe", "john.doe@example.com", "Password123")

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
            contentAsString(result) must include("""<input type="text" id="firstName" name="firstName" required""")
        }

        "include a Surname input box in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="text" id="surname" name="surname" required""")
        }

        "include an Email input box in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="email" id="email" name="email" required""")
        }

        "include a Password input box in the sign up page" in {
            val request = FakeRequest(GET, "/signup")
            val result = route(app, request).get

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="password" id="password" name="password" required""")
        }

        "include a link to the home page" in {
            val home = signUpController.showSignUpForm().apply(FakeRequest(GET, "/").withCSRFToken)

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

        "handle form submission for sign up with valid sign up details" in {
            when(mockUserRepository.createUser(any[SignUpData])).thenReturn(Future.successful((true)))
            val result = signUpController.submitSignUpDetails().apply(FakeRequest().withFormUrlEncodedBody(
                "firstName" -> "John",
                "surname" -> "Doe",
                "email" -> "john.doe@example.com",
                "password" -> "password123"
            ))

            status(result) mustBe SEE_OTHER // Expecting a redirect after form submission
            redirectLocation(result) mustBe Some("/") // Expecting to be redirected to the home page
            flash(result).get("success") mustBe Some("Sign up successful!") // Assuming a success message in flash scope
        }

        "show error for invalid sign up details when first name field is empty" in {
            when(mockUserRepository.createUser(any[SignUpData])).thenReturn(Future.successful((true)))
            val result = signUpController.submitSignUpDetails().apply(FakeRequest().withFormUrlEncodedBody(
                "firstName" -> "",
                "surname" -> "Doe",
                "email" -> "john.doe@example.com",
                "password" -> "password123"
            ).withCSRFToken)

            status(result) mustBe BAD_REQUEST // bad request due to validation errors
            contentAsString(result) must include("Sign Up") // Ensure we're still on the sign up page
            contentAsString(result) must include("error.required")
            contentAsString(result) must include("firstName")
        }

        "show error for invalid sign up details when surname field is empty" in {
            when(mockUserRepository.createUser(any[SignUpData])).thenReturn(Future.successful((true)))
            val result = signUpController.submitSignUpDetails().apply(FakeRequest().withFormUrlEncodedBody(
                "firstName" -> "John",
                "surname" -> "",
                "email" -> "john.doe@example.com",
                "password" -> "password123"
            ).withCSRFToken)

            status(result) mustBe BAD_REQUEST // bad request due to validation errors
            contentAsString(result) must include("Sign Up") // Ensure we're still on the sign up page
            contentAsString(result) must include("error.required")
            contentAsString(result) must include("surname")
        }

        "show error for invalid sign up details when email is invalid" in {
            when(mockUserRepository.createUser(any[SignUpData])).thenReturn(Future.successful((true)))
            val result = signUpController.submitSignUpDetails().apply(FakeRequest().withFormUrlEncodedBody(
                "firstName" -> "John",
                "surname" -> "Doe",
                "email" -> "invalid-email",
                "password" -> "password123"
            ).withCSRFToken)

            status(result) mustBe BAD_REQUEST // bad request due to validation errors
            contentAsString(result) must include("Sign Up") // Ensure we're still on the sign up page
            contentAsString(result) must include("error.required")
            contentAsString(result) must include("email")
        }

        "show error for invalid sign up details when password field is empty" in {
            when(mockUserRepository.createUser(any[SignUpData])).thenReturn(Future.successful((true)))
            val result = signUpController.submitSignUpDetails().apply(FakeRequest().withFormUrlEncodedBody(
                "firstName" -> "John",
                "surname" -> "Doe",
                "email" -> "john.doe@example.com",
                "password" -> ""
            ).withCSRFToken)

            status(result) mustBe BAD_REQUEST // bad request due to validation errors
            contentAsString(result) must include("Sign Up") // Ensure we're still on the sign up page
            contentAsString(result) must include("error.required")
            contentAsString(result) must include("password")
        }
    }
}
