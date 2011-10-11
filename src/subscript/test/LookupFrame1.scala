package subscript.test
import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing._
import subscript.swing.Scripts._
import subscript.vm._;

// Subscript sample application: a text entry field with a search button, that simulates the invocation of a background search
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala
object LookupFrame1 extends LookupFrame1Application

class LookupFrame1Application extends LookupFrameApplication {
  
  top.listenTo (searchTF.keys)
  val f = top.peer.getRootPane().getParent().asInstanceOf[javax.swing.JFrame]
  f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)

  /* the following subscript code has manually been compiled into Scala; see below
 override scripts
  searchCommand     = searchButton + KeyEvent.VK_ENTER 
 scripts
  _(keyValue:Key.Value??) = vkey(keyValue??)
*/

  override def searchCommand(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => default(here, searchButton))
                      , 
                      T_0_ary_code("call", (here: N_call) => default(here, Key.Enter))
                      ), 
                     "searchCommand")
                 )
 
  def default(caller: N_call, _keyValue:FormalConstrainedParameter[Key.Value])  =
    caller.calls(T_script("script",
		             T_0_ary_code("call", (here:N_call) => vkey(here, top, ActualAdaptingParameter(_keyValue))),
                     "default(Key.Value)", "keyValue"),
                  _keyValue
               )
               
// bridge methods; only the first one is actually used   
override def searchCommand              : ScriptExecuter = {val executer=new BasicExecuter; searchCommand(executer.anchorNode          ); executer.run}
         def default(keyValue:Key.Value): ScriptExecuter = {val executer=new BasicExecuter; default      (executer.anchorNode, keyValue); executer.run}
}
