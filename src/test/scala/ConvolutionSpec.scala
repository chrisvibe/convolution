package Ex0

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}
import TestUtils._

class ConvolutionSpec extends FlatSpec with Matchers {
  import ConvolutionTests._

  val height = 6
  val width = 6
  val kernelSize = 3


  behavior of "KernelConvolution"

  it should "Convolute an image the size of the kernel, output a pixel value" in {
    wrapTester(
      // modified to generate vcd output
      // chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new KernelConvolution(kernelSize)) { c =>
      chisel3.iotesters.Driver(() => new KernelConvolution(kernelSize)) { c =>
        new SimpleDotProd(c)
      } should be(true)
    )
  }

  it should "Interrupt kernel convolution and start again" in {
    wrapTester(
      // modified to generate vcd output
      // chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new KernelConvolution(kernelSize)) { c =>
      chisel3.iotesters.Driver(() => new KernelConvolution(kernelSize)) { c =>
        new ResetDotProd(c)
      } should be(true)
    )
  }

}

// waveform output 
// trying to produce vcd as per example above
// replace:
// chisel3.iotesters.Driver(() => new MatMul(rowDims, colDims)) { c =>
  // with:
// chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new KernelConvolution(kernelSize)) { c =>

object ConvolutionTests {

  class SimpleDotProd(c: KernelConvolution) extends PeekPokeTester(c) {

    println("runnig dot prod calc with inputs:")
    // val area = List.fill(c.kernelSize)(rand.nextInt(10))
    // val kernel = List.fill(c.kernelSize)(rand.nextInt(10)) // todo 1111
    val kernel: List[List[Int]] =
    List(
      List(1, 1, 1),
      List(1, 1, 1),
      List(1, 1, 1)
    )
    val area: List[List[Int]] =
    List(
      List(1, 2, 3),
      List(1, 3, 2),
      List(3, 2, 1)
    )
    print(kernel.map(_.mkString).mkString("\n"))
    print("\n")
    print(area.map(_.mkString).mkString("\n"))
    print("\n")
    val expectedOutput = 18 

    // load kernel
    for(i <- 0 until c.kernelSize){
      for(j <- 0 until c.kernelSize){
        poke(c.io.kernelVal_in, kernel(i)(j))
        step(1)
      }
    }
    // calculate convolution
    for(i <- 0 until c.kernelSize){
      for(j <- 0 until c.kernelSize){
        poke(c.io.pixelVal_in, area(i)(j))
        if((i == c.kernelSize) && (j == c.kernelSize)){
         expect(c.io.pixelVal_out, expectedOutput)
        }
        step(1)
      }
    }
  }

  class ResetDotProd(c: KernelConvolution) extends PeekPokeTester(c) {

    println("testing reset:")
    // val area = List.fill(c.kernelSize)(rand.nextInt(10))
    // val kernel = List.fill(c.kernelSize)(rand.nextInt(10)) // todo 1111
    val kernel1: List[List[Int]] =
    List(
      List(1, 1, 1),
      List(0, 1, 0),
      List(0, 0, 1)
    )
    val kernel2: List[List[Int]] =
    List(
      List(1, 1, 1),
      List(1, 1, 1),
      List(1, 1, 1)
    )
    val area: List[List[Int]] =
    List(
      List(1, 2, 3),
      List(1, 3, 2),
      List(3, 2, 1)
    )
    print(area.map(_.mkString).mkString("\n"))
    print("\n")
    val expectedOutput1 = 9 
    val expectedOutput2 = 18 

    // load kernel
    for(i <- 0 until c.kernelSize){
      for(j <- 0 until c.kernelSize){
        poke(c.io.kernelVal_in, kernel1(i)(j))
        step(1)
      }
    }
    // calculate convolution (interrupted by 1 row)
    for(i <- 0 until c.kernelSize - 1){
      for(j <- 0 until c.kernelSize){
        poke(c.io.pixelVal_in, area(i)(j))
        if((i == c.kernelSize) && (j == c.kernelSize)){
         expect(c.io.pixelVal_out, expectedOutput1)
        }
        step(1)
      }
    }

    // interrupt mid-execution
    poke(c.io.reset, true.B)
    expect(c.io.pixelVal_out, 0.U)
    step(1)
    poke(c.io.reset, false.B)
    expect(c.io.pixelVal_out, 0.U)

    // load kernel again
    for(i <- 0 until c.kernelSize){
      for(j <- 0 until c.kernelSize){
        poke(c.io.kernelVal_in, kernel2(i)(j))
        step(1)
      }
    }
    // calculate convolution again
    for(i <- 0 until c.kernelSize){
      for(j <- 0 until c.kernelSize){
        poke(c.io.pixelVal_in, area(i)(j))
        if((i == c.kernelSize) && (j == c.kernelSize)){
         expect(c.io.pixelVal_out, expectedOutput2)
        }
        step(1)
      }
    }

  }

}
