package subscript.vm

import java.awt.{Font, BasicStroke, Stroke, Color => AWTColor}
import java.awt.geom.AffineTransform
import scala.collection.mutable.ListBuffer
import scala.swing._
import subscript.swing._
import subscript.swing.Scripts._
import subscript.DSL._

object GraphicalDebugger extends GraphicalDebuggerApp {
  override def main(args: Array[String]): Unit = {
    if (args.isEmpty) return
    ScriptExecutorFactory.scriptDebugger = this
    top.visible = true
    new Thread{override def run={
      live;
      quit
    }}.start()
    
    try {
      val c = Class.forName(args.head) // TBD: should be a swing application
      val m = c.getMethod("main", classOf[Array[String]])
      m.invoke(null, args.tail)
    }
    catch {
      case e: ClassNotFoundException =>
    }
  }
}
//    

class GraphicalDebuggerApp extends SimpleSubscriptApplication with ScriptDebugger {

  
  // TBD:
  // create live method
  // create GUI
  // test
  // draw call graph
  // draw message lists

  var messageBeingHandled = false
  
  var currentMessage: CallGraphMessage[_] = null
  
  var             myLock = new Object
  var scriptExecuterLock = new Object
  
  val exitButton = new Button("Exit"  ) {enabled = false}
  val stepButton = new Button("Step"  ) {enabled = false}
  val drawingPanel = new Panel {
    background = AWTColor.white
    preferredSize  = new Dimension(1000,1000)
    override def paint(g: Graphics2D) {
        g.setColor(AWTColor.white)
        g.fillRect(0, 0, size.width, size.height)
        onPaint(g)
    }
  }
  val font = new Font("Arial", Font.BOLD, 16)
  val normalStroke = new BasicStroke(1)
  val    fatStroke = new BasicStroke(3)
  
