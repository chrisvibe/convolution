package Ex0

import chisel3._
import chisel3.util.Counter

class DotProd(val elements: Int) extends Module {

  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(32.W))
      val dataInB     = Input(UInt(32.W))
      val reset       = Input(Bool())

      val dataOut     = Output(UInt(32.W))
      val outputValid = Output(Bool())
    }
  )


  /**
    * Your code here
    */
  io.outputValid := false.B

  val (countVal, countReset)  = Counter(true.B, elements)
  // val counter = Counter(elements)
  val accumulator = RegInit(UInt(32.W), 0.U)
  when(io.reset) {
    countVal    := 0.U 
    accumulator := 0.U
    io.dataOut  := 0.U
  } .otherwise {
    val product = io.dataInA * io.dataInB
    accumulator := accumulator + product
    io.dataOut := accumulator + product

    when(countReset) { // here on overflow
      io.outputValid := true.B
      accumulator := 0.U
    }
  }
}
