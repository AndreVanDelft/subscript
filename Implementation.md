# Introduction #

A Subscript implementation will contain at least a compiler and a Subscript Virtual Machine (SVM).

The compiler would be a derivative of a Scala compiler. It will internally translate typical Subscript items to specific Scala methods and other features, in a form that is for the time being called "Internal Script Language". The compiler then generates the usual byte code for these features.

## State as of 28 September 2011 ##
Today there was an initial release at Google Code of the SVM. No work has been done on the compiler yet.
The code is of type "don't worry, be crappy". Some basic tracing functionality is provided. The three sample applications are the only tested applications, for the time being.

### Sample Applications ###
The initial SVM comes with three sample applications:
  * a simple Hello World program, consisting of a sequence with 2 atomic actions, each printing a word
  * a more advanced Hello World program, consisting of a sequence of two calls to a script that prints its parameter of type `string`.
  * a GUI program named LookupFrame, that is essentially the search application presented at the introduction page of Subscript at Google code. This has the following types of language elements:
    * a loop operand
    * a directive to perform the next code fragment in the Swing thread
    * a directive to enable a button, or disable it on deactivation
    * a code fragment that is executed in a new background thread

### VM Structure ###
The VM comes currently with 4 files:
  * TemplateNode.scala - lots of traits and classes to describe the Abstract Syntax Tree of compiled scripts.
  * CallGraphNode.scala - even more traits and classes, to hold the run time call graph of a subscript program
  * ScriptGraphMessage.scala - various kinds of messages describing events happening in the graph, or tasks that should be executed
  * Executer.scala - an executer for the Script Call Graph, plus some executers for code fragments, e.g. for normal code, threaded code, swing code and event handling code

# Internal Script Language #
Each non-communicating script translates to two methods in the "Internal Script Language" (ISL):

  * a method used by scripts calling one another; this transfers parameters, and calls some public methods in the Subscript Virtual Machine
  * a bridge method meant for calls from Scala. This sets up an executer in the Subscript Virtual Machine, that will execute the program. The return type is a ScriptExecuter. This returned executer may be used to query the result state of the execution: success or failure. A special case is when the script called "main" with a variable list of string arguments, that comes in place of a "main" method. This does not return an executer, since the return type is prescribed to be "Unit"

The following examples illustrate this:

## Hello World ##

### scripts ###
```
main(args: Array[String]) = print("Hello ") println("world!")
```

### ISL ###
```
def main(caller: N_call, args: Array[String])  =
  caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary_code("{}", (here:CallGraphNodeTrait[TemplateNode]) => print  ("Hello ")), 
                            T_0_ary_code("{}", (here:CallGraphNodeTrait[TemplateNode]) => println("world!"))
                            ), 
                     "main", new FormalInputParameter("args")),
                 args
               )

def main(args: Array[String]): Unit = {
  val executer = new BasicExecuter
  main(executer.anchorNode, args)
  executer.run	
}
```

## Hello World with script calls ##

### scripts ###
```
main(args: Array[String]) = "hello" "world!"
_(s: String) = println(s)
```

### ISL ###
```
def main(caller: N_call, args: Array[String])  =
  caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary_code("call", (here: N_call) => default(here, "hello")), 
		            		T_0_ary_code("call", (here: N_call) => default(here, "world!"))
                            ), 
                     "main(Array[String])", new FormalInputParameter("args")),
                 args
               )

def default(caller: N_call, s: String)  =
  caller.calls(T_script("script",
		             T_0_ary_code("{}", (here:N_code_normal) => println(here.getParameterValue("s").asInstanceOf[String])),
                     "default(String)", new FormalInputParameter("s")),
                  s
               )


def main(args: Array[String]): Unit = {val executer = new BasicExecuter; main(executer.anchorNode, args);   executer.run}
def default(s: String): ScriptExecuter = {val executer = new BasicExecuter; default(executer.anchorNode, s);  executer.run}
```

## LookupFrame ##
### scripts ###
```
live              = ...; searchSequence
searchSequence    = searchCommand    showSearchingText 
                    searchInDatabase showSearchResults

searchCommand     = searchButton
showSearchingText = @swing: {outputTA.text = "Searching: "+searchTF.text}
showSearchResults = @swing: {outputTA.text = "Found: 3 items"}
searchInDatabase  = {* Thread.sleep 1000 *} // simulate a time consuming action
_(b: Button)      = clicked(b)
```

