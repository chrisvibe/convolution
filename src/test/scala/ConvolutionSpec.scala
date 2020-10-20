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

  it should "Convolute an image the size of the kernel, output a pixel value" in {
    wrapTester(
      // modified to generate vcd output
      // chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new KernelConvolution(kernelSize)) { c =>
      chisel3.iotesters.Driver(() => new KernelConvolution(kernelSize)) { c =>
        new SimpleDotProd(c)
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
        if(i == c.kernelSize)
          expect(c.io.pixelVal_out, expectedOutput)
        step(1)
      }
    }
  }

  class reset(c: KernelConvolution) extends PeekPokeTester(c) {

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
        if(i == c.kernelSize)
          expect(c.io.pixelVal_out, expectedOutput)
        step(1)
      }
    }
  }

}
