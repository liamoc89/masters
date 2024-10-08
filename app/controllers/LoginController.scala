package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class LoginController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

    // Placeholder action to render the login form
    def showLoginForm() = Action { implicit request: Request[AnyContent] =>
        Ok("Login Page")
    }
}
