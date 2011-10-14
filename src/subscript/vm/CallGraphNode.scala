package subscript.vm

import scala.collection.mutable._
import subscript._

object ActivationMode extends Enumeration {
  type ActivationModeType = Value
  val Active, Optional, Inactive = Value
}

object OnActivate
object OnActivateOrResume
object OnDeactivate
object OnDeactivateOrSuspend

trait CallGraphNodeTrait[+T<:TemplateNode] {
  var hasSuccess = false
  var isExcluded = false
  var pass = 0
  def template: T
  def n_ary_op_else_ancestor: N_n_ary_op
  def lowestSingleCommonAncestor: CallGraphParentNodeTrait[_<:TemplateNode]
  def forEachParent(n: CallGraphParentNodeTrait[_<:TemplateNode] => Unit): Unit
//  def getParameterLookup: Map[Symbol,ActualParameter[_<:Any]] = null
//  def getParameter[P](formalName: Symbol): ActualParameter[_<:Any] = getParameterLookup(formalName)

  val index = CallGraphNode.nextIndex()
  var stamp = 0
  var aaStartedCount = 0
  var properties: Map[Any,Any] = new HashMap[Any,Any]
  var scriptExecuter: ScriptExecuter = null
  
  def getCodeProperty(key: Any): ()=>Unit = {
    properties.get(key) match {
      case None => null
      case Some(cp) => cp.asInstanceOf[()=>Unit]
    }
  }
  def setCodeProperty(key: Any, c: ()=>Unit) = {
    properties += key->c
  }
  def onActivate             : ()=>Unit  = getCodeProperty(OnActivate              )
  def onActivateOrResume     : ()=>Unit  = getCodeProperty(OnActivateOrResume      )
  def onDeactivate           : ()=>Unit  = getCodeProperty(OnDeactivate            )
  def onDeactivateOrSuspend  : ()=>Unit  = getCodeProperty(OnDeactivateOrSuspend   )
  def onActivate           (c: ()=>Unit) = setCodeProperty(OnActivate           , c)
  def onActivateOrResume   (c: ()=>Unit) = setCodeProperty(OnActivateOrResume   , c)
  def onDeactivate         (c: ()=>Unit) = setCodeProperty(OnDeactivate         , c)
  def onDeactivateOrSuspend(c: ()=>Unit) = setCodeProperty(OnDeactivateOrSuspend, c)

  def asynchronousAllowed: Boolean = false
  var codeExecuter: CodeExecuter = null
  def adaptExecuter(ca: CodeExecuterAdapter[CodeExecuter]): Unit = {codeExecuter = ca.adapt(codeExecuter);}
  var aChildHadFailure = false

  override def toString = index+" "+template

  // TBD: are these necessary? :
  //var aaStartedCountAtLastSuccess = 0
  //def recentSuccess = aaStartedCountAtLastSuccess==aaStartedCount
}

// a non-leaf node:
trait CallGraphParentNodeTrait[+T<:TemplateNode] extends CallGraphNodeTrait[T] {
  val children = new ListBuffer[CallGraphNodeTrait[_<:TemplateNode]]
  def forEachChild(task: (CallGraphNodeTrait[_<:TemplateNode]) => Unit): Unit = {
    children.foreach(task(_))
  }
}
// a node having code; probably to be dropped since @annotations: may attach code to any kind of node
trait CallGraphNodeWithCodeTrait[T<:TemplateNodeWithCode[_,R],R] extends CallGraphNodeTrait[T] {
  // var codeExecuter: AACodeFragmentExecuter    overriding not possible?
}

// should this not be an abstract class?
trait CallGraphNode[+T<:TemplateNode] extends CallGraphNodeTrait[T] {
  
  def n_ary_op_else_ancestor: N_n_ary_op = {
    this match {
      case n:N_n_ary_op => n
      case _            => n_ary_op_ancestor
    }
  }
  // answer the n_ary_op ancestor in case there is one and the path leading thereto does not branch
  def n_ary_op_ancestor: N_n_ary_op
  def getLogicalKind_n_ary_op_ancestor: LogicalKind.LogicalKindType = {
    val a = n_ary_op_ancestor
    if (a==null) return null
    a.getLogicalKind
  }
}


