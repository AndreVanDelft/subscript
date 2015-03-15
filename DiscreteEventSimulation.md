In discrete event simulations activities may have specified timing properties, such as start time, end time and duration. SuScript itself does not support such notions, but it allows you to plug in a simulation engine that takes care of timing aspects.
Suppose there is a variable named `sim` that represents such an engine; it offers features such as
  * `currentTime` - answers the current time
  * `startTime` - sets the start time of the actions "in here"
  * `duration` - sets the duration of the actions "in here"

Let's use [Daniel Wellman's library](http://blog.danielwellman.com/2009/03/pimp-my-library-5minutes-with-the-time-and-money-library.html) that helps writing down durations, such as in
```
5.seconds
5 seconds
3 days + 2 weeks
```

Using a directive we can make `sim` the simulation engine for the script named `live`:
```
main (args: Array[String]) = @sim: live
```
The directive code `sim` expands to
```
sim.apply(here)
```
Many discrete event simulations focus on queueing behavior. A typical queue simulation is would be:
> During a number of days customers  enter a shop  and queue for service by a server. There are several servers, but there is only one queue with a first-in-first-out rule. The customers arrive according to a random Poisson distribution, and the servers have a random negative-exponen­tially distributed service time,  i.e. number of customers served in unit time. After closing time no more customers are allowed to enter the shop. The shop closes its door after the final customer has been served and left.
A main outline for such a queue simulation with 10 servers and which will simulate a working week, is:
```
live             = days,5 || servers,10
servers (n: Int) = times,n & server,pass
server  (i: Int) = serveCustomer,i...
times   (n: Int) = while(pass<n)
```
The script `days` takes care of the arrival of customers in time. The or-parallel construct with days,5 guarantees that after 5 simulated days the 10 servers terminate as well, so that the script `live` halts.
The script `times` is a utility to iterate a given number of times. It activates `while(pass<n)`.
The field `here.pass` expresses the loop counter. `pass` (without `here`) refers to a convenience function in the object `subscript.Predef` that returns `here.pass`; it has `pass`  as an implicit parameter.
The script `days` is simply an iteration of day; day states there are two parallel aspects: the working day and the end of the day, at which a report is generated. During the working day customers are created from 9 to 5:
```
days        (n: Int) = times,n day,pass
day         (i: Int) = workingHours,9,17 && atHour,24; theDayEnds,i
workingHours(f: Int,
             t: Int) = atHour,f createCustomers
           	    || atHour,t theShopCloses
```
`workingHours` uses or-parallelism to stop the customer creation at closing time.
`createCustomers` creates the customers. This cannot be done by a parallel for loop since the customers should be generated randomly, according to a poisson distribution, and it is not known how many customers will arrive. We build a sequential iteration that each pass waits for a negative-exponentially distributed time and then launches the customer:
```
createCustomers = waitTime(randomNegExp 3 minutes) *customer,pass ...
```
The asterisk in `*customer,pass` launches the `customer` script as a separate process, much like the postfix ampersand "&" on the Unix command line.

A customer enters, gets served and leaves. The time he spends in the shop is computed by passing the entrance time `et` to the script customerLeaves:
```
customer(c: Int) = var et = sim.currentTime
              	   customerEnters,c 
              	   getServed,c
              	   customerLeaves,c,et
```
The customer getting served and a server providing the service are shared actions, so we define a communicating script:
```
serveCustomer(s: Int) ,
getServed    (c: Int)
= var d = (randomNegExp 30 minutes)
  @sim.duration = d:
  {traceInterval(sim currentTime, sim currentTime+d,
                 " server "+s+"serves customer "+c)}
```
Internally this communication first computes the duration, and keeps it in the variable named `d`. Then a directive tells the simulation engine to apply that duration at the next action. The directive code expands to
```
sim.duration.apply(here,d)
```
The other scripts in the simulation are straightforward:
```
waitTime (d: Duration) = @sim duration  = d: η
atHour        (h: Int) = @sim startTime = sim currentTime startOfDay + h hours: η

theShopCloses          = {! traceTime("the shop closes"); report() !}
theDayEnds    (i: Int) = {! traceTime("day "+i+" ends" ); report() !}
customerEnters(i: Int) = {! traceTime("customer "+i +" enters"); nCustomers++ !}
customerLeaves(i: Int, 
        st: TimeStamp) = {! traceTime("customer " +i+" leaves"); 
                                      qtime+=sim.currentTime-st !}
```
The customers in this generation are modeled solely using scripts, not by ordinary objects. Of course you can as well add a customer class and dynamicly launch instances;  this would make sense in case more statistical data is to be collected:
```
createCustomers = waitTime(randomNegExp 3 minutes) *new Customer(pass).live...
```