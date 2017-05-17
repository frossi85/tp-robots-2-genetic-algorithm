package com.example

import collection.JavaConverters._

object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
    val numberOfQueens = 12
    val nQueenProblem = new NQueenProblem(numberOfQueens)
    val result = nQueenProblem.getResult.asScala

    println(result)
  }
}


