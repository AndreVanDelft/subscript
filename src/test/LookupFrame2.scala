import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing.SimpleSubscriptApplication
import subscript.swing.Scripts._
import subscript._
import subscript.DSL._
import subscript.Predef._

import subscript.vm._

// Subscript sample application: a text entry field with a search button, that simulates the invocation of a background search
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala
object LookupFrame2 extends LookupFrame2Application

class LookupFrame2Application extends SimpleSubscriptApplication {

  val outputTA     = new TextArea         {editable      = false}
  val searchButton = new Button("Go"    ) {enabled       = false; focusable = false}
  val cancelButton = new Button("Cancel") {enabled       = false; focusable = false}
  val   exitButton = new Button("Exit"  ) {enabled       = false; focusable = false}
  val searchLabel  = new Label("Search")  {preferredSize = new Dimension(45,26)}
  val searchTF     = new TextField        {preferredSize = new Dimension(100, 26)}
  
//  listenTo(searchTF)
//  reactions += {
//      case event => {
//                 println(event); 
//                 println("**************************************")
//                 println(Thread.currentThread().getStackTrace().mkString("\n"))
//                 println("**************************************")
//               }
//  }
  
  val top          = new MainFrame {
    title          = "LookupFrame - Subscript"
    location       = new Point    (100,100)
    preferredSize  = new Dimension(500,300)
    contents       = new BorderPanel {
      add(new FlowPanel(searchLabel, searchTF, searchButton, cancelButton, exitButton), BorderPanel.Position.North) 
      add(outputTA, BorderPanel.Position.Center) 
    }
  }
  
  top.listenTo (searchTF.keys)
  val f = top.peer.getRootPane().getParent().asInstanceOf[javax.swing.JFrame]
  f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE) // TBD: does not seem to work on MacOS
  
  def sleep(duration_ms: Long) = try {Thread.sleep(duration_ms)} catch {case e: InterruptedException => /*println("sleep interrupted")*/}
  def confirmExit: Boolean = Dialog.showConfirmation(null, "Are you sure?", "About to exit")==Dialog.Result.Yes
  
  /* the following subscript code has manually been compiled into Scala; see below
 script..
	searchCommand     = searchButton + Key.Enter
	cancelCommand     = cancelButton + Key.Escape 
	exitCommand       =   exitButton + windowClosing
	
	exit              =   exitCommand @gui: while (!confirmExit)
	cancelSearch      = cancelCommand @gui: showCanceledText
	
	live              = ...searchSequence || exit
	searchSequence    = guard(searchTF, ()=> !searchTF.text.isEmpty); // searchCommand should not be active if the text field is empty
	                    searchCommand; 
	                    showSearchingText searchInDatabase showSearchResults / cancelSearch
	
	showSearchingText = @gui: {outputTA.text = "Searching: "+searchTF.text}
	showCanceledText  = @gui: {outputTA.text = "Searching Canceled"}
	showSearchResults = @gui: {outputTA.text = ...}
	searchInDatabase  = {* Thread.sleep(3000)*}||progressMonitor
	
	progressMonitor   = ...{*Thread.sleep(250)*} @gui:{searchTF.text+=pass}
	
	implicit vkey(k: Key.Value??) = vkey(top, k??)
*/
  override def _live     = _script(this, 'live             ) {_par_or2(_seq(_loop, _searchSequence), _exit)}
  def _searchCommand     = _script(this, 'searchCommand    ) {_alt(_clicked(searchButton), _vkey(Key.Enter))} 
  def _cancelCommand     = _script(this, 'cancelCommand    ) {_alt(_clicked(cancelButton), _vkey(Key.Escape))}
  def   _exitCommand     = _script(this, 'exitCommand      ) {_clicked(exitButton)} // windowClosing
  def _cancelSearch      = _script(this, 'cancelSearch     ) {_seq(_cancelCommand, _at{gui} (_call{_showCanceledText}))}
  def _searchSequence    = _script(this, 'searchSequence   ) {_seq(_guard(searchTF, ()=> !(searchTF.text.isEmpty)),  
                                                                   _searchCommand, 
     	                                                           _disrupt(_seq(_showSearchingText, _searchInDatabase, _showSearchResults),
                                                                                 _cancelSearch ))}
  def   _exit1            = _script(this, 'exit             ) {_seq(  _exitCommand, _at{gui} (_while0{!confirmExit}))}
  def   _exit            = {val _r = _declare[Boolean]('r)
                           _script(this, 'exit             ) {_seq(_var(_r, (here:N_localvar[_]) => false), 
                                                                   _exitCommand,
                                                                   _at{gui} (_normal{here => _r.at(here).value = confirmExit; println("confirmExit="+_r.at(here).value)}),
                                                                   _while{here=> {! _r.at(here).value}})}
  }
  
  def _showSearchingText = _script(this, 'showSearchingText) {_at{gui} (_normal0 {            
    outputTA.text = 
      "Searching: "+searchTF.text
      })}
  def _showSearchResults = _script(this, 'showSearchResults) {_at{gui} (_normal{(here: N_code_normal) => 
    outputTA.text = "Found: "+here.index+" items"})}
  def _showCanceledText  = _script(this, 'showCanceledText ) {_at{gui} (_normal0 {outputTA.text = "Searching Canceled"})}
  def _searchInDatabase  = _script(this, 'searchInDatabase ) {_threaded0{sleep(4000)}}//{_par_or2(_threaded0{sleep(5000)}, _progressMonitor)} 
  def _progressMonitor   = _script(this, 'progressMonitor  ) {
  _seq(_loop, 
      _at{gui} (
          _normal{
            (here: N_code_normal) => outputTA.text+=" "+pass(here)}), 
      _threaded0{sleep(200)})}
 
  def _vkey(_k:FormalConstrainedParameter[Key.Value]) = _script(this, 'vkey, _k~??'k) {subscript.swing.Scripts._vkey(top, _k~??)}
               
// bridge method   
override def live = _execute(_live)
}
