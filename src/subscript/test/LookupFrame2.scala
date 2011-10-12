package subscript.test
import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing._
import subscript.swing.Scripts._
import subscript.vm._;
import subscript.vm.DSL._

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
	searchCommand     = searchButton + KeyEvent.VK_ENTER
	cancelCommand     = cancelButton + KeyEvent.VK_ESCAPE 
	exitCommand       =   exitButton + windowClosing
	
	exit              =   exitCommand @swing: while (!confirmExit)
	cancelSearch      = cancelCommand @swing: showCanceledText
	
	live              = ...searchSequence || exit
	searchSequence    = searchCommand; showSearchingText searchInDatabase showSearchResults / cancelSearch
	
	showSearchingText = @swing: {outputTA.text = "Searching: "+searchTF.text}
	showCanceledText  = @swing: {outputTA.text = "Searching Canceled"}
	showSearchResults = @swing: {outputTA.text = ...}
	searchInDatabase  = {* Thread.sleep(3000)*}||...{*Thread.sleep(250); searchTF.text+=pass*}

  _(keyValue:Key.Value??) = vkey(keyValue??)
*/

  def _live(caller: N_call)  =
  _script(caller, 'live,
		             T_n_ary("||", 
		              T_n_ary(";", 
		            		T_0_ary("..."), 
		            		T_0_ary_code("call", (here: N_call) => _searchSequence(here))
                            ),
                     T_0_ary_code       ("call", (here: N_call) => _exit(here)))
                     )
 
  def _searchCommand(caller: N_call)  =
    _script(caller, 'searchCommand,
		             T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => __default(here, searchButton)), 
                      T_0_ary_code("call", (here: N_call) => __default(here, Key.Enter))
                      )
                     )
  def _cancelCommand(caller: N_call)  =
    _script(caller, 'cancelCommand,
		             T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => __default(here, cancelButton)), 
                      T_0_ary_code("call", (here: N_call) => __default(here, Key.Escape))
                      )
                     )
  def _exitCommand(caller: N_call)  =
    _script(caller, 'exitCommand,
		             //T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => __default(here, exitButton))
                     // T_0_ary_code("call", (here: N_call) => windowClosing(here)
                     // ), 
                     )
  def _exit(caller: N_call)  =
    _script(caller, 'exit,
		             T_n_ary(";", 
		            		T_0_ary_code ("call",  (here: N_call               ) => _exitCommand(here)), 
		                    T_1_ary_code ("@:",    (here: N_annotation[N_while]) => {implicit val there=here.there; swing}, 
		                     T_0_ary_test("while", (here:              N_while ) => confirmExit))
                            )
                     )
 
  def _cancelSearch(caller: N_call)  =
    _script(caller, 'cancelSearch,
		             T_n_ary(";", 
		            		T_0_ary_code ("call", (here: N_call)               => _cancelCommand    (here)), 
		                    T_1_ary_code ("@:",   (here: N_annotation[N_call]) => {implicit val there=here.there; swing}, 
                             T_0_ary_code("call", (here:              N_call ) => _showCanceledText(here)))
                            )
                     )
  def _searchSequence(caller: N_call)  =
    _script(caller, 'searchSequence,
		             T_n_ary(";", 
	            		T_0_ary_code("call", (here: N_call) => _searchCommand    (here)), 
     	                T_n_ary("/", 
		                  T_n_ary(";", 
		            		T_0_ary_code("call", (here: N_call) => _showSearchingText(here)), 
		            		T_0_ary_code("call", (here: N_call) => _searchInDatabase (here)), 
		            		T_0_ary_code("call", (here: N_call) => _showSearchResults(here))),
                          T_0_ary_code(  "call", (here: N_call) => _cancelSearch     (here))) 
                     ))
  def _showSearchingText(caller: N_call)  =
  _script(caller, 'showSearchingText,
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Searching: "+searchTF.text}))
                 )
  def _showSearchResults(caller: N_call)  =
  _script(caller, 'showSearchResults,
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Found: "+here.index+" items"}))
                 )
  def _showCanceledText(caller: N_call)  =
  _script(caller, 'showCanceledText,
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Searching Canceled"}))
                 )
  def _searchInDatabase(caller: N_call)  =
  _script(caller, 'searchInDatabase,
		             T_0_ary_code("{**}", (here:N_code_threaded) => {for(i<-0 to 9) {outputTA.text+=i;Thread.sleep(300)}}))
 
  def __default(caller: N_call, _b:FormalInputParameter[Button])  =
  _script(caller, '_, param(_b,'b),
		             T_0_ary_code("call", (here:N_call) => _clicked(here, _b.value))
               )
  def __default(caller: N_call, _keyValue:FormalConstrainedParameter[Key.Value])  =
  _script(caller, '_, param(_keyValue,'keyValue),
		             T_0_ary_code("call", (here:N_call) => _vkey(here, top, ActualAdaptingParameter(_keyValue)))
               )
               
// bridge methods; only the first one is actually used   
// bridge methods; only the first one is actually used   
def live                 : ScriptExecuter = {val executer=new BasicExecuter; _live             (executer.anchorNode  ); executer.run}
def searchSequence       : ScriptExecuter = {val executer=new BasicExecuter; _searchSequence   (executer.anchorNode  ); executer.run}
def searchCommand        : ScriptExecuter = {val executer=new BasicExecuter; _searchCommand    (executer.anchorNode  ); executer.run}
def cancelCommand        : ScriptExecuter = {val executer=new BasicExecuter; _cancelCommand    (executer.anchorNode  ); executer.run}
def   exitCommand        : ScriptExecuter = {val executer=new BasicExecuter;   _exitCommand    (executer.anchorNode  ); executer.run}
def searchInDatabase     : ScriptExecuter = {val executer=new BasicExecuter; _searchInDatabase (executer.anchorNode  ); executer.run}
def cancelSearch         : ScriptExecuter = {val executer=new BasicExecuter; _cancelSearch     (executer.anchorNode  ); executer.run}
def showSearchingText    : ScriptExecuter = {val executer=new BasicExecuter; _showSearchingText(executer.anchorNode  ); executer.run}
def showCanceledText     : ScriptExecuter = {val executer=new BasicExecuter; _showCanceledText (executer.anchorNode  ); executer.run}
def showSearchResults    : ScriptExecuter = {val executer=new BasicExecuter; _showSearchResults(executer.anchorNode  ); executer.run}
def _default(b:Button)   : ScriptExecuter = {val executer=new BasicExecuter; __default         (executer.anchorNode,b); executer.run}
def _default(k:Key.Value): ScriptExecuter = {val executer=new BasicExecuter; __default         (executer.anchorNode,k); executer.run}
}
