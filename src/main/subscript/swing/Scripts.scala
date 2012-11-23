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
  def  live: ScriptExecutor
}
object Scripts {

  def gui [N<:CallGraphNodeTrait[_]] = (there:N)=>gui1(there)  
  def gui1[N<:CallGraphNodeTrait[_]](implicit n:N) = {
    n.adaptExecutor(new SwingCodeExecutorAdapter[CodeExecutorTrait])
  }             

  object ScriptReactor {
    var scriptExecutorThatConsumedEvent: ScriptExecutor = null // event.consume not available for button clicks; this consumedEvent item is a workaround
  }
  // an extension on scala.swing.Reactor that supports event handling scripts in Subscript
  abstract class ScriptReactor[N<:N_atomic_action_eh[N]] extends Reactor {
    def publisher:Publisher
    var executor: EventHandlingCodeFragmentExecutor[N] = _
    def execute = executeMatching(true)
    def executeMatching(isMatching: Boolean): Unit = executor.executeMatching(isMatching)
    val publisher1 = publisher // needed in subclass since publisher does not seem to be accessible
    private var myEnabled = false
    def enabled = myEnabled
    def enabled_=(b:Boolean) = {myEnabled=b}
    def acknowledgeEventHandled = {ScriptReactor.scriptExecutorThatConsumedEvent = null} // will be done when an Event Handling Code Fragment succeeds, performed by the ScriptExecutor
    
    val listenedEvent: Event
    var currentEvent : Event = null
    def reaction: PartialFunction[Event,Unit] = myReaction
    private val myReaction: PartialFunction[Event,Unit] = {
      case event if (ScriptReactor.scriptExecutorThatConsumedEvent != executor.scriptExecutor) => {
                 execute
                 if (executor.n.hasSuccess) {
                   ScriptReactor.scriptExecutorThatConsumedEvent = executor.scriptExecutor
                   consumeEvent
                 }
               }
    }
    def consumeEvent = {}
    
    def subscribe(n: N): Unit = {
      executor = new EventHandlingCodeFragmentExecutor(n, n.scriptExecutor)
      n.codeExecutor = executor
      val wasAlreadyEnabled = enabled
      publisher.reactions += reaction;
      if (!wasAlreadyEnabled) {enabled=true}
    }
    def canDisableOnUnsubscribe = true
    def unsubscribe: Unit = {
      publisher.reactions -= reaction
      if (canDisableOnUnsubscribe && !publisher.reactions.isDefinedAt(listenedEvent)) {enabled=false}
    }
  }
  
  // a ScriptReactor that has a Component as a Publisher. Automatically enables and disables the component
  abstract class ComponentScriptReactor[N<:N_atomic_action_eh[N]](publisher:Publisher with Component, autoEnableComponent: Boolean = true) extends ScriptReactor[N] {
    override def enabled_=(b:Boolean) = {
      super.enabled_=(b)
      if (autoEnableComponent) publisher.enabled = b
    }
  }

  // a ComponentScriptReactor for any events
  case class AnyEventScriptReactor[N<:N_atomic_action_eh[N]](comp:Component) extends ComponentScriptReactor[N](comp) {
    def publisher = comp
    val listenedEvent: Event = null
    override def canDisableOnUnsubscribe = false
    private var myReaction: PartialFunction[Event,Unit] = {case e => currentEvent=e; execute; currentEvent=null}
    override def reaction: PartialFunction[Event,Unit] = myReaction
  }
  case class WindowClosingScriptReactor[N<:N_atomic_action_eh[N]](w:Window) extends ScriptReactor[N] {
    def publisher = w
    val listenedEvent: WindowClosing = null
    override def canDisableOnUnsubscribe = false
    private var myReaction: PartialFunction[Event,Unit] = {case e: WindowClosing => currentEvent=e; execute; currentEvent=null}
    override def reaction: PartialFunction[Event,Unit] = myReaction
  }
  case class SliderStateChangedScriptReactor[N<:N_atomic_action_eh[N]](s:Slider) extends ScriptReactor[N] {
    def publisher = s
    val listenedEvent: ValueChanged = null
    override def canDisableOnUnsubscribe = false
    private var myReaction: PartialFunction[Event,Unit] = {case e: ValueChanged => currentEvent=e; execute; currentEvent=null}
    override def reaction: PartialFunction[Event,Unit] = myReaction
  }
  case class MousePressedScriptReactor[N<:N_atomic_action_eh[N]](comp:Component) extends ScriptReactor[N] {
    def publisher = comp
    val listenedEvent: MousePressed = null
    override def canDisableOnUnsubscribe = false
    private var myReaction: PartialFunction[Event,Unit] = {case e: MousePressed => 
      currentEvent=e; execute; currentEvent=null}
    override def reaction: PartialFunction[Event,Unit] = myReaction
  }
  case class MouseDraggedScriptReactor[N<:N_atomic_action_eh[N]](comp:Component) extends ScriptReactor[N] {
    def publisher = comp
    val listenedEvent: MouseDragged = null
    override def canDisableOnUnsubscribe = false
    private var myReaction: PartialFunction[Event,Unit] = {case e: MouseDragged =>
      currentEvent=e; execute; currentEvent=null}
    override def reaction: PartialFunction[Event,Unit] = myReaction
  }
  
