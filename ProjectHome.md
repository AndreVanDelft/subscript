Subscript is an extension to the Scala programming language that is based on the Algebra of Communicating Processes (ACP). It adds "scripts" as a new refinement construct, that contain a kind of algebraic expressions.

Threading and event-handling are badly supported by mainstream programming languages: the flow of control is often unclear, and programmers lose overview. The result is frozen applications and stressed users.

Subscript aims to improve this situation. It supports parallelism and non-deterministic choice as much as sequential composition. Event-handling code is easily mixed with normal code. You can easily express

  * the "grammars" of Graphical User Interfaces
  * concurrency, e.g., in the benchmark program Sieve of Eratosthenes
  * language grammars, which are useful when parsing texts
  * discrete event simulations

Quick links:
  * [Video of a presentation at Scala Days 2012 in London](http://skillsmatter.com/podcast/scala/subscript-extending-scala-with-the-algebra-of-communicating-processes)
  * [Powerpoint slides used at Scala Days ](http://code.google.com/p/subscript/downloads/detail?name=Subscript-presentation20120417.ppt)
  * [The related research paper](http://code.google.com/p/subscript/downloads/detail?name=SubScript-TR2012A.pdf)
  * [A White Paper](http://code.google.com/p/subscript/downloads/detail?name=SubScript%20White%20Paper.pdf)
  * [PowerPoint presentation of a lecture at Amsterdam University for the course Thread Algebra](http://code.google.com/p/subscript/downloads/detail?name=Event-driven%20and%20Concurrent%20Programming.ppt)
  * [Presentation on 7 August 2012 at the Berlin Compiler Meetup](http://berlin.compilermeet.org/)

For a first impression, consider a simple lookup application:

![http://scriptic.googlecode.com/files/Lookup1.png](http://scriptic.googlecode.com/files/Lookup1.png)

In Java this is still quite hard to program, due to the threading aspects. In SubScript you can program this easily and concisely:
```
  searchSequence    = searchCommand    showSearchingText 
                      searchInDatabase showSearchResults

  searchCommand     = searchButton
  showSearchingText = @gui: {outputTA.text = "Searching: "+searchTF.text}
  showSearchResults = @gui: {outputTA.text = "Found: 3 items"}
  searchInDatabase  = {* Thread.sleep 3000 *} // simulate a time consuming action
```

The [Wiki](http://code.google.com/p/subscript/wiki/Contents) provides a more detailed explanation and some other examples. It also highlights the theoretical background, and describes the language features and the implementation model.
Among others:

  * [Introduction](http://code.google.com/p/subscript/wiki/Introduction)
  * [A Simple GUI Application](http://code.google.com/p/subscript/wiki/ASimpleGUIApplication)
  * [Dataflow Programming](http://code.google.com/p/subscript/wiki/DataflowProgramming)
  * [Sieve of Eratosthenes](http://code.google.com/p/subscript/wiki/SieveOfEratosthenes)
  * [Discrete Event Simulation](http://code.google.com/p/subscript/wiki/DiscreteEventSimulation)
  * [A Simulation of the Alternating Bit Protocol](http://code.google.com/p/subscript/wiki/ProtocolSimulation)
  * [Theoretical Background](http://code.google.com/p/subscript/wiki/TheoreticalBackground)
  * [From ACP and Scala to SubScript ](http://code.google.com/p/subscript/wiki/FromACPandScalaToSubScript)
  * [An Operational Model ](http://code.google.com/p/subscript/wiki/OperationalModel)
  * [Implementation](http://code.google.com/p/subscript/wiki/Implementation)

Note that these Wiki pages are meant to become a explanation and reference for an initial implementation and language definition; the aim of these pages is not to give programmers and researchers a clear and well dosed introduction and specification; these will only arrive after an implementation is made available.

Subscript succeeds the language [Scriptic](http://code.google.com/p/scriptic), which is freely available on Google Code. Subscript applies some lessons learnt from Scriptic usage in 2010:

  * simplified syntax for old functionality
  * simplified semantics for process communication
  * simplified support for 'forcing' parameters
  * removed language features for discrete event simulations
  * allow for plug-in support for discrete event simulations, parallel execution etc
  * syntactic sugar for very concise specifications
  * support for communication over networks and pipes
  * support for more flavours of parallelism and suspension operators
  * unambiguous language definition

The SubScript software is still being developed. There is a partial implementation as a small Scala library, so without the typical syntax but as a Scala DSL; a rudimentary graphical debugger is also available. To experiment with this SubScript implementation, you could use Eclipse with the Scala and Mercurial plugins.

To get a feeling for programming with the SubScript syntax, you could try out the predecessor language Scriptic. An installation guide and student assignments are available at [the Scriptic Wiki](http://code.google.com/p/scriptic/w/list).

I am looking for feedback and support. Feel free to contact me at andre.vandelft at gmail.
Students computer science and software engineering are welcome to do a [project](http://code.google.com/p/subscript/wiki/StudentProjects).

André van Delft