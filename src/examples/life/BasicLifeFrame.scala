package life

import scala.swing._
import scala.swing.event._
import subscript.DSL._
import subscript.swing.SimpleSubscriptApplication

object BasicLifeFrame extends BasicLifeFrameApplication
class BasicLifeFrameApplication extends SimpleSubscriptApplication {
 
  val canvas          = new LifeDrawingPanel(150,100)
  val     startButton = new Button("Start" ) {enabled       = false; focusable = false}
  val      stopButton = new Button("Stop"  ) {enabled       = false; focusable = false}
  val      stepButton = new Button("Step"  ) {enabled       = false; focusable = false}
  val randomizeButton = new Button("Random") {enabled       = false; focusable = false}
  val     clearButton = new Button("Clear" ) {enabled       = false; focusable = false}
  val  minSpeedButton = new Button("<<"    ) {enabled       = false; focusable = false}
  val    slowerButton = new Button("<"     ) {enabled       = false; focusable = false}
  val    fasterButton = new Button(">"     ) {enabled       = false; focusable = false}
  val  maxSpeedButton = new Button(">>"    ) {enabled       = false; focusable = false}
  val      exitButton = new Button("Exit"  ) {enabled       = false; focusable = false}
  val speedLabel      = new Label("speed"  ) {preferredSize = new Dimension(65,26)}
  val speedSlider     = new Slider           {min = 1; max = 10}
  
  val top          = new MainFrame {
    title          = "Life - Subscript"
    location       = new Point    (100,100)
    preferredSize  = new Dimension(1100,500)
    contents       = new BorderPanel {
      add(new FlowPanel(startButton, stopButton, stepButton, randomizeButton, clearButton, 
                        speedLabel , minSpeedButton, slowerButton, 
                        speedSlider, fasterButton, maxSpeedButton, exitButton), 
          BorderPanel.Position.North) 
      add(canvas, BorderPanel.Position.Center) 
    }
  }
  
  top.listenTo (canvas.keys)
  val f = top.peer.getRootPane().getParent().asInstanceOf[javax.swing.JFrame]
  f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE) // TBD: does not seem to work on MacOS

  def  minSpeed = speedSlider.min
  def  maxSpeed = speedSlider.max
  def  speed    = speedSlider.value
  def setSpeed(s: Int) {
    speedLabel.text = "Speed: " + s
    speedSlider.value = s  
  }
  def digit2Speed(c: Char) = if (c=='0') maxSpeed else minSpeed+(c-'1')
  
  setSpeed(5)
  
  override def _live = _script(this, 'live) {_threaded0{Thread.sleep(34567)}}
  override def  live = _execute(_live)
}
