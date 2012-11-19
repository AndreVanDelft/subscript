package subscript.test
import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing.SimpleSubscriptApplication
import subscript.swing.Scripts._
import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm._


object Test1 extends Test1Application

class Test1Application extends SimpleSwingApplication {
  
  val outputTA     = new TextArea        {editable      = false}
  val searchLabel  = new Label("Search") {preferredSize = new Dimension(45,26)}
  val searchTF     = new TextField       {preferredSize = new Dimension(100, 26)}
  val searchButton = new Button("Go")    {
    reactions.+= {
      case ButtonClicked(b) =>
        enabled = false
        outputTA.text = "Starting search..."
        new Thread(new Runnable {
         def run() {
          Thread.sleep(3000)
          javax.swing.SwingUtilities.invokeLater(new Runnable {
            def run() {outputTA.text = "Search ready"
                       enabled = true
          }})
        }}).start
    }
  }
  
  val top          = new MainFrame {
    title          = "LookupFrame - Subscript"
    location       = new Point    (100,100)
    preferredSize  = new Dimension(300,300)
    contents       = new BorderPanel {
      add(new FlowPanel(searchLabel, searchTF, searchButton), BorderPanel.Position.North) 
      add(outputTA, BorderPanel.Position.Center) 
    }
  }
}
