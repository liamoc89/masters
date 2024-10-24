package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.Logging

import javax.inject._
import play.api.mvc._
import repositories.UserRepository
import views.html.login

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginController @Inject()(cc: ControllerComponents, userRepository: UserRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) with Logging {

    val loginForm = Form(
        mapping(
            "email" -> email,
            "password" -> nonEmptyText
        )(LoginData.apply)(LoginData.unapply)
    )
    // Placeholder action to render the login form
    def showLoginForm() = Action { implicit request: Request[AnyContent] =>
        Ok(login(loginForm))
    }

    // Handle form submission
    def submitLoginDetails() = Action.async { implicit request =>
        loginForm.bindFromRequest.fold(
            formWithErrors => {
                // If the form has errors, re-render the page with errors
                Future.successful(BadRequest(login(formWithErrors))) // Pass the form with errors to the view
            },
            loginData => {
                // If successful, redirect to the home page with a success message
                userRepository.getUserByEmail(loginData.email).flatMap {
                    case Some(userData) =>
                        if (userRepository.checkPassword(loginData.password, userData.password)) {
                            Future.successful(Redirect(routes.HomeController.index())
                                .withSession("email" -> loginData.email)
                                .flashing("success" -> "Logged in successfully!"))
                        } else {
                            Future.successful(Redirect(routes.LoginController.showLoginForm())
                                .flashing("error" -> "Invalid password"))
                    }

                    case _ => {
                        Future.successful(Redirect(routes.LoginController.showLoginForm())
                            .flashing("error" -> "User not found"))
                    }

                }
            }
        )
    }

}

case class LoginData(email: String, password: String)