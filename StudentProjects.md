# Student Projects #

## Introduction ##

Students computer science or software engineering can take up projects and participate in the SubScript project. This is a great opportunity to learn Scala and also programming with Process Algebra.

Below three student project descriptions. Each may would be good for 150 up to 750 working hours, whatever is desired.

## Project 1: Enhance current implementation of SubScript-Scala ##

There is still plenty to do on the current implementation. You may work on:

  * unit tests
  * bug fixing
  * fill gaps in feature implementations
  * provide full grammar support
  * improve debugger

## Project 2: Investigate DSL on top of JavaScript ##

SubScript might as well extend other languages next to Scala.
An interesting starter would be JavaScript.

  * develop use cases to evaluate the desirability
  * create a translator for SubScript into JavaScript
  * define a strategy to send over SubScript and have it translated
  * provide a translator for Scala VM source code into JavaScript
  * JavaScript does not support explicit multithreading; develop an alternative for use by {**...**}

## Project 3: Mimic Rascal ##

[Rascal language](http://www.rascal-mpl.org/)  is a domain specific language for source code analysis and manipulation a.k.a. meta-programming. It has high level support for creating parsers that extract information from input texts, e.g. specification of keywords, lexical syntax, concrete syntax, and abstract syntax.

SubScript-Scala may be powerful enough to do very similar things, using an internal DSL. The result would then be quite powerful; the support for concrete syntax and abstract syntax could be handy for serialization and deserialization of application data.

  * design a DSL on SubScript-Scala that would supports typical Rascal constructs
  * implement the DSL