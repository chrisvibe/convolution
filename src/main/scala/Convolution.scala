package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class Convolution(val kernelSize: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val kernelVal_in  = Input(UInt(1.W))  // connected to mem 8 (8 bits per colour) // TODO make this float???
      val pixelVal_in   = Input(UInt(8.W))  // connected to mem 8 (8 bits per colour)
      val valid_in      = Input(Bool())

      val pixelVal_out  = Output(UInt(8.W)) // connected to mem 8 (8 bits per colour)
      val valid_out     = Output(Bool())
    }
  )

  // Convolution on an area the size of the kernel -> spit out a single rgb pixel (r, g, or b - one of them)
  // 1. Load kernel
  // 2. Calculate dotproduct
  // 3. Repeat

  // declare variables and counter regs
  val (j, reset_j)    = Counter.apply(true.B, kernelSize) // TODO only start again on reset signal
  val (i, reset_i)    = Counter.apply(reset_j, kernelSize)
  val master_counter  = Counter(kernelSize+1)
  val kernel          = Module(new Matrix(kernelSize, kernelSize)).io
  val dotProdCalc     = Module(new DotProd(kernelSize)).io
  val kernelReady     = RegInit(Bool(), false.B)
  val rowSum          = RegInit(UInt(8.W), 0.U) // think about spillover/normilization

  // init regs
  io.pixelVal_out     := 0.U
  io.valid_out        := false.B
  kernel.writeEnable  := true.B
  dotProdCalc.dataInA := 0.U
  dotProdCalc.dataInB := 0.U

  // fill image and make kernel TODO get kernel from somewhere
  kernel.rowIdx   := i
  kernel.colIdx   := j
  kernel.dataIn   := io.kernelVal_in 

  dotProdCalc.dataInA := io.pixelVal_in // TODO delete image?? just load kernel???
  dotProdCalc.dataInB := kernel.dataOut 
  
  when (reset_i && reset_j) {
    // if kernel is a (nxm) matrix, and a stage is an access to all (nxm) entries
    // one stage dedicated to loading kernel, n stages dedicated to dot_prod
    // pixel vals are inputed via io as needed
    // total n+1 stages 
    kernelReady := !master_counter.inc()
  }

  when (kernelReady) {
    kernel.writeEnable    := false.B
    // dotproduct
    dotProdCalc.dataInA := io.pixelVal_in
    kernel.rowIdx := master_counter.value - 1.U
    dotProdCalc.dataInB := kernel.dataOut
    when(dotProdCalc.outputValid) {
      rowSum := rowSum + dotProdCalc.dataOut
      io.valid_out := dotProdCalc.outputValid // TODO only ouput when done
      io.pixelVal_out := rowSum 
    }
  }
}
