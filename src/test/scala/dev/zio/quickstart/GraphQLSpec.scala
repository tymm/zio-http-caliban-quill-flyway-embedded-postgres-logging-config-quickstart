package dev.zio.quickstart

import caliban._
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import dev.zio.quickstart.Api.Queries
import zio._
import zio.test.Assertion._
import zio.test._
import dev.zio.quickstart.repository.OrderRepository
import dev.zio.quickstart.model.Order
import dev.zio.quickstart.model.OrderId
import java.time.Instant

object GraphQLSpec extends MigratedDatabaseSpec {

  private val api = graphQL[OrderRepository, Queries, Unit, Unit](RootResolver(Api.resolver))
  def spec = suite("GraphQLSpec")(
    test("Execute a GraphQL query") {
      for {
        order1      <- OrderRepository.create(Order(OrderId(1L), Instant.now()))
        order2      <- OrderRepository.create(Order(OrderId(2L), Instant.now()))
        interpreter <- api.interpreter
        result      <- interpreter.execute("query { orders(count: 2) { id } }")
      } yield assert(result.data.toString)(equalTo("""{"orders":[{"id":2},{"id":1}]}"""))
    }
  )

}
