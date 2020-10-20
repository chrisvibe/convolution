package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class KernelConvolution(val kernelSize: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val kernelVal_in  = Input(UInt(1.W))  // connected to mem 8 (8 bits per colour) // TODO make this float???
      val pixelVal_in   = Input(UInt(8.W))  // connected to mem 8 (8 bits per colour)
      val reset         = Input(Bool())     // re-load kernel and continue covolveing
      val valid_in      = Input(Bool())     // pause everything when this is false

      val pixelVal_out  = Output(UInt(8.W)) // connected to mem 8 (8 bits per colour)
      val valid_out     = Output(Bool())
    }
  )

  // Convolution on an area the size of the kernel -> spit out a single rgb pixel (r, g, or b - one of them)
  // 1. Load kernel
  // 2. Calculate dotproduct
  // 3. Repeat

  // declare variables and counter regs
  // val counter         = new Counter(kernelSize*kernelSize)
  val (countVal, countWrap) = Counter(true.B, kernelSize*kernelSize)
  // val (countVal, countWrap) = withReset(reset.asBool || io.reset)(Counter(io.valid_in,kernelSize*kernelSize))
  val kernel          = Module(new Matrix(kernelSize, kernelSize)).io
  val dotProdCalc     = Module(new DotProd(kernelSize)).io
  val kernelReady     = RegInit(Bool(), false.B)
  val rowSum          = RegInit(UInt(8.W), 0.U) // think about spillover/normilization
  
  when (io.reset) {
    countVal := 0.U
    kernelReady := false.B 
  }

  when(countVal === kernelSize.U) {
    kernelReady := true.B 
  }

  // init regs
  io.pixelVal_out     := 0.U
  io.valid_out        := false.B
  kernel.writeEnable  := true.B
  dotProdCalc.dataInA := 0.U
  dotProdCalc.dataInB := 0.U

  // fill image and make kernel TODO get kernel from somewhere
  // kernel.rowIdx   := i
  // kernel.colIdx   := j
  kernel.rowIdx   := countVal / kernelSize.U
  kernel.colIdx   := countVal % kernelSize.U
  kernel.dataIn   := io.kernelVal_in 

  dotProdCalc.dataInA := io.pixelVal_in // TODO delete image?? just load kernel???
  dotProdCalc.dataInB := kernel.dataOut 
  
  // when (reset_i && reset_j && !io.reset) {
  //   // if kernel is a (nxm) matrix, and a stage is an access to all (nxm) entries
  //   // one stage dedicated to loading kernel, n stages dedicated to dot_prod
  //   // pixel vals are inputed via io as needed
  //   // total n+1 stages 
  //   kernelReady := !master_counter.inc()
  // }

  when (kernelReady) {
    kernel.writeEnable    := false.B
    // dotproduct
    dotProdCalc.dataInA := io.pixelVal_in
    kernel.rowIdx := (countVal / kernelSize.U) - 1.U // row - 1
    dotProdCalc.dataInB := kernel.dataOut
    when(dotProdCalc.outputValid) {
      rowSum := rowSum + dotProdCalc.dataOut
      io.valid_out := dotProdCalc.outputValid // TODO only output when done
      io.pixelVal_out := rowSum 
    }
  }
}
