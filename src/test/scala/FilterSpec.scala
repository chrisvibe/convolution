package Ex0

import chisel3._
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{Matchers, FlatSpec}
import TestUtils._

class FilterSpec extends FlatSpec with Matchers {
  import FilterTests._

  val parallelFIlters = 6

  behavior of "FilterSpec"

      // modified to generate vcd output
      // chisel3.iotesters.Driver(() => new KernelConvolution(kernelSize, nModules)) { c =>
      
  it should "Filter" in {
    wrapTester(
      chisel3.iotesters.Driver.execute(Array("--generate-vcd-output", "on", "--backend-name", "treadle"), () => new Filter(parallelFIlters)) { c =>
        new Test(c)
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

  class Test(c: Filter) extends PeekPokeTester(c) {

    println("runnig filter...................")
    poke(c.io.test_in, true.B)
    step(1)
    expect(c.io.test_out, true.B)
    
  }
}
