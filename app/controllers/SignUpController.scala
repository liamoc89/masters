package controllers

import javax.inject._
import play.api.mvc._
import views.html.signUp
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}
import repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignUpController @Inject()(cc: ControllerComponents, userRepository: UserRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

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
    def submitSignUpDetails(): Action[AnyContent] = Action.async { implicit request =>
        println(s"${request.body} ELEPHANT")
        signUpForm.bindFromRequest.fold(
            formWithErrors => {
                println(formWithErrors.errors)
                // If the form has errors, re-render the page with errors
                Future.successful(BadRequest(views.html.signUp(formWithErrors)))
            },
            formData => {
                userRepository.createUser(formData).map {
                    userCreated =>
                        if(userCreated) {
                            Redirect("/").flashing("success" -> "Sign up successful!")
                        } else {
                            BadRequest(views.html.signUp(signUpForm.withError("email", "Unable to create user")))
                        }
                }
            }
        )
    }
}

case class SignUpData(firstName: String, surname: String, email: String, password: String)

object SignUpData {
    implicit val format: OFormat[SignUpData] = Json.format[SignUpData] // JSON format for serialization
}