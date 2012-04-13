import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm._

// Subscript sample application: "Hello world!", printed using a sequence of 2 code fragments,
// the first of which is optional
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object TestHelloWorld_withEpsilon {
 
// script..
//  main(args: Array[String]) = (+) + print("Hello "); println("world!")
  def sleep(duration_ms: Long) = try {Thread.sleep(duration_ms)} catch {case e: InterruptedException => println("sleep interrupted")}

  def _main(_args: FormalInputParameter[Array[String]]) = _script('main, _args~'args) {_seq(_alt(_empty, _threaded{sleep(25000);println("Hello ")}), {println("world!")})}

  def  main( args: Array[String]): Unit = _execute(_main(args))
}