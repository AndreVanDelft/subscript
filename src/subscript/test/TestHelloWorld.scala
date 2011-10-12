package subscript.test

import subscript.vm._
import subscript.vm.DSL._
import subscript.Predef._

// Subscript sample application: "Hello world!", printed using a sequence of 2 code fragments
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object TestHelloWorld {
 
// script..
//  main(args: Array[String]) = print("Hello ") println("world!")
//
// would be translated to 2 methods
  
// script method, to be called from bridge method or from other scripts
def _main(caller: N_call, _args: FormalInputParameter[Array[String]]) =
 _script(caller, 'main, param(_args,'args),
		             T_n_ary(";", 
		            		T_0_ary_code("{}", (_here:CallGraphNodeTrait[TemplateNode]) => {implicit val here = _here; print  ("Hello ")}), 
                            T_0_ary_code("{}", (_here:CallGraphNodeTrait[TemplateNode]) => {implicit val here = _here; println("world!")})
                            )
             )

// bridge method. 
// Normally returns a ScriptExecuter; 
// only a "main" method with the proper parameter type has return type Unit, to serve as a program entry point 
def main(args: Array[String]): Unit = {
  val executer = new BasicExecuter
  _main(executer.anchorNode, args)
  executer.run	
}
}