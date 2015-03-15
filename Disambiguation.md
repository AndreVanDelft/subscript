## The Case for Disambiguation Support ##

SubScript may well be used to describe programming language syntaxes, but at first it was limited to LL1 grammars: only the current input symbol is available to decide which alternative to choose.

The following grammars would lead to ambiguous choices:
```
  A·B·A·C+A·D
  (A·B+ε)·A·D
  A·B/A·C
```
Unambiguous versions would look like:
```
  A·(B·A·C+D)
  A·(B·A·D+D)
  A·(B+C+A·C)
```

As an experiment, SubScript offers some alternatives for unambiguous choice.

## Disambiguating Alternatives for Exclusive Choice ##

A first option would be to specify an or-parallel operator instead of the nondeterministic choice: `|` or `||`. For instance, in
```
  A·B·A·C || A·D
```
the A could be programmed in such a way that multiple instances can succeed given the corresponding input. Next, when B would succeed, D should fail and vice versa. This way, either the `A·B·A·C` part or the `A·D` part would succeed.
The other or-parallel operator `|` could be used as well. But there is a third option. A disambiguating choice operator |+| could be defined so that the equivalence
```
  A·B·A·C |+| A·D = A·(B·A·C+D)
```
would hold. `|+|` is much like the exclusive-or operator `+`, the difference being that an atomic action in one operand need not exclude an atomic action in another operand, in case they communicate or have another kind of simultaneous occurence.

This new idea is still rather vague; some axioms for this operator make it more concrete.
Let `a©b` denote that the communication or simultaneous occurrence of actions `a` and `b`; in case there is no such a communication it `a©b` comes down to `δ`.
Let `a✇b` denote a predicate that that `a` and `b` are exclusive, so they cannot occur simultaneously; `a©b` comes down to either `ε` or `δ`.
Then
```
    x|+|y  = y|+|x 
(x+y)|+|z  = x|+|z + y|+|z
   ax|+|by = (a✇b)(ax+by) + (a©b)(x|+|y)
    ε|+|x  =       ε   +   x 
    δ|+|x  =               x 
```
The third axiom considers the case where the operands of `|+|` each start with an atomic action. It distinguishes the subcases where these atomic actions may communicate or not.

## Disambiguating Alternatives for the Sequence Operator ##
Now there would also a need for other version of the sequence operator. Namely
```
  (A·B|+|ε)·A·D
```
would simply reduce to
```
  (A·B+ε)·A·D
```
We would need operators `|·`, `||·`, `|·|` (and `|;` etc.) that would give the following reductions:
```
  (A·B+ε)|· A·D = A·(B·A·D |   D)
  (A·B+ε)||·A·D = A·(B·A·D ||  D)
  (A·B+ε)|·|A·D = A·(B·A·D |+| D)
```
For this we need a kind of filter functions named `α` and `β`; these are not available in SubScript, but only introduced here to define the disambiguating operators.
`α(x)` yields the part of `x` that starts with atomic actions; in other words: it eats away leading `ε` alternatives.
`β(x)` works complementary: it yields `ε` if `x` is equivalent to `ε+y` for some `y`; else it yields `δ`.
The defining axioms are:
```
  α(x+y)  =  α(x)+α(y) 
  β(x+y)  =  β(x)+β(y) 
  α(ax)   =  ax 
  β(ax)   =  δ 
  α(δ)    =  δ 
  β(δ)    =  δ 
  α(ε)    =  δ
  β(ε)    =  ε 
```
With induction it is easy to see that `x = α(x)+β(x)`, so that
```
 x · y  = α(x)·y + β(x)·y
```
The disambiguating sequence operators are now defined similarly as
```
 x|· y  = (α(x)|· y) |   (β(x)·y)
 x||·y  = (α(x)||·y) ||  (β(x)·y)
 x|·|y  = (α(x)|·|y) |+| (β(x)·y)
```

## Disambiguating Alternatives for the Disrupt Operator ##

The disrupt operator `/` would also need disambiguating family members. The same would hold for the suspend/resume operators, i.e. the ones with the `#`character in their symbol. However, the suspend/resume operators have some special operational semantics so that disambiguating versions might be too complicated to understand and to implement. For the disrupt operator, let's first see the normal defining axioms. For these we need predicate functions `δ(x)` and `γ(x)`, telling whether or not their arguments come down to `δ`:
```
  γ(x+y)  =  γ(x)+γ(y) 
  δ(x+y)  =  δ(x)δ(y) 
  γ(ax)   =  ε 
  δ(ax)   =  δ 
  γ(δ)    =  δ 
  δ(δ)    =  ε 
  γ(ε)    =  ε
  δ(ε)    =  δ 
```
Now the normal disrupt operator is defined by
```
 (x+y)/z = γ(x)(x/z) + γ(y)(y/z) + δ(x+y)z
    ax/y = a(x/y) + y 
     δ/x = x 
     ε/x = ε
```
The disambiguating version `|/|` is defined like
```
 (x+y)|/|z = γ(x+y)((x|/|z) + (y|/|z)) + δ(x+y)z
    ax|/|y = a(x|/|y) |+| y 
     δ|/|x = x 
     ε|/|x = ε
```
Definitions for the other two versons `|/` and `||/` are likewise.

## Disambiguation by Execution Tweaking ##

These are a lot of extra symbols, but the programmer does not need to use them when dealing with ambiguous grammars. One may also tweak the execution using a directive such as
```
  @disambiguate: term
```
so that all `+`, `;` and `/` operators inside term will behave like one of their bar-prefixed counterparts. `disambiguate` would not be part of the SubScript language itself; it should be made available in a library.

## Related work ##

Delayed choice : an operator for joining message sequence charts
Authors: J.C.M. Baeten and S. Mauw
http://repository.tue.nl/586804