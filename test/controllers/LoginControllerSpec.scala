package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Play.materializer
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.Helpers._
import play.api.test._
import repositories.UserRepository

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class LoginControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with BeforeAndAfterEach{

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val mockUserRepository: UserRepository = mock[UserRepository]
    val loginController = new LoginController(stubControllerComponents(), mockUserRepository)
    val signUpController = new SignUpController(stubControllerComponents(), mockUserRepository)
    val validSignUpData: SignUpData = SignUpData("John", "Doe", "john.doe@example.com", "Password123")

    override def beforeEach(): Unit = {
        super.beforeEach()
        reset(mockUserRepository)
    }



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
            contentAsString(result) must include("""<input type="email" id="email" name="email" required """)
        }

        "include a Password input box in the login page" in {
            val result = loginController.showLoginForm().apply(FakeRequest(GET, "/login").withCSRFToken)

            status(result) mustBe OK
            contentAsString(result) must include("""<input type="password" id="password" name="password" required """)
        }

        "include a link to the home page" in {
            val controller = new LoginController(stubControllerComponents(), mockUserRepository)
            val home = controller.showLoginForm().apply(FakeRequest(GET, "/").withCSRFToken)

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

        "redirect to the dashboard page when correct login details are provided" in {
            when(mockUserRepository.getUserByEmail(any[String])).thenReturn(Future.successful(Some(validSignUpData)))
            when(mockUserRepository.checkPassword(any[String], any[String])).thenReturn(true)

            val request = FakeRequest(POST, "/login")
                .withFormUrlEncodedBody(
                    "email" -> "john.doe@example.com",
                    "password" -> "Password123"
                )

            val result = loginController.submitLoginDetails().apply(request)

            status(result) mustBe SEE_OTHER // Expecting a redirect after form submission
            redirectLocation(result) mustBe Some("/dashboard") // Expecting to be redirected to the home page
            flash(result).get("success") mustBe Some("Logged in successfully!") // Assuming a success message in flash scope
        }

        "stay on the login page and show a form with errors when user enters an email which does not exist in the database" in {
            when(mockUserRepository.getUserByEmail(any[String])).thenReturn(Future.successful(None))

            val request = FakeRequest(POST, "/login")
                .withFormUrlEncodedBody(
                    "email" -> "john.doe@example.com",
                    "password" -> "Password123"
                )

            val result = loginController.submitLoginDetails().apply(request)

            status(result) mustBe SEE_OTHER // Expecting a redirect after form submission
            redirectLocation(result) mustBe Some("/login") // Expecting to be redirected to the home page
            flash(result).get("error") mustBe Some("User not found") // Assuming a success message in flash scope
        }

        "stay on the login page and show a form with errors when user enters an invalid password" in {
            when(mockUserRepository.getUserByEmail(any[String])).thenReturn(Future.successful(Some(validSignUpData)))
            when(mockUserRepository.checkPassword(any[String], any[String])).thenReturn(false)

            val request = FakeRequest(POST, "/login")
                .withFormUrlEncodedBody(
                    "email" -> "john.doe@example.com",
                    "password" -> "incorrectPassword"
                )

            val result = loginController.submitLoginDetails().apply(request)

            status(result) mustBe SEE_OTHER // Expecting a redirect after form submission
            redirectLocation(result) mustBe Some("/login") // Expecting to be redirected to the home page
            flash(result).get("error") mustBe Some("Invalid password") // Assuming a success message in flash scope
        }

        "show errors for invalid login when email address is invalid" in {

            val result = loginController.submitLoginDetails().apply(FakeRequest().withFormUrlEncodedBody(
                    "email" -> "invalid-email",
                    "password" -> "Password123"
                ).withCSRFToken)


            status(result) mustBe BAD_REQUEST // bad request due to validation errors
            contentAsString(result) must include("Login") // Ensure we're still on the login page
            contentAsString(result) must include("error.required")
        }

        "show errors for invalid login when email address is empty" in {
            val result = loginController.submitLoginDetails().apply(FakeRequest().withFormUrlEncodedBody(
                "email" -> "",
                "password" -> "Password123"
            ).withCSRFToken)


            status(result) mustBe BAD_REQUEST // bad request due to validation errors
            contentAsString(result) must include("Login") // Ensure we're still on the login page
            contentAsString(result) must include("error.required")
        }

        "show errors for invalid login when password field is empty" in {
            val result = loginController.submitLoginDetails().apply(FakeRequest().withFormUrlEncodedBody(
                "email" -> "john.doe@example.com",
                "password" -> ""
            ).withCSRFToken)

            status(result) mustBe BAD_REQUEST // bad request due to validation errors
            contentAsString(result) must include("Login") // Ensure we're still on the login page
            contentAsString(result) must include("error.required")
        }

    }
}
