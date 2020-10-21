package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class Filter(val parallelPixels: Int) extends MultiIOModule {

    val io = IO(
        new Bundle {
            // input fra SPI
            
            val pixelVal_out   = Output(Vec(parallelPixels, UInt(8.W)))
            val valid_out      = Output(Bool())
        }
    )
    
    val colorInvert = RegInit(Bool(), false.B)
  
    val kernelIndex = RegInit(UInt(6.W), 0.U)
  
    val kernels = VecInit(
        VecInit(
            0.S(8.W), 0.S(8.W), 0.S(8.W),
            0.S(8.W), 1.S(8.W), 0.S(8.W),
            0.S(8.W), 0.S(8.W), 0.S(8.W)
        ),
        VecInit(
            0.S(8.W), 0.S(8.W), 0.S(8.W),
            0.S(8.W), 0.S(8.W), 0.S(8.W),
            0.S(8.W), 0.S(8.W), 0.S(8.W)
        )
    )
    val kernelSums = VecInit(
        RegInit(SInt(32.W), 1.S),
        RegInit(SInt(32.W), 1.S),
    )
  
    val image = VecInit(
        200.U(8.W), 100.U(8.W), 50.U(8.W), 200.U(8.W), 100.U(8.W), 50.U(8.W),
        100.U(8.W), 50.U(8.W), 200.U(8.W), 100.U(8.W), 50.U(8.W), 200.U(8.W),
        50.U(8.W), 200.U(8.W), 100.U(8.W), 50.U(8.W), 200.U(8.W), 100.U(8.W),
        200.U(8.W), 100.U(8.W), 50.U(8.W), 200.U(8.W), 100.U(8.W), 50.U(8.W),
        100.U(8.W), 50.U(8.W), 200.U(8.W), 100.U(8.W), 50.U(8.W), 200.U(8.W),
        50.U(8.W), 200.U(8.W), 100.U(8.W), 50.U(8.W), 200.U(8.W), 100.U(8.W)
    )

    val kernelConvolution = Module(new KernelConvolution(kernelSize, parallelPixels)).io
    
    val kernelSize = 3
    var imageWidth = 6
    var imageHeight = 6
    
    val (kernelCounter, kernelCountReset)  = Counter(true.B, kernelSize * kernelSize)
    kernelConvolution.kernelVal_in := kernels(kernelIndex)(kernelCounter)
    
    val (imageCounterX, imageCounterXReset) = Counter(true.B, kernelSize)
    val (imageCounterY, imageCounterYReset) = Counter(imageCounterXReset, kernelSize)
    val pixelReached = RegInit(UInt(32.W), 0.U)
    for (i <- 0 until parallelPixels){
        when((imageCounterX - 1 + i + pixelReached < 0) || (imageCounterX - 1 + i + pixelReached >= imageWidth) || (imageCounterY - 1 < 0) || (imageCounterY - 1 >= imageHeight) || i + pixelReached >= imageWidth * imageHeight){
            kernelConvolution.pixelVal_in(i) := 0.U
        }.otherwise{
            kernelConvolution.pixelVal_in(i) := image((imageCounterY - 1) * imageWidth + imageCounterX - 1 + i + pixelReached)
        }
    }
    
    for(i <- 0 until parallelPixels){
        when(colorInvert){
            pixelVal_out(i) := 255.U - kernelConvolution.pixelVal_out(i)
        }.otherwise{
            pixelVal_out(i) := kernelConvolution.pixelVal_out(i)
        }
    }
    
    valid_out := false.B
    
    when(kernelCounterReset){
        pixelReached := pixelReached + parallelPixels
        valid_out := true.B
    }
}
