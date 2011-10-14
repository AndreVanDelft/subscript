package subscript

import subscript.vm._

object DSL {
  
  def _param[T<:Any](p:       FormalInputParameter[T], n:Symbol) = {p.bindToFormalInputParameter      ; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}
  def _param[T<:Any](p:      FormalOutputParameter[T], n:Symbol) = {p.bindToFormalOutputParameter     ; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}
  def _param[T<:Any](p: FormalConstrainedParameter[T], n:Symbol) = {p.bindToFormalConstrainedParameter; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}

  def _execute(_script: N_call => Unit) = {val executer = new BasicExecuter; _script(executer.anchorNode); executer.run}

  def _codeFragmentKind [N<:N_atomic_action[N]](opSymbol: String, cf:  => Unit ): T_0_ary_code[N] = T_0_ary_code(opSymbol, (_here:N) => cf)
  def _codeFragmentKind1[N<:N_atomic_action[N]](opSymbol: String, cf: (N=>Unit)): T_0_ary_code[N] = T_0_ary_code(opSymbol,              cf)

  implicit 
  def _normal            (cf: => Unit) = _codeFragmentKind("{}",cf)
  def _threaded          (cf: => Unit) = _codeFragmentKind("{**}",cf)
  def _unsure            (cf: => Unit) = _codeFragmentKind("{??}",cf)
  def _tiny              (cf: => Unit) = _codeFragmentKind("{!!}",cf)
  def _eventhandling     (cf: => Unit) = _codeFragmentKind("{..}",cf)
  def _eventhandling_loop(cf: => Unit) = _codeFragmentKind("{......}",cf)

  implicit 
  def _normal1            [N<:N_atomic_action[N]](cf: => (N=>Unit)) = _codeFragmentKind1("{}",cf)
  def _threaded1          [N<:N_atomic_action[N]](cf: => (N=>Unit)) = _codeFragmentKind1("{**}",cf)
  def _unsure1            [N<:N_atomic_action[N]](cf: => (N=>Unit)) = _codeFragmentKind1("{??}",cf)
  def _tiny1              [N<:N_atomic_action[N]](cf: => (N=>Unit)) = _codeFragmentKind1("{!!}",cf)
  def _eventhandling1     [N<:N_atomic_action[N]](cf: => (N=>Unit)) = _codeFragmentKind1("{..}",cf)
  def _eventhandling_loop1[N<:N_atomic_action[N]](cf: => (N=>Unit)) = _codeFragmentKind1("{......}",cf)
  
  implicit def scriptCall_to_T_0_ary_code(_scriptCall: N_call=>Unit) =  T_0_ary_code("call", (_here: N_call) => {_scriptCall.apply(_here)})

  def _at[N<:CallGraphNodeTrait[T],T<:TemplateNode](_cf:N=>Unit)  
  = (_child: T) => T_1_ary_code("@:", (here:N_annotation[N]) => _cf(here.there), _child)
 
  def _var(name: Symbol, value: Any) = T_0_ary_code ("var" , {(here:N_localvar ) => here.initLocalVariable(name,value)})
  def _val(name: Symbol, value: Any) = T_0_ary_code ("val" , {(here:N_localvar ) => here.initLocalVariable(name,value)})
  
  
  def _op1(opSymbol: String)(child   : TemplateNode ) = T_1_ary(opSymbol, child)
  def _op (opSymbol: String)(children: TemplateNode*) = T_n_ary(opSymbol, children:_*)
  
  def _seq           = _op (";")_
  def _alt           = _op ("+")_
  def _par           = _op ("&")_
  def _par_or        = _op ("|")_
  def _par_and2      = _op ("&&")_
  def _par_or2       = _op ("||")_
  def _disrupt       = _op ("/")_
  def _not           = _op1("!")_
  def _not_react     = _op1("-")_
  def _react         = _op1("~")_
  def _launch        = _op1("*")_
  def _launch_anchor = _op1("**")_

  def _empty                             = T_0_ary("(+)")
  def _deadlock                          = T_0_ary("(-)")
  def _neutral                           = T_0_ary("(+-)")
  def _break                             = T_0_ary("break")
  def _optionalBreak                     = T_0_ary(".")
  def _optionalBreak_loop                = T_0_ary("..")
  def _loop                              = T_0_ary("...")
  def _while  (_cond:       =>Boolean)   = T_0_ary_test("while", (here: N_while ) => _cond)
  def _while  (_cond:N_while=>Boolean)   = T_0_ary_test("while",                     _cond)
  def _if     (_cond:       =>Boolean)(child0: TemplateNode) = T_1_ary_test("if", (here: N_if) => _cond, child0)
  def _if     (_cond:N_if   =>Boolean)(child0: TemplateNode) = T_1_ary_test("if",                 _cond, child0)
  def _if_else(_cond:       =>Boolean)(child0: TemplateNode, child1: TemplateNode) = T_2_ary_test("if_else", (here: N_if) => _cond, child0, child1)
  def _if_else(_cond:N_if   =>Boolean)(child0: TemplateNode, child1: TemplateNode) = T_2_ary_test("if_else",                 _cond, child0, child1)
  def _if_inline      = (child0: TemplateNode, child1: TemplateNode                      ) => T_2_ary("?" , child0, child1)
  def _if_else_inline = (child0: TemplateNode, child1: TemplateNode, child2: TemplateNode) => T_3_ary("?:", child0, child1, child2)
 
  def _script(name: Symbol, p: FormalParameter_withName[_]*) = (_t: TemplateNode) => ((_c: N_call) => _c.calls(T_script("script", name, _t), p:_*))
}