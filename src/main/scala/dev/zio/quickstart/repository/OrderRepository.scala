package dev.zio.quickstart.repository

import dev.zio.quickstart.model.Order
import dev.zio.quickstart.model.OrderId
import zio.IO
import zio.ZIO

import java.sql.SQLException

trait OrderRepository:

  def get(id: OrderId): IO[SQLException, Option[Order]]
  def getAll(ids: List[OrderId]): IO[SQLException, List[Order]]
  def getLatest(count: Int): IO[SQLException, List[Order]]
  def create(Order: Order): IO[SQLException, Order]
  def update(Order: Order): IO[SQLException, Order]
  def delete(id: OrderId): IO[SQLException, Unit]
  def dropTable(): IO[SQLException, Unit]

object OrderRepository:

  def get(id: OrderId)           = ZIO.serviceWithZIO[OrderRepository](_.get(id))
  def getAll(ids: List[OrderId]) = ZIO.serviceWithZIO[OrderRepository](_.getAll(ids))
  def getLatest(count: Int)      = ZIO.serviceWithZIO[OrderRepository](_.getLatest(count))
  def create(order: Order)       = ZIO.serviceWithZIO[OrderRepository](_.create(order))
  def update(order: Order)       = ZIO.serviceWithZIO[OrderRepository](_.update(order))
  def delete(id: OrderId)        = ZIO.serviceWithZIO[OrderRepository](_.delete(id))
  def dropTable()                = ZIO.serviceWithZIO[OrderRepository](_.dropTable())
