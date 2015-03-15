## The Sieve of Eratosthenes ##
New programming languagues are these days often benchmarked by showing how they allow for the specification of the Sieve of Eratosthenes. This is about an algorithm to compute prime numbers, thousands of years ago invented by the Greek Eratosthenes. Informally, the algorithm starts with the first prime number, 2.  From the natural numbers, up to a maximum value for practical reasons, it wipes out all multiples of this prime. The next remaining number in the list, 3 must then also be prime. Now all multiples of 3 are erased. This way prime numbers are discovered one by one, and each acts as a sieve to find more primes.

It is fun to program this using tiny sieves as processes that run in parallel, at least conceptually. Think of a pipeline with a simple number generator, a list of sieves and a printer. There is a sieve for each recognized prime number; sieve 2 filters out all multiples of 2, etc. After 3 tiny sieves have been generated, the processes would be like:

![http://subscript.googlecode.com/files/Erasthostenes.png](http://subscript.googlecode.com/files/Erasthostenes.png)

SubScript allows for a very short specification, in only 10 lines of code:
```
object Eratosthenes { 

  val toPrint = new NetworkConnection

  public script..
   main(args: Array[String]) = generator(2, 1000000) 
                                 ==> (..==>sieve) 
                                 ==[toPrint]==> printer

   generator(start,end:Int)  = for(i<-start to end) <=i
   printer                   = ..=>i:Int? println,i

   sieve                     = =>p:Int?      @toPrint:<=p;   
                             ..=>i:Int? if (i%p!=0) <=i

   <==>(i:Int)               = {}
}
```
The main script specifies 3 parallel processes that are configured in a pipeline:
```
   main(args: Array[String]) = generator(2, 1000000) 
                                 ==> (..==>sieve) 
                                 =={toPrint}==> printer
```
The generator process generates numbers from 2 to 10000000. It is connected using the pipe symbol ==> to a process that is specified by (..==>sieve). The pair of dots, also called small ellipsis, here specifies an iteration that proceeds with new loop passes "when they would become needed".

In this case the iteration is not sequential (as usually seen), but the pipe symbol specifies that the iteration builds an ever growing pipe. This iteration starts by creating a first pipe element called sieve. As soon as this pipe element starts doing something (by accepting a number from the left hand side),  the next pipe element is created etc. Each pipe element will act as a sieve for a recognized prime number. Prime numbers that pass through all sieves are sent through the pipeline to the printer process.

Pipe symbols such as `==>`may be split in two parts, `=={` and `}==>`, with in between some code that functions like a directive. For instance,
```
  =={toPrint}==>
```
will execute the code `toPrint` (or in fact `toPrint.apply`) upon activation of the right-hand operand. This code will mark the operand. A similar thing happens on activation of `@toPrint:<=p`. Given that toPrint is a NetworkConnection, it will make sure that the sending of a prime by `<= p` will be directed towards the printer.

The generator is a script with two parameters that indicate in what range numbers must be generated. Internally it contains a sequential loop using a for iterator that has been inherited from Scala:
```
  generator(start,end:Int)  = for(i<-start to end) <=i
```
The loop states that each pass the loop variable i is sent over the pipe, using <=i. This sending is synchronous: completion waits until the variable has been received at the other side. This ensures that the loop does not explode.

The next script defines the printer:
```
   printer = ..=>i:Int? println,i
```
Here again the two dots (small ellipsis) denote two things that are in principle more or less independent: a loop, and an optional break point. The effect of starting a sequence with this symbol causes it to happen zero or more times. It would have been one or more times when the dots had appeared at the end of the sequence instead, as in
`=>i:Int? println,i..`

In the sequence, the printer waits for an number to arrive over the pipe, using the phrase: =>i:Int? This does two things in one statement: it declares a variable i of type Int, and it waits on the pipe to receive the value. This is syntactic sugar for the following sequence:
```
var i:Int =>i?
```
This would be longer to write, and it repeats the i, which is error prone redundancy. I would recommend the shorter way of writing.
Then the printer prints the number using a call to the method println.

Both `generator` and `printer` use unnamed send and receive actions. These are comparable to "standard IO", and make these scripts more generic.

The next script specifies the behaviour of a single sieve process:
```
   sieve = =>p:Int?      @toPrint:<=p;   
         ..=>i:Int? if (i%p!=0) <=i
```
Note the semicolon at the end of the first line. The semicolon is optional, but when you leave it out, you get an operator that binds very strong, whereas the semicolon binds very weak. This way, the sequence of the first line is separated from the sequence of the second line. The second line denotes a loop of its own; whereas the first line does not. We could equivalently have written the specification with parentheses instead of the semicolon,
```
   sieve = (=>p:Int?      @toPrint:<=p)
         (..=>i:Int? if (i%p!=0) <=i)
```
This takes 3 more characters to type and to read. I think it is often better to go for the shorter option in such cases.
The sieve starts by accepting its own prime number named p from the pipe. Shortly after this action happens, the magic iteration `..==>sieve` in the main script activates the next sieve. Thereafter the "current" sieve (the one that just had accepted its prime number) must first send its prime on to the printer.

Just sending it over the pipe to the right would be wrong, because there the next sieve is listening. Therefore it is send using `@toPrint:<=p`, which is plumbed at a higher level to the printer.

Then, thanks to the semicolon, the sieve enters a loop:
```
 ..=>i:Int? if (i%p!=0) <=i
```
Every once in a while it receives a number from the pipe. Then it checks whether its prime is a divisor of this number; if not, the number could be prime, and it is send onward to the next sieve.
The last two scripts defines that integer numbers may be communicated over the pipe within the current object. Nothing else happens during that communication:
```
   <==>(i:Int)         = {}
```



References to the Sieve of Eratosthenes in other languages:

> 67 languages: http://rosettacode.org/wiki/Sieve_of_Eratosthenes

> C: http://primes.utm.edu/links/programs/sieves/Eratosthenes/C_source_code

> Java: http://primes.utm.edu/links/programs/sieves/Eratosthenes/Java_source

> Go: http://blog.onideas.ws/eratosthenes.go

> F#: http://fsharpnews.blogspot.com/2010/02/sieve-of-eratosthenes.html

> Maisie: http://pcl.cs.ucla.edu/projects/maisie/tutorial/programming

> C#,LINQ: http://www.codethinked.com/post/2010/01/12/The-TekPub-LINQ-Challenge-And-The-Sieve-Of-Eratosthenes.aspx