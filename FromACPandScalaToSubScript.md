SubScript extends Scala with scripts, which are inspired on ACP. There are many small and big differences between ACP and SubScript; yet the algebraic theory was a major inspiration for the programming language.

### Lexical differences ###
ACP specifications apply quite some mathematical symbols. For a programming language, it is in principle desirable that all characters are easy accessible on the keyboard. On the other hand it would as well be nice if SubScript would have some mathematical look and feel, using symbols like `δ`, `ε` and `ν` for deadlock, an empty process and a neutral process.
Since these symbols are not easy to type, predefined symbols `(-)`, `(+)` and `(+-)` exist for these processes. The variants with the Greek symbols are defined in a 'Predef' object.

ACP symbols for choice, sequence parallelism are `+`, `;` and  `&`. As a courtesy to the ACP scientists, the dot is available for multiplication too in SubScript. Just like in ACP the sequence operator may be left out, so that there are even three ways to denote sequences.
The operator precedences follow Scala rules, except for the strongly binding  `·`. Only the spacing without operator binds stronger than `·`.

### Syntactic sugar ###
With some syntactic sugar  SubScript programs can become very concise.
For instance, the following sequence of two actions would print lines with "Hello" and "World":
```
  {println("Hello")}; {println("World")}
```
We may leave out the braces and the semicolon. We also may replace the parentheses around the strings by a comma. We may even leave out the "println" method name, in case "println" is declared as the default script on strings. So the following code would do the same:
```
  println("Hello"); println("World")
  println("Hello")  println("World")
  println,"Hello"   println,"World"
  "Hello" "World"
```

Having two ways of expressing sequences (apart from the mathematical multiplication dot `·`), also allows for smaller code, with less parentheses. We use the fact that the semicolon binds weakly, whereas the "space operator" binds strongly. For instance, a sequence of A's terminated by a B would initially be
```
  (..;A);B
```
but the parentheses are not needed if we leave out the first semicolon:
```
  ..A;B
```

To avoid irritating and error prone repetitions of n-ary operators, a prefix notation is allowed. So the following specifications are equivalent:
```
  unaryOperator = "!" + "-" + "~" + "*" + "**"

  unaryOperator =+ "!"  "-"  "~"  "*"  "**"
```
By the way, this latter line is part of the SubScript syntax definition, that is written in SubScript itself. At two places in that definition, there is a choice between sequences. It is possible to have the alternatives on separate lines, while each line denotes a sequence, as in:
```
  simpleTerm    =;+ 
                  simpleValueLedTerm
                  codeFragment 
                  throwTerm
                  whileTerm
                  forTerm
                  specialTerm 
                  "(" scriptExpression ")"
                  arrow . actualParameters
```

### Native language interoperability ###

SubScript scripts are class members, next to variables and methods. From regular Scala code, one may call a script, provided that it has no formal output parameter. The script will return a boolean result, indicating success or failure. Failure occurs when in ACP terms the script ends essentially as the deadlock process δ.
Scala program execution normally starts at a function main(args: Array[String](String.md))
Instead such a function one may provide a script with the same parameter.

Scripts may contain operands that in turn contain Scala code:

  * code fragments that are enclosed in brace pairs such as `{ … }`. There are several flavours. One kind of code fragments execute in a separate thead. Other kinds act as event handlers.
  * method calls; these appear just like they would in Scala
  * script calls: the actual parameters are Scala expressions
  * constructs if, while, for and matches contain Scala expressions
  * directives of the form `@code: term`. These execute some code when an expression becomes active

