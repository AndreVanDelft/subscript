package subscript.vm

object SimpleScriptDebuggerObject extends SimpleScriptDebugger {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) return
    ScriptExecutorFactory.scriptDebugger = this
    try {
      val c = Class.forName(args.head)
      val m = c.getMethod("main", classOf[Array[String]])
      m.invoke(null, args.tail)
    }
    catch {
      case e: ClassNotFoundException =>
    }
  }
}

class SimpleScriptDebugger extends ScriptDebugger {

  def scriptGraphMessages = scriptExecutor.scriptGraphMessages
  def rootNode            = scriptExecutor.rootNode
  
  // some tracing stuff
  var nSteps = 0
  var maxSteps = 0 // 0 means unlimited
  var traceLevel = 2 // 0-no tracing; 1-message handling 2-message insertion+handling
  def trace(level:Int,as: Any*) = {
    if (traceLevel>=level) {
      as.foreach {a=>print(a.toString)}; 
      println
      //traceMessages
    }
    if (maxSteps>0 && nSteps > maxSteps) {println("Exiting after "+nSteps+"steps"); System.exit(0)}
    nSteps += 1
  }
  def traceTree: Unit = {
    var j = 0;
	  def traceTree[T <: TemplateNode](n: CallGraphNodeTrait[T], branches: List[Int], depth: Int): Unit = {
	    for (i<-1 to 30) {
	      print(if(i==depth)"*"else if (branches.contains(i)) "|" else if(j%5==0)"-"else" ")
	    }
	    j+=1
	    println(n)
	    n match {
	      case p:CallGraphParentNodeTrait[_] => 
	        val pcl=p.children.length
	        p.children.foreach{ c =>
	          var bs = if (c.template.indexAsChild<pcl-1) 
	                    depth::branches 
	                    else branches
	          traceTree(c, bs, depth+1)}
	      case _ =>
	    }
	  }
	if (traceLevel >= 1) traceTree(rootNode, Nil, 0)
  }
  def traceMessages: Unit = {
	if (traceLevel >= 1) {
	  println("=== Messages ===")
	  scriptGraphMessages.foreach(println(_))
	  println("=== End ===")
	}
  }
  
  
  def messageHandled(m: CallGraphMessage[_]): Unit = {
        trace(1,">> ",m)
        m match {
          case AAToBeExecuted(_) =>
            traceTree
            traceMessages
          case _ =>  
        }
  }
  def messageQueued      (m: CallGraphMessage[_]                 ) = trace(2, "++", m)
  def messageDequeued    (m: CallGraphMessage[_]                 ) = trace(2, "--", m)
  def messageContinuation(m: CallGraphMessage[_], c: Continuation) = trace(2, "**", c)
  def messageAwaiting: Unit = {traceTree; traceMessages}
}