package subscript.test

import subscript.vm._
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
def main(caller: N_call, args: FormalInputParameter[Array[String]])  =
  caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary_code("call", (__here: N_call) => {implicit val here=__here; default(here, "hello" )}), 
		            		T_0_ary_code("call", (__here: N_call) => {implicit val here=__here; default(here, "world!")})
                            ), 
                     "main(Array[String])", "args"),
                 args
               )


// bridge method. Normally returns a ScriptExecuter; 
// only a "main" method with the proper parameter type has return type Unit, to serve as a program entry point 
def main(args: Array[String]): Unit = {
  val executer = new BasicExecuter
  main(executer.anchorNode, args)
  executer.run	
}

// script method to be called from bridge-to-Scala method or from other scripts
def default(caller: N_call, s: FormalInputParameter[String])  =
  caller.calls(T_script("script",
		             T_0_ary_code("{}", (__here:N_code_normal) => {implicit val here=__here; println(s.value)}),
                     "default(String)", "s"),
                  s
               )


// bridge method. Returns a ScriptExecuter 
def default(s: String): ScriptExecuter = {
  val executer = new BasicExecuter
  default(executer.anchorNode, s)
  executer.run	
}

}