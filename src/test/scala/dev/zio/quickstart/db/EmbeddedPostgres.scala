package dev.zio.quickstart.db

import dev.zio.quickstart.ConfigProvider.PostgresConfig
import io.zonky.test.db.postgres.embedded.{EmbeddedPostgres => ZonkyEmbeddedPostgres}
import zio.Layer
import zio.ZIO
import zio.ZLayer
import zio.test.TestFailure

object EmbeddedPostgres {

  val layer: Layer[TestFailure[Nothing], PostgresConfig & PostgresDataSource] =
    (
      ZLayer.scoped[Any](
        ZIO.logDebug("Starting Postgres") *>
          ZIO
            .acquireRelease(ZIO.attemptBlocking(ZonkyEmbeddedPostgres.start()))(postgres =>
              ZIO.attemptBlocking(postgres.close()).orDie
            )
            .map { postgres =>
              import zio.durationInt
              PostgresConfig(
                user = "postgres",
                password = Some("postgres"),
                host = "localhost",
                port = postgres.getPort,
                database = "postgres",
                schema = None,
                connectionTimeout = 30.seconds,
                keepaliveTime = None,
                maximumPoolSize = None
              )
            } <* ZIO.logDebug("Postgres Started")
      ) >+> PostgresDataSource.live
    ).mapError(TestFailure.die)

}
