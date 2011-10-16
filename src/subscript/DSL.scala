package subscript

import subscript.vm._

object DSL {
  
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
 
  // 'with' construct should allow for
  //   with('myVar) {(_myVar: LocalVariable[MyVarType]) => {myCode}}
  //   with('myVar) {(_myVar: LocalVariable[MyVarType]) => if (_myVar==theValue) restOfScript}
  def _with[N<:CallGraphTreeNode[_],V<:Any](name: Symbol) (valueCode: LocalVariable[V] => TemplateNodeWithCode[_,V]): N => TemplateNodeWithCode[_,V] = {
    here:N => valueCode.apply(here.getLocalVariable[V](name)) 
    // TBD: instead of null, something would be needed like 
    //  "here.getLocalVariable[V](name)"
    // the problem is "here" - it is the parameter to the code in the TemplateNodeWithCode[_,V]
  }
    
  
  def _var(name: Symbol, valueCode: N_localvar[_]=>Any) = T_0_ary_name_valueCode("var" , name,  valueCode)
  def _val(name: Symbol, valueCode: N_localvar[_]=>Any) = T_0_ary_name_valueCode("val" , name,  valueCode)
  
  def _op0(opSymbol: String)                                                       = T_0_ary(opSymbol)
  def _op1(opSymbol: String)(c0: TemplateNode)                                     = T_1_ary(opSymbol, c0)
  def _op2(opSymbol: String)(c0: TemplateNode, c1: TemplateNode)                   = T_2_ary(opSymbol, c0, c1)
  def _op3(opSymbol: String)(c0: TemplateNode, c1: TemplateNode, c2: TemplateNode) = T_3_ary(opSymbol, c0, c1, c2)
  def _op (opSymbol: String)(children: TemplateNode*)                              = T_n_ary(opSymbol, children:_*)
  
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

  def _empty                             = _op0("(+)")
  def _deadlock                          = _op0("(-)")
  def _neutral                           = _op0("(+-)")
  def _break                             = _op0("break")
  def _optionalBreak                     = _op0(".")
  def _optionalBreak_loop                = _op0("..")
  def _loop                              = _op0("...")
  def _if_inline                         = _op2("?")_
  def _if_else_inline                    = _op3("?:")_
  def _while  (_cond:       =>Boolean)   = T_0_ary_test("while", (here: N_while ) => _cond)
  def _while  (_cond:N_while=>Boolean)   = T_0_ary_test("while",                     _cond)
  def _if     (_cond:       =>Boolean)(c0: TemplateNode) = T_1_ary_test("if", (here: N_if) => _cond, c0)
  def _if     (_cond:N_if   =>Boolean)(c0: TemplateNode) = T_1_ary_test("if",                 _cond, c0)
  def _if_else(_cond:       =>Boolean)(c0: TemplateNode, c1: TemplateNode) = T_2_ary_test("if_else", (here: N_if) => _cond, c0, c1)
  def _if_else(_cond:N_if   =>Boolean)(c0: TemplateNode, c1: TemplateNode) = T_2_ary_test("if_else",                 _cond, c0, c1)
 
  def _script(name: Symbol, p: FormalParameter_withName[_]*)(_t: TemplateNode) = ((_c: N_call) => _c.calls(T_script("script", name, _t), p:_*))
}