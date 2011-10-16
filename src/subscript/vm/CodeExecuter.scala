package subscript.vm
import scala.collection.mutable._

// Executers that execute any call to Scala code in the application:
// code fragments, script calls, parameter checks, tests in if and while, annotations
trait CodeExecuterTrait {
  // graph operations such as aaStarted may only be done when called from executer!
  def asynchronousAllowed: Boolean
  
  def doCodeExecution[R](code: ()=>R): R = code.apply() // for Atomic Actions, if, while, tiny, script calls etc, and also for onActivate etc
  
  private def shouldNotBeCalledHere = throw new Exception("Illegal Call")
  def executeAA     : Unit                                      = shouldNotBeCalledHere // TBD: clean up class/trait hierarchy so that this def can be ditched
  def executeAA(lowLevelCodeExecuter: CodeExecuterTrait): Unit  = shouldNotBeCalledHere // TBD: clean up class/trait hierarchy so that this def can be ditched
  def afterExecuteAA: Unit                                      = shouldNotBeCalledHere // TBD: clean up class/trait hierarchy so that this def can be ditched
  def interruptAA   : Unit                                      = shouldNotBeCalledHere // TBD: clean up class/trait hierarchy so that this def can be ditched
  def n: CallGraphNodeTrait[_<:TemplateNode]
  def scriptExecuter: ScriptExecuter
}
case class TinyCodeExecuter(n: CallGraphNodeTrait[_ <: TemplateNode], scriptExecuter: ScriptExecuter) extends CodeExecuterTrait  { // TBD: for while, {!!}, @:, script call
  val asynchronousAllowed = false
  override def doCodeExecution[R](code: ()=>R): R = super.doCodeExecution{()=>n.hasSuccess = true; code.apply()}
}
abstract class AACodeFragmentExecuter[N<:N_atomic_action[N]](_n: N, _scriptExecuter: ScriptExecuter) extends CodeExecuterTrait  {
  
  // Executer for Atomic Actions. These require some communication with the ScriptExecuter, to make sure that 
  // graph messages such as AAStarted, AAEnded en Success are properly sent.
  // Note: such messages may only be handled from the main ScriptExecuter loop!
  //
  // Since scala code execution from Subscript may be asynchronous (e.g., in the Swing thread or in a new thread), 
  // there is some loosely communication with the ScriptExecuter
  // E.g., after calling the Scala code, the method executionFinished is called, which inserts an AAExecutionFinished
  def n = _n
  def scriptExecuter = _scriptExecuter
  val asynchronousAllowed = true
  override def interruptAA = {}
  def naa = n.asInstanceOf[N]
  def doCodeExecution(lowLevelCodeExecuter: CodeExecuterTrait): Unit = lowLevelCodeExecuter.doCodeExecution{()=>n.hasSuccess = true; naa.template.code(naa); executionFinished}
  def aaStarted = scriptExecuter.insert(AAStarted(n,null))
  def aaEnded   = scriptExecuter.insert(AAEnded(n,null)) 
  def succeeded = scriptExecuter.insert(Success(n,null)) 
  override def executeAA: Unit = executeAA(this) // for Atomic Action execution...should ensure that executionFinished is called
  def executeAA(lowLevelCodeExecuter: CodeExecuterTrait): Unit // for Atomic Action execution...should ensure that executionFinished is called
  def afterExecuteAA             // to be called by executer, asynchronously, in reaction to executionFinished (through a message queue, not through a call inside a call)
  def executionFinished = scriptExecuter.insert(AAExecutionFinished(naa)) // so that executer calls afterRun here
  def toBeReexecuted    = scriptExecuter.insert(AAToBeReexecuted   (naa)) // so that executer reschedules n for execution
  def deactivate        = scriptExecuter.insert(Deactivation       (naa,null,false))
  def suspend   = {}
  def resume    = {}

  def notifyScriptExecuter = scriptExecuter.synchronized {
        scriptExecuter.notify() // kick the scriptExecuter, just in case it was waiting
  }

}

