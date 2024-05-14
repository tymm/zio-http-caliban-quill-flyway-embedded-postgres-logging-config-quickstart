package dev.zio.quickstart

import zio.Chunk
import zio.Exit
import zio.Trace
import zio.ZIO
import zio.query.CompletedRequestMap
import zio.query.DataSource
import zio.query.Request
import zio.query.ZQuery
import dev.zio.quickstart.repository.OrderRepository
import dev.zio.quickstart.model.Order
import dev.zio.quickstart.model.OrderId

object Api {

  case class OrderArgs(count: Int)
  case class Queries(orders: OrderArgs => ZQuery[OrderRepository, Nothing, List[Order]])

  def resolver: Queries =
    case class GetOrder(id: OrderId) extends Request[Throwable, Order]

    new DataSource.Batched[OrderRepository, GetOrder] {
      val identifier: String = "OrderDataSource"
      def run(
          requests: Chunk[GetOrder]
      )(implicit trace: Trace): ZIO[OrderRepository, Nothing, CompletedRequestMap] = {
        requests.toList match {
          case request :: Nil =>
            val result = OrderRepository.get(request.id).orDie
            result.foldCause(
              cause => CompletedRequestMap.single(request, Exit.failCause(cause)),
              optOrder =>
                optOrder.fold(
                  CompletedRequestMap.single(request, Exit.fail(new Exception("Order not found in database")))
                )(order => CompletedRequestMap.single(request, Exit.succeed(order)))
            )
          case batch =>
            val result =
              OrderRepository.getAll(batch.map(_.id)).orDie
            result.foldCause(
              CompletedRequestMap.failCause(requests, _),
              CompletedRequestMap.fromIterableWith(_)(
                kv => GetOrder(kv._1),
                kv => Exit.succeed(kv)
              )
            )
        }
      }
    }

    def getOrder(count: Int): ZQuery[OrderRepository, Nothing, List[Order]] =
      ZQuery
        .fromZIO(OrderRepository.getLatest(count).orDie)

    Queries(args => getOrder(args.count))

}
