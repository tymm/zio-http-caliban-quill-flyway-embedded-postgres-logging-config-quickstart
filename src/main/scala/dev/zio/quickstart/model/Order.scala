package dev.zio.quickstart.model

import caliban.schema.Annotations.GQLValueType
import caliban.schema.Schema
import io.getquill._
import java.time.Instant

@GQLValueType(true)
final case class OrderId(value: Long)
final case class Order(id: OrderId, orderDate: Instant) derives Schema.SemiAuto

object Order {

  implicit val encodeOrderId: MappedEncoding[OrderId, Long] =
    MappedEncoding[OrderId, Long](_.value)
  implicit val decodeOrderId: MappedEncoding[Long, OrderId] =
    MappedEncoding[Long, OrderId](OrderId(_))

  given Schema[Any, OrderId] = Schema.longSchema.contramap(_.value)

}
