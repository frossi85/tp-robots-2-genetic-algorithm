package com.example

import collection.JavaConverters._

object Hello {
  def main(args: Array[String]): Unit = {
    val boardSize = 12
    val nQueenProblem = new NQueenProblem(boardSize)
    val result = nQueenProblem.getResult.asScala

    println(result)
  }
}


