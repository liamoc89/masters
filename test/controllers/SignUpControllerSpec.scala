package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._

class SignUpControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {

    "SignUpController GET" should {

        "display the sign up page" in {
            val controller = inject[SignUpController]
            val request = FakeRequest(GET, "/signup")
            val result = controller.showSignUpForm().apply(request)

            status(result) mustBe OK
            contentType(result) mustBe Some("text/html")
            contentAsString(result) must include("Sign Up")
        }
    }
}
