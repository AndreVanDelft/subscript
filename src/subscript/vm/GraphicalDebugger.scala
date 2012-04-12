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
    var lArgs = args
    if (lArgs.isEmpty) return
    ScriptExecutorFactory.scriptDebugger = this
    top.visible = true
    
    lArgs.head match {
      case "-s" => lArgs = lArgs.tail; if (lArgs.isEmpty) return; descriptionLabel.text = lArgs.head
                   lArgs = lArgs.tail; if (lArgs.isEmpty) return
      case _ =>
    }
    new Thread{override def run={
      live;
      quit
    }}.start()
    
    try {
      val c = Class.forName(lArgs.head) // TBD: should be a swing application
      val m = c.getMethod("main", classOf[Array[String]])
      m.invoke(null, lArgs.tail)
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
  
  def interestingContinuationInternals(c: Continuation): List[String] = {
     var ss: List[String] = Nil
     if (c != null) {
          if (!c.aaStarteds   .isEmpty) ss ::= "AA Started"
          if ( c.activation    != null) ss ::= "Activation"
          if (!c.deactivations.isEmpty) ss ::= "Deactivations"
          if ( c.success       != null) ss ::= "Success"
     }
     ss
  }
  
  val exitButton = new Button("Exit"  ) {enabled = false}
  val stepButton = new Button("Step"  ) {enabled = false}
  val drawingPanel = new Panel {
    background = AWTColor.white
    preferredSize  = new Dimension(1000,2000)
    override def paint(g: Graphics2D) {
        g.setColor(AWTColor.white)
        g.fillRect(0, 0, size.width, size.height)
        onPaint(g)
    }
  }
  val fixedWidthFont = new Font("Monaco", Font.BOLD, 12)
  val     normalFont = new Font("Arial" , Font.BOLD, 16)
  val   normalStroke = new BasicStroke(1)
  val      fatStroke = new BasicStroke(3)
  
  def onPaint(g: Graphics2D) {
      val GRID_W  = 170
      val GRID_H  =  60
      val RATIO_W = 0.8
      val RATIO_H = 0.6
      val BOX_W   = (GRID_W * RATIO_W).toInt
      val BOX_H   = (GRID_H * RATIO_H).toInt
      
      val hOffset = (GRID_W - BOX_W)/2
      val vOffset = (GRID_H - BOX_H)/2
      
      val fontMetrics = g.getFontMetrics(normalFont)
      g.setFont(normalFont)

      def emphasize(doIt: Boolean) {
        if (doIt) {
          g.setColor(AWTColor.red)
          g.setStroke(fatStroke)
        }
        else {
          g.setColor(AWTColor.black)
          g.setStroke(normalStroke)
        }
      }
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
        val ARROW_HEAD_L = 15
        
        emphasize(s != null)
        if (s != null) {
          drawStringTopLeft(s, x1 + dx/2 + 9, y1 + dy/2 - 2)
        }
        val at = AffineTransform.getTranslateInstance(x1, y1)
        at.concatenate(AffineTransform.getRotateInstance(angle))
        val oldTransform = g.getTransform()
        g.setTransform(at)

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len, 0)
        
        if (s != null) {
          g.fillPolygon(Array(len, len-ARROW_HEAD_L, len-ARROW_HEAD_L, len),
                        Array(  0,    -ARROW_HEAD_W,     ARROW_HEAD_W,   0), 4)
        }
        g.setTransform(oldTransform)
        emphasize(false)
      }
      def getBreakText(a: ActivationMode.ActivationModeType): String = {
        a match {
          case ActivationMode.Optional => "Opt.Break"
          case _ => "Break"
        }
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
        
        if (currentMessage!=null) {
          currentMessage match {
            case AAStarted(p1,c1) if (p==p1 && c==c1)  => drawArrow(x2, y2, x1, y1, "AA Started")
            case Success  (p1,c1) if (p==p1 && c==c1)  => drawArrow(x2, y2, x1, y1, "Success")
            case Break    (p1,c1, activationMode) 
                                  if (p==p1 && c==c1)  => drawArrow(x2, y2, x1, y1,  getBreakText(activationMode))
            case Exclude  (p1,c1) if (p==p1 && c==c1)  => drawArrow(x1, y1, x2, y2, "Exclude")
            case _                                     => drawArrow(x1, y1, x2, y2, null)
          }
        }
        else drawArrow(x1, y1, x2, y2, null)
        
        g.setStroke(normalStroke)
        g.setColor (AWTColor.black)
      }
      def drawContinuationTexts(n: CallGraphNodeTrait[_], boxRight: Int, boxTop: Int) = {
        val x = boxRight + 3
        var y = boxTop
        n match {
          case nn: N_n_ary_op => 
            interestingContinuationInternals(nn.continuation).foreach{s: String=>drawStringTopLeft(s, x, y); y += fontMetrics.getHeight+2}
          case _ => if (currentMessage!=null&&currentMessage.node==n) currentMessage match {
            case s: Success   if (s.child==null) => drawStringTopLeft("Success"  , x, y) 
            case a: AAStarted if (a.child==null) => drawStringTopLeft("AAStarted", x, y) 
            case _ => 
          }
        }
      }
	  def drawTree[T <: TemplateNode](n: CallGraphNodeTrait[T], x: Double, y: Int): (Double, Double) = {
        var resultW = 0d // drawn width of this subtree
        var childHCs = new ListBuffer[Double]
        
        val isCurrentNode = currentMessage != null && currentMessage.node == n
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
        val thisX   = x+(resultW-1)/2 
        val boxLeft = (thisX*GRID_W).toInt+hOffset
        val boxTop  = y*GRID_H+vOffset
        val hCenter = boxLeft + BOX_W/2
        val vCenter = boxTop  + BOX_H/2
        
        val s: String = n match {
          case ns: N_script   => ns.template.name.name
          case no: N_n_ary_op => no.template.kind + (if (no.isIteration) " ..." else "")
          case _              => n .template.kind
        }
        
        val r = new Rectangle(boxLeft, boxTop, BOX_W, BOX_H)
        g.setColor(new AWTColor(230, 255, 230)) 
        g fill r
        emphasize(isCurrentNode)
        g draw r 
        if (isCurrentNode) {
          currentMessage match {
            case d: Deactivation =>
              n match {
                case pn: CallGraphParentNodeTrait[_] if (pn.children.length>0) =>
                case _ =>
                  g.drawLine(boxLeft, boxTop      , boxLeft+BOX_W, boxTop+BOX_H)
                  g.drawLine(boxLeft, boxTop+BOX_H, boxLeft+BOX_W, boxTop      )
              }
            case _ =>
          }
        }
        drawContinuationTexts(n, boxLeft+BOX_W, boxTop)
        emphasize(false)
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
  
  val msgLogListModel   = new javax.swing.DefaultListModel
  val msgQueueListModel = new javax.swing.DefaultListModel
  
  val currentMessageTF  = new TextField {
    preferredSize       = new Dimension(400,24)
    minimumSize         = preferredSize
    editable            = false
    font                = normalFont
    horizontalAlignment = scala.swing.Alignment.Left
  }
  val msgLogList        = new ListBuffer[String]
  val msgQueueList      = new ListBuffer[String]
  val msgLogListView    = new ListView(msgLogList) {
    font                = fixedWidthFont
    peer.setModel(msgLogListModel)
  }
  val msgQueueListView  = new ListView(msgQueueList) {
    font                = fixedWidthFont
    peer.setModel(msgQueueListModel)
  }
  val msgLogListViewScrollPane   = new ScrollPane
  {
  	contents                     = msgLogListView
  	verticalScrollBarPolicy      = ScrollPane.BarPolicy.Always
  }
  val msgQueueListViewScrollPane = new ScrollPane
  {
  	contents                     = msgQueueListView
  	verticalScrollBarPolicy      = ScrollPane.BarPolicy.Always
  }
  val splitPaneMsgs  = new SplitPane(scala.swing.Orientation.Horizontal, 
                                     msgLogListViewScrollPane, 
                                     msgQueueListViewScrollPane   ) {
    dividerLocation  = 350
  }
  val splitPaneMain  = new SplitPane(scala.swing.Orientation.Vertical, 
                                     splitPaneMsgs, 
                                     new ScrollPane(drawingPanel)    ) {
    dividerLocation  = 240
  }

  val descriptionLabel = new Label {
    preferredSize      = new Dimension(300,24)
    font               = normalFont
  }
  val top              = new Frame {
    title              = "Subscript Graphical Debugger"
    location           = new Point    (0,100)
    preferredSize      = new Dimension(800,800)
    contents           = new BorderPanel {
      add(new FlowPanel(currentMessageTF, stepButton, exitButton, descriptionLabel), BorderPanel.Position.North) 
      add(splitPaneMain, BorderPanel.Position.Center)
    }
  }
  
  def sleep(duration_ms: Long) = try {Thread.sleep(duration_ms)} catch {case e: InterruptedException => println("sleep interrupted")}
  def confirmExit: Boolean = Dialog.showConfirmation(null, "Are you sure?", "About to exit")==Dialog.Result.Yes
  
  
  def awaitMessageBeingHandled(value: Boolean) = {
    var sleeptime = 1
    while (messageBeingHandled!=value) {
      sleep(sleeptime)
      if (sleeptime<100) sleeptime *=2
    }
  }
  def shouldStep: Boolean =
    currentMessage match {
      case Activation(_) | Deactivation(_,_,_) | AAStarted(_,_) | Success(_,_)  | Break(_,_,_)  | Exclude(_,_) => true
      case c:Continuation if (!interestingContinuationInternals(c).isEmpty) => true
      case _ => false
    }
  
  def logMessage(m: String, msg: CallGraphMessage[_]) {
    msgLogListModel.addElement(m + " " + msg)
    msgLogListViewScrollPane.verticalScrollBar.value = msgLogListViewScrollPane.verticalScrollBar.maximum
    msgQueueListModel.clear
    scriptExecutor.scriptGraphMessages.foreach(msgQueueListModel.addElement(_)) 
  }
  def updateDisplay = {
    var s = currentMessage.toString
    if (s.length>50) s = s.substring(0, 50) + "..."
    currentMessageTF.text = s
    logMessage (">>", currentMessage)
    drawingPanel.repaint()
  }
  // script live = {*awaitMessageBeingHandled*}
  //               if (shouldStep) ( @gui: {!updateDisplay!}; stepCommand || if(!needsStep(m)) waitSomeTime )
  //               {messageBeingHandled=false}
  //               ...
  //            || exitDebugger
  
  override def _live  = _script('live) {_par_or2(_seq(_threaded{awaitMessageBeingHandled(true)}, 
                                                      _if{shouldStep} (_seq(_at{gui} (_tiny{updateDisplay}), _stepCommand)), 
                                                      _normal{messageBeingHandled=false}, 
                                                      _loop
                                                     ), 
                                                  _exitDebugger
                                                )}
  def   _stepCommand  = _script('stepCommand ) {_clicked(stepButton)}
  def   _exitCommand  = _script('exitCommand ) {_clicked(exitButton)} // windowClosing
  def   _exitDebugger = _script('exitDebugger) {_seq(  _exitCommand, _at{gui} (_while{!confirmExit}))}
  
  override def live = _execute(_live, new SimpleScriptDebugger)
  
  def scriptGraphMessages = scriptExecutor.scriptGraphMessages
  def rootNode            = scriptExecutor.rootNode
  
  def messageHandled(m: CallGraphMessage[_]): Unit = {
    currentMessage = m
    messageBeingHandled=true 
    awaitMessageBeingHandled(false)
  }
  def messageQueued      (m: CallGraphMessage[_]                 ) = logMessage("++", m)
  def messageDequeued    (m: CallGraphMessage[_]                 ) = logMessage("--", m)
  def messageContinuation(m: CallGraphMessage[_], c: Continuation) = logMessage("**", c)
  def messageAwaiting: Unit = {}
}