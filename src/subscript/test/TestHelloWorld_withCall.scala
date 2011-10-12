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
  
// script method, to be called from bridge-to-Scala method or from other scripts
def _main(caller: N_call, _args: FormalInputParameter[Array[String]])  =
  _script(caller, 'main, 'args, _args,
		             T_n_ary(";", 
		            		T_0_ary_code("call", (_here: N_call) => {implicit val here=_here; __default(here, "hello" )}), 
		            		T_0_ary_code("call", (_here: N_call) => {implicit val here=_here; __default(here, "world!")})))


// script method to be called from bridge-to-Scala method or from other scripts
def __default(caller: N_call, _s: FormalInputParameter[String])  =
  _script(caller, '_, param(_s,'s),
		             T_0_ary_code("{}", (_here:N_code_normal) => {implicit val here=_here; println(_s.value)}))

// bridge methods. Most return a ScriptExecuter; 
// only a "main" method with the proper parameter type has return type Unit, to serve as a program entry point 
def main(args: Array[String]): Unit     = {val executer = new BasicExecuter; _main(executer.anchorNode, args); executer.run}
def _default(s: String): ScriptExecuter = {val executer = new BasicExecuter; __default(executer.anchorNode, s); executer.run}

}