  def onPaint(g: Graphics2D) {
      val GRID_W  = 170
      val GRID_H  =  60
      val RATIO_W = 0.8
      val RATIO_H = 0.6
      val BOX_W   = (GRID_W * RATIO_W).toInt
      val BOX_H   = (GRID_H * RATIO_H).toInt
      
      val hOffset = (GRID_W - BOX_W)/2
      val vOffset = (GRID_H - BOX_H)/2
      
      val fontMetrics = g.getFontMetrics(font)

      g.setColor(AWTColor.black)
      g.setFont(font)
      
      def drawStringCentered(s: String, cx: Int, cy: Int) {
        val sw = fontMetrics.stringWidth(s)
        val sh = fontMetrics.getHeight
        g.drawString(s, cx-sw/2, cy+sh/2)
      }
      def drawStringTopLeft(s: String, x: Int, y: Int) {
        val sh = fontMetrics.getHeight
        g.drawString(s, x, y+sh/2)
      }
      def drawArrow(x1: Int, y1: Int, x2: Int, y2: Int, s: String) {
        val dx    = x2 - x1
        val dy    = y2 - y1
        val angle = Math.atan2(dy, dx)
        val len   = Math.sqrt(dx*dx + dy*dy).intValue
        val ARROW_HEAD_W = 5
        val ARROW_HEAD_L = 12
        
        val at = AffineTransform.getTranslateInstance(x1, y1)
        at.concatenate(AffineTransform.getRotateInstance(angle))
        val oldTransform = g.getTransform()
        g.setTransform(at)

        // Draw horizontal arrow starting in (0, 0)
        // g.drawLine(0, 0, len, 0)
        g.fillPolygon(Array(len, len-ARROW_HEAD_L, len-ARROW_HEAD_L, len),
                      Array(  0,    -ARROW_HEAD_W,     ARROW_HEAD_W,   0), 4)
        g.setTransform(oldTransform)
        drawStringTopLeft(s, x1 + dx/2 + 9, y1 + dy/2 - 2)
      }
      
      def drawEdge(p: CallGraphNodeTrait[_], pwx: Double, pwy: Int, 
                   c: CallGraphNodeTrait[_], cwx: Double, cwy: Int): Unit = {
        
        val pHCenter = (pwx*GRID_W).toInt + BOX_W/2 + hOffset
        val pBottom  =  pwy*GRID_H        + BOX_H   + vOffset
        val cHCenter = (cwx*GRID_W).toInt + BOX_W/2 + hOffset
        val cTop     =  cwy*GRID_H                  + vOffset
        
        val x1       = pHCenter
        val y1       = pBottom
        val x2       = cHCenter
        val y2       = cTop
        
        var stroke   = normalStroke
        
        if (currentMessage!=null) {
          stroke = fatStroke
          currentMessage match {
            case AAStarted(p,c)                  => drawArrow(x2, y2, x1, y1, "A")
            case Success  (p,c)                  => drawArrow(x2, y2, x1, y1, "S")
            case Break    (p,c, activationMode)  => drawArrow(x2, y2, x1, y1, "B")
            case Exclude  (p,c)                  => drawArrow(x1, y1, x2, y2, "E")
            case _                               => stroke = normalStroke; drawArrow(x2, y2, x1, y1, "M")
          }
        }
        g.setStroke(stroke)
        g.drawLine(pHCenter, pBottom, cHCenter, cTop)
      }
      
	  def drawTree[T <: TemplateNode](n: CallGraphNodeTrait[T], x: Double, y: Int): (Double, Double) = {
        var resultW = 0d // drawn width of this subtree
        var childHCs = new ListBuffer[Double]
	    n match {
	      case p:CallGraphParentNodeTrait[_] => 
	        val pcl=p.children.length
	        if (pcl==0) {
	          resultW = 1
	        }
	        else {
              p.children.foreach{ c =>
	            val (childW, childHC) = drawTree(c, x+resultW, y+1)
	            resultW  += childW
	            childHCs += childHC
              }
	        }
	      case _ => resultW = 1
	    }
        val thisX   = x+resultW/2 
        val boxLeft = (thisX*GRID_W).toInt+hOffset
        val boxTop  = y*GRID_H+vOffset
        val hCenter = boxLeft + BOX_W/2
        val vCenter = boxTop  + BOX_H/2
        
        val s: String = n match {
          case ns: N_script   => ns.template.name.name
          case no: N_n_ary_op => no.template.kind + (if (no.isIteration) " ..." else "")
          case _              => n .template.kind
        }
        
        g.setStroke(normalStroke)
        g draw new Rectangle(boxLeft, boxTop, BOX_W, BOX_H)
        drawStringTopLeft (n.index.toString, boxLeft+2, boxTop+5)
        drawStringCentered(s, hCenter, vCenter)
        
	    n match {
	      case p:CallGraphParentNodeTrait[_] => 
	        (p.children zip childHCs).foreach{ c_hc: (CallGraphNodeTrait[_], Double) =>
	          drawEdge(n, thisX, y, c_hc._1, c_hc._2, y+1)
	        }
	      case _ =>
	    }
        (resultW, thisX)
	  }
      if (scriptExecutor!=null) drawTree(rootNode, 0, 0)
  }
  
  
  val top          = new Frame {
    title          = "Subscript Graphic Debugger"
    location       = new Point    (100,100)
    preferredSize  = new Dimension(600,600)
    contents       = new BorderPanel {
      add(new FlowPanel(stepButton, exitButton), BorderPanel.Position.North) 
      add(new ScrollPane(drawingPanel), BorderPanel.Position.Center)
      //add(outputTA, BorderPanel.Position.Center) 
    }
  }
  
  def sleep(duration_ms: Long) = try {Thread.sleep(duration_ms)} catch {case e: InterruptedException => println("sleep interrupted")}
  def confirmExit: Boolean = Dialog.showConfirmation(null, "Are you sure?", "About to exit")==Dialog.Result.Yes
  
