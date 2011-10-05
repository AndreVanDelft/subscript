package subscript.test
import scala.swing._
import subscript.Predef._
import subscript.swing._
import subscript.swing.Scripts._
import subscript.vm._;

// Subscript sample application: a text entry field with a search button, that simulates the onvocation of a background search
//
// Note: the main part of this source file has been manually compiled from Subscript code into plain Scala

object LookupFrame1 extends SimpleSubscriptApplication {
  
  val outputTA     = new TextArea    {editable=false}
  val searchButton = new Button("Go") {enabled=false}
  val searchLabel  = new Label("Search") {preferredSize=new Dimension(45,26)}
  val searchTF     = new TextField       {preferredSize=new Dimension(100, 26)}
  
  def top = new MainFrame {
    title    = "LookupFrame - Subscript"
    location = new Point    (400,400)
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
  searchInDatabase  = {* Thread.sleep 1000 *} // simulate a time consuming action
  _(b: Button)      = clicked(b)
*/

  def live(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary("..."), 
		            		T_0_ary_code("call", (here: N_call) => searchSequence(here))
                            ), 
                     "live")
               )
  def searchSequence(caller: N_call)  =
    caller.calls(T_script("script",
		             T_n_ary(";", 
		            		T_0_ary_code("call", (here: N_call) => searchCommand    (here)), 
		            		T_0_ary_code("call", (here: N_call) => showSearchingText(here)), 
		            		T_0_ary_code("call", (here: N_call) => searchInDatabase (here)), 
		            		T_0_ary_code("call", (here: N_call) => showSearchResults(here))
                            ), 
                     "searchSequence")
               )
  def searchCommand(caller: N_call)  =
    caller.calls(T_script("script",
		             T_0_ary_code("call", (here: N_call) => default(here, ActualInputParameter(searchButton))), 
                     "searchCommand")
                 )
  def showSearchingText(caller: N_call)  =
    caller.calls(T_script("script",
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {val there=here.there; swing(there)}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Searching: "+searchTF.text})), 
                     "showSearchingText")
                 )
  def showSearchResults(caller: N_call)  =
    caller.calls(T_script("script",
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {val there=here.there; swing(there)}, 
		              T_0_ary_code("{}", (here:              N_code_normal ) => {outputTA.text = "Found: "+here.index+" items"})), 
                     "showSearchResults")
                 )
  def searchInDatabase(caller: N_call)  =
    caller.calls(T_script("script",
		             T_0_ary_code("{**}", (here:N_code_threaded) => {Thread.sleep(1000)}),
                     "searchInDatabase")
               )
 
def default(caller: N_call, b:ActualInputParameter[Button])  =
  caller.calls(T_script("script",
		             T_0_ary_code("call", (here:N_call) => clicked(here, ActualInputParameter(here.getParameter("b").value.asInstanceOf[Button]))),
                     "default(Button)", new FormalInputParameter("b")),
                  b
               )
               
// bridge methods; only the first one is actually used   
override
def live             : ScriptExecuter = {val executer=new BasicExecuter; live             (executer.anchorNode  ); executer.run}
def searchSequence   : ScriptExecuter = {val executer=new BasicExecuter; searchSequence   (executer.anchorNode  ); executer.run}
def searchCommand    : ScriptExecuter = {val executer=new BasicExecuter; searchCommand    (executer.anchorNode  ); executer.run}
def searchInDatabase : ScriptExecuter = {val executer=new BasicExecuter; searchInDatabase (executer.anchorNode  ); executer.run}
def showSearchingText: ScriptExecuter = {val executer=new BasicExecuter; showSearchingText(executer.anchorNode  ); executer.run}
def showSearchResults: ScriptExecuter = {val executer=new BasicExecuter; showSearchResults(executer.anchorNode  ); executer.run}
def default(b:Button): ScriptExecuter = {val executer=new BasicExecuter; default          (executer.anchorNode,ActualInputParameter(b)); executer.run}
}
