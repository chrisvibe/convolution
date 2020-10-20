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


  behavior of "Convolution"

  it should "Convolute an image" in {
    wrapTester(
      // modified to generate vcd output
      // chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new Convolution(rowDims, colDims)) { c =>
      chisel3.iotesters.Driver(() => new Convolution(kernelSize)) { c =>
        new DotProd(c)
      } should be(true)
    )
  }

}

// waveform example from tutorial
// replace:
// chisel3.iotesters.Driver(() => new SimpleDelay) { c =>
// with:
// chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new SimpleDelay) { c =>

// trying to produce vcd as per example above
// replace:
// chisel3.iotesters.Driver(() => new MatMul(rowDims, colDims)) { c =>
  // with:
// chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new MatMul(rowDims, colDims)) { c =>

object ConvolutionTests {

  val rand = new scala.util.Random(100)

  class TestExample(c: Convolution) extends PeekPokeTester(c) {
    println("Convolution.......................................")

    val mA = genMatrix(c.kernelSize, c.kernelSize)
    // val mC = matrixMultiply(mA, mB.transpose)

    // Input data
    for(ii <- 0 until c.kernelSize * c.kernelSize){
      val row = ii / c.kernelSize
      val col = ii % c.kernelSize
      poke(c.io.valid_in, false.B)
      poke(c.io.pixelVal_in, mA(row)(col))
      expect(c.io.pixelVal_out, mA(row)(col), "direct connection in/out")
      step(1)
    }

  }

  class DotProd(c: Convolution) extends PeekPokeTester(c) {

    println("runnig dot prod calc with inputs:")
    // val area = List.fill(c.kernelSize)(rand.nextInt(10))
    // val kernel = List.fill(c.kernelSize)(rand.nextInt(10)) // todo 1111
    // val area: List[List[Int]] =
    // List(
      // List(1, 0, 0),
      // List(0, 1, 0),
      // List(0, 0, 1)
    // )
    // print(area.map(_.mkString).mkString("\n"))
    val inputsA: List[Int] = List(1, 2, 3) 
    val inputsB: List[Int] = List(1, 1, 1) 
    println(inputsA.mkString("[", "] [", "]"))
    println(inputsB.mkString("[", "] [", "]"))
    val expectedOutput = (for ((a, b) <- inputsA zip inputsB) yield a * b) sum

    for(ii <- 0 until c.kernelSize){
      poke(c.io.pixelVal_in, inputsA(ii))
      if(ii == c.kernelSize - 1)
        expect(c.io.pixelVal_out, expectedOutput)
      step(1)
    }
  }

}
