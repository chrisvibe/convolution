package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class Filter(val parallelPixels: Int) extends MultiIOModule {

    val io = IO(
        new Bundle {
            // input fra SPI
            val test_in         = Input(Bool())
            
            val pixelVal_out    = Output(Vec(parallelPixels, UInt(16.W)))
            val valid_out       = Output(Bool())
            val test_out        = Output(Bool())
        }
    )
    
    val colorInvert = RegInit(Bool(), false.B)
  
    val kernelIndex = RegInit(UInt(6.W), 0.U)
  
    val kernels = VecInit(
        VecInit(
            0.U(16.W), 0.U(16.W), 0.U(16.W),
            0.U(16.W), 1.U(16.W), 0.U(16.W),
            0.U(16.W), 0.U(16.W), 0.U(16.W)
        ),
        VecInit(
            0.U(16.W), 0.U(16.W), 0.U(16.W),
            0.U(16.W), 0.U(16.W), 0.U(16.W),
            0.U(16.W), 0.U(16.W), 0.U(16.W)
        )
    )
    val kernelSums = VecInit(
        RegInit(UInt(32.W), 1.U),
        RegInit(UInt(32.W), 1.U),
    )
  
    val image = VecInit(
        200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W),
        100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W),
        50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W),
        200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W),
        100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W),
        50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W)
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
        when((imageCounterX + i.U + pixelReached < 1.U) || (imageCounterX + i.U + pixelReached > imageWidth.U) || (imageCounterY < 1.U) || (imageCounterY > imageHeight.U) || i.U + pixelReached >= (imageWidth * imageHeight).U){
            kernelConvolution.pixelVal_in(i) := 0.U
            printf("pixel_in%d: %d (out of bounds)\n", i.U, 0.U);
        }.otherwise{
            kernelConvolution.pixelVal_in(i) := image((imageCounterY - 1.U) * imageWidth.U + imageCounterX - 1.U + i.U + pixelReached)
            printf("pixel_in%d: %d\n", i.U, image((imageCounterY - 1.U) * imageWidth.U + imageCounterX - 1.U + i.U + pixelReached));
        }
        io.pixelVal_out(i) := kernelConvolution.pixelVal_out(i)
    }
    
    for(i <- 0 until parallelPixels){
        when(colorInvert){
            io.pixelVal_out(i) := 255.U - kernelConvolution.pixelVal_out(i)
        }.otherwise{
            io.pixelVal_out(i) := kernelConvolution.pixelVal_out(i)
        }
    }
    
    io.valid_out := kernelConvolution.valid_out 
    
    when(kernelCountReset){
        pixelReached := pixelReached + parallelPixels.U
    }
    io.test_out := io.test_in
    // printf("in filter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
}
