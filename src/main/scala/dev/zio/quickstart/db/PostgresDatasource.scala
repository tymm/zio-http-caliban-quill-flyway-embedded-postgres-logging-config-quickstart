package dev.zio.quickstart.db

import com.typesafe.config.ConfigFactory
import dev.zio.quickstart.ConfigProvider.PostgresConfig
import io.getquill.jdbczio.Quill
import zio.URLayer
import zio.ZEnvironment
import zio.ZIO
import zio.ZLayer

import javax.sql.DataSource

final case class PostgresDataSource private (dataSource: DataSource) {

  val env: ZEnvironment[DataSource] = ZEnvironment(dataSource)

}

object PostgresDataSource {

  val live: URLayer[PostgresConfig, PostgresDataSource] = {
    import scala.jdk.CollectionConverters.MapHasAsJava

    ZLayer.fromZIO {
      for {
        config <- ZIO.service[PostgresConfig]
        datasource = Quill.DataSource
                       .fromConfig(
                         ConfigFactory.parseMap(
                           (
                             Map[String, Any](
                               "dataSourceClassName"     -> "org.postgresql.ds.PGSimpleDataSource",
                               "dataSource.user"         -> config.user,
                               "dataSource.serverName"   -> config.host,
                               "dataSource.portNumber"   -> config.port,
                               "dataSource.databaseName" -> config.database,
                               "connectionTimeout"       -> config.connectionTimeout.toMillis
                             )
                               ++ config.schema.fold(Map.empty)(schema => Map("dataSource.currentSchema" -> schema))
                               ++ config.password.fold(Map.empty)(passwd => Map("dataSource.password" -> passwd))
                               ++ config.keepaliveTime.fold(Map.empty)(keepAlive => Map("keepaliveTime" -> keepAlive))
                               ++ config.maximumPoolSize.fold(Map.empty)(maxPoolSize =>
                                 Map("maximumPoolSize" -> maxPoolSize)
                               )
                           ).asJava
                         )
                       )
                       .project(PostgresDataSource(_))
                       .orDie
      } yield datasource
    }.flatten
  }

}
