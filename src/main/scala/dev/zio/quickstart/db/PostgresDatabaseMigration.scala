package dev.zio.quickstart.db

import dev.zio.quickstart.ConfigProvider.PostgresConfig
import zio.URLayer
import zio.ZLayer

object PostgresDatabaseMigration {

  private val migrationParamLayer: URLayer[PostgresConfig & PostgresDataSource, MigrationParam] =
    ZLayer.fromFunction { (postgresConfig: PostgresConfig, postgresDatasource: PostgresDataSource) =>
      MigrationParam(
        postgresDatasource.dataSource,
        postgresConfig.schema,
        FlywayLocation("db/migrations/postgres")
      )
    }

  val layer: URLayer[PostgresConfig & PostgresDataSource, DatabaseMigration] =
    ZLayer.makeSome[PostgresConfig & PostgresDataSource, DatabaseMigration](
      migrationParamLayer,
      DatabaseMigration.flyway
    )

}
