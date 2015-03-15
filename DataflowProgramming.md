# Dataflow Programming #

[Dataflow programming](http://en.wikipedia.org/wiki/Dataflow_programming) is a programming paradigm that models a program as adirected graph of the data flowing between operations. It is mainly used in Unix shell languages: small single-purpose tools are easily glued together using the pipeline symbol: `|`. Mainstream programming languages do not support it, though there are some libraries around.

SubScript supports dataflow programming though its network operator `==>` and communication scripts ending in a double arrow (`<=` and `=>`).

## Example: copying a file ##

As an example, consider a data flow program for copying a file. It mainly contains two processes, a reader and a writer, and connects these through a network operator:
```
copier(in: File, out: File) = reader(in) ==> writer(out)
```

The reader process is quite simple. It opens a file; then reads bytes from the file and outputs these over the network; when end of file is reached the loop ends and the file is closed. Note that it also transmits the end-of-file (-1) value over the network.
```
reader(f: File)  = val inStream = new FileInputStream(f);
                   val b = inStream.read()  <=b   while (b!=-1);
                   inStream.close()
```
writer is similar: it opens a file; then reads data from the network and, as long as end-of-file is not encountered, it writes these to the output file
```
writer(f: File)  = val outStream = new FileOutputStream(f);
                   => b?: Int   while (b != -1)    outStream.write(b);
                   outStream.close()
```
The communication channel for the data also needs to be declared:
```
<==>(b: Int) = {}
```
So we now have is a file copier using data flow programming. Would this be better than a copy method in traditional style?:
```
def copy(in: File, out: File): Unit = {
  val inStream = new FileInputStream(f)
  val outStream = new FileOutputStream(f);
  val eof = false
  while (!eof) {
    val b = inStream.read()
    if (b==-1) eof=true else outStream.write(b)
  }
  inStream.close()
  outStream.close()
}
```
The traditional style program is an order of magnitude faster, but in many cases the speed of the dataflow program is good enough.

The strength of the dataflow program is that it untwists two tasks: reading and writing. This way it becomes easier to put some processing between these tasks.

Moreover, reader and writer may well be placed in a library, so that you don't have to deal with the file protocol of opening, processing data and closing. Reader and writer communicate over unnamed channels using `<=` and `=>`. It is like communicating with standard input and output devices.

You may end op with a nice simple tool set for file handling and data processing; glue such tools together just like is done in Unix shell language.

## Example: encoding and decoding a text file ##

For an introductory programming course students Computer Science had been tasked to create a C++ program that encodes and decodes text files with run length compression.
In the encoded file
  * a backslash character and a digit are replaced by an escape sequence starting with a backslash
  * a run of two or more times the same character is replaced by a single occurrence of that character followed by an indicator of the run length; the indicator is a string representation in reverse order.

Encoding examples:

| Hello | Hel2o |
|:------|:------|
| \backslash1 | \\backslash\1 |
| 2222222222 | \201 |

### Solution ###

Network pipes with file readers and writers at both ends will do the encoding and decoding:
```
fileEncoder = reader(inFile) ==> encoder ==> writer(outFile)
fileDecoder = reader(inFile) ==> decoder ==> writer(outFile)
```

For the encoder and decoder we need a script lowPriority, which comes down to an action that only happens when there is nothing else to do:
```
lowPriority = @there.lowPriority: {}
```
The encoder and decoder are relatively easy, since they can reflect the grammar of the unencoded file and the encoded file. Loosely formulated:
```
unencodedFile = ..; anyChar; .. sameChar
  encodedFile = ..; . '\\'; anyChar; .. digit
```
Here sameChar denotes the same character as previously seen as anyChar.

The encoder has a loop:

Read a value from the network; then a zero or more times loop of reading the same value again (and counting the occurrences); then the lowPriority action so that the loop is only exited when no more same value arrive; then writing the escape character over the pipe, if necessary; then writing the value over the pipe; then writing the run length if it exceeds 1.

The loop ends after the end-of-file value (-1) has been processed.
```
encoder     = =>c?: Int; var n=1;
              ..=>c {n+=1};
              lowPriority;
              if (c=='\\'||c.toChar.isDigit) <='\\';
              <=c;
              if (n>1) (for(d<-n.toString.reverse)<=d);
              while (c != -1)
```
The decoder has loop:

Optionally read the escape character ('\') from the pipe; then the lowPriority action; read a character from the pipe (the lowPriority action makes sure this does not grab the escape character); then optionally read a sequence of digits that specify the encoded run length; then the lowPriority action, so that the digits must have been read when available; then write the character once or more times, depending on whether a run length was given.

The loop ends after the end-of-file value (-1) has been processed.
```
decoder     = .=>'\\';
              lowPriority;
              =>c?: Int;
              var n = 0; 
              var pow10 = 1;
              ..=>c? if?(c.toChar.isDigit) {n+=c.toChar.asDigit*pow10; pow10*=10};
              lowPriority;
              times(max(n,1)) <=c;
              while (c!=-1)
```