  // a ComponentScriptReactor for clicked events on a button
  // TBD: a way to consume clicked events on the button
  case class ClickedScriptReactor[N<:N_atomic_action_eh[N]](button:AbstractButton) extends ComponentScriptReactor[N](button) {
    val wasFocusable = button.focusable
    override def enabled_=(b:Boolean) = {
      super.enabled_=(b)
      button.focusable = wasFocusable
      //if (b && wasFocusable) button.requestFocus
    }
    def publisher = button
    //b.peer.addActionListener(new ActionListener {})
    val listenedEvent: Event = ButtonClicked(button)
    override def consumeEvent = {
      listenedEvent match {
        case ie: InputEvent => ie.consume // unfortunately, this is not applicable
        case _ => // no consume event option seems to be available
    } }
  }
  
  // a ScriptReactor for key press events
  case class KeyPressScriptReactor[N<:N_atomic_action_eh[N]](publisher:Publisher, keyCode: FormalConstrainedParameter[Char]) extends ScriptReactor[N] {
    // this does not compile: val event: Event = KeyPressed(comp, _, _, _, _)
    val listenedEvent = null
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
    val listenedEvent = null
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
  event     (r:ScriptReactor)                           =  @gui: @r.subscribe(there); there.onDeactivate{()=>r.unsubscribe};  there.onSuccess{()=>r.acknowledge}: {. .}
  event_loop(r:ScriptReactor, task: MousEvent=>Unit)    =  @gui: @r.subscribe(there); there.onDeactivate{()=>r.unsubscribe};  there.onSuccess{()=>r.acknowledge}: {. .}
  implicit clicked(b:Button)                            = event( ClickedScriptReactor(b))
  implicit  key(comp: Component, keyCode : Char     ??) = event(KeyPressScriptReactor(comp, keyCode ))
  implicit vkey(comp: Component, keyValue: Key.Value??) = event(KeyPressScriptReactor(comp, keyValue))
       anyEvent(comp: Component)                        = event(AnyEventScriptReactor(comp))                                                    
 
      windowClosing(_w: Window)  =  {_event(WindowClosingScriptReactor[N_code_eh](_w.value))} }

  implicit def _stateChange (_slider: FormalInputParameter[Slider]) = {_script(this, 'stateChange, _slider~'slider) {_event(SliderStateChangedScriptReactor[N_code_eh](_slider.value))} }

