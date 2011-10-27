package subscript.test
import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing.SimpleSubscriptApplication
import subscript.swing.Scripts._
import subscript._
import subscript.DSL._
import subscript.Predef._ //tweede keer geimporteerd!!!!!!!!!
import subscript.vm._

// Subscript sample application: a parallel recursive implementation of a Bag
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala
//

object Bag extends BagApplication

class BagApplication extends SimpleSubscriptApplication {
  
  val lA = new Label("A") {preferredSize = new Dimension(26,26)}
  val lB = new Label("B") {preferredSize = new Dimension(26,26)}
  val pA = new Button("+")          {enabled       = false}
  val pB = new Button("+")          {enabled       = false}
  val mA = new Button("-")          {enabled       = false}
  val mB = new Button("-")          {enabled       = false}
  val cA = new Label("")  {preferredSize = new Dimension(45,26)}
  val cB = new Label("")  {preferredSize = new Dimension(45,26)}
  val  X = new Button("Exit")       {enabled       = false}
  val bagLabel = new Label("Bag") {preferredSize = new Dimension(45,26)}
  val outputTA = new TextArea      {editable      = false}
  
  
  val top          = new MainFrame {
    title          = "Bag - Subscript"
    location       = new Point    (100,100)
    preferredSize  = new Dimension(300,300)
    contents       = new BorderPanel {
      //add(new FlowPanel(bagLabel, X), BorderPanel.Position.North) 
      add(new FlowPanel(lA, pA, mA, cA), BorderPanel.Position.North) 
      add(new FlowPanel(lB, pB, mB, cB), BorderPanel.Position.Center) 
      add(outputTA, BorderPanel.Position.South) 
    }
  }
  var ca = 0
  var cb = 0
  def dA(d: Int) = {ca+=d; cA.text = ca.toString}
  def dB(d: Int) = {cb+=d; cB.text = cb.toString}
  
/* the following subscript code has manually been compiled into Scala; see below
 script..
  override live = bag
            bag = A (bag&a)
                + B (bag&a)
                
            A   = pA @gui:{!dA(+1)!}
            Ax  = mA @gui:{!dA(-1)!}
            B   = pB @gui:{!dB(+1)!}
            Bx  = mB @gui:{!dB(-1)!}
*/

  override def _live = _script('live) {_bag}
           def _bag:N_call=>Unit  = _script('bag) {_alt(_seq(_A, _par(_bag, _Ax)),_seq(_B, _par(_bag, _Bx)))}
           def _A                 = _script('A)   {_seq(_clicked(pA), _at(gui) {_tiny{dA(+1)}})}
           def _B                 = _script('B)   {_seq(_clicked(pB), _at(gui) {_tiny{dB(+1)}})}
           def _Ax                = _script('Ax)  {_seq(_clicked(mA), _at(gui) {_tiny{dA(-1)}})}
           def _Bx                = _script('Bx)  {_seq(_clicked(mB), _at(gui) {_tiny{dB(-1)}})}
               
  // bridge methods; only the first one is actually used; implicit scripts do not get bridge methods   
  override def live = _execute(_live)
}
