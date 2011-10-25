/*
    This file is part of Subscript - an extension of the Scala language 
                                     with constructs from Process Algebra.

    Subscript is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License and the 
    GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    Subscript consists partly of a "virtual machine". This is a library; 
    Subscript applications may distribute this library under the 
    GNU Lesser General Public License, rather than under the 
    GNU General Public License. This way your applications need not 
    be made Open Source software, in case you don't want to.

    Subscript is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You may have received a copy of the GNU General Public License
    and the GNU Lesser General Public License along with Subscript.
    If not, see <http://www.gnu.org/licenses/>
*/

package subscript.vm
import scala.collection.mutable._

trait ScriptExecuter {
  val anchorNode: N_call
  def hasSucces: Boolean
  def run: ScriptExecuter
  def insert(sga: ScriptGraphMessage[_ <: CallGraphNodeTrait[_<:TemplateNode]])
}

/*
 * TBD:
 * 
 * compiled scripts; links to nary_op + script
 * here-features: hasSuccess, fail, neutral, breakFromLoop, optionalBreakFromLoop
 * optional exit
 * forced deactivate: / || &&
 * 
 * LookupFrame2
 * 
 * compiler
 * 
 * % ! -  ~
 * # #/ #/#/ #% #%#
 * communication
 * networks and pipes
 * exception handling 
 */

class BasicScriptExecuter extends ScriptExecuter {
  
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
	      case p:CallGraphParentNodeTrait[T] => 
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
  
  // send out a success when in an And-like context
  def doNeutral(n: CallGraphNode[_<:TemplateNode]) =
    if (n.getLogicalKind_n_ary_op_ancestor==LogicalKind.And) {
         insert(Success(n))
    }
  // .. ... for and while operate on the closest ancestor node that has an n_ary operator
  def setIteration_n_ary_op_ancestor(n: CallGraphNode[_<:TemplateNode]) = {
    val a = n.n_ary_op_ancestor
    if (a!=null) a.isIteration = true
  }

  // connect a parent and a child node in the graph
  def connect(parentNode: CallGraphParentNodeTrait[_<:TemplateNode], childNode: CallGraphTreeNode[_<:TemplateNode]) {
    childNode.parent = parentNode
    childNode.scriptExecuter = parentNode.scriptExecuter
    parentNode.children.append(childNode)
    if (parentNode.isInstanceOf[CallGraphTreeNode_n_ary]) {
      val p = parentNode.asInstanceOf[CallGraphTreeNode_n_ary]
      p.lastActivatedChild = childNode
    }
    //if (childNode.isInstanceOf[CallGraphTreeNode_n_ary]) {
    //  val p = childNode.asInstanceOf[CallGraphTreeNode_n_ary]
    //  p.lastActivatedChild = childNode
    //}
  }
  // disconnect a child node from its parent
  def disconnect(childNode: CallGraphNodeTrait[_<:TemplateNode]) {
    if (childNode.isInstanceOf[CallGraphTreeNode[_<:TemplateNode]]) {
      val ctn = childNode.asInstanceOf[CallGraphTreeNode[_<:TemplateNode]]
      val parentNode = ctn.parent
      if (parentNode==null) return;
      parentNode.children -= ctn
    }
  }
 

  val ScriptGraphMessageOrdering = 
	  new Ordering[ScriptGraphMessage[_ <: CallGraphNodeTrait[_<:TemplateNode]]] {
    def compare(x: ScriptGraphMessage[_ <: CallGraphNodeTrait[_<:TemplateNode]], 
                y: ScriptGraphMessage[_ <: CallGraphNodeTrait[_<:TemplateNode]]): Int = {
	        val p = x.priority - y.priority
	        if (p != 0) {p} // highest priority first
	        else        {x.node.index - y.node.index}  // oldest nodes first
        }
	}
  
  // TBD: the scriptGraphMessages queue should probably become synchronized
  // Also, it would become faster if it has internally separate queues for each priority level
  val scriptGraphMessages = new PriorityQueue[ScriptGraphMessage[_ <: CallGraphNodeTrait[_<:TemplateNode]]]()(ScriptGraphMessageOrdering)
  
