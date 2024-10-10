package repositories

import controllers.SignUpData
import org.scalatestplus.play.PlaySpec

class UserRepositorySpec extends PlaySpec {

    val mockUserRepository = new UserRepository {}

    "createUser" should {

        "create a user and return true when passed valid user data" in {
            // Create valid sign up data
            val userData = SignUpData(
                firstName = "John",
                surname = "Doe",
                email = "john.doe@example.com",
                password = "Password123"
            )
            val initialUserCount = mockUserRepository.userCount
            mockUserRepository.createUser(userData)
            val result = mockUserRepository.userExists(userData.email)

            // Ensure user count has increased by 1
            mockUserRepository.userCount mustBe initialUserCount + 1
            result mustBe true

        }

        "not create a user and return false when passed invalid user data" in {

            // Create invalid sign up data
            val invalidUserData = SignUpData(
                firstName = "", // Invalid
                surname = "", // Invalid
                email = "john.doe123@example.com", // different email to valid user data in previous test
                password = "Password123"
            )
            val initialUserCount = mockUserRepository.userCount
            mockUserRepository.createUser(invalidUserData)
            val result = mockUserRepository.userExists(invalidUserData.email)
            result mustBe false
            mockUserRepository.userCount mustBe initialUserCount
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
            mockUserRepository.createUser(userData)

            val result = mockUserRepository.userExists("john.doe@example.com")
            result mustBe true
        }

        "return false if a user does not exist" in {
            val result = mockUserRepository.userExists("nonExistent@example.com")
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

            val result = mockUserRepository.validateSignUpData(userData)
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

            val result = mockUserRepository.validateSignUpData(userData)
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

            mockUserRepository.createUser(userData)
            val result = mockUserRepository.getUserByEmail("test@example.com")

            result mustBe Some(userData)
        }

        "return None for a non-existing user" in {
            // Attempt to retrieve user data for an email that hasn't been used
            val result = mockUserRepository.getUserByEmail("nonexistent@example.com")

            // Verify that the result is None
            result mustBe None
        }

        "return user data for an existing user with a case-insensitive email lookup" in {
            val userRepository = new UserRepository {}
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
            val userRepository = new UserRepository {}
            val result = userRepository.getUserByEmail("")
            result mustBe None
        }

        "return correct user data when multiple users exist in the repository" in {
            val userRepository = new UserRepository {}
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
            val hashedPassword = mockUserRepository.hashPassword(plainPassword)

            // Check that the hashed password is not the same as the plain password
            hashedPassword must not equal plainPassword
        }

        "validate the password using BCrypt" in {
            val userRepository = new UserRepository {}

            val plainPassword = "Password123"
            val hashedPassword = userRepository.hashPassword(plainPassword)

            // Validate that the plain password matches the hashed password
            userRepository.checkPassword(plainPassword, hashedPassword) mustBe true
        }
//
        "fail password validation if passwords don't match" in {
            val userRepository = new UserRepository {}

            val plainPassword = "Password123"
            val hashedPassword = userRepository.hashPassword(plainPassword)

            // Validate that a different password does not match the hashed password
            userRepository.checkPassword("WrongPassword", hashedPassword) mustBe false
        }
    }
}
