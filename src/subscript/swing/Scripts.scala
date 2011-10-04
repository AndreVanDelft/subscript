package subscript.swing
import scala.swing._
import scala.swing.event._
import subscript.vm._;

abstract class SimpleSubscriptApplication extends SimpleSwingApplication{
  override def startup(args: Array[String]) {
    super.startup(args)
    new Thread{override def run=live}.start()
  }
  def live: ScriptExecuter
}
object Scripts {
  
  def swing[N<:CallGraphNodeTrait[_]](n:N) = {n.adaptExecuter(new SwingCodeExecuterAdapter[CodeExecuter])}             

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
  
/* the following subscript code has manually been compiled into Scala; see below
 
 scripts
  clicked(b:Button) = 
    val csr = AnchorClickedReactor(b)
    @swing(there):  // the redirection to the swing thread is needed because enabling and disabling the button must there be done
    @csr.subscribe(there); there.onDeactivate{()=>csr.unsubscribe}: 
    {. .}
 
 Note: the manual compilation yielded for the first annotation the type
  
   N_annotation[N_annotation[N_code_eh]]
   
 All the complicated generic type parameters on TemplateNodes and CallGraphNodes were needed
 to make it easy enforceable that "there" and even "there.there" would be of the proper type
*/
  
  def clicked(caller: N_call, b:Button)  = {
    caller.calls(T_script("script",
	                T_n_ary(";", 
	                 T_0_ary_code  ("val" , (here:                           N_localvar ) => {here.initLocalVariableValue_stepsUp("csr", 1, new ClickedScriptReactor[N_code_eh](b))}),
		             T_1_ary_code  ("@:"  , (here: N_annotation[N_annotation[N_code_eh]]) => {val there=here.there; swing(there)}, 
		              T_1_ary_code ("@:"  , (here:              N_annotation[N_code_eh] ) => {val there=here.there;here.getLocalVariableValue_stepsUp("csr", 2).asInstanceOf[ClickedScriptReactor[N_code_eh]].subscribe(there); 
		                                                                                     there.onDeactivate{()=>here.getLocalVariableValue_stepsUp("csr", 2).asInstanceOf[ClickedScriptReactor[N_code_eh]].unsubscribe}}, 
		               T_0_ary_code("{..}", (here:                           N_code_eh  ) => {println("\nCLICKED!!!")} // Temporary tracing
		            )))), 
                     "clicked", new FormalInputParameter("b")),
                  ActualInputParameter("b", b)
               )
  }
               
  // bridge methods
  def clicked(b:Button): ScriptExecuter = {val executer=new BasicExecuter; clicked (executer.anchorNode,b); executer.run}
             

}