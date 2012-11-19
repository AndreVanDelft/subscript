package subscript.vm

trait ScriptDebugger {
  var scriptExecutor: ScriptExecutor = null
  def messageHandled(m: CallGraphMessage[_])
  def messageQueued(m: CallGraphMessage[_])
  def messageDequeued(m: CallGraphMessage[_])
  def messageContinuation(m: CallGraphMessage[_], c: Continuation)
  def messageAwaiting

  def attach(se: ScriptExecutor): Unit = {scriptExecutor = se; se.scriptDebugger = this}
  
}