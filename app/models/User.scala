package models

import controllers.SignUpData
import play.api.libs.json.{Json, OFormat}


case class User(
    signUpData: SignUpData
)

object User {
    implicit val format: OFormat[User] = Json.format[User] // JSON format for serialization
}