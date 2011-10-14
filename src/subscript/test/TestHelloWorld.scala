package subscript.test

import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm.TemplateNode

// Subscript sample application: "Hello world!", printed using a sequence of 2 code fragments
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object TestHelloWorld {
 
// script..
//  main(args: Array[String]) = print("Hello ") println("world!")
//
// would be translated to 2 methods:   
// script method, to be called from bridge method or from other scripts
// bridge method. Normally returns a ScriptExecuter; 
// only a "main" method with the proper parameter type has return type Unit, to serve as a program entry point 

  def _main(_args: FormalInputParameter[Array[String]]) = _script('main, _param(_args,'args)) {_seq({print("Hello ")}, {println("world!")})}

  def  main( args: Array[String]): Unit = _execute(_main(args))
}