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

// Subscript sample application: a text entry field with a search button, that simulates the invocation of a background search
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala
object LookupFrame2 extends LookupFrame2Application

class LookupFrame2Application extends SimpleSubscriptApplication {

  val outputTA     = new TextArea         {editable      = false}
  val searchButton = new Button("Go"    ) {enabled       = false}
  val cancelButton = new Button("Cancel") {enabled       = false}
  val   exitButton = new Button("Exit"  ) {enabled       = false}
  val searchLabel  = new Label("Search")  {preferredSize = new Dimension(45,26)}
  val searchTF     = new TextField        {preferredSize = new Dimension(100, 26)}
  
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
  f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE) // TBD: does not seem to work

  def confirmExit: Boolean = Dialog.showConfirmation(null, "Are you sure?", "About to exit")==Dialog.Result.Yes
  
  /* the following subscript code has manually been compiled into Scala; see below
 scripts
	searchCommand     = searchButton + Key.Enter
	cancelCommand     = cancelButton + Key.Escape 
	exitCommand       =   exitButton + windowClosing
	
	exit              =   exitCommand @gui: while (!confirmExit)
	cancelSearch      = cancelCommand @gui: showCanceledText
	
	live              = ...searchSequence || exit
	searchSequence    = searchCommand; showSearchingText searchInDatabase showSearchResults / cancelSearch
	
	showSearchingText = @gui: {outputTA.text = "Searching: "+searchTF.text}
	showCanceledText  = @gui: {outputTA.text = "Searching Canceled"}
	showSearchResults = @gui: {outputTA.text = ...}
	searchInDatabase  = {* Thread.sleep(3000)*}||progressMonitor
	
	progressMonitor   = ...{*Thread.sleep(250)*} @gui:{searchTF.text+=pass}
	
	implicit vkey(k: Key.Value??) = vkey(top, k??)
*/

  override def _live     = _script('live             ) {_par_or2(_seq(_loop, _searchSequence), _exit)}
  def _searchCommand     = _script('searchCommand    ) {_alt(_clicked(searchButton), _vkey(Key.Enter))} 
  def _cancelCommand     = _script('cancelCommand    ) {_alt(_clicked(cancelButton), _vkey(Key.Escape))}
  def   _exitCommand     = _script('exitCommand      ) {_clicked(exitButton)} // windowClosing
  def   _exit            = _script('exit             ) {_seq(  _exitCommand, _at{gui} (_while{!confirmExit}))}
  def _cancelSearch      = _script('cancelSearch     ) {_seq(_cancelCommand, _at{gui} (scriptCall_to_T_0_ary_code(_showCanceledText)))}
  def _searchSequence    = _script('searchSequence   ) {_seq(_searchCommand, 
     	                                                     _disrupt(_seq(_showSearchingText, _searchInDatabase, _showSearchResults),
                                                                      _cancelSearch ))}
  def _showSearchingText = _script('showSearchingText) {_at{gui} (_normal {            
    outputTA.text = 
      "Searching: "+searchTF.text
      })}
  def _showSearchResults = _script('showSearchResults) {_at{gui} (_normal1{(here: N_code_normal) => 
    outputTA.text = "Found: "+here.index+" items"})}
  def _showCanceledText  = _script('showCanceledText ) {_at{gui} (_normal {                         outputTA.text = "Searching Canceled"})}
  def _searchInDatabase  = _script('searchInDatabase ) {_threaded{Thread.sleep(2000)}} // {_par_or2(_threaded{Thread.sleep(5000)}, _progressMonitor)} TBD...
  def _progressMonitor   = _script('progressMonitor  ) {
  _seq(_loop, 
      _at{gui} (
          _normal{
            (here: N_code_normal) => outputTA.text+=" "+(10-pass(here))}), 
      _threaded{Thread.sleep(200)})}
 
  //def _vkey(_k:FormalConstrainedParameter[Key.Value]) = _script('clicked, _k~??'k) {_vkey(top, _k~??)} 
  // the line above would give this strange error message: recursive method _vkey needs result type
  // therefore we append a 1 to the name
  def _vkey(_k:FormalConstrainedParameter[Key.Value]) = _script('clicked, _k~??'k) {subscript.swing.Scripts._vkey(top, _k~??)}
               
// bridge methods; only the first one is actually used   
override def live      = _execute(_live)
def searchCommand      = _execute(_searchCommand    )
def cancelCommand      = _execute(_cancelCommand    )
def   exitCommand      = _execute(_exitCommand      )
def   exit             = _execute(_exit             )
def cancelSearch       = _execute(_cancelSearch     )
def searchSequence     = _execute(_searchSequence   )
def showSearchingText  = _execute(_showSearchingText)
def showSearchResults  = _execute(_showSearchResults)
def showCanceledText   = _execute(_showCanceledText )
def searchInDatabase   = _execute(_searchInDatabase )
}
