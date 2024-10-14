package repositories

import controllers.SignUpData
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import utils.MongoConfig
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import play.api.Application

import scala.concurrent.ExecutionContext
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._



class UserRepositorySpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with BeforeAndAfterEach {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    override def fakeApplication(): Application = new GuiceApplicationBuilder()
        .configure("mongodb.uri" -> "mongodb://localhost:27017/masters2025_test")
        .build()

    val mongoConfig: MongoConfig = app.injector.instanceOf[MongoConfig]
    val userRepository: UserRepository = app.injector.instanceOf[UserRepository]

    override def beforeEach(): Unit = {
        Await.result(clearUsersCollection(), 5.seconds)
    }

    def clearUsersCollection(): Future[Unit] = {
        userRepository.usersCollection.flatMap { collection =>
            collection.drop(failIfNotFound = false).map(_ => ())
        }
    }

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

            val result = Await.result(userRepository.createUser(userData), 1.seconds)
            result mustBe true

            }


        "not create a user and return false when passed invalid user data" in {
            // Create invalid sign up data
            val invalidUserData = SignUpData(
                firstName = "", // Invalid
                surname = "", // Invalid
                email = "john.doe@example.com",
                password = "Password123"
            )

            whenReady(userRepository.createUser(invalidUserData), Timeout(1.seconds)) { createResult =>
                // Check if the user was created successfully
                createResult mustBe false
            }

        }

        "not create a user and return false when trying to" +
            " use an email address that has already been used" in {
            // Create valid sign up data
            val invalidUserData = SignUpData(
                firstName = "John",
                surname = "Doe",
                email = "john.doe2@example.com",
                password = "Password123"
            )
            val result = Await.result(userRepository.createUser(invalidUserData), 1.seconds)
            result mustBe true

            val result2 = Await.result(userRepository.createUser(invalidUserData), 1.seconds)
            result2 mustBe false
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

            val result = Await.result(userRepository.createUser(userData), 1.seconds)
            result mustBe true

            // Now check if the user exists
            val exists = Await.result(userRepository.userExists(userData.email), 1.seconds)
            exists mustBe true
        }



        "return false if a user does not exist" in {
            val result = Await.result(userRepository.userExists("nonExistent@example.com"), 1.seconds)
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
        "return an Option of SignUpData for an existing user" in {
            // Create valid sign up data
            val userData = SignUpData(
                firstName = "Test",
                surname = "Name",
                email = "test@example.com",
                password = "Password123"
            )

            Await.result(userRepository.createUser(userData), 1.seconds)

            whenReady(userRepository.getUserByEmail(userData.email), Timeout(1.seconds)) {
                case Some(user) =>
                    user.firstName mustBe userData.firstName
                    user.surname mustBe userData.surname
                    user.email mustBe userData.email
                    userRepository.checkPassword(userData.password, user.password) mustBe true
                case None => fail("User was not found")
            }
        }

        "return None for a non-existing user" in {
            val result = Await.result(userRepository.getUserByEmail("nonexistent@example.com"), 1.seconds)
            result mustBe None
        }

        "return user data for an existing user with a case-insensitive email lookup" in {
            val userData = SignUpData(
                firstName = "Jane",
                surname = "Doe",
                email = "jane.doe@example.com",
                password = "Password123"
            )
            Await.result(userRepository.createUser(userData), 1.seconds)

            whenReady(userRepository.getUserByEmail("JANE.DOE@EXAMPLE.COM"), Timeout(1.seconds)) {
                case Some(user) =>
                    user.firstName mustBe userData.firstName
                    user.surname mustBe userData.surname
                    user.email mustBe userData.email
                    userRepository.checkPassword(userData.password, user.password) mustBe true
                case None => fail("User was not found")
            }
        }

        "return None when searching for an empty email string" in {
            val result = Await.result(userRepository.getUserByEmail(""), 1.seconds)
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
            Await.result(userRepository.createUser(userData1), 1.seconds)
            Await.result(userRepository.createUser(userData2), 1.seconds)

            whenReady(userRepository.getUserByEmail(userData2.email), Timeout(1.seconds)) {
                case Some(user) =>
                    user.firstName mustBe userData2.firstName
                    user.surname mustBe userData2.surname
                    user.email mustBe userData2.email
                    userRepository.checkPassword(userData2.password, user.password) mustBe true
                case None => fail("User was not found")
            }
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