  // insert a message in the queue
  def insert(m: ScriptGraphMessage[_ <: CallGraphNodeTrait[_<:TemplateNode]]) = {
    trace(2,"<< ",m)
    scriptGraphMessages += m
  }
  def dequeue() : ScriptGraphMessage[_<:CallGraphNodeTrait[_<:TemplateNode]]  = scriptGraphMessages.dequeue
  def insertDeactivation(n:CallGraphNodeTrait[_ <: TemplateNode],c:CallGraphNodeTrait[_ <: TemplateNode]) = insert(Deactivation(n, c, false))
  def insertContinuation(message: ScriptGraphMessage[_<:CallGraphNodeTrait[_<:TemplateNode]], child: CallGraphTreeNode[_<:TemplateNode] = null): Unit = {
    val n = message.node.asInstanceOf[CallGraphTreeNode_n_ary]
    var c = n.continuation 
    
    // Continuations are merged with already existing ones
    // TBD: make separate priorities of continuations...
    // e.g. a continuation for AAActivated should not be merged (probably) with one for AAStarted
    if (c==null) {
      c = new Continuation(n)
    }
    if (c.childNode==null) // should be improved
    {
      c.childNode = 
        if (child!=null) child
        else message match {
          case Break(an,c,m) => c
          case Success(an,c) => c
          case _ => message.node
        }
    }
    message match {
      case a@ Activation  (node: CallGraphNodeTrait[_]) => c.activation = a
      case a@Deactivation (node: CallGraphNodeTrait[_],
                          child: CallGraphNodeTrait[_], excluded: Boolean) => c.deactivations ::= a
      case a@Success      (node: CallGraphNodeTrait[_],
                          child: CallGraphNodeTrait[_])  => c.success = a
      case a@Break        (node: CallGraphNodeTrait[_], 
                          child: CallGraphNodeTrait[_], 
                 activationMode: ActivationMode.ActivationModeType)  => c.break = a
      case a@AAActivated  (node: CallGraphNodeTrait[_], 
                          child: CallGraphNodeTrait[_]) =>  c.aaActivated = a
      case a@CAActivated  (node: CallGraphNodeTrait[_], 
                          child: CallGraphNodeTrait[_]) =>  c.caActivated = a
      case a@AAStarted    (node: CallGraphNodeTrait[_], 
                          child: CallGraphNodeTrait[_]) =>  c.aaStarteds ::= a
      case a@AAEnded      (node: CallGraphNodeTrait[_], 
                          child: CallGraphNodeTrait[_]) =>  c.aaEndeds ::= a
    }
    if (n.continuation==null) {
        n.continuation = c
       insert (c)
    }
    else {
      trace (2,"** ",c)
    }
  }
  def insertContinuation1(message: ScriptGraphMessage[_<:CallGraphNodeTrait[_<:TemplateNode]]): Unit = {
    val n = message.node.asInstanceOf[N_1_ary_op]
    var c = n.continuation
    if (c==null) {
      c = new Continuation1(n)
      n.continuation = c
      scriptGraphMessages += c
    }
    scriptGraphMessages += Continuation1(n)
  }
  
  def hasSucces      = rootNode.hasSuccess
  
  var aaStartedCount = 0; // TBD: use for determining success
  val anchorTemplate = new T_call(null)
  val rootTemplate   = new T_1_ary("**", anchorTemplate)
  val rootNode       = new N_launch_anchor(rootTemplate)
  val anchorNode     = new N_call(anchorTemplate)
  rootNode.scriptExecuter = this 
  connect(parentNode = rootNode, childNode = anchorNode)
  //insert(Activation(anchorNode)) 
  def activateFrom(parent: CallGraphParentNodeTrait[_<:TemplateNode], template: TemplateNode, pass: Int = 0): CallGraphTreeNode[_<:TemplateNode] = {
    val n = createNode(template)
    n.pass = pass
    connect(parentNode = parent, childNode = n)
    if (n.isInstanceOf[N_script]) {
      val ns = n.asInstanceOf[N_script]
      val pc = ns.parent.asInstanceOf[N_call]
    }
    insert(Activation(n))
    n
  }

