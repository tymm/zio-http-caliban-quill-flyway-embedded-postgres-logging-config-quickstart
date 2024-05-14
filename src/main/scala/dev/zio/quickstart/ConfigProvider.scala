package dev.zio.quickstart

import zio.Duration
import zio._
import zio.config._
import zio.config.magnolia._
import zio.config.magnolia.describe

object ConfigProvider {

  case class PostgresConfig(
      host: String,
      port: Int,
      user: String,
      password: Option[String],
      database: String,
      schema: Option[String],
      connectionTimeout: Duration,
      keepaliveTime: Option[Int],
      maximumPoolSize: Option[Int]
  )

  object PostgresConfig {

    val config: Config[PostgresConfig] = deriveConfig[PostgresConfig].nested("PostgresConfig")

    given DeriveConfig[Duration] =
      DeriveConfig[Long].map(Duration.fromSeconds(_))

    val live: ZLayer[Any, Config.Error, PostgresConfig] =
      ZLayer
        .fromZIO(
          ZIO.config[PostgresConfig](PostgresConfig.config)
        )

  }

}
