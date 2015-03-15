## Background ##
SubScript has been inspired by an theory called the Algebra of Communicating Processes.

The next overview of that theory has partly  been [copied from Wikipedia](http://en.wikipedia.org/wiki/Algebra_of_Communicating_Processes) - no need to reinvent the wheel. Another sources was Process Algebra with Explicit Termination by J.C.M. Baeten. http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.34.9980&rep=rep1&type=pdf

However, there are numerous differences between the theory and the programming language. These are all presented with their motivation.

### Algebra of Communicating Processes ###
The Algebra of Communicating Processes (ACP) is an algebraic approach to reasoning about concurrent systems. It is a member of the family of mathematical theories of concurrency known as process algebras or process calculi. More so than the other seminal process calculi (CCS and CSP), the development of ACP focused on the algebra of processes, and sought to create an abstract, generalized axiomatic system for processes and in fact the term process algebra was coined during the research that led to ACP.

ACP uses instantaneous, atomic actions `a,b,c,...` as its main primitives. Two special primitives are the deadlock process d and the empty process e.The primitives may be combined to form processes using a variety of operators. These operators can be roughly categorized as providing a basic process algebra, concurrency, and communication:

  * Choice and sequencing — the most fundamental of algebraic operators are the alternative operator `+`, which provides a choice between actions, and the sequencing operator `·`, which specifies an ordering on actions. So, for example, the process `(a+b)·c` first chooses to perform either `a` or `b`, and then performs action `c`. How the choice between `a` and `b` is made does not matter and is left unspecified. Note that alternative composition is commutative but sequential composition is not (because time flows forward).

  * Concurrency — to allow the description of concurrency, ACP provides a merge operator, ║. This represents the parallel composition of two processes, the individual actions of which are interleaved. As an example, the process `(a·b)║(c·d)` may perform the actions `a,b,c,d` in any of the sequences abcd,acbd,acdb,cabd,cadb,cdab.

  * Communication — pairs of atomic actions may be defined as communicating actions; they can then not be performed on their own, but only together, when active in two parallel processes. This way, the two processes synchronize, and they may exchange data.

ACP fundamentally adopts an axiomatic, algebraic approach to the formal definition of its various operators. Using the alternative and sequential composition operators, ACP defines a basic process algebra which satisfies the following axioms:
```
      x+y    = y+x
  (x+y)+z    = x+(y+z)
      x+x    = x
     (x+y)·z = x·z + y·z
     (x·y)·z = x·(y·z)
```
The special primitives δ and ε behave much like the 0 and 1 that are neutral elements for addition and multiplication in usual algebra:
```
     δ+x   = x
     δ·x   = δ
     ε·x   = x
     x·ε   = x
```
There is no axiom for `x·δ`.
It just means: x and then deadlock.
`x+ε` means: optionally `x`. This is illustrated by rewriting `(x+ε)·y` using the given axioms:
```
(x+ε)·y  =  x·y +ε·y  =  x·y + y
```

To define new operators, we use the fact that all "closed" processes are either essentially of one of the following elementary forms:
```
  x+y
  a·x
  ε
  δ
```
Note that a single atomic action `a` equals `a·ε` so that it is of the form `a·x`.

The merge operator `║` is defined in terms of the alternative and sequential composition operators. This definition requires a auxiliary operator named left-merge, with symbol `╙`. `x╙y` means that `x` starts with an action, and then the rest of `x` is done in parallel with y.

Another operator is the communication-merge, with symbol `|`. `x|y` means that `x` and `y` start with a communication (as a pair of atomic actions), and then the rest of `x` is done in parallel with the rest of `y`.
`a^b` would mean an atomic action implied by the communication of `a` and `b`. If no such communication is defined, `a^b` would equal `δ`

The axioms defining the merge are:
```
      x║y     =  x╙y + y╙x + x|y

  (x+y)╙z     = x╙z + y╙z
    a·x╙y     =  a·(x║y)
      ε╙x     =  δ
      δ╙x     =  δ

      x|y     = y|x
  (x+y)|z     = x|z + y|z
    a·x|b·y   = (a^b)·(x║y)
      ε|a·x   = δ
      ε|ε     = ε
      δ|x     = δ
```

Many extensions to ACP have been developed, e.g. interrupt and disrupt operators, and notions of time and priorities. Like the merge operator, additional operators were always basicly defined in terms of sequence, choice and communication.Since its inception in 1982, ACP has successfully been applied to the specification and verification of among others, communication protocols, traffic systems and manufacturing plants.

### New Ideas for ACP ###

SubScript applies a few new ideas to ACP:

  * different flavours of parallelism
  * mixture of processes and other items in expressions
  * generalized communication

#### Different Flavours of Parallelism ####

Instead of `║` for normal parallelism, SubScript writes `&`. There are also other flavours:
  * `|` weak or parallelism: has success when one operand has success
  * `&&` and parallelism: has success when each operand has success, and ends in deadlock when one operand ends in deadlock
  * `||` or parallelism: has success when one operand has success, and ends when one operand does while having success
The 4 kinds of parallelism are related to their counterpart operators for boolean values. The variants with the double symbols, `&&` and `||`, stop activity when the logic result has been settled.

Let's rephrase the ACP axioms for parallelism using normal ampersands: `&` for the operator, `&_` for left-merge, and `_&_` for communication merge:

```
      x&y     =  x&_y + y&_x + x_&_y

  (x+y)&_z     = x&_z + y&_z
    a·x&_y     =  a·(x&y)
      ε&_x     =  δ
      δ&_x     =  δ

      x_&_y     = y_&_x
  (x+y)_&_z     = x_&_z + y_&_z
    a·x_&_b·y   = (a^b)·(x&y)
      ε_&_a·x   = δ
      ε_&_ε     = ε
      δ_&_x     = δ
```

The axioms for weak or-parallelism are similar, replacing `&` by `|`. The difference is:

```
       ε|_x     =  ε
```

The axioms for and-parallelism and or-parallelism are again similar, replacing `&` by `&&` and {{|}}} by `||`; likewise for the axioms with the underscores. The difference is in the first axiom of the lists:

