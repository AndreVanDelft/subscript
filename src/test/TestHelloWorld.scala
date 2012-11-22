//package subscript.test

import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm._

import scala.swing._
import subscript.swing.SimpleSubscriptApplication

// Subscript sample application: "Hello world!", printed using a sequence of 2 code fragments
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

//object TestHelloWorld extends TestHelloWorld {}
object TestHelloWorld extends TestHelloWorldApp {
  
}
class  TestHelloWorldApp //extends SimpleSubscriptApplication 
{
 
  val top          = new MainFrame {
    title          = "A&B"
    location       = new Point    (0,0)
    preferredSize  = new Dimension(300,70)
  }
  
// script..
//  main(args: Array[String]) = print("Hello ") println("world!")
//
// would be translated to 2 methods:   
// script method, to be called from bridge method or from other scripts
// bridge method. Normally returns a ScriptExecuter; 
// only a "main" method with the proper parameter type has return type Unit, to serve as a program entry point 

  //def _main(_args: FormalInputParameter[Array[String]]) = _script(this, 'main, _args~'args) {_par({print("Hello ")}, _unsure{here=> here.result=UnsureExecutionResult.Failure; println("world!") })}
  def _main(_args: FormalInputParameter[Array[String]]) = _script(this, 'main, _args~'args) {
    _seq({print("Hello ")}, _seq(_optionalBreak, {print("World! ")} ))}
  def  main( args: Array[String]): Unit = println("Success: "+_execute(_main(args)).hasSuccess)

  //override def _live = _script(this, 'live) {_par({print("A. Hello ")}, _unsure{here=> here.result=UnsureExecutionResult.Failure; println("B. world!") })}
               
  // bridge methods; only the first one is actually used; implicit scripts do not get bridge methods   
  //override def live = _execute(_live)
}