  def createNode(template: TemplateNode): CallGraphTreeNode[_<:TemplateNode] = {
   val result =
    template match {
      case t @ T_0_ary     ("."                  ) => N_optional_break(t)
      case t @ T_0_ary     (".."                 ) => N_optional_break_loop(t)
      case t @ T_0_ary     ("..."                ) => N_loop          (t)
      case t @ T_0_ary     ("break"              ) => N_break         (t)
      case t @ T_0_ary     ("(-)"                ) => N_delta         (t)
      case t @ T_0_ary     ("(+)"                ) => N_epsilon       (t)
      case t @ T_0_ary     ("(+-)"               ) => N_nu            (t)
      case t @ T_call      (              _      ) => N_call          (t)
      case t @ T_0_ary_name("private"   , name   ) => N_privatevar    (t.asInstanceOf[T_0_ary_name[N_privatevar   ]])
      case t @ T_0_ary_local_valueCode(kind,lv:LocalVariable[_],_) => N_localvar    (t,isLoop=kind=="val..."||kind=="var...")
      case t @ T_0_ary_code("{}"        , _      ) => N_code_normal   (t.asInstanceOf[T_0_ary_code[N_code_normal  ]])
      case t @ T_0_ary_code("{??}"      , _      ) => N_code_unsure   (t.asInstanceOf[T_0_ary_code[N_code_unsure  ]])
      case t @ T_0_ary_code("{!!}"      , _      ) => N_code_tiny     (t.asInstanceOf[T_0_ary_code[N_code_tiny    ]])
      case t @ T_0_ary_code("{**}"      , _      ) => N_code_threaded (t.asInstanceOf[T_0_ary_code[N_code_threaded]])
      case t @ T_0_ary_code("{..}"      , _      ) => N_code_eh       (t.asInstanceOf[T_0_ary_code[N_code_eh      ]])
      case t @ T_0_ary_code("{......}"  , _      ) => N_code_eh_loop  (t.asInstanceOf[T_0_ary_code[N_code_eh_loop ]])
      case t @ T_0_ary_test("while"     , _      ) => N_while         (t.asInstanceOf[T_0_ary_test[N_while        ]])
      case t @ T_1_ary     ("*"         , _      ) => N_launch        (t)
      case t @ T_1_ary     ("**"        , _      ) => N_launch_anchor (t)
      case t @ T_1_ary     (kind: String, _      ) => N_1_ary_op      (t)
      case t @ T_annotation(              _, _   ) => N_annotation    (t)
      case t @ T_1_ary_test("if"        , _, _   ) => N_if            (t.asInstanceOf[T_1_ary_test[N_if]])
      case t @ T_2_ary_test("if_else"   , _, _, _) => N_if_else       (t.asInstanceOf[T_2_ary_test[N_if_else]])
      case t @ T_2_ary     ("?"         , _, _   ) => N_inline_if     (t)
      case t @ T_3_ary     ("?:"        , _, _, _) => N_inline_if_else(t)
      case t @ T_n_ary(kind: String, children@ _*) => N_n_ary_op      (t, T_n_ary.isLeftMerge(kind))
      case t @ T_script(kind: String, name: Symbol, 
                        child0: TemplateNode     ) => N_script        (t.asInstanceOf[T_script    ])
      case _ => null 
    }
    result.codeExecuter = defaultCodeFragmentExecuterFor(result)
    result
  }
  def defaultCodeFragmentExecuterFor(node: CallGraphNodeTrait[_<:TemplateNode]): CodeExecuterTrait = {
    node match {
      case n@N_code_normal  (_) => new   NormalCodeFragmentExecuter(n, this)
      case n@N_code_unsure  (_) => new   UnsureCodeFragmentExecuter(n, this)
      case n@N_code_threaded(_) => new ThreadedCodeFragmentExecuter(n, this)
      case _                    => new     TinyCodeExecuter(node, this)
    }
  }
  def executeCode_localvar      (n: N_localvar[_]   ) = executeCode(n, ()=>n.template.code.apply.apply(n))
  def executeCode_call          (n: N_call          ) = {
    var v = executeTemplateCode[N_call,T_call, N_call=>Unit](n)
    v(n)
  }
  def executeCode_annotation[CN<:CallGraphNodeTrait[CT],CT<:TemplateNode](n: N_annotation[CN,CT]) = executeTemplateCode[N_annotation[CN,CT], T_annotation[CN,CT],Unit](n)
  
  def executeTemplateCode[N<:CallGraphNodeWithCodeTrait[T,R],T<:TemplateNodeWithCode[N,R],R](n: N): R = {
    executeCode(n, 
        ()=>n.template.code.apply.apply(n))
  }
  def executeCode[R](n: CallGraphNodeTrait[_], code: =>()=>R): R = {
    n.codeExecuter.doCodeExecution(code)
  }
  def executeCodeIfDefined(n: CallGraphNodeTrait[_], code: =>()=>Unit): Unit = {
    if (code!=null) executeCode(n, code)
  }
  def handleDeactivation(message: Deactivation): Unit = {
       message.node match {
           case n@N_n_ary_op (_: T_n_ary, _  )  => if(message.child!=null) {
                                                     if (!message.child.hasSuccess) {
                                                        n.aChildHadFailure = true
                                                     }
                                                     insertContinuation(message); 
                                                     return}
           case _ => 
      }
      // TBD: node.onDeactivation
      message.node.forEachParent(p => insertDeactivation(p,message.node))
      executeCodeIfDefined(message.node, message.node.onDeactivate)
      executeCodeIfDefined(message.node, message.node.onDeactivateOrSuspend)
      disconnect(childNode = message.node)
  }

