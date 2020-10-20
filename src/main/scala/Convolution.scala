package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class Convolution(val kernelSize: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val pixelVal_in   = Input(UInt(24.W))  // connected to mem 3x8 (8 bits per colour)
      val valid_in      = Input(Bool())

      val pixelVal_out  = Output(UInt(24.W)) // connected to mem 3x8 (8 bits per colour)
      val valid_out     = Output(Bool())
      val pixelAddr     = Output(UInt(8.W))  // mem is word addressed
    }
  )

  // FOR NOW WE DO CONVOLUTION ON A KERNEL AREA, AND SPIT OUT A SINGLE PIXEL

  // declare variables and counter regs
  val (j, reset_j)    = Counter.apply(true.B, kernelSize)
  val (i, reset_i)    = Counter.apply(reset_j, kernelSize)
  val master_counter  = Counter(kernelSize+1)
  val area            = Module(new Matrix(kernelSize, kernelSize)).io // TODO add dimension for colour, black and white for now
  val kernel          = Module(new Matrix(kernelSize, kernelSize)).io
  val dotProdCalc     = Module(new DotProd(kernelSize)).io
  val multiply_stage  = RegInit(Bool(), false.B)

  // init regs
  io.pixelVal_out     := 0.U
  io.valid_out        := false.B
  io.pixelAddr        := 0.U
  // area.rowIdx         := 0.U 
  // area.colIdx         := 0.U 
  // area.dataIn         := 0.U 
  area.writeEnable    := true.B
  // kernel.rowIdx       := 0.U 
  // kernel.colIdx       := 0.U 
  // kernel.dataIn       := 0.U 
  kernel.writeEnable  := true.B
  dotProdCalc.dataInA := 0.U
  dotProdCalc.dataInB := 0.U

  // fill image and make kernel TODO get kernel from somewhere
  area.rowIdx     := i
  area.colIdx     := j
  area.dataIn     := io.pixelVal_in 
  kernel.rowIdx   := i
  kernel.colIdx   := j
  kernel.dataIn   := 1.U 


  dotProdCalc.dataInA := io.pixelVal_in // TODO delete image?? just load kernel???
  dotProdCalc.dataInB := 1.U
  
  when (reset_i && reset_j) {
    // if area is a (nxm) matrix, and a stage is an access to all (nxm) entries
    // one stage dedicated to loading, n stages dedicated to dot_prod
    // total n+1 stages 
    multiply_stage := !master_counter.inc()
  }

  when (multiply_stage) {
    kernel.writeEnable    := false.B
    area.writeEnable      := false.B
    // dotproduct
    area.rowIdx := master_counter.value - 1.U
    dotProdCalc.dataInA := area.dataOut
    dotProdCalc.dataInB := kernel.dataOut
  }

  // set output
  io.pixelAddr := 0.U
  io.valid_out := dotProdCalc.outputValid 
  io.pixelVal_out := dotProdCalc.dataOut 
}
