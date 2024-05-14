package dev.zio.quickstart

import dev.zio.quickstart.model.Order
import dev.zio.quickstart.model.OrderId
import dev.zio.quickstart.repository.OrderRepository
import zio._
import zio.test.Assertion._
import zio.test._
import java.time.Instant

object DatabaseSpec extends MigratedDatabaseSpec {

  def spec = suite("DatabaseSpec")(
    test("OrderRepository.get should return a created order") {
      val date = Instant.now()
      for {
        _     <- OrderRepository.create(Order(OrderId(1L), date))
        order <- OrderRepository.get(OrderId(1L))
      } yield assertTrue(order.get.orderDate == date)
    }
  )

}