  def handleActivation(message: Activation): Unit = {
      executeCodeIfDefined(message.node, message.node.onActivate)
      executeCodeIfDefined(message.node, message.node.onActivateOrResume)
      message.node match {
           //case n@N_root            (t: T_1_ary     ) => activateFrom(n, t.child0)
           case n@N_code_tiny       (t: T_0_ary_code[_])  =>                                    executeTemplateCode[N_code_tiny, T_0_ary_code[N_code_tiny], Unit](n); if (n.hasSuccess) doNeutral(n); insertDeactivation(n,null)
           case n@N_localvar        (t: T_0_ary_local_valueCode[_], isLoop)  => if (isLoop) setIteration_n_ary_op_ancestor(n); 
            val v = executeCode_localvar(n);n.n_ary_op_ancestor.initLocalVariable(t.localVariable.name, n.pass, v); doNeutral(n); insertDeactivation(n,null)
           case n@N_privatevar      (t: T_0_ary_name[_]) => n.n_ary_op_ancestor.initLocalVariable(t.name, n.pass, n.getLocalVariableHolder(t.name).value)
           case n@N_code_normal     (_: T_0_ary_code[_]) => insert(AAActivated(n,null)); insert(AAToBeExecuted(n))
           case n@N_code_unsure     (_: T_0_ary_code[_]) => insert(AAActivated(n,null)); insert(AAToBeExecuted(n))
           case n@N_code_threaded   (_: T_0_ary_code[_]) => insert(AAActivated(n,null)); insert(AAToBeExecuted(n))

           case n@( N_code_eh       (_: T_0_ary_code[_]) 
                  | N_code_eh_loop  (_: T_0_ary_code[_])) => // ehNodesAwaitingExecution.append(n) not used; could be handy for debugging
              
           case n@N_break           (t: T_0_ary        ) => doNeutral(n); insert(Break(n, null, ActivationMode.Inactive)); insertDeactivation(n,null)
           case n@N_optional_break  (t: T_0_ary        ) => doNeutral(n); insert(Break(n, null, ActivationMode.Optional)); insertDeactivation(n,null)
           case n@N_optional_break_loop
                                    (t: T_0_ary        ) => setIteration_n_ary_op_ancestor(n); doNeutral(n); insert(Break(n, null, ActivationMode.Optional)); insertDeactivation(n,null)
           case n@N_loop            (t: T_0_ary        ) => setIteration_n_ary_op_ancestor(n); doNeutral(n); insertDeactivation(n,null)
           case n@N_delta           (t: T_0_ary        ) =>                     insertDeactivation(n,null)
           case n@N_epsilon         (t: T_0_ary        ) => insert(Success(n)); insertDeactivation(n,null)
           case n@N_nu              (t: T_0_ary        ) => doNeutral(n);       insertDeactivation(n,null)
           case n@N_while           (t: T_0_ary_test[_]) => setIteration_n_ary_op_ancestor(n); 
                                                            n.hasSuccess = executeTemplateCode[N_while, T_0_ary_test[N_while], Boolean](n)
                                                            doNeutral(n)
                                                            if (!n.hasSuccess) {
                                                               insert(Break(n, null, ActivationMode.Inactive))
                                                            }
                                                            insertDeactivation(n,null)
                                                            
           case n@N_launch          (t: T_1_ary        ) => activateFrom(CallGraphNode.getLowestLaunchAnchorAncestor(n), t.child0); insertDeactivation(n,null)
           case n@N_launch_anchor   (t: T_1_ary        ) => activateFrom(n, t.child0)
           case n@N_1_ary_op        (t: T_1_ary        ) => activateFrom(n, t.child0); insertContinuation1(message)
           case n@N_annotation      (t: T_annotation[_,_]) => activateFrom(n, t.child0); executeCode_annotation(n)
           case n@N_if              (t: T_1_ary_test[_]) => if (executeTemplateCode[N_if     , T_1_ary_test[N_if     ], Boolean](n)) activateFrom(n, t.child0) else {doNeutral(n)}
           case n@N_if_else         (t: T_2_ary_test[_]) => if (executeTemplateCode[N_if_else, T_2_ary_test[N_if_else], Boolean](n)) activateFrom(n, t.child0) 
                                                                     else  activateFrom(n, t.child1)
           case n@N_inline_if       (t: T_2_ary        ) => activateFrom(n, t.child0)
           case n@N_inline_if_else  (t: T_3_ary        ) => activateFrom(n, t.child0)
           case n@N_n_ary_op        (t: T_n_ary, 
                                         isLeftMerge   ) => val cn = activateFrom(n, t.children.head); if (!isLeftMerge) insertContinuation(message, cn)
           case n@N_call            (t: T_call         ) => executeCode_call(n); activateFrom(n, n.t_callee)  // TBD: insert(CAActivated)+insert(CAActivatedTBD) depending on template
           case n@N_script          (t: T_script       ) => activateFrom(n, t.child0)
      }      
  }
  
