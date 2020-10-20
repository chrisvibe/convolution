package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class Convolution(val height: Int, val width: Int, val kernelSize: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val pixelVal_in   = Input(UInt(24.W))  // connected to mem 3x8 (8 bits per colour)
      val valid_in      = Input(Bool())

      val pixelVal_out  = Output(UInt(24.W)) // connected to mem 3x8 (8 bits per colour)
      val valid_out     = Output(Bool())
      val pixelAddr     = Output(UInt(8.W))  // mem is word addressed
    }
  )

  // read data 
  io.pixelAddr := 0.U
  io.valid_out := io.valid_in
  io.pixelVal_out := io.pixelVal_in

}
