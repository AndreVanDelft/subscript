package subscript.test
import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing._
import subscript.swing.Scripts._
import subscript.vm._;

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

  def searchCommand(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => _default(here, searchButton)), 
                      T_0_ary_code("call", (here: N_call) => _default(here, Key.Enter))
                      ), 
                     "searchCommand")
                 )
  def cancelCommand(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => _default(here, cancelButton)), 
                      T_0_ary_code("call", (here: N_call) => _default(here, Key.Escape))
                      ), 
                     "cancelCommand")
                 )
  def exitCommand(caller: N_call)  =
    caller.calls(T_script("script",
		             //T_n_ary("+", 
                      T_0_ary_code("call", (here: N_call) => _default(here, exitButton)), 
                     // T_0_ary_code("call", (here: N_call) => windowClosing(here)
                     // ), 
                     "exitCommand")
                 )
  def exit(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary_code ("call",  (here: N_call               ) => exitCommand(here)), 
		                    T_1_ary_code ("@:",    (here: N_annotation[N_while]) => {implicit val there=here.there; swing}, 
		                     T_0_ary_test("while", (here:              N_while ) => confirmExit))
                            ), 
                     "exit")
               )
 
  def cancelSearch(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary_code ("call", (here: N_call)               => cancelCommand    (here)), 
		                    T_1_ary_code ("@:",   (here: N_annotation[N_call]) => {implicit val there=here.there; swing}, 
                             T_0_ary_code("call", (here:              N_call ) => showCanceledText(here)))
                            ), 
                     "cancelSearch")
               )
  def live(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary("||", 
		              T_n_ary(";", 
		            		T_0_ary("..."), 
		            		T_0_ary_code("call", (here: N_call) => searchSequence(here))
                            ),
                     T_0_ary_code       ("call", (here: N_call) => exit(here))), 
                     "live")
               )
 
  def searchSequence(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary(";", 
	            		T_0_ary_code("call", (here: N_call) => searchCommand    (here)), 
     	                T_n_ary("/", 
		                  T_n_ary(";", 
		            		T_0_ary_code("call", (here: N_call) => showSearchingText(here)), 
		            		T_0_ary_code("call", (here: N_call) => searchInDatabase (here)), 
		            		T_0_ary_code("call", (here: N_call) => showSearchResults(here))),
                          T_0_ary_code(  "call", (here: N_call) => cancelSearch     (here))) 
                     ), 
                     "searchSequence")
               )
  def showSearchingText(caller: N_call)  =
    caller.calls(T_script("script",
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Searching: "+searchTF.text})), 
                     "showSearchingText")
                 )
  def showCanceledText(caller: N_call)  =
    caller.calls(T_script("script",
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Searching Canceled"})), 
                     "showCanceledText")
                 )
  def showSearchResults(caller: N_call)  =
    caller.calls(T_script("script",
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Found: "+here.index+" items"})), 
                     "showSearchResults")
                 )
  def searchInDatabase(caller: N_call)  =
    caller.calls(T_script("script",
		             T_0_ary_code("{**}", (here:N_code_threaded) => {for(i<-0 to 9) {outputTA.text+=i;Thread.sleep(300)}}),
                     "searchInDatabase")
               )
 
def _default(caller: N_call, _b:FormalInputParameter[Button])  =
  caller.calls(T_script("script",
		             T_0_ary_code("call", (here:N_call) => clicked(here, _b.value)),
                     "_default(Button)", "b"),
                  _b
               )
def _default(caller: N_call, _keyValue:FormalConstrainedParameter[Key.Value])  =
  caller.calls(T_script("script",
                     T_0_ary_code("call", (here:N_call) => vkey(here, top, ActualAdaptingParameter(_keyValue))),
                     "_default(Key.Value)", "keyValue"),
                 _keyValue
              )
               
// bridge methods; only the first one is actually used   
// bridge methods; only the first one is actually used   
def live                 : ScriptExecuter = {val executer=new BasicExecuter; live             (executer.anchorNode  ); executer.run}
def searchSequence       : ScriptExecuter = {val executer=new BasicExecuter; searchSequence   (executer.anchorNode  ); executer.run}
def searchCommand        : ScriptExecuter = {val executer=new BasicExecuter; searchCommand    (executer.anchorNode  ); executer.run}
def cancelCommand        : ScriptExecuter = {val executer=new BasicExecuter; cancelCommand    (executer.anchorNode  ); executer.run}
def   exitCommand        : ScriptExecuter = {val executer=new BasicExecuter;   exitCommand    (executer.anchorNode  ); executer.run}
def searchInDatabase     : ScriptExecuter = {val executer=new BasicExecuter; searchInDatabase (executer.anchorNode  ); executer.run}
def cancelSearch         : ScriptExecuter = {val executer=new BasicExecuter; cancelSearch     (executer.anchorNode  ); executer.run}
def showSearchingText    : ScriptExecuter = {val executer=new BasicExecuter; showSearchingText(executer.anchorNode  ); executer.run}
def showCanceledText     : ScriptExecuter = {val executer=new BasicExecuter; showCanceledText (executer.anchorNode  ); executer.run}
def showSearchResults    : ScriptExecuter = {val executer=new BasicExecuter; showSearchResults(executer.anchorNode  ); executer.run}
def _default(b:Button)   : ScriptExecuter = {val executer=new BasicExecuter; _default         (executer.anchorNode,b); executer.run}
def _default(k:Key.Value): ScriptExecuter = {val executer=new BasicExecuter; _default         (executer.anchorNode,k); executer.run}
}