class NormalCodeFragmentExecuter[N<:N_atomic_action[N]](n: N, scriptExecuter: ScriptExecuter) extends AACodeFragmentExecuter[N](n, scriptExecuter)  {
  //without the next two definitions the compiler would give the following error messages; TBD: get rid of these
  // class NormalCodeFragmentExecuter needs to be abstract, since: 
  //   method scriptExecuter in trait CodeExecuterTrait of type => subscript.vm.ScriptExecuter is not defined 
  //   method n in trait CodeExecuterTrait of type => subscript.vm.CallGraphNodeTrait[_ <: subscript.vm.TemplateNode] is not defined	CodeExecuter.scala	/subscript/src/subscript/vm	line 54	Scala Problem
  
  
  override def executeAA(lowLevelCodeExecuter: CodeExecuterTrait): Unit = {
    aaStarted
    doCodeExecution(lowLevelCodeExecuter)
  }
  override def afterExecuteAA = {
    if (!n.isExcluded) {
       aaEnded; succeeded
    }
    deactivate
  }
}
class UnsureCodeFragmentExecuter(n: N_code_unsure, scriptExecuter: ScriptExecuter) extends AACodeFragmentExecuter(n, scriptExecuter)  {
  override def executeAA(lowLevelCodeExecuter: CodeExecuterTrait): Unit = doCodeExecution(lowLevelCodeExecuter)
  override def afterExecuteAA = {
    if (n.hasSuccess) {
       aaStarted; aaEnded; succeeded; deactivate
    }
    else { // TBD: allow for deactivating result
      toBeReexecuted
    }
  }
}
// Adapter to wrap around other CodeExecuters
// TBD: improve
trait CodeExecuterAdapter[CE<:CodeExecuterTrait] extends CodeExecuterTrait {
  var adaptee: CE = _
  def adapt[R](codeExecuter: CE) = {adaptee = codeExecuter}
  def asynchronousAllowed = adaptee.asynchronousAllowed
  def notifyScriptExecuter = adaptee.synchronized {
        adaptee.notify() // kick the scriptExecuter, just in case it was waiting
  }
}
class ThreadedCodeFragmentExecuter(n: N_code_threaded, scriptExecuter: ScriptExecuter) extends NormalCodeFragmentExecuter(n, scriptExecuter)  {
  override def interruptAA: Unit = if (myThread!=null) myThread.interrupt
  var myThread: Thread = null
  override def doCodeExecution(lowLevelCodeExecuter: CodeExecuterTrait): Unit = {
      val runnable = new Runnable {
        def run() {
          ThreadedCodeFragmentExecuter.super.doCodeExecution(lowLevelCodeExecuter)
          notifyScriptExecuter // kick the scriptExecuter, just in case it was waiting
        }
      }
      myThread = new Thread(runnable)
      myThread.start()
  }
}
class SwingCodeExecuterAdapter[CE<:CodeExecuterTrait] extends CodeExecuterAdapter[CE]{
  def n = adaptee.n
  def scriptExecuter = adaptee.scriptExecuter
  override def      executeAA: Unit = adaptee.executeAA(this) // Not to be called? TBD: clean up class/trait hierarchy 
  override def afterExecuteAA: Unit = adaptee.afterExecuteAA  // TBD: clean up class/trait hierarchy so that this def can be ditched
  override def    interruptAA: Unit = adaptee.interruptAA     // TBD: clean up class/trait hierarchy so that this def can be ditched
  override def doCodeExecution[R](code: ()=>R): R = {
    
    // we need here the default value for R (false, 0, null or a "Unit")
    // for some strange reason, the following line would go wrong:
    //
    // var result: R = _
    //
    // A solution using a temporary class was found at
    // http://missingfaktor.blogspot.com/2011/08/emulating-cs-default-keyword-in-scala.html
    class Tmp {var default: R = _} 
    var result: R = (new Tmp).default
    // luckily we have the default value for type R now...
    
    if (adaptee.asynchronousAllowed) {
      var runnable = new Runnable {
        def run(): Unit = {result = adaptee.doCodeExecution(code); notifyScriptExecuter}
      }
      javax.swing.SwingUtilities.invokeLater(runnable)
    }
    else {
      var runnable = new Runnable {
        def run(): Unit = {result = adaptee.doCodeExecution(code)}
      }
      javax.swing.SwingUtilities.invokeAndWait(runnable)
    }
    result
  }
}
case class EventHandlingCodeFragmentExecuter[N<:N_atomic_action_eh[N]](_n: N, _scriptExecuter: ScriptExecuter) extends AACodeFragmentExecuter(_n, _scriptExecuter)  {
  override def executeAA(lowLevelCodeExecuter: CodeExecuterTrait): Unit = executeMatching(true) // dummy method needed because of a flaw in the class hierarchy
  def executeMatching(isMatching: Boolean): Unit = {  // not to be called by scriptExecuter, but by application code
    n.hasSuccess = isMatching
    if (n.hasSuccess) 
    {
      n.template.code(n) // may affect n.hasSuccess
    }
    if (n.hasSuccess) 
    {
      executionFinished // will probably imply a call back to afterExecute from the ScriptExecuter thread
      // TBD: maybe a provision should be taken here to prevent handling a second event here, in case this is a N_code_eh
      notifyScriptExecuter // kick the scriptExecuter, just in case it was waiting
    }
  }
  override def afterExecuteAA = {
    if (!n.isExcluded && n.hasSuccess) {
      aaStarted
      aaEnded
      succeeded
      deactivate 
      // TBD: handle looping eh code fragment; check for optionalBreak and break...:
      //n match {
      //  case N_code_eh     (_) => 
      //  case N_code_eh_loop(_) => 
      //}
    }
  }
}