```
       x&&y     =  !failure(x)  !failure(y) (x&&_y + y&&_x + x_&&_y)

       x||y     =  !success(x)  !success(y) (x||_y + y||_x + x_||_y)
                +   success(x) + success(y)
```

This uses a prefix operator `!` predicates `failure` and `success` as explained below.

##### Predicates for failure and success #####
The definitions for and-parallelism, or-parallelism, failure continuation and negation require predicates that tell whether a process expression reduces to failure or success, or whether it has success. Another auxiliary predicate tells whether a process expression can start with an atom. Normally, predicates yield Boolean values true or false; here they can yield failure and success, since they have a similar effect at the head of process terms. A predicate ! is defined only on the domain of {δ,ε}; it negates these primitives

```
hasSuccess   (x+y) = hasSuccess (x) + hasSuccess (y)
hasSuccess   (ax)  = δ
hasSuccess   (ε)   = ε
hasSuccess   (δ)   = δ

hasStartAtom (x+y) = hasStartAtom (x) + hasStartAtom (y)
hasStartAtom (ax)  = ε
hasStartAtom (ε)   = δ
hasStartAtom (δ)   = δ

!δ = ε
!ε = δ

success (x)   = !hasStartAtom (x)  hasSuccess (x)
failure (x)   = !hasStartAtom (x) !hasSuccess (x)
```

Or equivalently

```
success (x+y) = success(x) success(y)
success (ax)  = δ
success (ε)   = ε
success (δ)   = δ

failure (x+y) = failure(x) failure(y)
failure (ax)  = δ
failure (ε)   = δ
failure (δ)   = ε
```

#### Item Algebra ####
Some previous SubScript examples presented algebraic expressions on items that were not directly processes:
```
  cancelCommand = cancelButton + escapeKey

  main(args: Array[String]) = "Hello" "World"
```
These items were converted into processes using default scripts, which could as a start be abstract. Now what is for instance the addition of a button and a key? It is just a choice. It is not yet decided what the choice is used for. It could be made operational so that it becomes an input description for a GUI program, as done using the default scripts in the earlier example. However, it could also be made operational as an instruction for an operator who is testing that GUI program, listing the possible actions to try.

We may easily modify ACP a little so that it becomes an Item Algebra: next to the action atoms `a,b,c,...` allow for item atoms `i,j,k,...`. Then note that there are no axioms for the merge operators with `i,j,k`. So merges concerning item atoms cannot be rewritten in terms of sequence and choice.

It is possible to turn an Item Algebra specification into a Process Algebra specification: just add definitions for the item atoms (that are then no atoms any more) so that these `i,j,k, ...` are defined in terms of action atoms `a,b,c,...` . This would be an operationalisation of the Item Algebra specification.

