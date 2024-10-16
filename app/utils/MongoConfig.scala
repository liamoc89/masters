package utils

import javax.inject._
import play.api.{Configuration, Logging}
import reactivemongo.api.{AsyncDriver, MongoConnection}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoConfig @Inject()(config: Configuration)(implicit ec: ExecutionContext) extends Logging {

    private val driver = AsyncDriver()

    private val mongoUri: String = config.get[String]("mongodb.uri")
    println(mongoUri)
    private val parsedUri: Future[MongoConnection.ParsedURI] = MongoConnection.fromString(uri=mongoUri)
    private val connection: Future[MongoConnection] = parsedUri.flatMap(uri => driver.connect(uri))

    parsedUri.onComplete {
        case scala.util.Success(uri) => logger.info(s"Parsed MongoDB URI: $uri")
        case scala.util.Failure(ex) => logger.error("Failed to parse MongoDB URI", ex)
    }


    def getConnection: Future[MongoConnection] = connection

    def close(): Future[Unit] = {
        connection.flatMap { conn =>
            conn.close()(5.seconds).map(_ => logger.info("MongoDB connection closed"))
        }
    }

}
