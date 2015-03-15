## Syntax definition ##
```
script..
  subScriptCode           = "script";
                             operatorModifiers (.directive) scriptDefinition
                          + ".." operatorModifiers (.directive) (scriptDefinition..)
  scriptDefinition        = (."override") scriptHeader . ("+="+"=") scriptExpression
  scriptHeader            = scriptNameWithDots optionalParameters..","
                          + (.scriptName) doubleArrow optionalParameters
  scriptNameWithDots      = scriptName .".." + ("."+"..")scriptName
  scriptName              = (identifier + "implicit").simpleArrow
  optionalParameters      = . "(" formalParameters ")"
  formalParameters        = .; formalParameter .. ","
  formalParameter         = identifier ":" type . formalOutputMarker
  formalOutputMarker      = "?" + "??"
  scriptExpression        = operatorModifiers scriptExpression(10)
  operatorModifiers       = . ("," + naryOperatorDesignator) . naryOperatorDesignator

  scriptExpression(i:Int) = if (i>=0) (scriptExpression(i-1) .. operator(i))
                            else scriptTerm

  operator(i:Int)         =+ i matches (
                             case 8 ==> if newLineSignificant newLine else δ
                             case 7 ==> ";" 
                             case 6 ==> "||"  "|"   "||:"  "|:" 
                                         "|+"  "|;"  "|/" 
                                         "||+" "||;" "||/" 
                                         "|+|" "|;|" "|/|" 
                             case 5 ==> "&&"  "&"  "&&:"  "&:" 
                             case 4 ==> "=="  "==:" networkingArrow
                             case 3 ==> "+"  
                             case 2 ==> "/"  "%"  "%/"  "%/%/"  "%&"  "%&"
                             case 1 ==> "·" "!;"
                             case 0 ==> if commasOmittable δ else ε 
                          )

   scriptTerm              =;+ variableDeclaration 
                              privateDeclaration 
                              ternary
                              "try" simpleTerm (catchClause %; finallyClause)
                              "if" valueExpression simpleTerm . "else" simpleTerm 
  ternary                 =   unary . "?" ternary . ":" ternary
  unary                   =  ..(directive+unaryOperator); simpleTerm
  directive               = "@" scalaCode ":"
  unaryOperator           =+ "!"  "-"  "~"  "*"  "**"
  simpleTerm              =;+ 
                            simpleValueLedTerm
                            codeFragment 
                            throwTerm
                            whileTerm
                            forTerm
                            specialTerm 
                            "(" scriptExpression ")"
                            arrow . actualParameters
  actualParameters        = simpleActualParameters 
                          + classicActualParameters  . "if?" valueExpression 
  simpleActualParameters  = simpleActualParameter..parameterSeparator
  classicActualParameters = "("  ( .; actualParameter.."," )  ")"
  parameterSeparator      = "," + if commasOmittable ε
  simpleActualParameter   = simpleValueExpression . actualOutputMarker
  actualParameter         =       valueExpression . actualOutputMarker 
  actualOutputMarker      = (. ":" type) "?" + "??"; . "if?" valueExpression
  specialTerm             =+ "(-)"  "(+)"  "(+-)"   "."  ".."  "..."  "break"
  identifiers             = identifier..","
  variableDeclaration     = "val" identifiers              "=" simpleValueExpression
                          + "var" identifiers (":" type %; "=" simpleValueExpression)
  privateDeclaration      = "private"; identifier..","
  simpleValueLedTerm      = simpleValueExpression;
                             (.actualOutputMarker)
                              ..parameterSeparator simpleValueExpression
                            + arrow . actualParameters  
                            + "match" "(" scriptCaseClauses ")" 
  simpleValueExpression   = "_"
                          + literal
                          + "{=" scalaExpression "=}"
                          + "new" (classTemplate + templateBody)
                          + ( "here" 
                            + currentInstanceExpression 
                            + identifier . "." currentInstanceExpression )
                            (.. "." identifier) 
                            (. classicActualParameters)
  currentInstanceExpression = "this" + "super" "." identifier
  codeFragment            = ;+ 
                           "{"  scalaCode "}"
                           "{*" scalaCode "*}"
                           "{?" scalaCode "?}"
                           "{!" scalaCode "!}"
                           "{." scalaCode ".}"
                           "{..." scalaCode "...}"
 
  whileTerm               = "while" valueExpression 
  throwTerm               = "throw" valueExpression 
  forTerm                 = "for"; "(" enumerators ")" + "{" enumerators "}"
  catchClause             = "catch" "(" (scriptCaseClause..) ")"
  scriptCaseClause        =;;
                            "case" pattern
                            . "if" valueExpression
                            ("=>" + "*=>") scriptExpression
  finallyClause           = "finally" "{" scalaCode "}"
  valueExpression         = parenthesizedExpression  + simpleValueExpression
  parenthesizedExpression = "(" scalaExpression ")"


  naryOperatorDesignator  =+ ";"  
                             "||"  "|"    "||·" "|·" 
                             "|+"  "|;"   "|/" 
                             "||+" "||;"  "||/" 
                             "|+|" "|;|"  "|/|" 
                             "&&"  "&" "&&·"  "&·"
                             "=="  networkingArrow        
                             "+"  
                             "/"  "/"  "%"  "%/"  "%/%/"  "%&"  "%&"
                             "·"  "!;"
                             (; "?" simpleValueExpression ":")

   networkingArrow        =+ "<=="  "==>"  "<==>"   "<<==>"   "<==>>"  "<<==>>"
                             "<==·" "==>·" "<==>·" "<<==>·" "<==>>·" "<<==>>·"
                             ("<<=={" "<=={" "=={"; scalaCode; 
                              "}==>>" "}==>" "}==" "}==>>·" "}==>·" "}==·")
                              ("<<==(" "<==(" "==("; scalaTupel; 
                              ")==>>" ")==>" ")==" ")==>>·" ")==>·" ")==·")

  doubleArrow             =+ "<-->"   "<==>" 
                             "<-.->"  "<=.=>" 
                             "<-..->" "<=..=>"

  simpleArrow             =+ "<-" "<=" "->" "=>"

  arrow                   =+ simpleArrow "<-*" "<=*" "*->" "*=>" "?->" "?=>"


```
## Ambiguities ##
```
"." : specialTerm + simpleValueExpression part
ident : scriptHeader part + scriptCall part 
      + actualParameters("<-","->") part + class initializer
"(" : actualParameters part + simpleTerm part
"else", ":" : nesting
arrowLeft, arrowRight : following simpleValueExpression
"@" : also as regular annotation for scripts...
```
### Special Characters ###
```
   "·" 
```
#### Greek symbols defined in subscript.Predef ####

