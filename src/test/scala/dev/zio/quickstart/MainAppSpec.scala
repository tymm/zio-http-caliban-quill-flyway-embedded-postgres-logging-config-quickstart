package dev.zio.quickstart

import zio._
import zio.test._

import java.io.IOException

object HelloWorld {

  def sayHello: ZIO[Any, IOException, Unit] =
    Console.printLine("Hello, World!")

}

object MainAppSpec extends ZIOSpecDefault {

  import HelloWorld._

  def spec = suite("MainAppSpec")(
    test("sayHello correctly displays output") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assertTrue(output == Vector("Hello, World!\n"))
    }
  )

}
