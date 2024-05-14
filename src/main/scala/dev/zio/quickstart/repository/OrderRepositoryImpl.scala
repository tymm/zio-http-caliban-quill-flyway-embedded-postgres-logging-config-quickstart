package dev.zio.quickstart.repository

import dev.zio.quickstart.db.PostgresDataSource
import dev.zio.quickstart.model.Order
import dev.zio.quickstart.model.OrderId
import io.getquill._
import zio.IO
import zio.ZIO
import zio.ZLayer

import java.sql.SQLException

import Order._

object Context extends PostgresZioJdbcContext(SnakeCase)

case class OrderRepositoryImpl(ds: PostgresDataSource) extends OrderRepository:

  import Context._

  override def get(id: OrderId): IO[SQLException, Option[Order]] = {
    inline def q = quote {
      querySchema[Order]("order_table").filter(_.id == lift(id))
    }
    run(q).map(_.headOption).provideEnvironment(ds.env)
  }

  override def getAll(ids: List[OrderId]): IO[SQLException, List[Order]] = {
    inline def q = quote {
      querySchema[Order]("order_table").filter(o => liftQuery(ids).contains(o.id))
    }
    run(q).provideEnvironment(ds.env)
  }

  override def getLatest(count: Int): IO[SQLException, List[Order]] = {
    inline def q = quote {
      querySchema[Order]("order_table").sortBy(_.id)(Ord.desc).take(lift(count))
    }
    run(q).provideEnvironment(ds.env)
  }

  override def create(order: Order): IO[SQLException, Order] = {
    inline def q = quote {
      querySchema[Order]("order_table").insertValue(lift(order)).returning(_.id)
    }
    run(q).map(id => order.copy(id = id)).provideEnvironment(ds.env)
  }

  override def update(order: Order): IO[SQLException, Order] = {
    inline def q = quote {
      querySchema[Order]("order_table").filter(_.id.value == lift(order.id.value)).updateValue(lift(order))
    }
    run(q).map(_ => order).provideEnvironment(ds.env)
  }

  override def delete(id: OrderId): IO[SQLException, Unit] = {
    inline def q = quote {
      querySchema[Order]("order_table").filter(_.id == lift(id)).delete
    }
    run(q).unit.provideEnvironment(ds.env)
  }

  override def dropTable(): IO[SQLException, Unit] = {
    inline def q = quote {
      querySchema[Order]("order_table").delete
    }
    run(q).unit.provideEnvironment(ds.env)
  }

object OrderRepositoryImpl:

  val layer = ZLayer.fromFunction(OrderRepositoryImpl(_))
