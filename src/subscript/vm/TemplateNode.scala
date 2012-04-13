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

// Template Nodes are used to describe abstract syntax trees of the compiled scripts

trait TemplateNode { 
  def kind: String
  var indexAsChild: Int = 0
  override def toString = kind
  def children: Iterable[TemplateNode]}
trait TemplateNode_0_Trait extends TemplateNode         {                          override def children: Iterable[TemplateNode] = Nil}
trait TemplateNode_1_Trait extends TemplateNode_0_Trait {def child0: TemplateNode; override def children: Iterable[TemplateNode] = child0::Nil}
trait TemplateNode_2_Trait extends TemplateNode_1_Trait {def child1: TemplateNode; override def children: Iterable[TemplateNode] = child0::child1::Nil}
trait TemplateNode_3_Trait extends TemplateNode_2_Trait {def child2: TemplateNode; override def children: Iterable[TemplateNode] = child0::child1::child2::Nil}
trait TemplateNode_n_Trait extends TemplateNode   {}

// something having code that accepts a specific kind of Call Graph Node
trait CallGraphNodeCode[N<:CallGraphNodeTrait[TemplateNode], +R] {
  val code: () => N => R;
  def execute(here: N): R = code().apply(here)
}
trait TemplateNodeWithCode   [N<:CallGraphNodeTrait[TemplateNode], R] extends TemplateNode with CallGraphNodeCode[N, R]
trait TemplateNode_0_WithCode[N<:CallGraphNodeTrait[TemplateNode], R] extends TemplateNodeWithCode[N, R] with TemplateNode_0_Trait
trait TemplateNode_1_WithCode[N<:CallGraphNodeTrait[TemplateNode], R] extends TemplateNodeWithCode[N, R] with TemplateNode_1_Trait
trait TemplateNode_2_WithCode[N<:CallGraphNodeTrait[TemplateNode], R] extends TemplateNodeWithCode[N, R] with TemplateNode_2_Trait

// all concrete template node case classes 

case class T_script (kind: String, name: Symbol, child0: TemplateNode) extends TemplateNode_1_Trait {
  override def toString = super.toString+" "+name.name
}
case class T_commscript(kind: String, communicator: Communicator) extends TemplateNode_0_Trait {
  override def toString = super.toString+" "+communicator.name.name
}
case class T_communication(kind: String, names: Seq[Symbol]) extends TemplateNode_0_Trait {
  override def toString = super.toString+" "+names.mkString(",")
}

case class T_0_ary (kind: String)                                                                           extends TemplateNode_0_Trait
case class T_1_ary (kind: String, child0: TemplateNode)                                                     extends TemplateNode_1_Trait
case class T_2_ary (kind: String, child0: TemplateNode, var child1: TemplateNode)                           extends TemplateNode_2_Trait {child1.indexAsChild = 1}
case class T_3_ary (kind: String, child0: TemplateNode, var child1: TemplateNode, var child2: TemplateNode) extends TemplateNode_3_Trait {child1.indexAsChild = 1; child1.indexAsChild = 2}
case class T_n_ary (kind: String, override val children: TemplateNode*)                                     extends TemplateNode_n_Trait {
  {
    var i = 0; children.foreach{c=>c.indexAsChild = i; i=i+1}
  }
}
case class T_annotation[CN<:CallGraphNodeTrait[CT],CT<:TemplateNode](code: () => N_annotation[CN,CT] => Unit, child0: TemplateNode) extends TemplateNode_1_WithCode[N_annotation[CN,CT], Unit] {def kind = "@:"}
case class T_call                                                   (code: () => N_call    => N_call => Unit                      ) extends TemplateNode_0_WithCode[N_call   , N_call => Unit] {def kind = "call"}

case class T_0_ary_name           [N<:CallGraphNodeTrait[TemplateNode]] (kind: String, name: Symbol)                                              extends TemplateNode_0_Trait
case class T_0_ary_local_valueCode[V<:Any] (kind: String, localVariable: LocalVariable[V], code: () => N_localvar[_]=>V)                          extends TemplateNode_0_WithCode[N_localvar[_], V]
case class T_0_ary_code[N<:CallGraphNodeTrait[TemplateNode]] (kind: String, code: () => N => Unit)                                                extends TemplateNode_0_WithCode[N, Unit]
case class T_1_ary_code[N<:CallGraphNodeTrait[TemplateNode]] (kind: String, code: () => N => Unit, child0: TemplateNode)                          extends TemplateNode_1_WithCode[N, Unit]
case class T_2_ary_code[N<:CallGraphNodeTrait[TemplateNode]] (kind: String, code: () => N => Unit, child0: TemplateNode, child1: TemplateNode)    extends TemplateNode_2_WithCode[N, Unit]  {child1.indexAsChild = 1}