// a node that may have at most 1 parent; always used, except for rendez-vous style communication
// should this not be an abstract class?
trait CallGraphTreeNode   [+T<:TemplateNode] extends CallGraphNode[T] {
  var parent: CallGraphParentNodeTrait[_<:TemplateNode] = null
  // answer the n_ary_op ancestor in case there is one and the path leading thereto does not branch
  def n_ary_op_ancestor = parent.n_ary_op_else_ancestor
  def lowestSingleCommonAncestor = parent
  def forEachParent(task: CallGraphParentNodeTrait[_<:TemplateNode] => Unit): Unit = {
    if (parent!=null) {
      task(parent)
    }
  }
//  override def getParameterLookup: Map[Symbol,ActualParameter[_<:Any]] = {
//    if (parent==null) null
//    else parent.getParameterLookup
//  }
  def    initLocalVariable[T<:Any](name: Symbol, value: T)                       = CallGraphNode.upInGraphToNAry(this, 0     ).   initLocalVariable_pass(name, pass, value)
  def     getLocalVariable[T<:Any](name: Symbol, stepsUp: Int): LocalVariable[T] = CallGraphNode.upInGraphToNAry(this,stepsUp).    getLocalVariable_pass(name, pass)
  def privateLocalVariable        (name: Symbol, stepsUp: Int)                   = CallGraphNode.upInGraphToNAry(this, 0     ).privateLocalVariable_pass(name, pass,stepsUp)

  def withLocal[T<:Any](name: Symbol, stepsUp: Int = 0, task: LocalVariable[T] => Unit) = {
    task.apply(getLocalVariable[T](name, stepsUp))
  }
}
// a node that may have multiple parents; used for rendez-vous style communication
// should this not be an abstract class?
trait CallGraphNonTreeNode[+T<:TemplateNode] extends CallGraphNode[T] {
  val parents = new LinkedList[CallGraphParentNodeTrait[_<:TemplateNode]]
  var lowestSingleCommonAncestor: CallGraphParentNodeTrait[_<:TemplateNode] = null

  // answer the n_ary_op ancestor in case there is one and the path leading thereto does not branch
  def n_ary_op_ancestor: N_n_ary_op = null
  def forEachParent(task: CallGraphParentNodeTrait[_<:TemplateNode] => Unit): Unit = {
    parents.foreach(task(_))
  }
}

trait CallGraphLeafNode         [+T<:TemplateNode] extends CallGraphTreeNode   [T] {var queue: Buffer[CallGraphNodeTrait[_<:TemplateNode]] = null}
trait CallGraphTreeParentNode   [+T<:TemplateNode] extends CallGraphTreeNode   [T] with CallGraphParentNodeTrait[T] {}
trait CallGraphNonTreeParentNode[+T<:TemplateNode] extends CallGraphNonTreeNode[T] with CallGraphParentNodeTrait[T] {}


abstract class N_atomic_action[N<:N_atomic_action[N]](template: T_0_ary_code[N]) 
       extends CallGraphLeafNode [T_0_ary_code[N]] 
          with CallGraphNodeWithCodeTrait[T_0_ary_code[N], Unit] {
  override def asynchronousAllowed: Boolean = true
}
  // TBD keep track of state of N_atomic_action in order to be able to decide on what to do when
  // - code execution started or ended
  // - aa started or ended or endedOptionally
  // - suspend/resume/exclude
abstract class N_atomic_action_eh[N<:N_atomic_action[N]](template: T_0_ary_code[N]) extends N_atomic_action(template)

abstract class CallGraphTreeNode_n_ary extends CallGraphTreeParentNode[T_n_ary] {
  var isIteration      = false
  var hadBreak         = false
  var activationMode = ActivationMode.Active
  def getLogicalKind = T_n_ary.getLogicalKind(template)
  var continuation: Continuation = null
  var lastActivatedChild: CallGraphNodeTrait[_<:TemplateNode] = null
  var aaStartedSinceLastOptionalBreak = false
  // TBD keep track of state in order to be able to decide on what to do when
  // - aa activated, started or ended
  // - suspend/resume/exclusion
  // - child succeeded
  // - child deactivated
}

case class N_code_normal   (template: T_0_ary_code[N_code_normal  ]) extends N_atomic_action   [N_code_normal  ](template)
case class N_code_tiny     (template: T_0_ary_code[N_code_tiny    ]) extends N_atomic_action   [N_code_tiny    ](template) // not 100% appropriate
case class N_code_threaded (template: T_0_ary_code[N_code_threaded]) extends N_atomic_action   [N_code_threaded](template)
case class N_code_unsure   (template: T_0_ary_code[N_code_unsure  ]) extends N_atomic_action   [N_code_unsure  ](template)
case class N_code_eh       (template: T_0_ary_code[N_code_eh      ]) extends N_atomic_action_eh[N_code_eh      ](template)
case class N_code_eh_loop  (template: T_0_ary_code[N_code_eh_loop ]) extends N_atomic_action_eh[N_code_eh_loop ](template)
case class N_localvar      (template: T_0_ary_code[N_localvar     ]) extends N_atomic_action   [N_localvar     ](template)
case class N_privatevar    (template: T_0_ary_code[N_privatevar   ]) extends N_atomic_action   [N_privatevar   ](template)
case class N_localvar_loop (template: T_0_ary_code[N_localvar_loop]) extends N_atomic_action   [N_localvar_loop](template)
case class N_while         (template: T_0_ary_test[N_while        ]) extends CallGraphLeafNode  [T_0_ary_test[N_while]] with CallGraphNodeWithCodeTrait[T_0_ary_test[N_while], Boolean]
case class N_break         (template: T_0_ary     ) extends CallGraphLeafNode  [T_0_ary]
case class N_optional_break(template: T_0_ary     ) extends CallGraphLeafNode  [T_0_ary]
case class N_optional_break_loop
                           (template: T_0_ary     ) extends CallGraphLeafNode  [T_0_ary]