  def handleSuccess(message: Success): Unit = {
          message.node match {
               case n@  N_annotation    (_: T_1_ary_code[_]) => {} // onSuccess?
               case n@  N_inline_if     (t: T_2_ary        )  => if (message.child.template==t.child0) {
                                                                              activateFrom(n, t.child1)
                                                                              return
                                                                       }
               case n@  N_inline_if_else(t: T_3_ary        )  => if (message.child.template==t.child0) {
                                                                              activateFrom(n, t.child1)
                                                                              return
                                                                       }
               case n@  N_1_ary_op      (t: T_1_ary        )  => if(message.child!=null) {
                                                                   insertContinuation1(message) 
                                                                   return
                                                                 }
               case n@  N_n_ary_op      (_: T_n_ary, _      ) => if(message.child!=null) {
                                                                   insertContinuation(message) 
                                                                   return
                                                                 }
               case n@  N_call          (_: T_call          ) => if (!n.allActualParametersMatch) {return}
                                                                 n.transferParameters
               case _ => {}
          }
          // TBD: node.onSuccess
         message.node.hasSuccess = true
         message.node.forEachParent(p => insert(Success(p, message.node)))
  }
  def handleAAActivated(message: AAActivated): Unit = {
          message.node match {
               case n@  N_1_ary_op      (t: T_1_ary        )  => if(message.child!=null) {
                                                                   insertContinuation1(message)
                                                                   //don't return; just put the continuations in place
                                                                 }
               case n@  N_n_ary_op      (_: T_n_ary, _     )  => if(message.child!=null) {
                                                                   insertContinuation(message)
                                                                   //don't return; just put the continuations in place
                                                                 }
               case _ => 
          }
          message.node.forEachParent(p => insert(AAActivated(p, message.node)))
  }
  // immediate handling of activated communications. 
  // This may be of interest for a "+" operator higher up in the graph
  def handleCAActivated(message: CAActivated): Unit = {
          message.node match {
               case n@  N_1_ary_op      (t: T_1_ary        )  => if(message.child!=null) {
                                                                   insertContinuation1(message)
                                                                   //don't return; just put the continuations in place
                                                                 }
               case n@  N_n_ary_op      (_: T_n_ary, _     )  => if(message.child!=null) {
                                                                   insertContinuation(message)
                                                                   //don't return; just put the continuations in place
                                                                 }
               case _ => 
          }
          message.node.forEachParent(p => insert(CAActivated(p, message.node)))
  }
  // TBD: process all fresh CA nodes to activate prospective communications
  def handleCAActivatedTBD(message: CAActivatedTBD): Unit = {
  }
  def handleAAStarted(message: AAStarted): Unit = {
    message.node.hasSuccess = false
	  message.node match {
               case n@  N_1_ary_op      (t: T_1_ary        )  => if(message.child!=null) {
                                                                   insertContinuation1(message)
                                                                   //don't return; just put the continuations in place
                                                                 }
               case n@  N_n_ary_op      (_: T_n_ary, _      ) => if(message.child!=null) {
                                                                   insertContinuation(message)
                                                                   //don't return; just put the continuations in place
                                                                 }
               case _ => 
	  }
      message.node.forEachParent(p => insert(AAStarted(p, message.node)))
  }
  def handleAAEnded(message: AAEnded): Unit = {
    message.node.hasSuccess = false
	  message.node match {
               case n@  N_1_ary_op      (t: T_1_ary        )  => if(message.child!=null) {
                                                                   insertContinuation1(message)
                                                                   //don't return; just put the continuations in place
                                                                 }
               case n@  N_n_ary_op      (_: T_n_ary, _     )  => if(message.child!=null) {
                                                                   insertContinuation(message)
                                                                   //don't return; just put the continuations in place
                                                                  }
              case _ => 
	  }
      message.node.forEachParent(p => insert(AAEnded(p, message.node)))
  }
  
