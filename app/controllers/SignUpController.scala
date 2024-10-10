package controllers

import javax.inject._
import play.api.mvc._
import views.html.signUp
import play.api.data.Form
import play.api.data.Forms._

@Singleton
class SignUpController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

    val signUpForm: Form[SignUpData] = Form(
        mapping(
            "firstName" -> nonEmptyText,
            "surname" -> nonEmptyText,
            "email" -> email,
            "password" -> nonEmptyText
        )(SignUpData.apply)(SignUpData.unapply)
    )

    def showSignUpForm(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
        Ok(signUp(signUpForm))
    }

    // Handle form submission
    def submitSignUpDetails(): Action[AnyContent] = Action { implicit request =>
        signUpForm.bindFromRequest.fold(
            formWithErrors => {
                // If the form has errors, re-render the page with errors
                BadRequest(views.html.signUp(formWithErrors))
            },
            formData => {
                // If successful, redirect to the home page with a success message
                Redirect("/").flashing("success" -> "Sign up successful!")
            }
        )
    }
}

case class SignUpData(firstName: String, surname: String, email: String, password: String)