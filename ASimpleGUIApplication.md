## A Simple GUI Application ##
Suppose we need a simple program to look up items in a database, based on a search string.

![http://scriptic.googlecode.com/files/Lookup1.png](http://scriptic.googlecode.com/files/Lookup1.png)

The user can enter a search string in the text field and then press the Go button. This will at first put a "Searching…" message in the text area at the lower part. Then the actual search will be performed at a database, which may take a few seconds. Finally the results from the database are shown in the text area.

In SubScript you can program that sequence of events and actions in an intuitively clear way:
```
  searchSequence = searchCommand  showSearchingText searchInDatabase showSearchResults
```
Here `searchCommand` would represent the event of the user pressing the button. `showSearchingText` and `showSearchResults` each write something in the text area. `searchInDatabase` does the database search.

`searchCommand` is refined with a reusable script named `clicked`:
```
  searchCommand  = clicked(searchButton)
```
This `clicked` script "happens" when the user presses the search button. It is defined in a utility object `subscript.swing.Scripts`. As a bonus, the action script makes sure the button is exactly enabled when applicable. It will automatically be disabled as long as searchInDatabase is going on.

The definition of `clicked` is also marked as `implicit` so that its name may be left out:
```
  searchCommand = searchButton
```
This states as concise as possible that clicking the search button triggers he search.

The script calls `showSearchingText` and `showSearchResults` set the text contents of the text area, which is represented by the variable named `outputTA`. A complication is that this must happen in the swing thread:
```
  showSearchingText = @gui: {outputTA.text = "Searching: "+searchTF.text}
  showSearchResults = @gui: {outputTA.text = ...}
```
Here `@gui:` is again an annotation': `gui` is a method in `subscript.swing.Scripts`, that has the special value here as a parameter. This the code of this annotation is executed on activation. It makes sure that its operand (the code fragment) is executed in the swing thread, just as needed.

The `searchInDatabase` could in a similar way perform a search on the database in a background thread. In this example, the search is simulated by a short sleep, but still in a background thread, so that the GUI will not be harmed during the sleep. A nice looking way to specify that an action must happen in a background thread is by enclosing it in braces with asterisks:
```
searchInDatabase = {* Thread.sleep 3000 *}
```
If you would to program this functionality in plain Java, the resulting code will be much more complex. The code would look like:
```
private void searchButton_actionPerformed() {
  outputTA.text = "Searching for: " + searchTF.text;
  searchButton. setEnabled(false);
  new Thread() {
    public void run() {
      Thread.sleep(3000) //i.e. searchInDatabase
      SwingUtilities.invokeLater(
        new Runnable() {
          public void run() {
            outputTA.text = "Search ready";
            searchButton. setEnabled(true);
          }
        }
      );
    }
  }.start();
}
```
In Scala it would be much similar:
```
  val searchButton = new Button("Go")    {
    reactions.+= {
      case ButtonClicked(b) =>
        enabled = false
        outputTA.text = "Searching for: " + searchTF.text
        new Thread(new Runnable {
         def run() {
          Thread.sleep(3000) //i.e. searchInDatabase
          javax.swing.SwingUtilities.invokeLater(new Runnable {
            def run() {outputTA.text = "Search ready"
                       enabled = true
          }})
        }}).start
    }
  }
```

For a good comparison of sizes, the SubScript version without refinements is:
```
live =       clicked(searchButton) 
       @gui: {outputTA.text="Searching for: " + searchTF.text} 
             {* Thread.sleep(3000) *} //i.e. searchInDatabase
       @gui: {outputTA.text="Search ready"}
             ...
```

### Extending the program ###

It is easy to extend the functionality of this program. For instance, the search action may also be triggered by the user pressing the Enter key in the search text field (searchTF). For this purpose we can adapt the searchCommand. Another user command could be to cancel an ongoing search in the database. For this the user could press a Cancel button, or press the Escape key. Finally the user may want to exit the application by pressing an Exit button, rather than clicking in the closing box. Moreover, he should then be presented a dialog where he can confirm the exit.

![http://scriptic.googlecode.com/files/Lookup2.png](http://scriptic.googlecode.com/files/Lookup2.png)

```
  searchCommand = searchButton + Key.Enter
  cancelCommand = cancelButton + Key.Escape
  exitCommand   = exitButton   + windowClosing
```
Here the plus symbol denotes choice, just like the semicolon denotes sequence. There are quite some other operators like these, most of which express a specific flavour of parallelism. windowClosing is a predefined script.

`Key.Enter` and `Key.Escape` stand for calls to implicit scripts `key` and `vkey`.

We see that searchCommand has been defined as an addition of a button and a character. This is something new in programming; I call it ''Item Algebra'', see later.

After the exit command, a confirmation dialog should come up, in the Swing thread. If the user confirms, then the program should end. After the cancel command, an applicable text should be shown in the text area:
```
  exit         =   exitCommand @gui: while (!confirmExit)
  cancelSearch = cancelCommand @gui: showCanceledText
```
This `exit` script is easily added to the live script:
```
  live             = ...searchSequence || exit
```
The double bar `||` denotes or-parallelism: both operands happen, but when one operand terminates successfully the parallel composition also terminates successfully. The double bar is here analogous to its usage in boolean expressions. The same holds for 3 other parallel operators: `&&`, `|`and `&`.The 3 dots (`...`) are equivalent to `while(true)`. Two dots (`..`) would create a loop with an optional exit, a bit comparable to the asterisk in regular expressions.

To fit in a call to the cancelSearch script, we have to split up the script searchSequence:
```
  searchSequence = searchCommand; searchAction / cancelSearch
  searchAction   = showSearchingText searchInDatabase showSearchResults
```
This way the Cancel button will only become enabled after the search command has been issued, and it will be disabled again after the search results have been shown.The slash symbol (`/`) denotes breaking behavior: the left hand side happens, but the right hand side may take over. The semicolon should not be left out here, because an "empty symbol" would bind too strong.