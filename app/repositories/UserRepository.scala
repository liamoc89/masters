package repositories

import controllers.SignUpData
import org.mindrot.jbcrypt.BCrypt
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import utils.MongoConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject()(mongoConfig: MongoConfig)(implicit ec: ExecutionContext) extends Logging {
    def usersCollection: Future[JSONCollection] = mongoConfig.getConnection.flatMap(_.database("masters2025")).map(_.collection[JSONCollection]("users"))

    def createUser(userData: SignUpData): Future[Boolean] = {
        if (validateSignUpData(userData)) {
            userExists(userData.email).flatMap {
                exists =>
                    if (exists) {
                        Future.successful(false)
                    } else {
                        val hashedPw: String = hashPassword(userData.password)
                        val userWithHashedPw: SignUpData = userData.copy(password = hashedPw)
                        usersCollection.flatMap(_.insert.one(userWithHashedPw)).map {
                            writeResult =>
                                logger.info(s"User inserted with result: $writeResult")
                                true
                        }
                    }
            }
        } else {
            logger.info("Invalid user data")
            Future.successful(false)
        }
    }


    def userExists(email: String): Future[Boolean] = {
        // Check if any user with the email address provided exists within users list
        val query = Json.obj("email" -> email)
        usersCollection.flatMap { collection =>
            collection.find(query, Option.empty[JsObject]).one[SignUpData].map {
                case Some(_) => {
                    logger.info(s"USER: '$email' ALREADY EXISTS")
                    true
                }
                case _ => false

            }
        }

    }

    def validateSignUpData(userData: SignUpData): Boolean = {
        val isValid = userData.firstName.nonEmpty && userData.surname.nonEmpty &&
            userData.email.nonEmpty && userData.password.nonEmpty
        isValid

    }

    def getUserByEmail(email: String): Future[Option[SignUpData]] = {
        val query = Json.obj("email" -> Json.obj("$regex" -> s"^$email$$", "$options" -> "i"))
        usersCollection.flatMap { collection =>
            collection.find(query, Option.empty[JsObject]).one[SignUpData]
        }
    }


    // hash password using BCrypt
    def hashPassword(password: String): String =
        BCrypt.hashpw(password, BCrypt.gensalt())

    // Check password using BCrypt
    def checkPassword(plainPassword: String, hashedPassword: String): Boolean = {
        BCrypt.checkpw(plainPassword, hashedPassword)
    }

}
