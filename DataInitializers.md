# Work in progress #

The following work on data initializers and text processing is still in progress.
It is experimental, and eventually it may or may not result in useful coding patterns.

## Data initializers ##
Scala comes with several simple example programs. One of these is about using data maps; it fills a certain map from color names to RGB values as follows:
```
val colors = Map(
  "red"       ->0xFF0000,
  "turquoise" -> 0x00FFFF,  
  "black"     -> 0x000000,
  "orange"    -> 0xFF8040,
  "brown"     -> 0x804000
 )
```
This is not a bad way, as compared to how it is typically done in Java. However, it is not the most convenient option. Often, data to be put in a map comes by copy+paste from a spreadsheet or a similar source. Then you have just rows and columns of cell values, which may or may not be separated by symbols like comma's. To use these as a map initializer would require the insertion of arrow symbols and comma's at the end of the line. In SubScript that would not be necessary:
```
val colors = new SelfLoadingMap[String,Int] {
 script.. 
  _ = ,;
   "red"       0xFF0000
   "turquoise" 0x00FFFF
   "black"     0x000000
   "orange"    0xFF8040
   "brown"     0x804000
}
```
The first two lines here introduce some complexity, but for longer lists of data this approach would be useful, especially when you would copy these from sources such as spread sheets.
SelfLoadingMap would typically be defined as follows:
```
trait Data2[T,U] {
 script..
  _(t:T,u:U)
  _
}
trait SelfLoadingMap[T,U] extends HashMap[T,U] with Data2[T,U] {
 script..
  _(t:T,u:U) = {this+=(t->u)} 
 script_
}
```
Trait Data2 tells that their is a default script to process data pairs, and there is another parameterless default script. (For Scala newbies: a trait is, simply said, like an interface in Java, but it may contain complete method definitions. Traits almost support multiple inheritance.)

Trait SelfLoadingMap extends HashMap. It specifies that processing a data pair just adds it to itself as hash map. Moreover, the SelfLoadingMap has a body that calls its default script named `_`. Scala would not understand a mere underscore `_` here, so that by convention it is written as `script_`.

Note that the indentation is crucial here: `script_` stands more to the left than the script defined in the previous line. Therefore it is not seen as start of a new script declaration, but as a start of the trait body.

A similar rule holds for the interpretation of the line containing just "`_`" in trait Data2. It is just below the start of the previous script; in case it would have been more to the right, it would have been seen as part of the previous script body (although there was none).

