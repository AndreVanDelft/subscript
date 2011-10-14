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
  
  val top = new MainFrame {
    title    = "LookupFrame - Subscript"
    location = new Point    (100,100)
    preferredSize     = new Dimension(500,300)
    contents = new BorderPanel {
      add(new FlowPanel(searchLabel, searchTF, searchButton, cancelButton, exitButton), BorderPanel.Position.North) 
      add(outputTA, BorderPanel.Position.Center) 
    }
    //setDefaultCloseOperation(WindowConstants.NONE)
    //defaultCloseOperation = JFrame.EXIT_ON_CLOSE    TBD how to do this in Scala.swing?
  }
  
  top.listenTo (searchTF.keys)
  val f = top.peer.getRootPane().getParent().asInstanceOf[javax.swing.JFrame]
  f.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)

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
	searchInDatabase  = {* Thread.sleep(3000)*}||...{*Thread.sleep(250); searchTF.text+=pass*}

    implicit(keyValue:Key.Value??) = vkey(keyValue??)
*/

  override def _live = _script('live) {_par_or2(_seq(_loop, _searchSequence), _exit)}
  def _searchCommand = _script('searchCommand) {_alt(_implicit(searchButton), _implicit(Key.Enter))}
  def _cancelCommand = _script('cancelCommand) {_alt(_implicit(cancelButton), _implicit(Key.Escape))}
  def   _exitCommand = _script('exitCommand) {_implicit(exitButton)} // windowClosing
  def   _exit        =  _script('exit) {_seq(_exitCommand, 
		                    T_1_ary_code ("@:",    (here: N_annotation[N_while]) => {implicit val there=here.there; gui}, 
		                     _while{!confirmExit})
                            )}
 
  def _cancelSearch = _script('cancelSearch) {_seq(_cancelCommand, 
		                    T_1_ary_code ("@:",   (here: N_annotation[N_call]) => {implicit val there=here.there; gui}, 
                             _showCanceledText)
                            )
  }
  def _searchSequence = _script('searchSequence) {_seq(_searchCommand, 
     	                  _disrupt( 
		                    _seq(_showSearchingText, _searchInDatabase, _showSearchResults),
                            _cancelSearch 
                         ))}
  def _showSearchingText = _script('showSearchingText) {
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; gui}, 
		              {outputTA.text = "Searching: "+searchTF.text})
  }
  def _showSearchResults = _script('showSearchResults) {
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; gui}, 
		              {(here: N_code_normal) => outputTA.text = "Found: "+here.index+" items"})}
		              
  def _showCanceledText = _script('showCanceledText) {
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; gui}, 
		              {outputTA.text = "Searching Canceled"})
  }
  def _searchInDatabase = _script('searchInDatabase) {_threaded{for(i<-0 to 9) {outputTA.text+=i;Thread.sleep(300)}}}
 
  def _implicit(_b:FormalInputParameter[Button])  = _script('_, _param(_b,'b)) {_clicked(_b.value)}
  def _implicit(_keyValue:FormalConstrainedParameter[Key.Value]) = _script('_, _param(_keyValue,'keyValue)) {_vkey(top, ActualAdaptingParameter(_keyValue))}
               
// bridge methods; only the first one is actually used   
override def live      = _execute(_live)
def searchSequence     = _execute(_searchSequence   )
def searchCommand      = _execute(_searchCommand    )
def cancelCommand      = _execute(_cancelCommand    )
def   exitCommand      = _execute(_exitCommand      )
def cancelSearch       = _execute(_cancelSearch     )
def searchInDatabase   = _execute(_searchInDatabase )
def showSearchingText  = _execute(_showSearchingText)
def showSearchResults  = _execute(_showSearchResults)
def showCanceledText   = _execute(_showCanceledText )
}