### ISL ###
```

  def live(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary("..."), 
		            		T_0_ary_code("call", (here: N_call) => searchSequence(here))
                            ), 
                     "live")
               )
  def searchSequence(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary_code("call", (here: N_call) => searchCommand    (here)), 
		            		T_0_ary_code("call", (here: N_call) => showSearchingText(here)), 
		            		T_0_ary_code("call", (here: N_call) => searchInDatabase (here)), 
		            		T_0_ary_code("call", (here: N_call) => showSearchResults(here))
                            ), 
                     "searchSequence")
               )
  def searchCommand(caller: N_call)  =
    caller.calls(T_script("script",
		             T_0_ary_code("call", (here: N_call) => default(here, searchButton)), 
                     "searchCommand")
                 )
  def showSearchingText(caller: N_call)  =
    caller.calls(T_script("script",
		             T_1_ary_code("@:", (here: N_annotation[N_code_normal]) => {val there=here.there; swing(there)}, 
		            		T_0_ary_code("{}", (here: N_code_normal) => {
		            		  outputTA.text = "Searching: "+searchTF.text})), 
                     "showSearchingText")
                 )
  def showSearchResults(caller: N_call)  =
    caller.calls(T_script("script",
		             T_1_ary_code("@:", (here: N_annotation[N_code_normal]) => {val there=here.there; swing(there)}, 
		            		T_0_ary_code("{}", (here: N_code_normal) => {outputTA.text = "Found: "+here.index+" items"})), 
                     "showSearchResults")
                 )
  def searchInDatabase(caller: N_call)  =
    caller.calls(T_script("script",
		             T_0_ary_code("{**}", (here:N_code_threaded) => {
		              Thread.sleep(1000)}),
                     "searchInDatabase")
               )
 
def default(caller: N_call, b:Button)  =
  caller.calls(T_script("script",
		             T_0_ary_code("call", (here:N_call) => clicked(here, here.getParameterValue("b").asInstanceOf[Button])),
                     "default(Button)", new FormalInputParameter("b")),
                  b
               )
               
// bridge methods; only the first one is actually used   
override
def live             : ScriptExecuter = {val executer=new BasicExecuter; live             (executer.anchorNode  ); executer.run}
def searchSequence   : ScriptExecuter = {val executer=new BasicExecuter; searchSequence   (executer.anchorNode  ); executer.run}
def searchCommand    : ScriptExecuter = {val executer=new BasicExecuter; searchCommand    (executer.anchorNode  ); executer.run}
def searchInDatabase : ScriptExecuter = {val executer=new BasicExecuter; searchInDatabase (executer.anchorNode  ); executer.run}
def showSearchingText: ScriptExecuter = {val executer=new BasicExecuter; showSearchingText(executer.anchorNode  ); executer.run}
def showSearchResults: ScriptExecuter = {val executer=new BasicExecuter; showSearchResults(executer.anchorNode  ); executer.run}
def default(b:Button): ScriptExecuter = {val executer=new BasicExecuter; default          (executer.anchorNode,b); executer.run}
```

## Button click ##
### scripts ###
```
  clicked(b:Button) = 
    val csr = AnchorClickedReactor(b)
    @swing(there):  // the redirection to the swing thread is needed because enabling and disabling the button must there be done
    @csr.subscribe(there); there.onDeactivate{()=>csr.unsubscribe}: 
    {. .}
```
### ISL ###
```
  // local variables are not yet supported; FTTB emulate using instance variable
  var csr: ClickedScriptReactor[N_code_eh] = _
  
  def clicked(caller: N_call, b:Button)  = {
    csr = new ClickedScriptReactor(b) // also part of the emulation of local variable
    caller.calls(T_script("script",
		             T_1_ary_code("@:", (here: N_annotation[N_annotation[N_code_eh]]) => {val there=here.there; swing(there)}, 
		                T_1_ary_code("@:", (here:           N_annotation[N_code_eh] ) => {val there=here.there; csr.subscribe(there); there.onDeactivate{()=>csr.unsubscribe}}, 
		            	  T_0_ary_code("{..}", (here:                    N_code_eh  ) => {println("\nCLICKED!!!")} // Temporary tracing
		             ))), 
                     "clicked", new FormalInputParameter("b")),
                  b
               )
  }
  def clicked(b:Button): ScriptExecuter = {val executer=new BasicExecuter; clicked (executer.anchorNode,b); executer.run}
```
Note: the manual compilation yielded for the first annotation the type
```
   N_annotation[N_annotation[N_code_eh]]
```
All the complicated generic type parameters on TemplateNodes and CallGraphNodes were needed to make it easy enforceable that "there" and even "there.there" would be of the proper type, without casting.

### ClickedScriptReactor ###
BTW The click script refers to an ClickedScriptReactor that reacts on button click events; within such a reaction it executes an event handling code fragment:
```
  // an extension on scala.swing.Reactor that supports event handling scripts in Subscript
  abstract class ScriptReactor[N<:N_atomic_action_eh[N]](publisher:Publisher) extends Reactor {
    var executer: EventHandlingCodeFragmentExecuter[N] = _
    def execute: Unit = executer.execute
    
    private var myEnabled = false
    def enabled = myEnabled
    def enabled_=(b:Boolean) = {myEnabled=b}
    
    val event: Event
    val reaction: PartialFunction[Event,Unit] = {case event => execute}
    
    def subscribe(n: N): Unit = {
      executer = new EventHandlingCodeFragmentExecuter(n, n.scriptExecuter)
      n.codeExecuter = executer
      val wasAlreadyEnabled = enabled
      publisher.reactions += reaction;
      if (!wasAlreadyEnabled) {enabled=true}
    }
    def unsubscribe: Unit = {
      publisher.reactions -= reaction
      if (!publisher.reactions.isDefinedAt(event)) {enabled=false}
    }
  }
  
  // a ScriptReactor that has a Component as a Publisher. Automatically enables and disables the component
  abstract class ComponentScriptReactor[N<:N_atomic_action_eh[N]](publisher:Publisher with Component) extends ScriptReactor[N](publisher) {
    override def enabled_=(b:Boolean) = {super.enabled_=(b); publisher.enabled = b}
  }
  
  // a ComponentScriptReactor for clicked events on a button
  case class ClickedScriptReactor[N<:N_atomic_action_eh[N]](b:AbstractButton) extends ComponentScriptReactor[N](b) {
    val event: Event = ButtonClicked(b)
  }
```