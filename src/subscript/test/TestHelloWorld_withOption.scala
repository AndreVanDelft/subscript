package subscript.test

import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm._

// Subscript sample application: "Hello world!", printed using a sequence of 2 code fragments,
// the first of which is optional
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object TestHelloWorld_withOption {
 
// script..
//  main(args: Array[String]) = . print("Hello "); println("world!")

  def _main(_args: FormalInputParameter[Array[String]]) = _script('main, _args~'args) {_seq(_seq(_optionalBreak, {println("Hello ")}), {println("world!")})}

  def  main( args: Array[String]): Unit = _execute(_main(args))
}