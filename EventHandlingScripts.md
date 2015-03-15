# Event handling scripts #

Event handling scripts are relatively complex. Usually they use a specific event listener which extends a regular event listener with `EventApplier` variables. These will execute the code of an event handling code fragment as a result of handling the event.

Note: the current implementation is still volatile. For code, see the file `subscript/swing/Scripts.scala` in the repository.

class `EventAppliers` will be like
```
 ....
```
A special key listener has EventAppliers that will execute the event handling code fragment
```
 ....
```
The script `key` has 2 parameters: a GUI component and a character value, which may be a constrained output parameter.
`key` creates such a SubScriptKeyListener. Then a directive applies this listener to the event handling code fragment, and it makes sure that the listener is added to and removed from the GUI component when needed.
The code fragment retrieves the character from the key event, and then matches this value against actual parameter constraints, when present.
```
 ....
```
For button actions, a similar listener and script would do. The code in the directive for the code fragment also enables and disables the button as needed. This must be done in the GUI thread; a preceding directive specifies that:
```
 ....
```