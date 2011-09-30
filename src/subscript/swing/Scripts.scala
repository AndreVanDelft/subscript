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

  case class AnchorClickedReactor[N<:N_atomic_action_eh[N]](n: N, b:AbstractButton) extends Reactor {
    val clicked = new EventHandlingCodeFragmentExecuter(n, n.scriptExecuter)
    val myReaction: PartialFunction[Event,Unit] = {case ButtonClicked(b) => clicked.execute } 
    def subscribe: Unit = {
      b.reactions += myReaction
    }
    def unsubscribe: Unit = {
      b.reactions -= myReaction
    }
  }
  
/* the following subscript code has manually been compiled into Scala; see below
 
 scripts
  clicked(b:Button) = 
    val a = AnchorClickedReactor(b)
    @swing(there):  // the redirection to the swing thread is needed because of enabling and disabling the button
    @b.reactions += a; 
     b.enabled = true
     there.onDeactivate(()=>{
         b.reactions -= a
         if (!b.reactions.isDefinedAt(ButtonClicked(b))) {button.enabled=false}
     }: 
     @a: {. .}
 
 Note: the manual compilation yielded for the first annotation the type
  
   N_annotation[N_annotation[N_code_eh]]
   
 All the complicated generic type parameters on TemplateNodes and CallGraphNodes were needed
 to make it easy enforceable that "there" and even "there.there" would be of the proper type
*/
  // local variables are not yet supported; FTTB emulate using instance variable
  var a: AnchorClickedReactor[N_code_eh] = _
  
  def clicked(caller: N_call, b:Button)  = {
    caller.calls(T_script("script",
		             T_1_ary_code("@:", (here: N_annotation[N_annotation[N_code_eh]]) => {val there=here.there; swing(there)}, 
		                T_1_ary_code("@:", (here: N_annotation[N_code_eh]) => {
		                      val there=here.there
		                      a = AnchorClickedReactor(there, b) 
		                      there.codeExecuter = a.clicked
		                      a.subscribe
		                      b.enabled = true
		                      there.onDeactivate(()=>{
		                        a.unsubscribe
		                        if (!b.reactions.isDefinedAt(ButtonClicked(b))) {
		                          b.enabled = false
		                        }
		                      }
		                      )
		                  }, 
		            	  T_0_ary_code("{..}", (here: N_code_eh) => {
		            	    println("\nCLICKED!!!")
		            	  }))), 
                     "clicked", new FormalInputParameter("b")),
                  b
               )
  }
               
  // bridge methods
  def clicked(b:Button): ScriptExecuter = {val executer=new BasicExecuter; clicked (executer.anchorNode,b); executer.run}
             

}