  def handleBreak(message: Break): Unit = {
      message.node match {
        case nn:CallGraphTreeNode_n_ary =>
	        if (nn.activationMode!=ActivationMode.Inactive) {
	            nn.activationMode = message.activationMode
	        }
	        insertContinuation(message)
        case _ => message.node.forEachParent(p => insert(Break(p, message.node, message.activationMode)))
      }
  }
  
  def error(s: String) {throw new Exception(s)}
  def handleExclude(message: Exclude): Unit = { // TBD: remove messages for the node; interrupt execution
    val n = message.node
    n.isExcluded = true
    if (      n.isInstanceOf[CallGraphTreeParentNode[_<:TemplateNode]]) {
      val p = n.asInstanceOf[CallGraphTreeParentNode[_<:TemplateNode]]
      p.forEachChild(c => insert(Exclude(c)))
      return
    }
    if (       n.isInstanceOf[CallGraphNodeWithCodeTrait[_,_]]) {
      val nc = n.asInstanceOf[CallGraphNodeWithCodeTrait[_,_]]
      if (nc.codeExecuter != null) {
        nc.codeExecuter.interruptAA
      }
    }
    val ln = message.node.asInstanceOf[CallGraphLeafNode[_<:TemplateNode]]
    if (ln!=null) {
      dequeue(ln) // TBD: also for caNodes!!
      insert(Deactivation(ln, null, excluded=true))
    }
    
  }
	                
  def handleContinuation1(message: Continuation1): Unit = {
    val n = message.node.asInstanceOf[N_1_ary_op]
    n.continuation = null
    // TBD
  }
  
  def handleAAToBeExecuted[T<:TemplateNodeWithCode[_,R],R](message: AAToBeExecuted[T,R]) {
      message.node.codeExecuter.executeAA
  }
  def handleAAToBeReexecuted[T<:TemplateNodeWithCode[_,R],R](message: AAToBeReexecuted[T,R]) {
     insert(AAToBeExecuted(message.node)) // this way, failed {??} code ends up at the back of the queue
  }
  def handleAAExecutionFinished[T<:TemplateNodeWithCode[_,R],R](message: AAExecutionFinished[T,R]) {
     message.node.codeExecuter.afterExecuteAA
  }
  
  def dequeue(n: CallGraphLeafNode[_<:TemplateNode]): Unit = {
    if (n.queue==null) {
      return
    }
    n.queue -= (n)
    n.queue = null
  }
  def enqueue(n: CallGraphLeafNode[_<:TemplateNode], b: Buffer[CallGraphNodeTrait[_<:TemplateNode]]): Unit = {
    b.append(n)
    n.queue = b
  }
  
