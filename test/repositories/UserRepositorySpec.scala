package repositories

import controllers.SignUpData
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder

import utils.MongoConfig

import scala.concurrent.ExecutionContext.Implicits.global


class UserRepositorySpec extends PlaySpec with GuiceOneAppPerSuite {

    override def fakeApplication() = new GuiceApplicationBuilder()
        .configure("mongodb.uri" -> "mongodb://localhost:27017/masters2025")
        .build()

    val mongoConfig: MongoConfig = app.injector.instanceOf[MongoConfig]
    val userRepository: UserRepository = app.injector.instanceOf[UserRepository]

    "UserRepository" should {
        "connect to the MongoDB instance" in {
            val uri = mongoConfig.getConnection
            assert(uri != null )
        }
    }


    "createUser" should {
        "create a user and return true when passed valid user data" in {
            // Create valid sign up data
            val userData = SignUpData(
                firstName = "John",
                surname = "Doe",
                email = "john.doe@example.com",
                password = "Password123"
            )
            val initialUserCount = userRepository.userCount
            val result = userRepository.createUser(userData)

            result mustBe true
            userRepository.userExists(userData.email) mustBe true
            userRepository.userCount mustBe initialUserCount + 1

        }

        "not create a user and return false when passed invalid user data" in {
            // Create invalid sign up data
            val invalidUserData = SignUpData(
                firstName = "", // Invalid
                surname = "", // Invalid
                email = "john.doe@example.com",
                password = "Password123"
            )

            val result = userRepository.createUser(invalidUserData)

            result mustBe false

        }

        "not create a user and return false when trying to" +
            " use an email address that has already been used" in {
            // Create invalid sign up data
            val invalidUserData = SignUpData(
                firstName = "John", // Invalid
                surname = "Doe", // Invalid
                email = "john.doe@example.com",
                password = "Password123"
            )
            userRepository.createUser(invalidUserData)
            val result = userRepository.createUser(invalidUserData)

            result mustBe false

        }

    }

    "userExists" should {
        "return true if a user exists" in {
            val userData = SignUpData(
                firstName = "John",
                surname = "Doe",
                email = "john.doe@example.com",
                password = "Password123"
            )
            userRepository.createUser(userData)

            val result = userRepository.userExists("john.doe@example.com")
            result mustBe true
        }

        "return false if a user does not exist" in {
            val result = userRepository.userExists("nonExistent@example.com")
            result mustBe false
        }
    }

    "validateSignUpData" should {
        "return true if valid sign up data is provided" in {
            // Create valid sign up data
            val userData = SignUpData(
                firstName = "John",
                surname = "Doe",
                email = "john.doe@example.com",
                password = "Password123"
            )

            val result = userRepository.validateSignUpData(userData)
            result mustBe true
        }

        "return false if invalid sign up data is provided" in {
            // Create valid sign up data
            val userData = SignUpData(
                firstName = "", // invalid first name
                surname = "Doe",
                email = "john.doe@example.com",
                password = "Password123"
            )

            val result = userRepository.validateSignUpData(userData)
            result mustBe false
        }

    }

    "getUserByEmail" should {
        "return user data for an existing user" in {
            // Create valid sign up data
            val userData = SignUpData(
                firstName = "Test",
                surname = "Name",
                email = "test@example.com",
                password = "Password123"
            )

            userRepository.createUser(userData)
            val result = userRepository.getUserByEmail("test@example.com")

            result mustBe Some(userData)
        }

        "return None for a non-existing user" in {
            val result = userRepository.getUserByEmail("nonexistent@example.com")
            result mustBe None
        }

        "return user data for an existing user with a case-insensitive email lookup" in {
            val userData = SignUpData(
                firstName = "Jane",
                surname = "Doe",
                email = "jane.doe@example.com",
                password = "Password123"
            )
            userRepository.createUser(userData)

            val result = userRepository.getUserByEmail("JANE.DOE@EXAMPLE.COM") // Uppercase email lookup
            result mustBe Some(userData)
        }

        "return None when searching for an empty email string" in {
            val result = userRepository.getUserByEmail("")
            result mustBe None
        }

        "return correct user data when multiple users exist in the repository" in {
            val userData1 = SignUpData(
                firstName = "First",
                surname = "User",
                email = "first.user@example.com",
                password = "Password123"
            )
            val userData2 = SignUpData(
                firstName = "Second",
                surname = "User",
                email = "second.user@example.com",
                password = "Password456"
            )
            userRepository.createUser(userData1)
            userRepository.createUser(userData2)

            val result = userRepository.getUserByEmail("second.user@example.com")
            result mustBe Some(userData2)
        }

    }

    "hashPassword" should {
        "hash passwords correctly" in {
            val plainPassword = "Password123"
            val hashedPassword = userRepository.hashPassword(plainPassword)

            // Check that the hashed password is not the same as the plain password
            hashedPassword must not equal plainPassword
        }

        "produce different hashes for the same password on different calls" in {
            val plainPassword = "Password123"
            val hashedPassword1 = userRepository.hashPassword(plainPassword)
            val hashedPassword2 = userRepository.hashPassword(plainPassword)

            hashedPassword1 must not equal hashedPassword2 // Ensure different hashes
        }

        "validate the password using BCrypt" in {
            val plainPassword = "Password123"
            val hashedPassword = userRepository.hashPassword(plainPassword)

            // Validate that the plain password matches the hashed password
            userRepository.checkPassword(plainPassword, hashedPassword) mustBe true
        }

        "fail password validation if passwords don't match" in {
            val plainPassword = "Password123"
            val hashedPassword = userRepository.hashPassword(plainPassword)

            // Validate that a different password does not match the hashed password
            userRepository.checkPassword("WrongPassword", hashedPassword) mustBe false
        }
    }
}
