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

object Bag_AB_loops extends Bag_AB_loops_Application

class Bag_AB_loops_Application extends SimpleSubscriptApplication {
  
  val A = new Button("A")           {enabled       = false}
  val B = new Button("B")           {enabled       = false}
  val a = new Button("a")           {enabled       = false}
  val b = new Button("b")           {enabled       = false}
  
  val top          = new MainFrame {
    title          = "Bag = (A a & ..) & (B b & ..)"
    location       = new Point    (800,0)
    preferredSize  = new Dimension(400,70)
    contents       = new BorderPanel {
      add(new FlowPanel(A, B, a, b), BorderPanel.Position.North) 
    }
  }
  
/* the following subscript code has manually been compiled into Scala; see below
 script..
  override live = A (live&a) 
                + B (live&b)
*/

  override def _live = _script('live) {_par(_seq(_clicked(A),_clicked(a)), _optionalBreak_loop)}
  //override def _live = _script('live) {_par(_par(_seq(_clicked(A),_clicked(a)), _optionalBreak_loop), 
  //                                          _par(_seq(_clicked(B),_clicked(b)), _optionalBreak_loop))}
               
  // bridge methods; only the first one is actually used; implicit scripts do not get bridge methods   
  override def live = _execute(_live)
}
