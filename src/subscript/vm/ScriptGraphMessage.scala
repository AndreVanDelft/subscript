package subscript.vm
import scala.collection.mutable._

  object ScriptGraphMessage {
      var n = 0
      def nextN = {n+=1; n}
  }
  trait ScriptGraphMessage[N <: CallGraphNodeTrait[_<:TemplateNode]] {
      var priority = 0 // TBD: determine good priority levels
      var id = ScriptGraphMessage.nextN
	  def node: N

	  val className = "%14s".format(getClass.getSimpleName)
      override def toString = id+" "+className+" "+node
  }
  // various kinds of messages sent around in the script call graph
  abstract class ScriptGraphMessageN extends ScriptGraphMessage[CallGraphNodeTrait[_<:TemplateNode]]
  
	case class Activation   (node: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN
	case class Continuation (node: CallGraphTreeNode_n_ary) extends ScriptGraphMessageN {
	  priority = -1
	  var activation: Activation = null
	  var deactivations: List[Deactivation] = Nil
	  var success: Success = null
	  var break: Break = null
	  var aaActivated: AAActivated = null
	  var caActivated: CAActivated = null
	  var aaStarteds : List[AAStarted] = Nil
	  var aaEndeds   : List[AAEnded  ] = Nil
	  var childNode  : CallGraphNodeTrait[_<:TemplateNode] = null
	  
	  override def toString = {
	    var result = super.toString
	    if (activation   !=null) result += " "+activation
	    if (deactivations!=Nil ) result += " "+deactivations
	    if (success      !=null) result += " "+success
	    if (break        !=null) result += " "+break
	    if (aaActivated  !=null) result += " "+aaActivated
	    if (caActivated  !=null) result += " "+caActivated
	    if (aaStarteds   !=Nil ) result += " "+aaStarteds
	    if (aaEndeds     !=Nil ) result += " "+aaEndeds
	    if (childNode    !=null) result += " "+childNode
	    result
	  }
	}
	case class Continuation1    (node: N_1_ary_op) extends ScriptGraphMessageN {priority = -1}
	case class Deactivation     (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode], excluded: Boolean) extends ScriptGraphMessageN {priority = -3}
	case class Suspend          (node: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN
	case class Resume           (node: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN
	case class Exclude          (node: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN
	case class Success          (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode] = null) extends ScriptGraphMessageN
	case class Break            (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode], activationMode: ActivationMode.ActivationModeType) extends ScriptGraphMessageN
	case class AAActivated      (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN
	case class CAActivated      (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN {priority = 2} // for immediate handling
	case class CAActivatedTBD   (node: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN {priority = -10} // for late handling
	case class AAStarted        (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN {priority = 2}
	case class AAEnded          (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends ScriptGraphMessageN {priority = 1}
	case class AAToBeExecuted     [T<:TemplateNodeWithCode[_,R],R](node: CallGraphNodeWithCodeTrait[T,R]) extends ScriptGraphMessage[CallGraphNodeWithCodeTrait[T,R]] {priority = -20}
	case class AAToBeReexecuted   [T<:TemplateNodeWithCode[_,R],R](node: CallGraphNodeWithCodeTrait[T,R]) extends ScriptGraphMessage[CallGraphNodeWithCodeTrait[T,R]] {priority = -30}
	case class AAExecutionFinished[T<:TemplateNodeWithCode[_,R],R](node: CallGraphNodeWithCodeTrait[T,R]) extends ScriptGraphMessage[CallGraphNodeWithCodeTrait[T,R]] {priority = -1}
 
