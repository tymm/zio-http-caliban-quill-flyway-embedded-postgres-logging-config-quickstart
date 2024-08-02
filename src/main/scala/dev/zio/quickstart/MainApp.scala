package dev.zio.quickstart

import caliban._
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import dev.zio.quickstart.Api.Queries
import dev.zio.quickstart.ConfigProvider.PostgresConfig
import dev.zio.quickstart.db.PostgresDataSource
import dev.zio.quickstart.repository.OrderRepository
import dev.zio.quickstart.repository.OrderRepositoryImpl
import zio._
import zio.logging.fileLogger
import zio.config.typesafe.TypesafeConfigProvider
import zio.http._

import scala.language.postfixOps

object MainApp extends ZIOAppDefault {

  val api = graphQL[OrderRepository, Queries, Unit, Unit](RootResolver(Api.resolver))

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> Runtime.setConfigProvider(
      TypesafeConfigProvider.fromResourcePath()
    ) >>> fileLogger()

  override def run =
    (for {
      handlers <- api.interpreter.map(QuickAdapter(_).handlers)
      routes = Routes(
                 Method.GET / "text"            -> handler(Response.text("Hello World!")),
                 Method.ANY / "api" / "graphql" -> handlers.api,
                 Method.GET / "graphql"         -> handler(Response.text(api.render))
               )
      _ <- Server.serve(routes)
      _ <- ZIO.logInfo("Server started on port 8080")
    } yield ()).provide(
      PostgresConfig.live,
      PostgresDataSource.live,
      OrderRepositoryImpl.layer,
      Server.defaultWithPort(8080)
    )

}
