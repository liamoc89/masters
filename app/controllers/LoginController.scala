package controllers

import play.api.data.Form
import play.api.data.Forms._
import javax.inject._
import play.api.mvc._
import views.html.login

@Singleton
class LoginController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

    val loginForm = Form(
        mapping(
            "email" -> email,
            "password" -> nonEmptyText
        )(LoginData.apply)(LoginData.unapply)
    )
    // Placeholder action to render the login form
    def showLoginForm() = Action { implicit request: Request[AnyContent] =>
        Ok(login())
    }

    // Handle form submission
    def submitLoginDetails() = Action { implicit request =>
        loginForm.bindFromRequest.fold(
            formWithErrors => {
                // If the form has errors, re-render the page with errors
                BadRequest(views.html.signUp())
            },
            formData => {
                // If successful, redirect to the home page with a success message
                Redirect("/").flashing("success" -> "Login successful!")
            }
        )
    }

}

case class LoginData(email: String, password: String)