  // The most complicated method of the Script Executer: determine what an N-ary operator will do
  // after it has received a set of messages.
  // The decision is based on three aspects:
  // - the kind of operator
  // - the state of the node
  // - the received messages
  // 
  def handleContinuation(message: Continuation): Unit = {
    val n = message.node.asInstanceOf[CallGraphTreeNode_n_ary]
    n.continuation = null
    
    if (message.id==140)
    {
      println
    }
    // decide on what to do: 
    // activate next operand and/or have success, suspend, resume, exclude, or deactivate or nothing
    
    // decide on activate next operand
    
    var activateNextOrEnded = false
    var activateNext        = false
    var activationEnded     = false
    var childNode: CallGraphNodeTrait[_<:TemplateNode] = null // may indicate node from which the a message came

    val isSequential = 
     n.template.kind match {
      case ";" | "|;"  | "||;" |  "|;|" => true
      case _ => false
     }

    if (n.activationMode!=ActivationMode.Inactive) {
     n.template.kind match {
      case "%" => val d = message.deactivations; 
                  val b = message.break
                  if (d!=Nil) {
                    activateNextOrEnded = d.size>0 && !d.head.excluded
                    if (activateNextOrEnded) {
                      childNode = d.head.node
                    }
                  }
      case ";" | "|;" 
       | "||;" |  "|;|" => // messy; maybe the outer if on the activationMode should be moved inside the match{}
                         val s = message.success
                         val b = message.break
                         if (s!=null) {
                           activateNextOrEnded = true
                           childNode = s.child
                         }
                         else if (b!=null) {
                           activateNextOrEnded = true // && b.activationMode==ActivationMode.Optional -- not needed because of "if" before the "match"
                           childNode = b.child
                         }
      
                         
      case "+" | "|+" | "|+|" 
                      => val a = message.aaActivated; val c = message.caActivated; val b = message.break
                         activateNextOrEnded = b==null || a!=null || c!=null
                         if (activateNextOrEnded) {
                           childNode = n.lastActivatedChild         
                           //childNode = message.childNode         

                         }
                  
      case kind if (T_n_ary.isLeftMerge(kind)) => 
                         val aa = message.aaActivated
                         val ca = message.caActivated
                         val as = message.aaStarteds
                         val b  = message.break
                         activateNextOrEnded = aa==null && ca==null ||
                                               as!=Nil  && as.exists( (as:AAStarted) => as.node==n.lastActivatedChild )
                         if (b!=null) {
                           // ???
                         }
                         if (activateNextOrEnded) {
                           childNode = n.lastActivatedChild
                         }
                         
      case _          => if (message.activation!=null || message.success!=null || message.deactivations != Nil) {
                           val b  = message.break
                           val as = message.aaStarteds
                           n.aaStartedSinceLastOptionalBreak = n.aaStartedSinceLastOptionalBreak || as!=Nil
                           if (b==null) {
                             if (n.activationMode==ActivationMode.Optional) {
                                 activateNextOrEnded = n.aaStartedSinceLastOptionalBreak
                                 if (activateNextOrEnded) {
                                   n.activationMode = ActivationMode.Active
                                   n.aaStartedSinceLastOptionalBreak = false
                                   childNode = n.lastActivatedChild
                                 }
                             }
                             else {
                               activateNextOrEnded = true
                               childNode = n.lastActivatedChild
                             }
                           }
                         }
      }
    }
	var nextActivationTemplateIndex = 0
	var nextActivationPass = 0
	if (activateNextOrEnded) {
	  // old: childNode = if (T_n_ary.isLeftMerge(n.template.kind)) n.lastActivatedChild else message.childNode ; now done before
	  nextActivationTemplateIndex = childNode.template.indexAsChild+1
	  var nextActivationPass = childNode.pass 
	  
	  message.node.activationMode = ActivationMode.Active
	  if (nextActivationTemplateIndex==message.node.template.children.size) {
	    if (message.node.isIteration) {
	      nextActivationTemplateIndex = 0
	      nextActivationPass += 1
	      activateNext = true
	    }
	    else {
	      activationEnded = true
	    }
	  }
	  else {
	    activateNext = true
	  }  
	  if (activationEnded) {
	    n.activationMode = ActivationMode.Inactive
	  }
    }
    
    // decide on exclusions and suspensions; deciding on exclusions must be done before activating next operands, of course
    var nodesToBeExcluded : Buffer[CallGraphNodeTrait[_ <:TemplateNode]] = null
    var nodesToBeSuspended: Buffer[CallGraphNodeTrait[_ <:TemplateNode]] = null
    n.template.kind match {
      case ";" | "|;" 
       | "||;" |  "|;|" 
       | "+"   | "|+"  => if (message.aaStarteds!=Nil) {
                            nodesToBeExcluded = n.children -- message.aaStarteds.map( (as:AAStarted) => as.child)
                          }
                  
      case "/" | "|/" 
        | "|/|"        => if (message.aaStarteds!=Nil) { // deactivate to the left when one has started
                            val minIndex = message.aaStarteds.map(_.child.index).min
                            nodesToBeExcluded = n.children.filter(_.index < minIndex)
                          }
                          else {
                            // deactivate to the right when one has finished successfully
                            message.deactivations match {
                              case d::tail => if (d.child.hasSuccess && !d.excluded) {
                                nodesToBeExcluded = n.children.filter(_.index>d.child.index)
                              }
                              case _ =>
                            }
                          }
                  
      case "&&"  | "||" 
         | "&&:" | "||:" => val isLogicalOr = T_n_ary.getLogicalKind(n.template.kind)==LogicalKind.Or
                            val consideredNodes = message.deactivations.map(_.child).filter(
                               (c: CallGraphNodeTrait[_ <:TemplateNode]) => c.hasSuccess==isLogicalOr)
                            if (consideredNodes!=Nil) {
                              nodesToBeExcluded = n.children -- consideredNodes
                            }
      case "&"  | "|" 
         | "&:" | "|:" =>          
    }
    if (T_n_ary.isSuspending(n.template) && !message.aaStarteds.isEmpty) {
      val s = message.aaStarteds.head.node
      if (s.aaStartedCount==1) {
        n.template.kind match {
          case "#" | "#%#"  => nodesToBeSuspended = n.children - s 
          case "#%"         => nodesToBeExcluded  = n.children.filter(_.index < s.index) 
                               nodesToBeSuspended = n.children.filter(_.index > s.index)
          case "#/" | "#/#/" => nodesToBeSuspended = n.children.filter(_.index < s.index) 
        }
      }
    }
    var shouldSucceed = false    
    // decide further on success and resumptions
    if (!shouldSucceed) { // could already have been set for .. as child of ;
      
      // TBD: improve
      if (activateNextOrEnded || message.success != null) {
        var nodesToBeResumed: Buffer[CallGraphNodeTrait[_ <:TemplateNode]] = null
        if (message.success != null || message.aaEndeds != Nil) {
          T_n_ary.getLogicalKind(n.template.kind) match {
            case LogicalKind.None =>
            case LogicalKind.And  => shouldSucceed = (isSequential || !n.aChildHadFailure) &&
                                                     n.children.forall((e:CallGraphNodeTrait[_])=>e.hasSuccess)
            case LogicalKind.Or   => shouldSucceed = n.children.exists(_.hasSuccess)
          }
        }
      }
	}
    if (shouldSucceed && (
        !isSequential   ||   // TBD: check for other Sequential operators";"
        activationEnded || // reached the end
        message.break != null // succeed on sequential breaks, including the optional ones
      ) ) {
      insert(Success(n)) // TBD: prevent multiple successes at same "time"
    }
    // do exclusions
    if (nodesToBeExcluded!=null) {
      nodesToBeExcluded.foreach((n) => insert(Exclude(n)))
    }  
    // do activation    
    
    if (activateNext) {
      val t = message.node.template.children(nextActivationTemplateIndex)
      activateFrom(message.node, t, nextActivationPass)
    }
    else if (n.children.isEmpty) {
      insertDeactivation(n, null)
    }
      
    // decide on deactivation of n
    
  }
  
