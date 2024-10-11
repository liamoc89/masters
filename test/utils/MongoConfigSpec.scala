package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.{Configuration, Logging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await

class MongoConfigSpec extends AnyWordSpec with Matchers with MockitoSugar with Logging {

    "MongoConfig" should {
        "establish a connection to MongoDB" in {
            val mockConfig = mock[Configuration]
            when(mockConfig.get[String]("mongodb.uri")).thenReturn("mongodb://localhost:27017")
            when(mockConfig.get[String]("mongodb.dbName")).thenReturn("test_db")

            val mongoConfig = new MongoConfig(mockConfig)

            val connection = mongoConfig.getConnection

            val connectionResult = Await.result(connection, 5.seconds)

            connectionResult should not be null
            connectionResult.database("test_db") should not be null

        }

        "return the correct database name" in {
            val mockConfig = mock[Configuration]
            when(mockConfig.get[String]("mongodb.uri")).thenReturn("mongodb://localhost:27017")
            when(mockConfig.get[String]("mongodb.dbName")).thenReturn("test_db")

            val mongoConfig = new MongoConfig(mockConfig)

            mongoConfig.getDatabaseName shouldBe "test_db"
        }

        "close the connection properly" in {
            val mockConfig = mock[Configuration]
            when(mockConfig.get[String]("mongodb.uri")).thenReturn("mongodb://localhost:27017")
            when(mockConfig.get[String]("mongodb.dbName")).thenReturn("test_db")

            val mongoConfig = new MongoConfig(mockConfig)
            val closeFuture = mongoConfig.close()
            val closeResult: Unit = Await.result(closeFuture, 5.seconds)

            closeResult shouldBe (())
            logger.info("Connection closed successfully")



        }
    }
}