In the [DataInitializers](http://code.google.com/p/subscript/wiki/DataInitializers) example script, the phrase `"red" 0xFF0000` would neither denote a sequence or choice, but a tuple: it will act as a pair of parameters for a default script. So we could say that in an Item Algebra specification, tuples of the form `(i,j)` would be allowed. To operationalize, definitions should be added for such tuples in terms of atomic actions.

##### Example #####
You may consider formal syntax definitions of programming languages as a kind of Item Algebra specifications. A particular instance will be presented later: the SubScript syntax that is specified using SubScript itself. A simplified preview is:
```
  scriptDefinition   = scriptHeader "=" scriptExpression
  scriptHeader       = scriptName optionalParameters 
  scriptName         = identifier + "_"
  optionalParameters = . "(" formalParameters ")"
  formalParameters   = .; formalParameter .. ","
  formalParameter    = identifier ":" type . formalOutputMarker
  formalOutputMarker = "?" + "??"
```
Here type and scriptExpression have not been worked out. The period "`.`" is an optional exit; the difference with the ellipsis "`..`" is that it does not denote a loop.

#### Generalized Communication ####

In Subscript, communication does in general not yield just a single atomic action, but a general process instead.
The next part is an attempt to formalize this, unaware of possible existing work on this topic.

The symbols `a, b, ...` are now just atomic actions; they cannot communicate any more with one another. For communication, there are now symbols `c, d, ...`, that are allowed to communicate, yielding processes denoted by `c^d` etc.

This distinction suggests another elementary form for processes: `c·x`. However, it turns out that a more useful elementary form is `c·x╙y`.
We will introduce an axiom `c·x = c·x╙ε `. This may also be used as rewriting rule, so that the elementary form results. However, the rewriting should not be "overdone", as the `c·x` in `c·x╙ε ` could be rewritten again.

Elementary forms are now:
```
  x+y
  a·x
  c·x╙y
  ε
  δ
```

The new axioms defining the merge are:
```
      x║y     =  x╙y + y╙x + x|y

  (x+y)╙z     = x║z + y║z
    a·x╙y     =  a·(x║y)
      ε╙x     =  δ
      δ╙x     =  δ
(c·x╙y)╙z     = c·x╙(y║z)
      c·x     = c·x╙ε

      x|y     = y|x
  (x+y)|z     = x|z + y|z
    a·x|y     = δ
      ε|ε     = ε
      δ|x     = δ

(c·w╙x)|(d·y╙z) = (c^d)·(w║y)╙(x║z)

```

Rules for hiding communication after rewriting:

> `δ`<sub>H</sub>`(x+y)   = δ`<sub>H</sub>`(x) + δ`<sub>H</sub>`(y)`

> `δ`<sub>H</sub>`(a·x)   = a·δ`<sub>H</sub>`(x)`

> `δ`<sub>H</sub>`(c·x╙ε) = δ`

> `δ`<sub>H</sub>`(ε)     = ε`

> `δ`<sub>H</sub>`(δ)     = δ`

##### Associativity #####
The new version of the merge operator should be associative for closed terms:
```
  x║(y║z) = (x║y)║z
```
With induction, this is easily proven in case x has one of the classic elementary forms `x+y, a·x, ε, δ`. For the new elementary form the proof is different.

Say `x = c·v╙w`. Then to be shown:
```
  (c·v╙w)║(y║z) = ((c·v╙w)║y)║z
```
We will write out both the left-hand side and the right-hand side, and show these lead to equivalent terms. We will assume that the following laws hold (though these need also still proof of their own):
```
  (x╙y)╙z = x╙(y║z)
  (x╙y)|z = (x|z)╙y
```
LHS:
```
  (c·v╙w)║(y║z)

= (c·v╙w)╙(y║z) + (y║z)╙(c·v╙w) + (c·v╙w)|(y║z)

= c·v╙(w║(y║z)) + (y╙z+z╙y+y|z)╙(c·v╙w) + (c·v╙w)|(y╙z+z╙y+y|z)

(use associativity by induction step)

= c·v╙(w║y║z) 
+ (y╙z)╙(c·v╙w) + (z╙y)╙(c·v╙w) + (y|z)╙(c·v╙w)
+ (c·v╙w)|(y╙z) + (c·v╙w)|(z╙y)+ (c·v╙w)|(y|z)

= c·v╙(w║y║z)
+  y╙(z║(c·v╙w))+ z╙(y║c·v╙w)  + (y|z)╙(c·v╙w)
+ (c·v|y)╙(w║z) + (c·v|z)╙(w║y)+ (c·v|y|z)╙w

```

RHS:
```
  ((c·v╙w)║y)║z

=   ((c·v╙w)║y)╙z 
+ z╙((c·v╙w)║y) 
+   ((c·v╙w)║y)|z

=   ((c·v╙(w║y) + y╙(c·v╙w) + (c·v╙w)|y)╙z
+ z╙((c·v╙w)║y)
+   ((c·v╙(w║y) + y╙(c·v╙w) + (c·v╙w)|y)|z

=   ((c·v╙(w║y))╙z + (y╙(c·v╙w))╙z + ((c·v╙w)|y)╙z
+ z╙((c·v╙w)║y)
+   ((c·v╙(w║y))|z + (y╙(c·v╙w))|z + ((c·v╙w)|y)|z

=    (c·v╙((w║y)║z) + y╙((c·v╙w)║z) + ((c·v|y)╙(w║z)
+ z╙((c·v╙w)║y)
+    (c·v|z)╙(w║y) + (y|z)╙(c·v╙w) +  (c·v|y|z)╙w
```
The 7 terms of the LHS have their counterparts at the RHS, in the order:
1-2-4-6-3-5-7
QED

##### Multi Party Communication #####

The communication `c^d` stands for the definition of any process, even of the form of another communicating symbol, say `f`, for which a communication `f^e` would be defined. In such a case we could write as well a three way communication `c^d^e`.

The following would then hold:

```
   (c·u╙v)|(d·w╙x)|(e·y╙z) = (c^d^e)·(u║w║y)╙(v║x║z)
```
In general, a formula for n-ary communication would hold, like

> `|`<sub>i</sub>`(c`<sub>i</sub>`·x`<sub>i</sub>`╙y`<sub>i</sub>`) = (^`<sub>i</sub>`c`<sub>i</sub>`)·(║`<sub>i</sub>`x`<sub>i</sub>`)╙(║`<sub>i</sub>`(y`<sub>i</sub>`)`

Here `║`<sub>i</sub>, `|`<sub>i</sub> are the merge operators working on multiple operands, enumerated by a number `i` that varies from 1 to a certain `n` that is equal to or greater than 1. Likewise for communication definitions `^`<sub>i</sub>.

For n=1 we would get `║x` which equals to `x`, and `|x`, which is a strange form, meaning that `x` starts with 1-party communication.

For n=1 the n-ary communication formula would give:
```
   |(c·x╙y) = (^c)·(║x)╙(║y) = ^c·x╙y
```
Note that `^c` is the process defined by the "unary communication" `c`. In other words: it is the process definition of the refinement `c`.

A unary version of the main merge axiom would be:
```
   ║x = x╙ε + |x
```
or
```
   x = x╙ε + |x
```
so that
```
   c·x = c·x╙ε + |(c·x)
       = c·x╙ε + |(c·x╙ε)
       = c·x╙ε +  ^c·x╙ε 
       = c·x╙ε +  ^c·x
```
and thus

> `δ`<sub>H</sub>`(c·x) = δ`<sub>H</sub>`(c·x╙ε + ^c·x) = δ`<sub>H</sub>`(c·x╙ε) + δ`<sub>H</sub>`(^c·x) = δ + δ`<sub>H</sub>`(^c·x) = δ`<sub>H</sub>`(^c·x)`

This all suggests that process refinement is a special case of multi-party communication.

Recall the axiom presented earlier on `c·x` in the context of binary communication:
```
    c·x╙ε = c·x
```
This should be restated to cover process refinement/unary communication:
```
    c·x = c·x╙ε + ^c·x
```

##### Notations for actions and refinements #####

Here two groups of symbols have been distinguished:
  * `a, b, ...` - atomic actions
  * `c, d, ...` - things that can communicate (1 or more parties)

This dichotomy is reflected by concrete SubScript syntax:
  * all atomic actions appear as code between brace pairs
  * the things that can communicate appear as script calls. Normal script calls may be considered as calls to single party communication. They are syntactically equal to calls to communicating scripts

This held as well in SubScript's predecessor Scriptic. The concrete syntax more or less pointed at the opportunity to make communication in ACP more powerful: one or more parties performing a shared process expression.
