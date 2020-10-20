package Ex0

import chisel3._
import chisel3.util.Counter
import chisel3.experimental.MultiIOModule

class MatMul(val rowDimsA: Int, val colDimsA: Int) extends MultiIOModule {

  val io = IO(
    new Bundle {
      val dataInA     = Input(UInt(32.W))
      val dataInB     = Input(UInt(32.W))

      val dataOut     = Output(UInt(32.W))
      val outputValid = Output(Bool())
    }
  )

  val debug = IO(
    new Bundle {
      val myDebugSignal = Output(Bool())
    }
  )


  /**
    * Your code here
    */

  // Init datastructures and counters
  
  // j++ every cycle, i++ on j reset (like reading left->right)
  val (j, reset_j) = Counter.apply(true.B, colDimsA)
  val (i, reset_i) = Counter.apply(reset_j, rowDimsA)
  val master_counter = Counter(rowDimsA+1)
  val matrixA     = Module(new Matrix(rowDimsA, colDimsA)).io
  val matrixB     = Module(new Matrix(rowDimsA, colDimsA)).io
  val dotProdCalc = Module(new DotProd(colDimsA)).io
  val multiply_stage = RegInit(Bool(), false.B)

  // init regs
  io.outputValid := false.B
  dotProdCalc.dataInA := 0.U
  dotProdCalc.dataInB := 0.U
  io.dataOut := 0.U
  matrixA.writeEnable := false.B
  matrixB.writeEnable := false.B

  // select matrices for loading
  // input for B is supplied transposed
  // pattern to achieve two (nxm) matrices:
  // A, B.T: row, col++
  matrixA.rowIdx := i
  matrixA.colIdx := j
  matrixB.rowIdx := i
  matrixB.colIdx := j

  // ordering note: index is selected before we connect these
  // safe as write is only enabled on load
  // TODO but how do we avoid "smearing" the index on write?
  matrixA.dataIn := io.dataInA
  matrixB.dataIn := io.dataInB

  // flip between load|calc stages
  // stages are not the same length:
  // load: nxm cycles (A/B in parallel)
  // calc:  
  when (reset_i && reset_j) {
    // if A is a (nxm) matrix, and a stage is an access to all (nxm) entries
    // one stage dedicated to loading, n stages dedicated to dot_prod
    // total n+1 stages 
    multiply_stage := !master_counter.inc()
  }

  when (multiply_stage) {
    debug.myDebugSignal := true.B
    // dotproduct
    matrixA.rowIdx := master_counter.value - 1.U
    dotProdCalc.dataInA := matrixA.dataOut
    dotProdCalc.dataInB := matrixB.dataOut
    io.dataOut := dotProdCalc.dataOut 
    io.outputValid := dotProdCalc.outputValid 
  } otherwise {
    debug.myDebugSignal := false.B
    // load matrices A and B
    matrixA.writeEnable := true.B
    matrixB.writeEnable := true.B
  }
}
