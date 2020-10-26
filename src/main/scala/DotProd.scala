package Ex0

import chisel3._
import chisel3.util.Counter

class DotProd(val elements: Int) extends Module {

  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(16.W))
      val dataInB     = Input(UInt(16.W))

      val dataOut     = Output(UInt(16.W))
      val outputValid = Output(Bool())
    }
  )

  val (countVal, countReset)  = Counter(true.B, elements)
  val accumulator = RegInit(UInt(16.W), 0.U)
  val product = io.dataInA * io.dataInB
  accumulator := accumulator + product
  io.dataOut := accumulator + product

  when(countReset) { // here on overflow
    io.outputValid := true.B
    accumulator := 0.U
  } .otherwise {
    io.outputValid := false.B
  }
}