case class N_delta         (template: T_0_ary     ) extends CallGraphLeafNode  [T_0_ary]
case class N_epsilon       (template: T_0_ary     ) extends CallGraphLeafNode  [T_0_ary]
case class N_nu            (template: T_0_ary     ) extends CallGraphLeafNode  [T_0_ary]
case class N_loop          (template: T_0_ary     ) extends CallGraphLeafNode  [T_0_ary]
case class N_1_ary_op      (template: T_1_ary     ) extends CallGraphTreeParentNode[T_1_ary] {
  var continuation: Continuation1 = null
}
case class N_if            (template: T_1_ary_test[N_if        ]) extends CallGraphTreeParentNode[T_1_ary_test[N_if        ]] with CallGraphNodeWithCodeTrait[T_1_ary_test[N_if        ], Boolean]
case class N_if_else       (template: T_2_ary_test[N_if_else   ]) extends CallGraphTreeParentNode[T_2_ary_test[N_if_else   ]] with CallGraphNodeWithCodeTrait[T_2_ary_test[N_if_else   ], Boolean]
case class N_launch        (template: T_1_ary     ) extends CallGraphLeafNode      [T_1_ary]

case class N_annotation[N<:CallGraphNodeTrait[_]] (template: T_1_ary_code[N_annotation[N]]) extends 
   CallGraphTreeParentNode[T_1_ary_code[N_annotation[N]]] with CallGraphNodeWithCodeTrait[T_1_ary_code[N_annotation[N]], Unit] {def there:N=children.head.asInstanceOf[N]}

// the following 4 types may have multiple children active synchronously
case class N_launch_anchor (template: T_1_ary     ) extends CallGraphTreeParentNode[T_1_ary]
case class N_inline_if     (template: T_2_ary     ) extends CallGraphTreeParentNode[T_2_ary]
case class N_inline_if_else(template: T_3_ary     ) extends CallGraphTreeParentNode[T_3_ary]
case class N_n_ary_op      (template: T_n_ary, isLeftMerge: Boolean) extends CallGraphTreeNode_n_ary {
  val mapNamePassToLocalVariable = new HashMap[(Symbol,Int), LocalVariable[_]]
  def    initLocalVariable_pass[T<:Any](name: Symbol, fromPass: Int, value: T)        = mapNamePassToLocalVariable += ((name,fromPass)->new LocalVariable(name,value))
  def     getLocalVariable_pass[T<:Any](name: Symbol, fromPass: Int):LocalVariable[T] = mapNamePassToLocalVariable.get((name,fromPass)) match {case None=>null case Some(v:T) => v}
  def privateLocalVariable_pass        (name: Symbol, fromPass: Int,stepsUp:Int)      =          initLocalVariable_pass(name,fromPass, getLocalVariable(name, stepsUp).value)
}

case class N_call          (template: T_0_ary_code[N_call]) extends CallGraphTreeParentNode[T_0_ary_code[N_call]] {
  var t_callee: T_script = null
  var actualParameters: scala.collection.immutable.Seq[ActualParameter[_<:Any]] = Nil
  def calls(t: T_script, args: FormalParameter_withName[_]*) = {
    this.t_callee = t
    this.actualParameters = args.toList.map(_.asInstanceOf[ActualParameter[_]])
  }
  def allActualParametersMatch: Boolean = actualParameters.forall {_.matches}
  def transferParameters      : Unit    = actualParameters.foreach{_.transfer}
}

case class N_script    (var template: T_script    ) extends CallGraphTreeParentNode[T_script] {
//  var parameterLookup = new scala.collection.mutable.HashMap[Symbol, ActualParameter[_<:Any]]
//  override def getParameterLookup = parameterLookup
}
case class N_communication(var template: T_script) extends CallGraphNonTreeParentNode[T_script] {
//  var parameterLookup = new scala.collection.mutable.HashMap[Symbol, ActualParameter[_<:Any]]
//  override def getParameterLookup = parameterLookup
}


