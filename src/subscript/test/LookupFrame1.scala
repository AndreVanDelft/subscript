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
object LookupFrame1 extends LookupFrame1

class LookupFrame1 extends LookupFrame {
  
  override def top = new MainFrame {
    title    = "LookupFrame - Subscript"
    location = new Point    (100,100)
    preferredSize     = new Dimension(300,300)
    contents = new BorderPanel {
      add(new FlowPanel(searchLabel, searchTF, searchButton), BorderPanel.Position.North) 
      add(outputTA, BorderPanel.Position.Center) 
    }
    //setDefaultCloseOperation(WindowConstants.NONE)
    //defaultCloseOperation = JFrame.EXIT_ON_CLOSE    TBD how to do this in Scala.swing?
  }
/* the following subscript code has manually been compiled into Scala; see below
 override scripts
  searchCommand     = searchButton + KeyEvent.VK_ENTER 
 scripts
  _(comp: Component, keyCode:Char??) = key(comp, keyCode??)
*/

  override def searchCommand(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => default(here, searchButton)), 
                      T_0_ary_code("call", (here: N_call) => default(here, Key.Enter))
                      ), 
                     "searchCommand")
                 )
 
  def default(caller: N_call, _keyCode:FormalConstrainedParameter[Key.Value])  =
    caller.calls(T_script("script",
		             T_0_ary_code("call", (here:N_call) => vkey(here, top, ActualAdaptingParameter(_keyCode))),
                     "default(Key.Value)", "keyCode"),
                  _keyCode
               )
               
// bridge methods; only the first one is actually used   
override def searchCommand             : ScriptExecuter = {val executer=new BasicExecuter; searchCommand(executer.anchorNode         ); executer.run}
         def default(keyCode:Key.Value): ScriptExecuter = {val executer=new BasicExecuter; default      (executer.anchorNode, keyCode); executer.run}
}