  // script live = {*awaitScriptExecuterPause, m:CallGraphMessage[_]?*}
  //               display,m 
  //               if (!stopFor m) (stepCommand + if(!needsStep(m)) waitSomeTime )
  //               scriptExecuter.notify
  //               ...
  //            || exitDebugger
  def awaitScriptExecuterPause1 = {sleep(1000)}
  def awaitScriptExecuterPause = {
    println("awaitScriptExecuterPause START")
    if (!messageBeingHandled) {
      myLock.synchronized{
        myLock.wait
      }
    }
    trace(1,">> ", currentMessage)
    drawingPanel.repaint()
    currentMessage match {
      case AAToBeExecuted(_) =>
        traceTree
        traceMessages
      case _ =>  
    }
    println("awaitScriptExecuterPause END")
  }
  def notifyScriptExecuter = {
    println("notifyScriptExecuter START")
    scriptExecuterLock.synchronized{scriptExecuterLock.notify}
    println("notifyScriptExecuter END")
  }  
  override def _live  = _script('live) {_par_or2(_seq(_threaded{awaitScriptExecuterPause1}, _stepCommand, _normal{notifyScriptExecuter}, _loop), _exitDebugger)}
  def   _stepCommand  = _script('stepCommand ) {_clicked(stepButton)} // windowClosing
  def   _exitCommand  = _script('exitCommand ) {_clicked(exitButton)} // windowClosing
  def   _exitDebugger = _script('exitDebugger) {_seq(  _exitCommand, _at{gui} (_while{!confirmExit}))}
  
  override def live = _execute(_live, false)
  
  def scriptGraphMessages = scriptExecutor.scriptGraphMessages
  def rootNode            = scriptExecutor.rootNode
  
  // some tracing stuff
  var nSteps = 0
  var maxSteps = 0 // 0 means unlimited
  var traceLevel = 2 // 0-no tracing; 1-message handling 2-message insertion+handling
  def trace(level:Int,as: Any*) = {
    if (traceLevel>=level) {
      as.foreach {a=>print(a.toString)}; 
      println
      //traceMessages
    }
    if (maxSteps>0 && nSteps > maxSteps) {println("Exiting after "+nSteps+"steps"); System.exit(0)}
    nSteps += 1
  }
  def traceTree: Unit = {
    var j = 0;
	  def traceTree[T <: TemplateNode](n: CallGraphNodeTrait[T], branches: List[Int], depth: Int): Unit = {
	    for (i<-1 to 30) {
	      print(if(i==depth)"*"else if (branches.contains(i)) "|" else if(j%5==0)"-"else" ")
	    }
	    j+=1
	    println(n)
	    n match {
	      case p:CallGraphParentNodeTrait[_] => 
	        val pcl=p.children.length
	        p.children.foreach{ c =>
	          var bs = if (c.template.indexAsChild<pcl-1) 
	                    depth::branches 
	                    else branches
	          traceTree(c, bs, depth+1)}
	      case _ =>
	    }
	  }
	if (traceLevel >= 1) traceTree(rootNode, Nil, 0)
  }
  def traceMessages: Unit = {
	if (traceLevel >= 1) {
	  println("=== Messages ===")
	  scriptGraphMessages.foreach(println(_))
	  println("=== End ===")
	}
  }
  
  
  def messageHandled(m: CallGraphMessage[_]): Unit = {
    currentMessage = m
    println("myLock.notify START")
    myLock.synchronized{
      messageBeingHandled=true; 
      myLock.notify
    }
    println("myLock.notify END")
    scriptExecuterLock.synchronized{
      scriptExecuterLock.wait; 
      messageBeingHandled=false
    }
    println("scriptExecuterLock.wait END")
  }
  def messageQueued      (m: CallGraphMessage[_]                 ) = trace(2, "++ ", m)
  def messageDequeued    (m: CallGraphMessage[_]                 ) = trace(2, "-- ", m)
  def messageContinuation(m: CallGraphMessage[_], c: Continuation) = trace(2, "** ", c)
  def messageAwaiting: Unit = {traceTree; traceMessages}
}