// Utility stuff for Script Call Graph Nodes
object CallGraphNode {
  var currentStamp = 0; // used for searching common ancestors
  
  var nCreated = 0
  def nextIndex() = {nCreated = nCreated+1; nCreated}
  def nextStamp() = {currentStamp = currentStamp+1; currentStamp}
  
  // answer the stepsUp'th N_n_ary_op ancestor node
  def upInGraphToNAry(n: CallGraphTreeNode[_<:TemplateNode], stepsUp: Int) : N_n_ary_op = {
    var a = n
    var s = stepsUp
    while (true) {
      a match {
        case nary: N_n_ary_op => if (s==0) return nary else s -= 1
        case _ =>
      }
      a = a.parent.asInstanceOf[CallGraphTreeNode[_<:TemplateNode]]
    }
    return null // dummy exit
  }
  
  // find the lowest launch_anchor common ancestor of a nodes
  //
  def getLowestLaunchAnchorAncestor(n: CallGraphNodeTrait[_]) = 
      getLowestSingleCommonAncestor(n, _.isInstanceOf[N_launch_anchor] )
      
  // find the lowest single common ancestor of a nodes, that fulfills a given condition:
  // easy when there is 0 or 1 parents
  //
  def getLowestSingleCommonAncestor(n: CallGraphNodeTrait[_], condition: (CallGraphNodeTrait[_])=>Boolean): CallGraphParentNodeTrait[_<:TemplateNode] = {
    val lsca = n.lowestSingleCommonAncestor
    if (lsca==null) return null
    if (condition(lsca)) return lsca
    return getLowestSingleCommonAncestor(lsca,condition)
  }
  
  private def stampNodeWithAncestors(n: CallGraphNodeTrait[_<:TemplateNode]): Unit = {
    if (n.stamp==currentStamp) {
      // this one has already been stamped this round, so here branches come together
      // maybe it is the oldest of such nodes thus far; then record it
      if (lowestSingleCommonAncestor==null
      ||  lowestSingleCommonAncestor.index > n.index)
      {
        lowestSingleCommonAncestor = n
      }
    }
    else
    {
      n.stamp = currentStamp;
      n.forEachParent(stampNodeWithAncestors)
    }
  }
  
  private var lowestSingleCommonAncestor: CallGraphNodeTrait[_<:TemplateNode] = null
  
  // find the lowest common ancestor of a collection of nodes:
  // for each node, stamp upwards in the graph; 
  // each time when the current stamp is encountered, that node may be the lowest common ancestor
  // the oldest of such candidates is considered the one.
  //
  // NOTE: this will return a false LCA when the true LCA has multiple paths to the graph source!!!
  private def getLowestSingleCommonAncestor(nodes: List[CallGraphNode[_<:TemplateNode]]): CallGraphNodeTrait[_] = {
    nextStamp() 
    lowestSingleCommonAncestor = null
    nodes.foreach(stampNodeWithAncestors(_))
    return lowestSingleCommonAncestor
  }
}

	  /* for copy/paste convenience, overview of types to match against:
		  node match {
		 	  case n@( N_code_tiny     (_: T_0_ary_code) 
	                 | N_code_normal   (_: T_0_ary_code) 
		 	         | N_code_unsure   (_: T_0_ary_code)
		 	         | N_code_threaded (_: T_0_ary_code)
		 	         | N_code_eh       (_: T_0_ary_code) 
		 	         | N_code_eh_0_more(_: T_0_ary_code) 
		 	         | N_code_eh_1_more(_: T_0_ary_code) 
		 	         | N_code_eh_many  (_: T_0_ary_code)
		 	         | N_break         (_: T_0_ary     )
		 	         | N_optional_break(_: T_0_ary     )
		 	         | N_delta         (_: T_0_ary     )
		 	         | N_epsilon       (_: T_0_ary     )
		 	         | N_nu            (_: T_0_ary     )
		 	         | N_while         (_: T_0_ary_test)
		 	         | N_1_ary_op      (_: T_1_ary     )
		 	         | N_annotation    (_: T_1_ary_code)
		 	         | N_if            (_: T_1_ary_test)
		 	         | N_if_else       (_: T_2_ary_test)
		 	         | N_inline_if     (_: T_2_ary     )
		 	         | N_inline_if_else(_: T_3_ary     )
		 	         | N_n_ary_op      (_: T_n_ary     )
		 	         | N_n_ary_op_par  (_: T_n_ary, _  )
		 	         | N_n_ary_op_seq  (_: T_n_ary     )
		 	         | N_call          (_: T_0_ary_code)
		 	         | N_script        (_: T_script    )) => {}
	      }
	      */	  