It is easy to use a data definition in multiple ways, by using Scala's near-multiple inheritance. E.g., suppose there was also a `trait InverseSelfLoadingMap[T,U]` that would create an inverse map, from "values" to "list of keys":
```
trait InverseSelfLoadingMap[T,U] extends HashMap[U,List[T]] with Data2[T,U] {
 script..
  _(t:T,u:U) = {+= (u->(t::( get(u) match {case Some(list)=>list
                                           case None      => Nil}
                   )   )   )
               }
 script_
}
```
We can now put some colors in a trait, including "darkness" as a synonym for "black":
```
trait MyData extends Data2[String,Int] {
 script.. _ =,;
   "red"       0xFF0000
   "turquoise" 0x00FFFF
   "darkness"  0x000000
   "black"     0x000000
   "orange"    0xFF8040
   "brown"     0x804000
}
```
Then we could store these data in either the hash map or the inverse hash map using
```
object        MapWithMyData extends        SelfLoadingMap[String,Int] with MyData
object InverseMapWithMyData extends InverseSelfLoadingMap[String,Int] with MyData
```
Printing using
```
println ("MapWithMyData: "+MapWithMyData) 
println ("InverseMapWithMyData: "+InverseMapWithMyData)
```
results essentially in:
```
MapWithMyData: Map( 
  turquoise -> 65535, 
  red   -> 16711680,  
  orange  -> 16744512,  
  brown   -> 8404992,  
  darkness  -> 0,  
  black   -> 0)

InverseMapWithMyData: Map( 
     65535  -> List(turquoise),  
  16711680  -> List(red),  
  16744512  -> List(orange),  
   8404992  -> List(brown),  
         0  -> List(darkness, black))
```
### Text manipulation ###
Scala lets you conveniently mix XML stuff with normal program elements, which is for instance useful for generating web content. The Scala site contains an illustrative Addressbook sample
> [http://www.scala-lang.org/node/45](http://www.scala-lang.org/node/45)
A typical class in that example is:
```
  class AddressBook(a: Person*) {
    private val people: List[Person] = a.toList

    /** Serialize to XHTML. Scala supports XML literals
     *  which may contain Scala expressions between braces,
     *  which are replaced by their evaluation
     */
    def toXHTML =
      <table cellpadding="2" cellspacing="0">
        <tr>
          <th>Name</th>
          <th>Age</th>
        </tr>
        { for (val p <- people) yield
            <tr>
              <td> { p.name } </td>
              <td> { p.age.toString() } </td>
            </tr> 
        }
      </table>;
  }
```
In SubScript you could write instead of toXHTML:
```
    script
      tableXHTML  =,;
""""
<table cellpadding="2" cellspacing="0">
  <tr>
    <th>Name</th>
    <th>Age</th>
  </tr>
""""
( for (val p <- people)
""""
  <tr>
    <td>"""" p.name """"</td>
    <td>"""" p.age.toString() """"</td>
  </tr>
""""
) 
"</table>"
     _(s1:String) = print(s1)       
     _(s1:String,s2:String) = print(s1,s2)       
     _(s1:String,s2:String,s3:String) = print(s1,s2,s3)
```

At the center there is a for loop construct. Note that the `for` construct occurs inside the parenthesized loop expression, rather than before that.

tableXHTML does not very different over the former pure Scala version, and it not really an improvement: the specification is a few lines longer, and the result is just a string, rather a more structured piece of XML. However, such SubScript code not only applies to creating XML strings, but as well all to creating other kinds of text.

The above code could be improved using some special definitions for XML tags:
```
  class XMLTag {
    def start = "<"+getClass.getName+">"
    def end  = "</"+getClass.getName+">"
  }
  case class tr extends XMLTag
  case class th extends XMLTag
```
With scripts that print table headers, rows and cells the alternative for toXHTML would become:
```
  script..
    tableHeader(s1:String, s2:String) 
    = """<table cellpadding="2" cellspacing="0">"""
      tableRow,s1,s2

    tableRow(s1:String, s2:String)
    = tr.start tableCell,s1 tableCell,s2 tr.end

    tableCell(s:String) = th.start s th.end

    tableXHTML = tableHeader,"Name","Age";
                 for (val p<-people) tableCell,p.name 
                                     tableCell,p.age.toString();
                 tableFooter
```
Subscript also eases parsing text. For instance, the following code could parse the just generated XML text from an input source:
```
  script..
    tableHeader(s1:String??, s2:String??) 
    = anyText tableRow,s1?,s2?

    tableRow(s1:String??, s2:String??)
    = tr.start tableCell,s1? tableCell,s2? tr.end

    tableCell(s:String??) = th.start s? th.end

    tableXHTML = tableHeader,"Name","Age";
                    .. 
                    tableRow, name:String?,age:String?
                    {people.add(Person(name,Int.parse(age))};
                 tableFooter

     anyText  = scanAny
     _(s1:String??) = scan(s?)
```
Here the script `scan(s:String??)` would stand for scanning a string. What string? It could be any string, to be yielded in the formal output parameter s. The first question mark appending the parameter declaration denotes that it is an output parameter; the second question mark denotes that this parameter may also be used with a forcing value.
That forcing happens a few lines higher, with `th.start`. This reduces to `_(th.start)`.

Here the actual parameter does not have a question mark, whereas the formal parameter has two question marks; therefore the parameter th.start is regarded as a forcing value. The idea of this forcing is that the deeper call to scan will only succeed if the input matches this paramter value.

`_(s1:String??)` calls `(s?)` with 1 question mark. Normally this single question mark in a call denotes an output parameter, but since the actual parameter is itself a formal parameter that might be forcing, the parameter to scan scan will adapt itself to be forcing in such a case.

So the table cell, defined as th.start s? th.end, will "happen" when the following text is scanned: first `<th>`, then a quite arbitrary string, and then `</th>`.

The actual output parameters name:String?,age:String? are at the same time variable declarations.

Just as in the previous example, tableXHTML contains a loop; this time not with a for-iterator, but with an ellipis symbol (..). Thiis symbol denotes a loop, but at the same time an optional exit point. The loop will exit in this case when tableFooter happens instead of tableRow.