# Introduction #

Subscript is a new event-driven and parallel extension to the Scala programming language. It lets you specify many kinds of tasks very concisely and clearly:

  * Controllers for graphical user interfaces
  * Event handling
  * Event simulations
  * Parallelism: background processing and threading
  * Language processing: both parsing and producing texts

Subscript basically adds to Scala constructs from a mathematical theory named Algebra of Communicating Processes (ACP). Some spoons with syntactic sugar then make it very powerful. The resulting language constructs remind of features offered in several programming languages and techniques, such as:

  * Backus Naur Form, YACC and Java\_CC
  * CSP and OCCAM
  * Linda

But SubScript also supports some real new programming idioms. This paper shows some examples.

# Examples #

## Hello World ##
Most new programming languages are these days introduced by showing how to show the text “Hello World” on the console. In SubScript, you could fall back to the solution in the base language, Scala:
```
object HelloWorld {
  def main(args: Array[String]) {println("Hello, world!")}
}
```
This is exactly the standard HelloWorld program in Scala. It is a valid SubScript program as well, since SubScript is a quite clean extension to Scala. But this shows nothing specific about SubScript. A more telling alternative would be:
```
object HelloWorld {
 public script
  main(args: Array[String]) = {println(“Hello World”)}
}
```
The second line mentions so called "scripts". These are refinement constructs that Subscript adds as class members, next to the variables and methods that Scala offers. Typically, a script specifies dynamic behavior, but they may also be useful to manipulate data. A script definition mentions the name followed by an equals sign, and then a script body. In this case, HelloWorld has only one script, which acts in this case much like the Scala main method. Its script body contains a fragment of Scala code that calls println; curly braces enclose the code fragment. Such a code fragment is called an "action".

To define multiple scripts, it is not necessary to repeat the keyword "script". You may create an "iteration" of script definitions by appending two dots "..":
```
object HelloWorld {
 public script..
  main(args: Array[String]) = {println(“Hello World”)}
  test          =  {println(“test”)}
}
```

The pair of dots may similarly create other iterations of definitions: for "val", "var", "def", "type" and "class".
Such an iteration expects a new definition based on layout: a new definition starts on lines with the same horizontal position of the first non-white character as the for the first definition.

### Hello World Variations ###
To show how sequences are specified in SubScript, we could split the printing of "Hello World" in two actions, so that they result in two output lines:
```
main(args: Array[String]) = {println(“Hello”)}; {println(“World”)}
```
The semicolon denotes sequence, just like in Java and Scala. There are quite some more similar operators, to denote choice, parallelism, interuption and other concepts. But as sequences are omnipresent, the semicolon is treated specially. In many cases it may be left out:
```
main(args: Array[String]) = {println(“Hello”)} {println(“World”)}
```
In Scala, a semicolon can be left out at the end of the line. SubScript was inspired by this, and goes beyond it by making all semicolons optional. Moreover, there is a close correspondence between sequences of processes, and multiplication in algebra. The algebraic symbol for multiplication is also usually omitted. However, a semicolon binds very weakly (as in Scala) whereas the "empty symbol" binds very strongly. Later we will see that this distinction allows to prevent lots of clumsy parentheses.
Back to "Hello World". As another portion of syntactic sugar, the action braces around the call to println may be dropped:
```
main(args: Array[String]) = println(“Hello World”)
```
Yet another piece of syntactic sugar allows the parameter to follow the call after a comma:
```
main(args: Array[String]) = println, “Hello World”
```
It is possible to state that the "empty symbol" should be the comma rather than the semicolon. Just specify the comma after the equals sign:
```
main(args: Array[String]) =, println “Hello World”
```
When printing "Hello" and "World" again on two separate lines we could use the semicolon again:
```
main(args: Array[String]) =, println “Hello”; println “World”
```
And we can get rid of the semicolon there by appending it behind the comma.
```
main(args: Array[String]) =,; 
                              println “Hello”
                              println “World”
```
The comma and the semicolon behind the equals sign denote that an "empty symbol" is a parameter separator in case it is not at the end of a line, or a sequence operator in case it is at the end of a line. For small script bodies it would not be very useful to get rid of operators this way, but the longer the script bodies, the more convenient it may be.
Now we see twice the word println. It could have been there even more times, in case the program would do more output. So the word println would be a good candidate for further simplification. Let's make an _implicit_ script `println` that calls the `println` method. Since the script is implicit, it does not need to be named explicitly in calls:
```
implicit println(String s) = {println(s)}
main(args: Array[String]) = “Hello” "World”
```
The nice thing that starts popping up here is that using some syntactic sugar, some scripts can specify data structures, and other scripts in another may determine what default operations would apply to simple data items. Then these scripts may be combined...