| Name | Symbol | subscript.Predef | Equivalent expression |
|:-----|:-------|:-----------------|:----------------------|
| Deadlock | `(-)` | `δ` | `{!here.fail!}` |
| Empty process | `(+)` | `ε` | `{!!}` |
| Neutral process | `(+-)` | `ν` | `{!here.neutral!}` |

## References to Scala syntax ##
type, scalaCode, scalaExpression, enumerators,
classTemplate, templateBody, identifier, literal, pattern

## Ambiguity operators ##
```
 x|;|y  = (ε(x);y) |+| (ε(x);y) 
 x|+|y  =       y  |+|  x 
 δ|+|x  =               x 
 ε|+|x  =       ε   +   x 
ax|+|by = (a✇b)(ax+by) + (a✇b)(x|+|y) 
ε(x+y)  =  ε(x)+ε(y) 
ε(x+y)  =  ε(x)+ε(y) 
ε(ax)   =  δ 
ε(ax)   =  ax 
ε(δ)    =  δ 
ε(δ)    =  δ 
ε(ε)    =  ε 
ε(ε)    =  δ 
x+y|/|z = (x|/|z + y|/|z
 ax|/|y = a(x|/|y) |+| y 
  δ|/|x = x 
  ε|/|x = ε + x 
```

### Nested loops ###
```
A ( B .. (C..))
A; B..(C..)
A;; B;..;C..
```

## Logic operators ##
```
a>=b    = !a | b   
a>=b>=c = ???
a^b     = !(a==b)
a^b^c   = ???
```
## Non-LL1 grammar ##
```
  simpleValueLedTerm      = normalCallExpression
                          + channelCallExpression
                          + matchExpression
                            
  normalCallExpression    = simpleValueExpression;
                             (.actualOutputMarker)
                              ..parameterSeparator simpleValueExpression
  channelCallExpression   = simpleValueExpression arrow . actualParameters  
  matchExpression         = simpleValueExpression "match" "(" scriptCaseClauses ")" 
```