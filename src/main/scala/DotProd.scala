package Ex0

import chisel3._
import chisel3.util.Counter

class DotProd(val elements: Int) extends Module {

  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(32.W))
      val dataInB     = Input(UInt(32.W))

      val dataOut     = Output(UInt(32.W))
      val outputValid = Output(Bool())
    }
  )


  /**
    * Your code here
    */
  // TODO needed? avoid false results...
  io.outputValid := false.B
  io.dataOut := 0.U

  val counter = Counter(elements)
  // val counter = Counter(elements+1)
  val accumulator = RegInit(UInt(32.W), 0.U)
  val product = io.dataInA * io.dataInB
  accumulator := accumulator + product

  when(counter.inc()) { // here on overflow
    // ??? why not add a 1 to elements and ignore this?
    // val product = io.dataInA * io.dataInB 
    // io.dataOut := accumulator
    io.dataOut := accumulator + product
    io.outputValid := true.B
    accumulator := 0.U
  }
}
