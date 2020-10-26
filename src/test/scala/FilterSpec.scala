package Ex0

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}
import TestUtils._

class FilterSpec extends FlatSpec with Matchers {
  import FilterTests._

  val parallelPixels = 1

  behavior of "FilterSpec"

      // modified to generate vcd output
      // chisel3.iotesters.Driver(() => new KernelConvolution(kernelSize, nModules)) { c =>
      
  it should "Filter" in {
    wrapTester(
      chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new Filter(parallelPixels)) { c =>
        new IdentityTest(c)
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

object FilterTests {

  class IdentityTest(c: Filter) extends PeekPokeTester(c) {

    val kernelSize = 3

    val image: List[UInt] = List( 
        200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W),
        100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W),
        50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W),
        200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W),
        100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W),
        50.U(16.W), 200.U(16.W), 100.U(16.W), 50.U(16.W), 200.U(16.W), 100.U(16.W)
    )

    println("running filter...................")
    poke(c.io.test_in, true.B)
    step(kernelSize + 1)
    expect(c.io.test_out, true.B)

    for(i <- 0 until c.parallelPixels){
      expect(c.io.pixelVal_out(i), image(i))
    }
  }
}
