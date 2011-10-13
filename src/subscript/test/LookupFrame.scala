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

  override def _live     = _script('live          , _seq(_loop, _searchSequence))
  def _searchSequence    = _script('searchSequence, _seq(_searchCommand, _showSearchingText, _searchInDatabase, _showSearchResults))
  def _searchCommand     = _script('searchCommand , __default(searchButton))
  def _showSearchingText = _script('showSearchingText,
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              {outputTA.text = "Searching: "+searchTF.text})
                 )
  def _showSearchResults = _script('showSearchResults,
		             T_1_ary_code ("@:", (here: N_annotation[N_code_normal]) => {implicit val there=here.there; swing}, 
		              {(here: N_code_normal) => outputTA.text = "Found: "+here.index+" items"}))
		              
  def _searchInDatabase = _script('searchInDatabase, _threaded{Thread.sleep(2000)})
 
  def __default(_b:FormalInputParameter[Button]) = _script('_, _param(_b,'b), _clicked(_b.value))
               
// bridge methods; only the first one is actually used   
override def live      = execute(_live             )
def searchSequence     = execute(_searchSequence   )
def searchCommand      = execute(_searchCommand    )
def searchInDatabase   = execute(_searchInDatabase )
def showSearchingText  = execute(_showSearchingText)
def showSearchResults  = execute(_showSearchResults)
def _default(b:Button) = execute(__default      (b))
}
