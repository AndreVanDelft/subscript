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

  object CallGraphMessage {
      var n = 0
      def nextN = {n+=1; n}
  }
  trait CallGraphMessage[N <: CallGraphNodeTrait[_<:TemplateNode]] {
      var priority = 0 // TBD: determine good priority levels
      var id = CallGraphMessage.nextN
	  def node: N

	  val className = "%14s".format(getClass.getSimpleName)
      override def toString = id+" "+className+" "+node
  }
  // various kinds of messages sent around in the script call graph
  abstract class CallGraphMessageN extends CallGraphMessage[CallGraphNodeTrait[_<:TemplateNode]]
  
	case class Activation   (node: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 8 }
	case class Continuation (node: CallGraphTreeNode_n_ary) extends CallGraphMessageN {
	  priority = 4
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
	case class Continuation1    (node: N_1_ary_op) extends CallGraphMessageN {priority = 5}
	case class Deactivation     (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode], excluded: Boolean) extends CallGraphMessageN {priority = 6}
	case class Suspend          (node: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 9}
	case class Resume           (node: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 10}
	case class Exclude          (node: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 11}
	case class Success          (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode] = null) extends CallGraphMessageN {priority = 12}
	case class Break            (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode], activationMode: ActivationMode.ActivationModeType) extends CallGraphMessageN {priority = 13}
	case class AAActivated      (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 14}
	case class CAActivated      (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 15} // for immediate handling
	case class CAActivatedTBD   (node: N_call                             ) extends CallGraphMessageN {priority = 2} // for late handling
	case class AAStarted        (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 17}
	case class AAEnded          (node: CallGraphNodeTrait[_<:TemplateNode], 
	                            child: CallGraphNodeTrait[_<:TemplateNode]) extends CallGraphMessageN {priority = 16}
	case class AAToBeExecuted     [T<:TemplateNodeWithCode[_,R],R](node: CallGraphNodeWithCodeTrait[T,R]) extends CallGraphMessage[CallGraphNodeWithCodeTrait[T,R]] {priority = 1}
	case class AAToBeReexecuted   [T<:TemplateNodeWithCode[_,R],R](node: CallGraphNodeWithCodeTrait[T,R]) extends CallGraphMessage[CallGraphNodeWithCodeTrait[T,R]] {priority = 0}
	case class AAExecutionFinished[T<:TemplateNodeWithCode[_,R],R](node: CallGraphNodeWithCodeTrait[T,R]) extends CallGraphMessage[CallGraphNodeWithCodeTrait[T,R]] {priority = 6}
	case object CommunicationMatchingMessage extends CallGraphMessage[CallGraphNodeTrait[TemplateNode]] {
	  priority = 3 
	  def node:CallGraphNodeTrait[TemplateNode] = null
	  def activatedCommunicationCalls = scala.collection.mutable.ArrayBuffer.empty[N_call]
	}
	// TBD: AAActivated etc to inherit from 1 trait; params: 1 node, many children
	// adjust insert method
	// CommunicationMatching should have Set[CommunicationRelation] (?), and have List[Communicators]
	// timestamp of Communicators should be determined by timestamp of newest N_call node
	//
	//
	// Prolog/Linda style question: 
	// can we test all possible communications of a certain type?
	// i.e. 
	// ->..?p:Int? loops and matches all sent integers like <-*1  <-*3
	
