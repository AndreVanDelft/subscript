# Introduction #

The exact behaviour of SubScript programs is defined using "call graphs". These are a kind of generalization to call stacks in imperative programming languages.

The call graphs are related to the static structures of the called scripts. These structures may also be viewed as graphs, that are even trees. We call these the "Template trees"

### Template Trees ###

Consider the script with two code fragments in sequence:
```
  main(args: Array[String]) = print,"Hello" print,"World"
```
The corresponding template tree is depicted:

![http://subscript.googlecode.com/files/templateMainHelloWorld.png](http://subscript.googlecode.com/files/templateMainHelloWorld.png)

### Call Graph ###

Executing a script from Scala involves a call graph that grows and shrinks while occasionally executing code fragments etc. Most nodes in the call graphs are related to nodes in the template tree.

A SubScript program has an execution rhythm that determines when various kinds of operations on the graph are done: growing by activating nodes, shrinking, by deactivating nodes, and others. Nodes are created by prescription of the templates; their activation, deactivation etc have specific characteristics.

The call graphs are directed and acyclic; the arrows depict an activation relation. There is some resemblance with the template trees. In fact, the call graph that is lives during execution of the earlier main script looks much at some stages like its template tree.

The call graph is a call tree as long as no communication is involved. With communication, leaves will grow together, so that the call graph is not a tree any more. However, the graph is directed and contains no cycles.

Nodes in the call graphs are depicted with similar labels as corresponding nodes in the template trees.

### Calling a Script from Scala ###

Each non-communicating script implies 2 boolean methods at the Scala level
  * one that accepts wrapped parameters and returns a "behavior". The behavior is a function that accepts a "caller" node as a parameter and pends to it a "callee" node with the script's template tree
  * one having just similar parameters, acting as a bridge for calls from Scala. Normally, such a bridge method returns a "Script executer"; only if the script is named "main" in an object with a variable string argument list, then the return type is Unit.

Effectively, the bridge method creates a "script executer". This contains in turn a new script call graph and an executer; its pends a pseudo "caller" node to the root node of the graph, and calls the other implied method. After that method has been anchored, the method "execute" is called on the script executer, and that runs the subscript call from Scala.
```
  def _main(_args: FormalInputParameter[Array[String]]) = _script('main, _args~'args) {_seq({print("Hello ")}, {println("world!")})}

  def  main( args: Array[String]): Unit = _execute(_main(args))
```
The root node is depicted in the call graph with the symbol `***`.

### Example: the `main` "Hello world" script ###
The development of graph for the "Hello world" example script is like:

![http://subscript.googlecode.com/files/callGraphMainHelloWorld1.png](http://subscript.googlecode.com/files/callGraphMainHelloWorld1.png)

Superficially, the following steps occur in order:

| Action | Node | Label | Remarks |
|:-------|:-----|:------|:--------|
| Scala call starts| Root | `***` | Bridge method called |
| Activate | Script | `main` | activates in turn its template´s body |
| Activate | Sequence | `;` | activates its template´s leftmost operand |
| Activate | Code fragment | `{}` | will await execution |
| Execute | Code fragment  | `{}` | happens to be succesful |
| Succeed | Code fragment  | `{}` | reports to parent node, then becomes inactive |
| Activate | Sequence | `;` | activates its template´s next operand |
| Deactivate | Code fragment | `{}` | inactive node is removed from the graph |
| Execute | Code fragment  | `{}` | happens to be succesful |
| Succeed | Code fragment  | `{}` | reports to parent node (and onwards), then becomes inactive |
| Deactivate | Code fragment | `{}` | inactive node is removed from the graph |
| Deactivate | Code fragment | `;` | inactive node is removed from the graph |
| Deactivate | Code fragment | `main` | inactive node is removed from the graph |
| Scala call ends| Root | `***` | `true` returned because of reported success |

### Execution Rhythm ###

The call graph management is done using so called Call Graph Messages. These concern activation, deactivation, success, exclusion and notifications that atomic actions have been activated, started or ended.

An active script executer sends around such messages in the graph until the graph has become empty.
For this purpose there is a message queue and message loop. It is possible that there are no waiting messages whereas the graph is still not empty: this happens when the execution waits for an event that drives an event handling code fragment. When that happens, the execution wait comes to an end, and new messages may be executed again.

The messages have relative priorities, that are very relevant to the execution.