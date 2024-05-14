package dev.zio.quickstart

import dev.zio.quickstart.ConfigProvider.PostgresConfig
import dev.zio.quickstart.db.DatabaseMigration
import dev.zio.quickstart.db.EmbeddedPostgres
import dev.zio.quickstart.db.PostgresDataSource
import dev.zio.quickstart.db.PostgresDatabaseMigration
import dev.zio.quickstart.repository._
import zio.Chunk
import zio.ZIO
import zio.ZLayer
import zio.internal.stacktracer.Tracer
import zio.test.TestAspect
import zio.test.TestAspectAtLeastR
import zio.test.TestEnvironment
import zio.test.ZIOSpec
import zio.test.testEnvironment

abstract class MigratedDatabaseSpec
    extends ZIOSpec[OrderRepository & DatabaseMigration & PostgresConfig & PostgresDataSource] {

  override val bootstrap = MigratedDatabaseSpec.layer

  override def aspects: Chunk[TestAspectAtLeastR[Environment & TestEnvironment]] =
    super.aspects ++ Chunk(
      TestAspect.before(ZIO.service[DatabaseMigration].flatMap(_.migrate()).orDie),
      TestAspect.after(ZIO.service[OrderRepository].flatMap(_.dropTable()).orDie)
    )

}

object MigratedDatabaseSpec {

  private val layer: ZLayer[
    Any,
    Any,
    OrderRepository & DatabaseMigration & PostgresConfig & PostgresDataSource & TestEnvironment
  ] = {
    implicit val trace: zio.Trace = Tracer.newTrace
    testEnvironment >+> EmbeddedPostgres.layer >+> PostgresDatabaseMigration.layer >+> OrderRepositoryImpl.layer
  }

}
