package dev.zio.quickstart.db

import org.flywaydb.core.Flyway
import zio.Task
import zio.URLayer
import zio.ZIO
import zio.ZLayer

import javax.sql.DataSource
import scala.util.chaining.scalaUtilChainingOps

final private[db] case class FlywayLocation(directory: String)
final private[db] case class MigrationParam(datasource: DataSource, schema: Option[String], location: FlywayLocation)

trait DatabaseMigration {

  /** The `target` parameter should probably never be changed except in tests.
    *
    * See https://documentation.red-gate.com/fd/target-184127546.html
    */
  def migrate(target: String = "latest"): Task[Unit]

}

object DatabaseMigration {

  val flyway: URLayer[MigrationParam, DatabaseMigration] = ZLayer.fromFunction(new FlywayMigrationService(_))

}

final class FlywayMigrationService(param: MigrationParam) extends DatabaseMigration {

  override def migrate(target: String = "latest"): Task[Unit] =
    ZIO.attemptBlocking {
      val config =
        Flyway
          .configure()
          .locations(param.location.directory)
          .dataSource(param.datasource)
          .pipe(config => param.schema.fold(config)(schema => config.defaultSchema(schema).createSchemas(true)))
          .pipe(_.target(target))

      config
        .load()
        .migrate()
    }

}
