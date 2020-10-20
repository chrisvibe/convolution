package Ex0

import chisel3._

// This import statement makes the scala vector invisible, reducing confusion
import scala.collection.immutable.{ Vector => _ }

class Matrix(val rowsDim: Int, val colsDim: Int) extends Module {

  val io = IO(
    new Bundle {
      val colIdx      = Input(UInt(32.W))
      val rowIdx      = Input(UInt(32.W))
      val dataIn      = Input(UInt(32.W))
      val writeEnable = Input(Bool())

      val dataOut     = Output(UInt(32.W))
    }
  )

  /**
    * Placeholders.
    *
    * You can delete these when you see fit, they're just here so
    * that the circuit is valid and compiles/synthesizes correctly.
    *
    * In your finished work these can be deleted.
    */
  // val rows = VecInit(Seq.fill(rowsDim)(Module(new Vector(colsDim)).io))

  // io.dataOut := 0.U
  // for(ii <- 0 until rowsDim){
  //   rows(ii).dataIn      := 0.U
  //   rows(ii).writeEnable := false.B
  //   rows(ii).idx         := 0.U
  // }


  /**
    * Your code here
    */
  // Fill a Vec with your Vector from the previous exercise.
  // The naming conflict is a little unfortunate.
  
  // our matrix
  // Vec.fill(n)(gen) -> depreciated
  // val rows = Vec.fill(rowsDim)(Module(new Vector(colsDim)).io)
  // VecInit(Seq.fill(n)(gen)) -> new
  val rows = VecInit(Seq.fill(rowsDim)(Module(new Vector(colsDim)).io))

  // init - TODO can we do without this? Is this generalized?
  for(ii <- 0 until rowsDim){
    rows(ii).dataIn      := 0.U
    rows(ii).writeEnable := false.B
    rows(ii).idx         := 0.U
  }
  
  // select a row of Vector modules
  val cols = rows(io.rowIdx)
  // Drive the idx of Vector sub-module to select column
  cols.idx := io.colIdx
  // NOTE: from here we assume we have already selected sub-module
  
  // When writeEnable is high, use rowIdx to select which row you want to operate on.
  when(io.writeEnable){

    // propagate enable write to sub-module
    cols.writeEnable := io.writeEnable
    // propagate data signal to selected sub-module
    cols.dataIn := io.dataIn
  }

  // now we just forward the data from the selected sub-module
  // this is last in case the data was written (and possibly changed)
  io.dataOut := cols.dataOut

}
