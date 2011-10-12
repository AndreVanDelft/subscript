package subscript.test
import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing._
import subscript.swing.Scripts._
import subscript.vm._;
import subscript.vm.DSL._

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

  override def _searchCommand(caller: N_call)  =
  _script(caller, 'searchCommand,
		             T_n_ary("+",
                      T_0_ary_code("call", (here: N_call) => __default(here, searchButton)), 
                      T_0_ary_code("call", (here: N_call) => __default(here, Key.Enter))
                     )
                 )
 
  def __default(caller: N_call, _keyValue:FormalConstrainedParameter[Key.Value])  =
  _script(caller, '_, param(_keyValue,'keyValue),
		             T_0_ary_code("call", (here:N_call) => _vkey(here, top, ActualAdaptingParameter(_keyValue)))
               )
               
// bridge methods; only the first one is actually used   
override def searchCommand        : ScriptExecuter = {val executer=new BasicExecuter; _searchCommand(executer.anchorNode   ); executer.run}
         def _default(k:Key.Value): ScriptExecuter = {val executer=new BasicExecuter; __default     (executer.anchorNode, k); executer.run}
}