  // message dispatcher; not really OO, but all real activity should be at the executers; other things should be passive
  def handle(message: ScriptGraphMessage[_]):Unit = {
    message match {
      case a@ Activation        (_) => handleActivation   (a)
      case a@Continuation       (_) => handleContinuation (a)
      case a@Continuation1      (_) => handleContinuation1(a)
      case a@Deactivation  (_,_, _) => handleDeactivation (a)
      case a@Suspend            (_) => {}
      case a@Resume             (_) => {}
      case a@Exclude            (_) => handleExclude    (a)
      case a@Success          (_,_) => handleSuccess    (a)
      case a@Break        (_, _, _) => handleBreak      (a)
      case a@AAActivated      (_,_) => handleAAActivated(a)
      case a@CAActivated      (_,_) => handleCAActivated(a)
      case a@CAActivatedTBD     (_) => handleCAActivatedTBD(a)
      case a@AAStarted        (_,_) => handleAAStarted  (a)
      case a@AAEnded          (_,_) => handleAAEnded    (a)
      case a@AAExecutionFinished(_) => handleAAExecutionFinished(a)
      case a@AAToBeReexecuted   (_) => handleAAToBeReexecuted   (a)
      case a@AAToBeExecuted     (_) => handleAAToBeExecuted     (a)
    }
  }
  
  // Main method of BasicExecutioner
  def run: ScriptExecuter = {
    activateFrom(anchorNode, anchorNode.t_callee)
    var isActive = true
    while (isActive) { // main execution loop
      
      if (!scriptGraphMessages.isEmpty) {
        val m = scriptGraphMessages.dequeue
        trace(1,">> ",m)
        m match {
          case AAToBeExecuted(_) =>
            traceTree
            traceMessages
          case _ =>  
        }
        handle(m)
      }
      else if (!rootNode.children.isEmpty) {
        traceTree
        traceMessages
        synchronized { // TBD: there should also be a synchronized call in the CodeExecuters
          if (scriptGraphMessages.isEmpty) // looks stupid, but event may have happened&notify() may have been called during tracing
            synchronized {wait()} // for an event to happen 
        }
        // note: there may also be deadlock because of unmatching communications
        // so there should preferably be a check for the existence of waiting eh actions
      }
      else {
        isActive = false
      }
    }
    this
  }
}
