import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing.SimpleSubscriptApplication
import subscript.swing.Scripts._
import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm._

// Subscript sample application: A..B
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object Bag_A extends Bag_A_Application

class Bag_A_Application extends SimpleSubscriptApplication {
  
  val A = new Button("A") {enabled = false}
  val a = new Button("a") {enabled = false}
  
  val top          = new MainFrame {
    title          = "A Bag with A's: live = A; live&a"
    location       = new Point    (900,0)
    preferredSize  = new Dimension(200,70)
    contents       = new BorderPanel {
      add(new FlowPanel(A, a), BorderPanel.Position.North) 
    }
  }
  
/* the following subscript code has manually been compiled into Scala; see below
 script..
  override live = A (live&a) 
*/

  override def _live = _script('live) {_seq(_clicked(A), _par(_live, _clicked(a)))}
               
  // bridge methods; only the first one is actually used; implicit scripts do not get bridge methods   
  override def live = _execute(_live)
}
