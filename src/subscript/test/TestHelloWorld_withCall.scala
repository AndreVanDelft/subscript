package subscript.test

import subscript.vm._
import subscript.vm.DSL._
import subscript.Predef._

// Subscript sample application: "Hello world!", printed using a sequence of calls to a default script that prints its string parameter
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object TestHelloWorld_withCall {
 
// script..
//  main(args: Array[String]) = "hello" "world!"
//  _(s: String) = println(s)
//
// would be translated to 2*2 = 4 methods
  
// script methods, to be called from bridge-to-Scala method or from other scripts
def _main (_args: FormalInputParameter[Array[String]]) = _script('main, _param(_args, 'args), _seq(__default("hello" ), __default("world!")))
def __default(_s: FormalInputParameter[String])        = _script('_   , _param(_s,'s)       , {println(_s.value)})

// bridge methods. Most return a ScriptExecuter; 
// only a "main" method with the proper parameter type has return type Unit, to serve as a program entry point 
def main(args: Array[String]): Unit = execute(_main(args))
def _default(s: String)             = execute(__default(s))
}