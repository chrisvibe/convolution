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
      chisel3.iotesters.Driver(() => new Convolution(height, width, kernelSize)) { c =>
        new TestExample(c)
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

    val mA = genMatrix(c.height, c.width)
    // val mC = matrixMultiply(mA, mB.transpose)

    // Input data
    for(ii <- 0 until c.height * c.width){
      val row = ii / c.width
      val col = ii % c.width
      poke(c.io.valid_in, false.B)
      poke(c.io.pixelVal_in, mA(row)(col))
      expect(c.io.pixelVal_out, mA(row)(col), "direct connection in/out")
      step(1)
    }

  }

  // class FullMatMul(c: Convolution) extends PeekPokeTester(c) {

  //   val mA = genMatrix(c.rowDimsA, c.colDimsA)
  //   val mB = genMatrix(c.rowDimsA, c.colDimsA)
  //   val mC = matrixMultiply(mA, mB.transpose)

  //   println("Multiplying")
  //   println(printMatrix(mA))
  //   println("With")
  //   println(printMatrix(mB.transpose))
  //   println("Expecting")
  //   println(printMatrix(mC))

  //   // Input data
  //   for(ii <- 0 until c.colDimsA * c.rowDimsA){

  //     val rowInputIdx = ii / c.colDimsA
  //     val colInputIdx = ii % c.colDimsA

  //     poke(c.io.dataInA, mA(rowInputIdx)(colInputIdx))
  //     poke(c.io.dataInB, mB(rowInputIdx)(colInputIdx))
  //     expect(c.io.outputValid, false, "Valid output during initialization")

  //     step(1)
  //   }

  //   // Perform calculation
  //   for(ii <- 0 until (c.rowDimsA * c.rowDimsA)){
  //     for(kk <- 0 until c.colDimsA - 1){
  //       expect(c.io.outputValid, false, "Valid output mistimed")
  //       step(1)
  //     }
  //     expect(c.io.outputValid, true, "Valid output timing is wrong")
  //     expect(c.io.dataOut, mC(ii / c.rowDimsA)(ii % c.rowDimsA), "Wrong value calculated")
  //     step(1)
  //   }
  // }
}
