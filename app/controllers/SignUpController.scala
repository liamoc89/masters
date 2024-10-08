package controllers

import javax.inject._
import play.api.mvc._
import views.html.signUp

@Singleton
class SignUpController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

    // Placeholder action to render the login form
    def showSignUpForm() = Action { implicit request: Request[AnyContent] =>
        Ok(signUp())
    }
}