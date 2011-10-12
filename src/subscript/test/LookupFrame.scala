package subscript.test
import scala.swing._
import subscript.Predef._
import subscript.swing._
import subscript.swing.Scripts._
import subscript.vm._;
import subscript.vm.DSL._

// Subscript sample application: a text entry field with a search button, that simulates the invocation of a background search
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object LookupFrame extends LookupFrameApplication

class LookupFrameApplication extends SimpleSubscriptApplication {
  
  val outputTA     = new TextArea        {editable      = false}
  val searchButton = new Button("Go")    {enabled       = false}
  val searchLabel  = new Label("Search") {preferredSize = new Dimension(45,26)}
  val searchTF     = new TextField       {preferredSize = new Dimension(100, 26)}
  
  val top = new MainFrame {
    title    = "LookupFrame - Subscript"
    location = new Point    (100,100)
    preferredSize     = new Dimension(300,300)
    contents = new BorderPanel {
      add(new FlowPanel(searchLabel, searchTF, searchButton), BorderPanel.Position.North) 
      add(outputTA, BorderPanel.Position.Center) 
    }
    //setDefaultCloseOperation(WindowConstants.NONE)
    //defaultCloseOperation = JFrame.EXIT_ON_CLOSE    TBD how to do this in Scala.swing?
  }
/* the following subscript code has manually been compiled into Scala; see below
 override scripts
  live              = ...; searchSequence
 scripts
  searchSequence    = searchCommand    showSearchingText 
                      searchInDatabase showSearchResults

  searchCommand     = searchButton
  showSearchingText = @swing: {outputTA.text = "Searching: "+searchTF.text}
  showSearchResults = @swing: {outputTA.text = "Found: 3 items"}
  searchInDatabase  = {* Thread.sleep(2000) *} // simulate a time consuming action
  _(b: Button)      = clicked(b)
*/

  override def _live(caller: N_call)  =
  _script(caller, 'live,
		             T_n_ary(";", 
		            		T_0_ary("..."), 
		            		T_0_ary_code("call", (here: N_call) => _searchSequence(here))
               ))
  def _searchSequence(caller: N_call)  =
  _script(caller, 'searchSequence,
		             T_n_ary(";", 
		            		T_0_ary_code("call", (here: N_call) => _searchCommand    (here)), 
		            		T_0_ary_code("call", (here: N_call) => _showSearchingText(here)), 
		            		T_0_ary_code("call", (here: N_call) => _searchInDatabase (here)), 
		            		T_0_ary_code("call", (here: N_call) => _showSearchResults(here))
                     )
               )
  def _searchCommand(caller: N_call)  =
  _script(caller, 'searchCommand,
		             T_0_ary_code("call", (here: N_call) => __default(here, searchButton)))
		             
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
  def _searchInDatabase(caller: N_call)  =
  _script(caller, 'searchInDatabase,
		             T_0_ary_code("{**}", (here:N_code_threaded) => {Thread.sleep(2000)}))
 
  def __default(caller: N_call, _b:FormalInputParameter[Button])  =
  _script(caller, '_, param(_b,'b),
		             T_0_ary_code("call", (here:N_call) => _clicked(here, _b.value))
               )
               
// bridge methods; only the first one is actually used   
override def live     : ScriptExecuter = {val executer=new BasicExecuter; _live             (executer.anchorNode  ); executer.run}
def searchSequence    : ScriptExecuter = {val executer=new BasicExecuter; _searchSequence   (executer.anchorNode  ); executer.run}
def searchCommand     : ScriptExecuter = {val executer=new BasicExecuter; _searchCommand    (executer.anchorNode  ); executer.run}
def searchInDatabase  : ScriptExecuter = {val executer=new BasicExecuter; _searchInDatabase (executer.anchorNode  ); executer.run}
def showSearchingText : ScriptExecuter = {val executer=new BasicExecuter; _showSearchingText(executer.anchorNode  ); executer.run}
def showSearchResults : ScriptExecuter = {val executer=new BasicExecuter; _showSearchResults(executer.anchorNode  ); executer.run}
def _default(b:Button): ScriptExecuter = {val executer=new BasicExecuter; __default         (executer.anchorNode,b); executer.run}
}