case class T_0_ary_test[N<:CallGraphNodeTrait[TemplateNode]] (kind: String, code: () => N => Boolean)                                             extends TemplateNode_0_WithCode[N, Boolean]
case class T_1_ary_test[N<:CallGraphNodeTrait[TemplateNode]] (kind: String, code: () => N => Boolean, child0: TemplateNode)                       extends TemplateNode_1_WithCode[N, Boolean]
case class T_2_ary_test[N<:CallGraphNodeTrait[TemplateNode]] (kind: String, code: () => N => Boolean, child0: TemplateNode, child1: TemplateNode) extends TemplateNode_2_WithCode[N, Boolean]  {child1.indexAsChild = 1}

// TBD: case class T_match    (code: ScriptNode => Unit, caseParts)    extends TemplateNode;
// TBD: case class T_exception(?)    extends TemplateNode;

// some utility objects

object LogicalKind extends Enumeration {
  type LogicalKindType = Value
  val And, Or, None = Value
}

object ExclusiveKind extends Enumeration {
  type ExclusiveKindType = Value
  val All, LeftOnly, None, Disambiguating_all, Disambiguating_leftOnly = Value
}

object T_n_ary {
  
  def getLogicalKind(t: T_n_ary): LogicalKind.LogicalKindType = getLogicalKind(t.kind)
  def getLogicalKind(kind: String): LogicalKind.LogicalKindType = {
    kind match {
      case ";" | "|;" | "||;" | "|;|" 
         | "&&" | "&" | "&&:" | "&:"
         | "=="  | "<<=="  | "<=="  | "==>>"  | "==>"  | "<==>"  | "<<==>"  | "<==>>"  | "<<==>>"
         | "==:" | "<<==:" | "<==:" | "==>>:" | "==>:" | "<==>:" | "<<==>:" | "<==>>:" | "<<==>>:"
         | "#" | "#/"          => LogicalKind.And
                             
      case "||"  | "|"  
         | "||:" | "|:" 
         | "|+"  | "|/" 
         | "||+" | "||/" 
         | "|+|" | "|/|" 
         | "+"   | "/" | "%" 
         | "#%"  | "#%#"       => LogicalKind.Or                         
      
      case "#/#/"               => LogicalKind.None
      
      case _ => null
    }
  }
  def getExclusiveKind(t: T_n_ary): ExclusiveKind.ExclusiveKindType = getExclusiveKind(t.kind)
  def getExclusiveKind(kind: String): ExclusiveKind.ExclusiveKindType = {
    kind match {
      case ";" | "." | "+"  => ExclusiveKind.All                             
      case "/"              => ExclusiveKind.LeftOnly                             
      case "|;|" | "|+|"    => ExclusiveKind.Disambiguating_all
      case "|/|"            => ExclusiveKind.Disambiguating_leftOnly
      case _                => ExclusiveKind.None
    }
  }
  def isMerge(t: T_n_ary): Boolean = isMerge(t.kind)
  def isMerge(kind: String): Boolean = {
    kind match {
      case "&&" | "&" | "&&:" | "&:"
         | "=="  | "<<=="  | "<=="  | "==>>"  | "==>"  | "<==>"  | "<<==>"  | "<==>>"  | "<<==>>"
         | "==:" | "<<==:" | "<==:" | "==>>:" | "==>:" | "<==>:" | "<<==>:" | "<==>>:" | "<<==>>:"
         | "||"  | "|"  
         | "||:" | "|:"   => true                         
      
      case _ => false
    }
  }
  def isLeftMerge(t: T_n_ary): Boolean = isLeftMerge(t.kind)
  def isLeftMerge(kind: String): Boolean = {
    kind match {
      case "&&:" | "&:"
         | "==:" | "<<==:" | "<==:" | "==>>:" | "==>:" | "<==>:" | "<<==>:" | "<==>>:" | "<<==>>:"
         | "||:" | "|:" => true
      case _            => false
    }
  }
  def isSuspending(t: T_n_ary): Boolean = isSuspending(t.kind)
  def isSuspending(kind: String): Boolean = {
    kind match {
      case "#" 
         | "#%" | "#%#"
         | "#/" | "#/#/" => true
      case _             => false
    }
  }
}