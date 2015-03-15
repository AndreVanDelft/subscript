## Introduction ##

Syntax descriptions may not just be useful for documentation and programming parsers, but also to generate conforming texts. This trial is meant to see whether Subscript could support both the consumption and production of texts according to a single syntax description.

The following test is for the time being work in progress. Maybe readers could contribute some ideas.

## Use case: a CSV record ##
A syntax for a comma separated record would be:
```
  csv = fieldValue..","
```
Given a scanner, a parser typically does things like:
```
  var mustContinue = true
  while(mustContinue) {
    fieldValueList += nextSym
    mustContinue = peekSym == ","
    if (mustContinue) nextSym
  }
```
An CSV generator has a similar structure with a if statement within a loop:
```
  var i = 0;
  for (fv<-fieldValueList) {
    if (i++ > 0) print ","
    print(fv)
  }
```

This similar structure suggests that a single syntax description could direct both the consumption and production.
On a high level, you would have a class hierarchy like:
```
  trait SyntaxDirectedConverter {
    script..
       live = input ==> syntax ==> output
       input
       output
  }
  trait Data[T] {
    script..
      implicit decompose(T data)
      implicit compose
  } 
  trait SyntaxDirectedConsumer[T] extends SyntaxDirectedConverter with Data[T] {
    script..
      input = scanner
      output = data
      scanner
      implicit handleData(data: T) = decompose(data)
  }
  trait SyntaxDirectedProducer[T] extends SyntaxDirectedConverter with Data[T] {
    script..
      input = data
      output = writer
      writer
      implicit handleData(data: T) = compose(data)
  }

trait ListData[T] extends Data[List[T]] {
  def isSending: Boolean
  implicit decompose(data: List[T]) = for (e<-data) e
  implicit compose(data: List[T]?) = @data=new List[T]: (e? {!data+=e!} ..)
```
The idea is that there is a pipe from input to output. The central pipe element, `syntax`, eats away or adds symbols that do not belong to the Abstract Syntax Tree.
Especially adding these symbols is prone to ambiguity. The writer should be able to handle this ambiguity.

TBD: elaborate; ideas are still quite vague

## Use case: some CSV lines ##
set of lines with comma separated records is:
```
  lines  = ..record "\n"
  record = fieldValue..","
```
So the separating comma comes after each field, except for the last field of a record. Equivalently, a comma comes before each field except for the first one of a record.
Typical Scala code to output such a line would be:
```
  openFile
  for (var r<-recordList) {
    var i = 0;
    for (fv <-r.fieldValueList) {
      if (i++ > 0) print ","
      print(fv)
    }
    println
  }
  closeFile
```
TBD: subscript approach

Old writings:
An alternative in SubScript would look like:
```
  doOutput = dataFeed ==> openFile lines closeFile
  dataFeed = for (var r<-data) recordStart<= (for (var f<-r fields) field<=f) recordEnd<=

  lines  = ..record "\n"
  record = recordStart=>; field=>_..","; recordEnd=>

  field      <==>(f: String) = f
  recordStart<==> = highPriority
  recordEnd  <==> = highPriority
  _(s: String) = print,s
```
The script `highPriority` would contain an action that would have a higher priority as normal alternative actions.