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

package subscript.swing
import scala.swing._
import scala.swing.event._
import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm._

abstract class SimpleSubscriptApplication extends SimpleSwingApplication{
  override def startup(args: Array[String]) {
    super.startup(args)
    new Thread{override def run={live;quit}}.start()
  }
  def _live: N_call => Unit
  def  live: ScriptExecuter
}
object Scripts {

  def gui [N<:CallGraphNodeTrait[_]] = (there:N)=>gui1(there)  
  def gui1[N<:CallGraphNodeTrait[_]](implicit n:N) = {
    n.adaptExecuter(new SwingCodeExecuterAdapter[CodeExecuterTrait])
  }             

  // an extension on scala.swing.Reactor that supports event handling scripts in Subscript
  abstract class ScriptReactor[N<:N_atomic_action_eh[N]] extends Reactor {
    def publisher:Publisher
    var executer: EventHandlingCodeFragmentExecuter[N] = _
    def execute = executeMatching(true)
    def executeMatching(isMatching: Boolean): Unit = executer.executeMatching(isMatching)
    val publisher1 = publisher // needed in subclass since publisher does not seem to be accessible
    private var myEnabled = false
    def enabled = myEnabled
    def enabled_=(b:Boolean) = {myEnabled=b}
    
    val event: Event
    def reaction: PartialFunction[Event,Unit] = myReaction
    private val myReaction: PartialFunction[Event,Unit] = {case event => execute}
    
    def subscribe(n: N): Unit = {
      executer = new EventHandlingCodeFragmentExecuter(n, n.scriptExecuter)
      n.codeExecuter = executer
      val wasAlreadyEnabled = enabled
      publisher.reactions += reaction;
      if (!wasAlreadyEnabled) {enabled=true}
    }
    def unsubscribe: Unit = {
      publisher.reactions -= reaction
      if (!publisher.reactions.isDefinedAt(event)) {enabled=false}
    }
  }
  
  // a ScriptReactor that has a Component as a Publisher. Automatically enables and disables the component
  abstract class ComponentScriptReactor[N<:N_atomic_action_eh[N]](publisher:Publisher with Component, autoEnableComponent: Boolean = true) extends ScriptReactor[N] {
    override def enabled_=(b:Boolean) = {
      super.enabled_=(b); 
      if (autoEnableComponent) publisher.enabled = b
    }
  }

  // a ComponentScriptReactor for any events
  case class AnyEventScriptReactor[N<:N_atomic_action_eh[N]](comp:Component) extends ComponentScriptReactor[N](comp) {
    def publisher = comp
    val event: Event = null
    private var myReaction: PartialFunction[Event,Unit] = {case _ => execute}
    override def reaction: PartialFunction[Event,Unit] = myReaction
  }
  
  // a ComponentScriptReactor for clicked events on a button
  case class ClickedScriptReactor[N<:N_atomic_action_eh[N]](b:AbstractButton) extends ComponentScriptReactor[N](b) {
    def publisher = b
    val event: Event = ButtonClicked(b)
  }
  
  // a ScriptReactor for key press events
  case class KeyPressScriptReactor[N<:N_atomic_action_eh[N]](publisher:Publisher, keyCode: FormalConstrainedParameter[Char]) extends ScriptReactor[N] {
    // this does not compile: val event: Event = KeyPressed(comp, _, _, _, _)
    val event = null
    override def reaction = myReaction
    private val myReaction: PartialFunction[Event,Unit] = {
      case KeyPressed(comp, keyPressedValue, keyModifiers, keyLocationValue) => 
        if (keyPressedValue.id < 256) {
          val c = keyPressedValue.id.asInstanceOf[Char]
	      if (keyCode.matches(c)) {
	        keyCode.value = c
	        executeMatching(true)
	      }
        }
    }
    override def unsubscribe: Unit = {
      publisher1.reactions -= reaction
      //if (!publisher1.reactions.isDefinedAt(KeyPressed(comp, _, _, _))) {enabled=false}
    }
  }
  
