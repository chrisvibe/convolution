package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class KernelConvolution(val kernelSize: Int, val nModules: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val kernelVal_in  = Input(UInt(32.W)) // connected to stream from camera/hdmi (8 bits per colour)
      val valid_in      = Input(Bool())     // pause everything when this is false
      // val pixelVal_in   = Input(UInt(8.W))  // connected to stream from camera/hdmi (8 bits per colour)
      val pixelVal_in   = Input(Vec(2, UInt(8.W)))
      val reset         = Input(Bool())     // re-load kernel and continue convolving


      // val pixelVal_out  = Output(UInt(8.W)) // connected to hdmi-out (8 bits per colour)
      val pixelVal_out  = Output(Vec(2, UInt(8.W)))
      val valid_out     = Output(Bool())
    }
  )

  // Convolution on an area the size of the kernel -> spit out a single rgb pixel (r, g, or b - one of them)
  // 1. Load kernel 
  // 2. Calculate dotproduct
  // 3. Repeat

  // declare shared variables and counter regs
  val (countVal, countReset)  = Counter(true.B, kernelSize*kernelSize)
  val kernelReady   = RegInit(Bool(), false.B)
  val kernel        = Module(new Matrix(kernelSize, kernelSize)).io
  val extReset      = RegInit(Bool(), false.B)

  // declare dynamic variables
  // val dotProdCalc     = VecInit(Seq.Fill(2)(Module(new DotProd(kernelSize*kernelSize)).io))
  val dotProdCalc     = Module(new DotProd(kernelSize*kernelSize)).io
  val accumulator     = RegInit(UInt(32.W), 0.U)  // Think about how big this should be
  
  extReset := io.reset
  dotProdCalc.reset := false.B

  when (extReset) {
    countVal    := 0.U
    kernelReady := false.B 
    accumulator := 0.U
    dotProdCalc.reset := true.B
  }

  // count sum of area to normalize output matrix
  accumulator := accumulator + io.pixelVal_in(0)

  when(countReset) {
    kernelReady := true.B 
  }

  // init regs and output
  io.pixelVal_out(0)  := 0.U
  io.pixelVal_out(1)  := 16.U
  io.valid_out        := false.B
  dotProdCalc.dataInA := 0.U
  dotProdCalc.dataInB := 0.U
  kernel.writeEnable  := true.B

  // load kernel and feed convolution
  kernel.rowIdx   := countVal / kernelSize.U
  kernel.colIdx   := countVal % kernelSize.U
  kernel.dataIn   := io.kernelVal_in 
  
  when (kernelReady) {
    kernel.writeEnable    := false.B
    dotProdCalc.dataInA   := io.pixelVal_in(0)
    dotProdCalc.dataInB   := kernel.dataOut
    io.pixelVal_out(0)    := dotProdCalc.dataOut // TODO normalize this
    io.valid_out          := dotProdCalc.outputValid
  }
  // printf("here---- row %d, col %d, kernelVal %d, ready? %d, dotProd %d\n", countVal / kernelSize.U, countVal % kernelSize.U, kernel.dataOut, kernelReady, dotProdCalc.dataOut);

}
