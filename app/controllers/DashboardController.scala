package controllers

import play.api.mvc._

import javax.inject._
import views.html.dashboard


class DashboardController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

    def showDashboard(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
        Ok(dashboard())
    }
}