[![](http://subscript.googlecode.com/files/lennon_this_is_not_here_cover_page.jpg)](http://www.google.com/search?q=%22this+is+not+here%22&um=1&ie=UTF-8&hl=nl&tbm=isch&source=og&sa=N&tab=wi&biw=969&bih=573)

Scala code in scripts may refer to a special value named `here`, which is much like `this`, the current object (of course, `this` is not `here`). `here` refers to the current operand. This operand has various useful features such as a state, which are accessible through the `here` value.
In case of a directive `@code: term`, there is often a need to refer to the expression on which the directive works. That is done using the special value named `there`.

`here` is also defined as an implicit value (for directives, `there` is implicit, instead).

### n-ary operators ###
To describe the n-ary operators, and others, various more or less vague phrases like "activation", "success", "failure", "suspend", "resume", "process", "parent", and "call hierarchy" are used. These are defined further on. For the time being, trust an intuitive meaning.

#### Arity ####
The ACP operators for sequence, choice and parallelism are in principle binary, but as they are associative, the operators may also be considered to be n-ary. In SubScript they are n-ary by definition, since they are not associative any more. This non-associativity is caused by the fact that operands can turn the n-ary operators into iterations. For instance, consider the following three specifications:
  * `(..A)B`  - a sequence of one or more A's, and finally B
  * `..(A B)` - a sequence of one or more sequences of A and B
  * `.. A B`  - idem

#### Parallelism ####
The ACP parallelism operator, `‖`, is a simple kind of "and-parallelism": it may succeed when each of its operands may succeed. Other forms of parallelism would occasionaly be useful as well, such as simple or-parallelism. SubScript supports both flavours, with symbols `&` and `|`. As with boolean expressions, SubScript has also stronger versions:
  * `&&` - "strong and": the whole ends as deaclock (δ) as soon as one operand does so
  * `||` - "strong or": the whole ends successfully as soon as one operand does so
To make it logically more complete, there is also:
  * `==` - "equal parallelism": this succeeds like `&` when each operand succeeds, but also when each operand has ended as deaclock.

Another parallel operator is
  * `==>` - like `&`, with some extra support for sending data over a pipe. The construct is well comparable to a Unix pipe
  * `<==` - like `==>`, but with the opposite direction
  * `<==>` - also like `&`, now creating a network with by default, pipe connections between each combination of the operands. A more restricted topology may be imposed
  * double arrows as in `<<==`denote that the communication may skip operators in that direction, as long as these are connected using double arrows

All parallel operators have left-merge variations, formed by appending a colon to the symbol, as in `&:`.
A left-merge parallel operator only activates an operand after an atomic action has started in the previous operand, or when the previous operand behaved neutrally.

#### Neutral element ####
Each of the introduced n-ary operators has a neutral element, which is either `δ` or `ε`. For instance,
```
  x+δ = δ+x = x
  x;ε = ε;x = x
```
We will call the corresponding operators or-like operators and and-like. So
```
  `+` `|` `||` are or-like
  `;` `&` `&&` `==` are and-like
```
There are a few more n-ary operators. Some of these do not have a neutral operand, so they are not clearly and-like or or-like.

A special operand is `ν`, named the neutral process. If this operand belongs to an or-like operator, then it behaves like `δ`. For and-like operators and in "unclear" circumstances, the neutral process behaves like `ε`.

The neutral process is implicit in the definition of if-expression without an else-part, and also for operands such as iterators.

Since working with Greek symbols is at times problematic, Subscript defines `(-)`, `(+)` and `(+-)`for the deadlock, empty process and neutral process. The Greek symbols are defined as scripts in the subscript.Predef object (which is much like scala.Predef)

#### Interrupt, Disrupt and Suspend/Resume operators ####
ACP has extensions with operators for disruption and interruption; ACP calls these "modal transfer" operators. Subscript has similar operators:

  * `/`  - disrupt: `x/y` means that `x` happens, possibly disrupted by `y`
  * `%/` - interrupt: `x%/y` means that `x` happens, with 1 interruption by `y`

The interrupt definition is different from the one in ACP; there the interruption is mandatory. The ACP interrupt operator is optional; it cannot model forced interruption. In Subscript the interruption is mandatory; to make it effectively optional, write `x%/(y+ε)`.

The interrupt operator symbol has two characters, one of which is `%`. It belongs to a family of suspend/resume operators, each starting a `%`character in its symbol. Two of these don't have a neutral element:

|Operator|ν|Description|
|:-------|:-|:----------|
|`x%&y`|ε|`x` and `y` in any order. As soon as x starts, `y` is suspended, and vice versa. As soon as x is (or may be) ready, `y` is resumed, and vice versa. Similar, but not equal, to `xy+yx`|
|`x%;y`|- |first x and then `y`, with `x` and y both optional, but when `x` does not happen, `y` must happen. Similar, but not equal, to `x(y+ε)+y`|
|`x%%y`|ε|`x` and `y` in any order and both optional, but at least one of the two must happen. Similar, but not equal, to `x(y+ε)+y(x+ε)`|
|`x%/y`|ε|`x` interrupted by `y`; `x` happens, but `y` as well; `x` is suspended as soon as `y` starts to happen. When `y` is (or may be) ready, `x` is (or may be) resumed|
|`x%/%/y`|- |`x` sequentially interrupted zero or more times by `y`|


#### Deadlock continuation operator ####
In normal sequences of the form `x;y`, `y` may start when `x` succeeds. A dual kind of sequence is `x!;y`. This means: `y` may start when `x` ends in deadlock, as `δ`. This operator is or-like. An equivalence holds: `!x;!y = !(x!;y)`

#### Left-merge operators ####
For each parallel operators there is a left-merge version, with a symbol equal to the original symbol with `·` appended:
```
  &·  &&·  |·  ||·  ==·  ==>·  <==>·  <==·
```
The right-hand side only becomes active when an action at the left hand side occurs for the first time.
The high dot postfix expresses that there is also a sequentiallity involved. Moreover, the following expressions are in effect quite similar:
```
  x &· y
  x &.& y
```
The difference is that in the first case, both `x` and `y` need to happen, whereas in the second case `y` is even optional.

### unary operators ###
There are some unary operators on expressons:
  * `!x` - negation; ends in deadlock when `x` ends successfully, and vice verca.
  * `-x` - strong negation; ends in deadlock when `x` ends successfully, and vice verca. `-x` also succeeds when an action in `x` happens without `x` succeeding
  * `~x` - action tracing; succeeds when an action in `x` happens
  * `*x` - process spawning. After activation, `x` executes in parallel with its parent process `p`, as if `p` had become `p&x`. This parent process is by default the highest level script that had been called from the base language.
  * `**x` - marking of a "parent process". A spawned process starts to run in parallel to its nearest by parent process as seen in the call hierarchy

### If-else, Matches and the ternary operator ###

Just like Scala, SubScript offers if-else and matches constructs; the difference being that operands such as the then part and else part, are script expressions rather than pieces of regular Scala code.
`if (b) x` is shorthand for `if (b) x else ν`
Similarly, the neutral process `ν` is also the implicit default option when the matches alternatives do not include an explicit default case.

SubScript has also support for a ternary operator.
> `x? y: z` does x; when it has success, y may start happening. In case x ends in deadlock, z starts.
> `x? y: δ` behaves much like `x;y`. A difference is that such a sequence cannot become an iteration.
> `x? ε: y` behaves much like `x!;y`. Again it cannot become an iteration.
> `x? y` is shorthand for `x? y: δ`. It is a good way to express a precondition or a postcondition

### Iterators and related operands ###

There are 6 operand for supporting iterations and similar stuff:

  * `while` - denotes a loop and an conditional mandatory break point
  * `for` - a for-comprehension, similar to `while`
  * `...` - denotes a loop; no break point, at least not here
  * `..` - denotes a loop, and at the same time an optional break point here
  * `.` - an optional break point here
  * `break` - a mandatory break point here

Note that these iterations are operands; they often belong to a sequential operator, but the iterations may as well be alternative or parallel.
An n-ary operator becomes an iteration when an activated operand is one of the iterator operands (`while`,  `for`, `...` and `..`), or when such an operand is activated through a unary operand, a script call, an if expression or etc.
An n-ary operator that has become an iterator, acts much like as if its specification text is repeated an infinite number of times.

### Local Variables and Constants ###

Local variables and constants are written down as in Scala using the keywords `var` and `val`. There is a restriction: they should be a direct operand of an n-ary operator such as `;` and `&`. They can be used only in subsequent operands.

#### Private Local Variables ####
Sometimes different operands of an n-ary operator need their own copy of a variable. Then a private declaration would be useful. For instance consider these two script expressons:
```
  var i=here.pass; while(i<10) & println(i)
  var i=here.pass; while(i<10) & private i println(i)
```
here.pass yields the pass count of the iteration: at the first loop passage, it is 0; thereafter 1, 2, etc.
The first one would print 10 lines with "10", whereas the second one would print "0" up to "9".

#### Looping Local Variables and Constants ####
A local variable or constant may be initialized using a "looping" expression. E.g.,
```
  val i=0...(i+1)
```
This turns the declaration into an iterator. The value `i` becomes 0 during the first iteration pass; in subsequent passes it becomes the value of the previous pas. For these passes the reference to `i` in the initialization expression refers to the value of `i` in the previous pass.
The following fragment has two iterators; it will print the numbers 0 to 9:
```
  val i=0...(i+1) while(i<10) print,i
```

### Code fragments ###

The atomic actions of ACP have their SubScript counterparts in code fragments. These are operands with pieces of Scala code, enclosed in braces. However, it is often more accurate to say that the start and end of the code fragment execution are like ACP atomic actions. Code fragments may even behave like `ε` or `δ`.

Applications may assign "executors" to manipulate the way code fragments are executed. Examples of such executors would be:
  * simulation engine
  * a scheduler for parallel hardware
  * a probabilistic engine doing Monte Carlo execution

Symbols next to the braces denote different flavours of code fragments:

  * `{ code }` - plain code fragment. Normally, no other actions take place between the start and end of this fragment. However, a simulation engine may attribute a positive duration to this fragment, so that other actions may come in between. The same happens when an executor defers the code asynchronously to the GUI thread.
  * `{* code *}` - threaded code fragment. This code fragment normally runs in its own thread
  * `{? code ?}` - unsure code fragment. When executed, the code fragment may not reflect a happening atomic action, but instead `ε` or `δ`. It may even get state `undetermined`, meaning that it remains elegible for another execution
  * `{! code !}` - immediate code fragment. Executed immediately upon activation. Normally gets the ACP meaning of `ε`, but that may be overridden by the code to become `δ` or `ν`.
  * `{. code .}` - event handling code fragment; meant to be executed by an installed event handler, e.g. for handling keyboard or mouse input. Normally it becomes an atomic action shortly after the code execution, but the code may set it to behave like `δ` or  `undetermined`
  * `{... code ...}` - looping event handling code fragment. The code may also trigger an optional break from the loop or a mandatory break, as by `here.optionalBreak` and `here.break`

### Method calls ###

Calls to a Scala methods may appear in scripts. They are shorthand for code fragments with the same calls. The following three phrases are equivalent:
```
   print("Hello")
   print, "Hello"
  {print("Hello")}
```
In the forms without braces there is no option to capture a return value from the call.

### Script calls ###

Script calls as operands may look like method calls, but they have extra support for output parameters. Output parameters are neither present in ACP refinements, nor in Scala methods.

Refinements in ACP may have value parameters. This leads to specifications with Sigma symbols, standing for parameterized addition.
For instance, suppose a number i between 0 and 1000 is read from a channel, depicted by `r(i)`; then some action `a(i)` is performed. In ACP this would typically be written down like

> ra = <sub>i=0</sub>Σ<sup>1000</sup> r(i)a(i)

Programmers would be much more familiar with a solution that would not require a sigma. SubScript therefore offers output parameters. For instance, a script definition could start with
```
  r(i: Int?) = ....
```
The question mark suffix denotes that parameter `i` is an output parameter. Then the following calls would be allowed:
```
  var i: Int r(i?) a(i)
  var i: Int r,i?  a(i)
         r(i:Int?) a(i)
         r,i:Int?  a(i)
```
The first 2 calls to `r` have a normal output parameter. Note that the actual parameter `i` had been predeclared as a local variable, and then it needed to be initialized, like it is needed in Scala.
Then two calls show how the local variable is declared inside the script call. No initialization applies here.

#### Constrained Parameters ####

Given
```
  r(i: Int?) = ....
```
the call to r may yield any number of type Integer. We may want to restrict the received values to the range 0..1000, as in the ACP example. This would be possible if the parameter i in the script definition gets another question mark suffix:
```
  r(i: Int??) = ....
```
This makes the parameter i a _constrained_ output parameter. The caller of such a script may specify a normal output parameter, but it may also add some constraints. This is done Scala style using the keyword `if`, as in
```
  var i: Int r(i? if?(i>=0&&i<=1000)) a(i)
  var i: Int r,i? if?(i>=0&&i<=1000)  a(i)
         r(i:Int? if?(i>=0&&i<=1000)) a(i)
         r,i:Int?if?(i>=0&&i<=1000)  a(i)
```
Such a single-parameter constraint condition is evaluated with the formal value of the corresponding parameter of the called script.
Only when such a script call succeeds (whatever that may mean will be defined later), the parameter values are copied onto the actual output parameters.

A single parameter constraint only works on a single parameter. It may be needed to test the values of multiple parameters. That is possible when the called script had been defined with two question marks after the header. A call with such a constraint should have the parameters enclosed between parentheses:
```
  r(i, j: Int??)?? = {?i=1; j=2; matchParameters?}

  var i,j: Int r(i?, j?)if?(i+1==j)
         r(i:Int?,j:Int)if?(i+1==j)
```

A special kind of constraint is calling the script with a value parameter without a question mark suffix. Such a parameter is called a _forcing_ parameter:
```
                 r(1)
                 r,1
```

Normally the definition of script `r` should ensure that its atomic actions may only happen if the constraints evaluate to true. This is possible for instance as:
```
  r(i: Integer??) = {? i=computed; if (!(_i.matches) fail ?}
```
So a parameter `i` may be referred to by `_i`; this returns an object that has a method named `matches`. Other available features are `value` and `originalValue`.

A convenience method doing the same check for all parameters is
```
  r(i: Int??) = {? i=computed; matchParameters ?}
```

Possibly constrained parameters may be transparantly passed through script calls, by having the parameter list enclosed in parentheses:
```
  rr(i: Int??) = r(i??)
```
Optionally a postfix test may be added:
```
  rr(i: Int??) = r(i?? if?(i%2==0))
```

#### Overview of formal and actual parameter use ####

| Formal declaration   |        Formal type              |  Actual call          | Value of _p_|
|:---------------------|:--------------------------------|:----------------------|:---------|
|` p: P               `|`       FormalInputParameter[P] `|` expr                `|`  ActualValueParameter     (   expr) `|
|` p: P?              `|`      FormalOutputParameter[P] `|` varExpr?            `|` ActualOutputParameter     (varExpr, {=>varExpr=_) `|
|` p: P??             `|` FormalConstrainedParameter[P] `|` expr                `|`  ActualValueParameter     (   expr, {=>   expr=_) `|
|`                    `|`                               `|` varExpr?            `|` ActualOutputParameter     (varExpr, {=>varExpr=_) `|
|`                    `|`                               `|` varExpr if(c)?      `|` ActualConstrainedParameter(   expr, {=>   expr=_}, {_=>c}) `|
|`                    `|`                               `|` formalParam??       `|`    ActualAdaptingParameter(_formalParam) `|
|`                    `|`                               `|` formalParam if(c)?? `|`    ActualAdaptingParameter(_formalParam, {=>c}) `|

#### !! To be updated from source code !! ####

Suppose a script `key(c:Char??)` reads a character from the keyboard.
How to use this script in `numKey(i: Int??)`, that reads a digit key?
```
  numKey(i: Int??) =
    _i match (
      case ActualValueParameter(value) => if (i>=0 && i<=9) key('0'+i) else (-)
      case ActualOutputParameter(_)     => key,c: Char if(c>='0' && c<='9')? {!i=c-'0'!}
      case ActualConstrainedParameter(_,constraint) => key,c: Char if(c>='0' && c<='9' && constraint(c-'0'))? {!i=c-'0'!}
      case ActualAdaptingParameter(_,formalParameter,constraint) => TBD
```

#### Implicit Scripts ####

Scripts with parameters that are named with the keyword `implicit` are called implicit scripts. In their calls, the name may be omitted, so that only the parameters remain. E.g., given
```
  implicit(s: String) = ....
  implicit(i: Int?) = ....
  implicit(i: Int?, s: String) = ....
```
these script may be called as
```
  "Hello" // normal parameter
  1  // forcing parameter
  1,"Hello" // forcing parameter and normal parameter
  var n: Int n?  // output parameter
  var n: Int n?,"Hello"  // output parameter and normal parameter
```
For the latter two forms shorthand notations apply:
```
  n:Int?  
  n:Int?,"Hello"
```
In case the shorthand notation with 1 output parameter was a direct operand of a sequential operator, then that operand is replaced by two operands of that same sequential operator: the first one declares the local variable, and the next one does the script call. Likewise for multiple output parameters in shorthand notation.

### Directives ###

Directives such as `@code: term`. The directive code executes when its operand is about to become activated. It is possible to put a piece of code in a handler so that it will be called on another occasion, e.g.:

  * onActivateOrResume - executed when the operand is activated or resumed
  * onDeactivate - executed when the operand is deactivated
  * onDeactivateOrSuspend - executed when the operand is deactivated or suspended

```
  @code1
   there.onActivateOrResume  (_ => code2)
   there.onDeactivate         (_ => code3)
   there.onDeactivateOrSuspend (_ => code4)
```
In fact, `code1` is executed as if it was `there.onActivate`: the `there` operand has already been created when the directive executes.

## Execution Manipulation using directives ##

Using directives of the form `@code:`, the execution of parts of a SubScript program may be manipulated. Specific objects with names such as `sim`, `gui`, `processor` may be defined in a way that could give for instance the following meaning when they are used in directives:

| **Directive** | **Possible meaning** |
|:--------------|:---------------------|
| gui       | The Scala code `there` must be executed in the GUI thread |
| dbWriteThread | The Scala code `there` must be executed in the given database write thread |
| threadPool | The Scala code `there` must be executed in a thread in the given thread pool |
| processor | All Scala code `there` and below must be executed at the given processor |
| lowPriority | The threaded Scala code `there` should run at a low priority |
| lowActionPriority | The atomic actions `there` and below have a low priority |
| key.typed | The event handling code `there` is be executed in response to key typed events |
| topology | The topology for the network `there` |
| parentNetwork | The send or receive call `there` is directed to the network one level up |
| parentPipe | The send or receive call `there` is directed to the pipe one level up |
| disambiguate | Operators `there` and below are disambiguated |
| markov| The program part `there` and below is managed by a specific Markov system |
| markov chance = .5 | The atomic action `there` has a relative chance to succeed in the given Markov system |
| realtimer | The program part `there` and below is managed by a specific realtime engine |
| realtimer startTime = 1 pm | The atomic action `there` starts at 1 PM real time |
| realtimer duration = 2 seconds | The atomic action `there` succeeds after 2 real time seconds from its start |
| sim: | The program part `there` and below is managed by a specific timed simulation engine |
| sim startTime = 1 pm | The atomic action `there` starts at 1 PM simulation time |
| sim duration = 2 seconds | The atomic action `there` succeeds after 2 simulation time seconds from its start |

### Communication ###
In ACP atoms a, b, c denote normally atomic actions, but they may alternatively be partners of pairs of communicating actions. For instance, it may be defined that atoms a, b and c communicate in the possible pairs (a,b) and (a,c), yielding some atomic actions d and e.
At the top level of an ACP program, single occurrences of a, b and c are hidden so that these can not be mistaken as atomic actions.

In SubScript, there are special kinds of communicating scripts. For instance,
```
  a,b = "hello"
```
When `a` and `b` are active in parallel to one another, their shared action that probably prints "hello" may happen. In case only `a` is active, no action would follow; the active `a` would just have to wait for a partner `b`; maybe it will be deactivated before that would happen. At least, there is no hiding needed anyway.

In case `a` may also communicate with `c`, SubScript prescribes that these alternatives are marked, by writing `+=` instead of `= ` in the definition.
```
  a,b += "hello"
  a,c += "world"
```
This is a bit similar to marking overridden methods in Scala with the keyword "override".

Communication may involve more than 2 partners:
```
  a,b,c = "hello"
```
A normal script may in some sense be viewed as an efficient kind of communication. It is possible to express that `a` can act on its own, but also as a partner in a communication:
```
  a   += "hello"
  a,b += "world"
```
Even any number of partners with a given name and signature may be allowed to communicate:
```
  a..   = "hello"
  b,c.. = "world"
```
So 1 or more calls to `a` could together do "hello", and 1 call to `b` and 1 or more calls to `c` could together do "world". The SubScript execution must bind a maximum number of partners. That is, a set of partners is allowed if it cannot be extended any more with another active call.

The body of a communication in ACP may be a normal atomic action, but also an atom that wants to communicate in turn. In SubScript, any kind of script expression is allowed as the body of the communication.
```
  a,b = "hello" "world"
```
Normally a communication body should be built up from atomic actions. Syntacticly it is possible to abuse the freedom, such as in:
```
  a,b = .
  c,d = ...
  e,f = ν
```
Such definitions are not recommended, but their behaviour is well defined. For instance, `ν` behaves like `δ` if each of the communication partners belongs to an or-like operator (loosly said); else the neutral process behaves like `ε`.

Communicating scripts may have parameters. For instance, to specify a send action and a receive action for a number:
```
  s(i:Int),r(j:Int??) = {j=i; matchParameters}

  test1 = s,1 & receive,j:Int? print,j
  test2 = s,1 & receive,1      print,1
  test3 = s,1 & receive,2      print,2
```
`test1` and `test2` would simply print "1".
`test3` would activate the code fragment; when the code inside is executed, the formal parameter `j` gets the value of `i`. Then `matchParameters` makes the code fragment fail, and the communication fails.

It is often better to have the parameter transfer and matching done at an earlier stage, at the time it is decided whether the `s` and the `r` may communicate. This would be possible by letting the communicating partners share the parameters:
```
  s(i:Int),r(i??) = {}
```

### Communication over channels ###

There is a more convenient notation for common send and receive pairs.

Script names may end in arrow symbols: `<-  -> `. When a communication is defined for a left arrow script and a right arrow script, some shorthand notations exist:

For
```
  a<-(i:Int),b->(i??) = {}
```
we write the shorthand
```
  a<-b->(i:Int) = {}
```
In case the `a` and `b` are the same, we have a channel:
```
  c<-->(i:Int) = {}
```
The send and receive actions would then be written as
```
  test1 = c<-1 & c->j:Int? print,j
  test2 = c<-1 & c->1      print,1
  test3 = c<-1 & c->2      print,2
```
The names before the arrows may be empty:
```
  <-->(i:Int) = {}

  test1 = <-1 & ->j:Int? print,j
  test2 = <-1 & ->1      print,1
  test3 = <-1 & ->2      print,2
```
In exceptional cases you may want to skip the parameter matching at the time it is decided whether 2 partners may communicate. This could be used to simulate a communication error. To specify that the parameter matching should be skipped, append a question mark to the formal parameter in the channel header:
```
  channel<-->(i:Int?) = {}
```
To do an asynchronous send over a channel, just launch it as a process using the unary prefix operator `*`:
```
  *channel<-i
```
An equivalent notation is:
```
  channel<-*i
```
The next two variations for receiving over channels had been inspired by the Linda model for tuple spaces.
Sometimes it may be useful to do a non-blocking receive:
```
  channel->?2
  ->i:Int?
```
Such phrases would behave like `δ` in case no applicable send partner is available.

Also it may be at times be useful to do a non-consuming receive:
```
  channel->*2
  ->*i:Int?
```
Such receive actions would leave the corresponding send action available for yet another communication.

### Communication over networks ###

A special kind of communication-with-arrows is in the context of a network operator. The arrows now have double horizonal lines, made up using `=` characters, instead of the dashes, as in
```
c<=1
c=>i:Int?
<=2
=>j:Int?
```

(Note: the following text is still under construction).

First there is an n-ary operator `<<==>>`. This is much like "normal" parallelism (using `&`). However, send actions and receive actions that belong to operands have now restricted matching opportunities. ("Belong" here means "belong" in a proper sense, TBD).

The n-ary operator `<<==>>` defines a topology that interconnects every subset of operands (not just every pair, since communication is n=-ary in general).

In many cases a restricted topology such as a pipe will do, like the one in Unix command shell. For these the following operator symbols apply:
```
  <==      <<==
    ==>     ==>>
 <==>  <<==>>
 <<==>  <==>>
```
A single arrow towards an operand means that communication to that operand is allowed; the operand may be hopped over if it does not activate an action, as in `.. ==> sieve`.

A double arrow towards an operand means that communication to that operand is allowed, and operands may be hopped over, until an operand is reached from which no such arrow in that direction goes out.

E.g., in `p1<==p2<==>>p3<==>p4==>p5`, process `p2` may send to `p1` and `p3` and `p4`, etc.

Inside the pipe arrows, special directives may be placed between brackets, as in `==[directive]==>`. These further control the topology. E.g., the code from the Sieve of Eratosthenes example had:
```
   generator(2, 1000000) ==> (..==>sieve) ==[toPrint]==> printer
```
and a send action `@toPrint:<=p`.
The same directive `toPrint` is executed; when creating the pipe, and when activating the send action. By some internal magic, this code then ensures that only this `<=p` send action can reach the printer.

The pipe arrows may also be marked with value tuples, as in `==(i,j)==>`. This can be picked up by a topology controller that is specified using a directive. E.g.,
```
  @myTopology: (
    for (i<-0 to m; j<-0 to n) <<=(i,j)=>> p(i,j)
  )
```
`myTopology` could for instance impose a torus topology by allowing only connections between "adjacent" i,j pairs.

Send actions and receive actions are normally bound to the nearest by networking operator (seen upwards). However, some of such actions may "fall" through, upwards.

For instance:
```
  <=1 ==> (=>i:Int? <=i  ==> =>j:Int?)
```
The receive action `=>i:Int?` cannot possibly communicate in the LHS of the inner pipe operator `<==>`; therefore it falls through towards the outer pipe operator, where it may communicate with `<=1`.

The subsequent send action `<=i` may communicate over the inner pipe, so it will likely communicate with `=>j:Int?`.

### Asynchronous send and receive over networks ###
Asynchronous sends over networks should usually not be done using the unary prefix operator `*` for launching. For instance:
```
  *<=1 <==> =>1
```
The reason is that `*<=i` spawns the process `<=i` at a too high level, not subordinate to the network operator `<==>`. Asynchronous sends over network channels should therefore be done using the `<=*` symbol, as in
```
  <=*1 <==> =>1
```
This places the spawned send process as a subordinate to the network operator `<==>`.

### Exception handling ###

A try-catch-finally construct is available, much like the one in Scala. The main differences are that the try and catch parts contain script expressions, rather than Scala code. Also, the catch handlers normally disrupt the try part. This is important, since a thrown exception does not automatically kill the try part, as it would in Scala code.

Suppose an exception would be thrown somewhere inside the script `a` in
```
  try ( a & b ) catch (e: Exception => println e)
```
Then `b` would be disrupted, as well as a possibly still active part of `a`.
In case catch handler should not disrupt the try part, specify using `*=>` that it will act as if it launches a process:
```
  try ( a & b ) catch (e: Exception *=> println e)
```

"throw anException" may also be used as a special operand, just as in Scala code.


