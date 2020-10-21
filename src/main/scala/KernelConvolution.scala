package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class KernelConvolution(val kernelSize: Int, val nModules: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val kernelVal_in  = Input(UInt(32.W)) // connected to stream from camera/hdmi (8 bits per colour)
      val pixelVal_in   = Input(Vec(nModules, UInt(8.W)))
      val reset         = Input(Bool())     // re-load kernel and continue convolving // TODO fix reset mechanism as it sucks 


      val pixelVal_out  = Output(Vec(nModules, UInt(8.W)))
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

  // declare array variables
  val dotProdCalc   = VecInit(Seq.fill(nModules)(Module(new DotProd(kernelSize*kernelSize)).io))
  val accumulator   = VecInit(Seq.fill(nModules)(RegInit(UInt(32.W), 0.U)))
  
  extReset := io.reset

  when (extReset) {
    countVal       := 0.U
    kernelReady    := false.B 
    for(i <- 0 until nModules){
      accumulator(i)  := 0.U
      dotProdCalc(i).reset := true.B
    }
  } .otherwise {
    for(i <- 0 until nModules){
      accumulator(i)  := 0.U
      dotProdCalc(i).reset := false.B
    }
  }

  when(countReset) {
    kernelReady := true.B 
  }

  // init regs and output
  for(i <- 0 until nModules){
    io.pixelVal_out(i)      := 0.U
    dotProdCalc(i).dataInA  := 0.U
    dotProdCalc(i).dataInB  := 0.U
  }

  io.valid_out        := false.B
  kernel.writeEnable  := true.B

  // Load kernel
  kernel.rowIdx   := countVal / kernelSize.U
  kernel.colIdx   := countVal % kernelSize.U
  kernel.dataIn   := io.kernelVal_in 

  when (kernelReady) { // feed convolution
    kernel.writeEnable       := false.B

    // TODO make loops everywhere
    for(i <- 0 until nModules){
      dotProdCalc(i).dataInA   := io.pixelVal_in(i)
      dotProdCalc(i).dataInB   := kernel.dataOut
      io.pixelVal_out(i)       := dotProdCalc(i).dataOut // TODO normalize this
    }
    io.valid_out             := dotProdCalc(0).outputValid // one represents all
  } .otherwise {
    for(i <- 0 until nModules){
      accumulator(i)  := 0.U
    }
  }
  // printf("here---- row %d, col %d, kernelVal %d, ready? %d, dotProd %d\n", countVal / kernelSize.U, countVal % kernelSize.U, kernel.dataOut, kernelReady, dotProdCalc.dataOut);

}
