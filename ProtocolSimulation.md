# Introduction #

As data communication protocols become more and more complicated, the need for clear formal specifications increases. In fact, SubScript offers a specification formalism; you can also execute such protocol specifications so that you can observe their simulated behavior. Note that it is hard to give clear formal specifications for many protocols; the subject seems more complicated than queuing systems.

# Example #

The following example is therefore only meant for those who know SubScript fairly well.

Consider the so-called alternating bit protocol. A sender process and a receiver process exchange information by means of two channels. Each information packet (d) that the sender sends is accompanied by an alternating bit (a); in even passes this bit normally is zero; in odd passes it normally is one. The receiver returns the alternating bit as an acknowledgement. When no errors occur in the network, both sender and receiver notice that the alternating bit agrees with the parity of the communication pass, and they switch to the next datum d.

However, errors may occur in the channels, and the channels themselves detect errors by means of an internal error detection mechanism. Upon detection (using CRC or the like) they send a special error value E, instead of the regular alternating bit value. Then the special behavior of the sender and the receiver process will lead to error recovery.

If the receiver gets E instead of a “real” bit, it returns the inverted alternating bit to the sender (so it has to know the parity of the pass!); the sender then recognizes this; it again sends the datum, and so on. The sender also sends the datum when it receives the error value E. If the receiver had already accepted the datum with the correct alternating bit, it now sees an "old" and wrong alternating bit, so it returns it. Communication continues until both parties know that both d and a have arrived correctly.

We will build a program of which the main processes are `sender` and `receiver`. `sender` will get its input (data to be transmitted) from a process `ins` that buffers keyboard input. After successful transmission to `receiver`, the latter sends the datum onwards over to a process `outs`, which may visualise the succeeded reception.

So we have a pipe of processes:
```
  ins ==> sender ==> receiver ==> outs
```


`sender` and `receiver` are iterations of scripts `S` and `R` that together take care of the complete transmission of a single datum; for recognizing whether the alternating bit is good or wrong, `S` and `R` need to know the pass parity. Parameters will take care of this:
```
  sender   = ...S(pass%2==0)
  receiver = ...R(pass%2==0)
```
`pass` translates into `here pass`, which is a loop counter.

`sender` and `receiver` are connected in forward direction using the pipe. For the alternating bit feedback in the reverse direction, a default channel is used. Unfortunately the pipe can not be used bidirectionally (as of the time of writing).

First `S` accepts a datum from the pipe. Then it sends this datum onward over the pipe, together with the pass parity as the alternating bit. Then `S` waits for an answer from the return channel. As long as `S` receives the wrong alternating bit (`!a`), or the error value `'E'`, it retries to send the datum. The loop ends when `S` receives the correct alternating bit.

`R` may get a number of times a datum with the inverted alternating bit or with the error value `'E'`, which it responds by sending the inverted alternating bit back onto the reverse channel. Its looping behavior ends when `R` receives a datum with a correct alternating bit; then `R` sends the datum onwards over the pipe:

```
  S(a: Boolean) = =>d: Char?; <=d,a..(->(!a)+ ->'E'); ->a
  R(a: Boolean) = var d: Char;
                  ..(=>(d?,!a)+ =>(d?,'E')) <-(!a); =>d?,a <=d
```

A random generator will decide whether or not to simulate an channel error; the boolean isOK in the pipe and channel will take care of it.

Communication between the parties will succeed if the alternating bit corresponds with the forced value of the receiving party. In case of success, a simple animation script starts. Finally the alternating bit value is overruled to 'E' in case of an error; therefore it has type `Any` rather than Boolean:
```
<==>(d: Char, a: Any?) = var isOK = chance(errorRate)
                         {? if (isOK) matchParameters
                            else parmetersMatchIf 
                              parameter "a" receiver value 'E' ?} 
                         animate,d,a,isOK

<-->(a: Any?) = var isOK = chance(errorRate):
                    {? if (isOK) matchParameters
                           else parmetersMatchIf 
                             parameter "a" receiver value 'E' ?} 
                        animate,a,isOK

<==>(d: Char) = η
```
The input and output processes:
```
  ins  = .. & @lowPriority: d:Char? <=d
  outs = ... =>d: Char? println,d
```
The parallel iteration in `ins` here allows for buffered input: `ins accepts new key presses even when the previous ones have not yet been sent yet over the pipe. The priority of key acceptance is made low; this give other processes such as the speed control the first choice to “eat away” keys. The ellipsis operand ("some") ensures that only new rounds in the iterations are created when needed.

The main program now may look like:
```
live = || ins ==> sender ==> receiver ==> outs
          speedControl
          errorRateChanges
          exit
```