package repositories

import controllers.SignUpData
import org.mindrot.jbcrypt.BCrypt
import play.api.Logging
import utils.MongoConfig

import javax.inject.Inject

class UserRepository @Inject()(mongoConfig: MongoConfig) extends Logging {
    private var users: List[SignUpData] = List()

    def createUser(userData: SignUpData): Boolean =
        if (validateSignUpData(userData) && !userExists(userData.email)) {
            users = users :+ userData
            true
        } else if (userExists(userData.email)) {
            logger.info("User already exists")
            false
        } else {
            logger.info("Invalid user data")
            false
        }

    def userExists(email: String): Boolean =
        // Check if any user with the email address provided exists within users list
        users.exists(_.email == email)

    def validateSignUpData(userData: SignUpData): Boolean =
        userData.firstName.nonEmpty && userData.surname.nonEmpty &&
            userData.email.nonEmpty && userData.password.nonEmpty

    def userCount: Int = users.size

    def getUserByEmail(email: String): Option[SignUpData] =
        users.find(_.email.equalsIgnoreCase(email))

    // hash password using BCrypt
    def hashPassword(password: String): String =
        BCrypt.hashpw(password, BCrypt.gensalt())

    // Check password using BCrypt
    def checkPassword(plainPassword: String, hashedPassword: String): Boolean = {
        BCrypt.checkpw(plainPassword, hashedPassword)
    }

}