           def _mousePresses(_c: FormalInputParameter[Component], _task: FormalInputParameter[MouseEvent=>Unit]) 
                = {_script(this, '_mousePresses, _c~'c, _task~'task) {_event_loop(AnyEventScriptReactor[N_code_eh_loop](_c.value), _task.value)} }

  guard(comp: Component, test: => Boolean) = ... if (test) (.. anyEvent(comp, =>!test)) else anyEvent(comp, =>test)


 Note: the manual compilation yielded for the first annotation the type
  
   N_annotation[N_annotation[N_code_eh]]
   
 All the complicated generic type parameters on TemplateNodes and CallGraphNodes were needed
 to make it easy enforceable that "there" and even "there.there" would be of the proper type
*/
  
  def _event(_r:FormalInputParameter[ScriptReactor[N_code_eh]])  = {
   _script(this, 'event, _r~'r) {
       _at{gui} (_at{(there:N_code_eh) => {_r.value.subscribe(there); 
                        there.onDeactivate{_r.value.unsubscribe}; 
                        there.onSuccess   {_r.value.acknowledgeEventHandled}}}
         (_eventhandling0{})
       )
   } 
  }
  def _event_loop[E<:Event](_r:FormalInputParameter[ScriptReactor[N_code_eh_loop]], task: E=>Unit)  = {
   _script(this, 'event_loop, _r~'r) {
       _at{gui} (_at{(there:N_code_eh_loop) => {_r.value.subscribe(there); 
                             there.onDeactivate{_r.value.unsubscribe}; 
                             there.onSuccess   {_r.value.acknowledgeEventHandled}}}
         (_eventhandling_loop0{task.apply(_r.value.currentEvent.asInstanceOf[E])})
       )
   } 
  }
  // in principle, _key could call _event, but that is one call level deeper, which is unhandy for the GraphicalScriptDebugger
  // _key0 is a version that calls _event
  // likewise for _clicked and _clicked0
  implicit def  _key(_p: FormalInputParameter[Publisher], _k: FormalConstrainedParameter[Char     ])  = {
     _script(this,  'key, _p~'p, _k~??'k) { 
       _at{gui} (_at{(there:N_code_eh) => {val _r = KeyPressScriptReactor[N_code_eh](_p.value, _k) 
                                             _r.value.subscribe(there); 
                          there.onDeactivate{_r.value.unsubscribe}; 
                          there.onSuccess   {_r.value.acknowledgeEventHandled}}}
         (_eventhandling0{})
      )
    }
  }           
  implicit def  _clicked(_b: FormalInputParameter[Button])  = {
     _script(this,  'clicked, _b~'b) { 
       _at{gui} (_at{(there:N_code_eh) => {val _r = ClickedScriptReactor[N_code_eh](_b.value) 
                                             _r.value.subscribe(there); 
                          there.onDeactivate{_r.value.unsubscribe}; 
                          there.onSuccess   {_r.value.acknowledgeEventHandled}}}
         (_eventhandling0{})
      )
    }
  }           
  implicit def  _key0(_p: FormalInputParameter[Publisher], _k: FormalConstrainedParameter[Char     ])  = {_script(this,  'key, _p~'p, _k~??'k) {_event( KeyPressScriptReactor[N_code_eh](_p.value, _k))}}
  implicit def _vkey (_p: FormalInputParameter[Publisher], _k: FormalConstrainedParameter[Key.Value])  = {_script(this, 'vkey, _p~'p, _k~??'k) {_event(VKeyPressScriptReactor[N_code_eh](_p.value, _k))}}
                
  implicit def _clicked0(_b: FormalInputParameter[Button   ])  = {_script(this,       'clicked, _b~'b) {_event( ClickedScriptReactor[N_code_eh](_b.value))} }
           def _anyEvent(_c: FormalInputParameter[Component])  = {_script(this,      'anyEvent, _c~'c) {_event(AnyEventScriptReactor[N_code_eh](_c.value))} }
      def _windowClosing(_w: FormalInputParameter[Window   ])  = {_script(this, 'windowClosing, _w~'w) {_event(WindowClosingScriptReactor[N_code_eh](_w.value))} }

  implicit def _stateChange (_slider: FormalInputParameter[Slider]) = {_script(this, 'stateChange, _slider~'slider) {_event(SliderStateChangedScriptReactor[N_code_eh](_slider.value))} }

           def _mousePresses(_c: FormalInputParameter[Component], _task: FormalInputParameter[MouseEvent=>Unit]) 
                = {_script(this, '_mousePresses, _c~'c, _task~'task) {_event_loop(MousePressedScriptReactor[N_code_eh_loop](_c.value), 
                                                                              _task.value)
                                                                     } }
           
           def _mouseDraggings(_c: FormalInputParameter[Component], _task: FormalInputParameter[MouseEvent=>Unit]) 
                = {_script(this, 'mouseDraggings, _c~'c, _task~'task) {_event_loop(MouseDraggedScriptReactor[N_code_eh_loop](_c.value), 
                                                                                _task.value)
                                                                      } }
           
  // TBD: work in progress
  //       def _anyEvent(_c: FormalInputParameter[Component], _t: FormalInputParameter[() => Boolean]) = {_script('anyEvent, _c~'c, _t~'t) {_handleEventOn(AnyEventScriptReactor[N_code_eh](_c.value, _t))} }
  def _guard(_comp: FormalInputParameter[Component], _test: FormalInputParameter[()=> Boolean]) = { 
    // if(test) .. else ... anyEvent
    _script(this, 'guard, _comp~'comp, _test~'test) {
      _seq(_if_else((n:N_if_else) => _test.value.apply) (_optionalBreak_loop, _loop), _anyEvent(_comp.value))
    }
  }
}