  // a ScriptReactor for virtual key press events
  case class VKeyPressScriptReactor[N<:N_atomic_action_eh[N]](publisher:Publisher, keyValue: FormalConstrainedParameter[Key.Value]) extends ScriptReactor[N] {
    // this does not compile: val event: Event = KeyPressed(comp, _, _, _, _)
    val event = null
    override def reaction = myReaction
    private val myReaction: PartialFunction[Event,Unit] = {
      case KeyPressed(comp, keyPressedValue, keyModifiers, keyLocationValue) => 
        if (keyValue.matches(keyPressedValue)) {
          keyValue.value = keyPressedValue
          executeMatching(true)
        }
    }
    override def unsubscribe: Unit = {
      publisher1.reactions -= reaction
      //if (!publisher1.reactions.isDefinedAt(KeyPressed(comp, _, _, _))) {enabled=false}
    }
  }
  
/* the following subscript code has manually been compiled into Scala; see below
  // the redirections to the swing thread are needed because enabling and disabling the button etc must there be done
 scripts
  implicit clicked(b:Button) = val r = ClickedScriptReactor(b)
                               @gui: @r.subscribe(there); there.onDeactivate{()=>r.unsubscribe}: {. .}
    
  implicit key(comp: Component, keyCode: Char??) = val r = KeyPressScriptReactor(comp, keyCode)
                                                   @r.subscribe(there); there.onDeactivate{()=>r.unsubscribe}: {. .}
 
  implicit vkey(comp: Component, keyValue: Key.Value??) = val r = KeyPressScriptReactor(comp, keyValue)
                                                          @r.subscribe(there); there.onDeactivate{()=>r.unsubscribe}: {. .}
                                                          
  anyEvent(comp: Component) = val r = AnyEventReactor; @r.subscribe(there); there.onDeactivate{()=>r.unsubscribe}: {. .}                                                    
 
  guard(comp: Component, test: => Boolean) = ..anyEvent(comp); if (!test.apply) (-)


 Note: the manual compilation yielded for the first annotation the type
  
   N_annotation[N_annotation[N_code_eh]]
   
 All the complicated generic type parameters on TemplateNodes and CallGraphNodes were needed
 to make it easy enforceable that "there" and even "there.there" would be of the proper type
*/
  
  implicit def _clicked(_b:FormalInputParameter[Button])  = {
   val _r = _declare[ClickedScriptReactor[N_code_eh]]('r)
   _script('clicked, _b~'b) {
    _seq( 
         _val(_r, (here:N_localvar[_]) => new ClickedScriptReactor[N_code_eh](_b.value)),
         _at{gui} (_at{(there:N_code_eh) => {_r.at(there).value.subscribe(there); there.onDeactivate{()=>_r.at(there).value.unsubscribe}}}
                      (_eventhandling{}) //{println("\nCLICKED!!!")} // Temporary tracing
       )          )
   } 
  }
               
  implicit def _key(_publisher: FormalInputParameter[Publisher], _keyCode: FormalConstrainedParameter[Char])  = {
   val _r = _declare[KeyPressScriptReactor[N_code_eh]]('r)
   _script('key, _publisher~'publisher, _keyCode~??'keyCode) {
    _seq( 
         _val(_r, (here:N_localvar[_]) => new KeyPressScriptReactor[N_code_eh](_publisher.value, _keyCode)),
         _at{(there:N_code_eh) => {_r.at(there).value.subscribe(there); there.onDeactivate{()=>_r.at(there).value.unsubscribe}}}
            (_eventhandling{})//{println("\nKey"+_keyCode.value)} // Temporary tracing
    )
   }
  }
               
 implicit def _vkey(_publisher: FormalInputParameter[Publisher], _keyValue: FormalConstrainedParameter[Key.Value])  = {
   val _r = _declare[VKeyPressScriptReactor[N_code_eh]]('r)
  _script('vkey, _publisher~'publisher, _keyValue~??'keyValue) {
    _seq( 
     _val(_r, (here:N_localvar[_]) => new VKeyPressScriptReactor[N_code_eh](_publisher.value, _keyValue)),
     _at{(there:N_code_eh) => {_r.at(there).value.subscribe(there); there.onDeactivate{()=>_r.at(there).value.unsubscribe}}}
         (_eventhandling{})//{println("\nVKey"+_keyValue.value)} // Temporary tracing
    )
   }
  }
               
 def _anyEvent(_comp: FormalInputParameter[Component])  = {
   val _r = _declare[AnyEventScriptReactor[N_code_eh]]('r)
  _script('anyEvent, _comp~'comp) {
    _seq( 
     _val(_r, (here:N_localvar[_]) => new AnyEventScriptReactor[N_code_eh](_comp.value)),
     _at{(there:N_code_eh) => {_r.at(there).value.subscribe(there); there.onDeactivate{()=>
         _r.at(there).value.
         unsubscribe
       }}}
         (_eventhandling{})
    )
   }
  }

 
 def mytest = true
  implicit def _guard(_comp: FormalInputParameter[Component], _test: FormalInputParameter[()=> Boolean]) = { 
    _script('guard, _comp~'comp, _test~'test) {
      _seq(_seq(_optionalBreak_loop, _anyEvent(_comp.value)), _if((n:N_if) => !_test.value.apply)(_deadlock))
    }
  }

 
  // no bridge methods for implicit scripts
}