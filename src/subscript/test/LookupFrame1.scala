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
  searchCommand     = searchButton + Key.Enter 
 scripts
  _(keyValue:Key.Value??) = vkey(keyValue??)
*/

  override def _searchCommand = _script('searchCommand, _alt(__default(searchButton)), __default(Key.Enter))
 
  def __default(_keyValue:FormalConstrainedParameter[Key.Value]) = _script('_, _param(_keyValue,'keyValue), _vkey(top, ActualAdaptingParameter(_keyValue)))
               
// bridge methods; only the first one is actually used   
override def searchCommand         = execute(_searchCommand)
         def _default(k:Key.Value) = execute(__default  (k))
}
