package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class KernelConvolution(val kernelSize: Int, val nModules: Int) extends MultiIOModule {
    
    val io = IO(
        new Bundle {
            val kernelVal_in  = Input(UInt(16.W))
            val pixelVal_in   = Input(Vec(nModules, UInt(16.W))) 
            
            
            val pixelVal_out  = Output(Vec(nModules, UInt(16.W)))
            val valid_out     = Output(Bool())
        }
    )
    
    // Convolution on an area the size of the kernel -> spit out a single rgb pixel (r, g, or b - one of them)
    
    val dotProdCalc   = VecInit(Seq.fill(nModules)(Module(new DotProd(kernelSize * kernelSize)).io))
    
    // init regs and outputs
    for(i <- 0 until nModules){
        io.pixelVal_out(i)      := 0.U
        dotProdCalc(i).dataInA  := 0.U
        dotProdCalc(i).dataInB  := 0.U
    }
    
    for(i <- 0 until nModules){
        dotProdCalc(i).dataInA   := io.pixelVal_in(i)
        dotProdCalc(i).dataInB   := io.kernelVal_in
        io.pixelVal_out(i)       := dotProdCalc(i).dataOut
    }
    io.valid_out             := dotProdCalc(0).outputValid // one represents all
}

