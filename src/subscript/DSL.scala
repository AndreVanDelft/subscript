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

package subscript

import subscript.vm._

object DSL {
  
  def _execute(_script: N_call => Unit) = {val executer = new BasicScriptExecuter; _script(executer.anchorNode); executer.run}

  def _codeFragmentKind [N<:N_atomic_action[N]](opSymbol: String, cf:  => Unit ): T_0_ary_code[N] = T_0_ary_code(opSymbol, () => (_here:N) => cf)
  def _codeFragmentKind1[N<:N_atomic_action[N]](opSymbol: String, cf: (N=>Unit)): T_0_ary_code[N] = T_0_ary_code(opSymbol, () =>              cf)

  implicit 
  def _normal            (cf: => Unit) = _codeFragmentKind("{}",cf)
  def _threaded          (cf: => Unit) = _codeFragmentKind("{**}",cf)
  def _unsure            (cf: => Unit) = _codeFragmentKind("{??}",cf)
  def _tiny              (cf: => Unit) = _codeFragmentKind("{!!}",cf)
  def _eventhandling     (cf: => Unit) = _codeFragmentKind("{..}",cf)
  def _eventhandling_loop(cf: => Unit) = _codeFragmentKind("{......}",cf)

  implicit 
  def _normal1            (cf: => (N_code_normal  =>Unit)) = _codeFragmentKind1("{}",cf)
  def _threaded1          (cf: => (N_code_threaded=>Unit)) = _codeFragmentKind1("{**}",cf)
  def _unsure1            (cf: => (N_code_unsure  =>Unit)) = _codeFragmentKind1("{??}",cf)
  def _tiny1              (cf: => (N_code_tiny    =>Unit)) = _codeFragmentKind1("{!!}",cf)
  def _eventhandling1     (cf: => (N_code_eh      =>Unit)) = _codeFragmentKind1("{..}",cf)
  def _eventhandling_loop1(cf: => (N_code_eh_loop =>Unit)) = _codeFragmentKind1("{......}",cf)

  implicit def _call      (cf: => (N_call         =>Unit)) = T_call(()=>n=>cf)
  
  implicit def valueToActualValueParameter[T<:Any](value: T) = new ActualValueParameter(value)

  def _at[N<:CallGraphNodeTrait[T],T<:TemplateNode](_cf:N=>Unit)  
  = (_child: T) => T_annotation(() => (here:N_annotation[N,T]) => _cf(here.there), _child)
 
  def _declare[T](name: Symbol) = new LocalVariable[T](name)
  
  def _var     [T<:Any](v: LocalVariable[T], valueCode: => N_localvar[_]=>T) = T_0_ary_local_valueCode("var"    , v, () => valueCode)
  def _val     [T<:Any](v: LocalVariable[T], valueCode: => N_localvar[_]=>T) = T_0_ary_local_valueCode("val"    , v, () => valueCode)
  def _var_loop[T<:Any](v: LocalVariable[T], valueCode: => N_localvar[_]=>T) = T_0_ary_local_valueCode("var..." , v, () => valueCode)
  def _val_loop[T<:Any](v: LocalVariable[T], valueCode: => N_localvar[_]=>T) = T_0_ary_local_valueCode("val..." , v, () => valueCode)
  
  def _op0(opSymbol: String)                                                       = T_0_ary(opSymbol)
  def _op1(opSymbol: String)(c0: TemplateNode)                                     = T_1_ary(opSymbol, c0)
  def _op2(opSymbol: String)(c0: TemplateNode, c1: TemplateNode)                   = T_2_ary(opSymbol, c0, c1)
  def _op3(opSymbol: String)(c0: TemplateNode, c1: TemplateNode, c2: TemplateNode) = T_3_ary(opSymbol, c0, c1, c2)
  def _op (opSymbol: String)(children: TemplateNode*)                              = T_n_ary(opSymbol, children:_*)
  
  def _seq               = _op (";")_
  def _alt               = _op ("+")_
  def _par               = _op ("&")_
  def _par_or            = _op ("|")_
  def _par_and2          = _op ("&&")_
  def _par_or2           = _op ("||")_
  def _par_equal         = _op ("==")_
  def _disrupt           = _op ("/")_
  def _shuffle           = _op ("#")_
  def _shuffle_1_or_more = _op ("#%#")_
  def _seq_1_or_more     = _op ("#%#")_
  def _disrup            = _op ("#/")_
  def _disrupt_0_or_more = _op ("#/#/")_
  def _not               = _op1("!")_
  def _not_react         = _op1("-")_
  def _react             = _op1("~")_
  def _launch            = _op1("*")_
  def _launch_anchor     = _op1("**")_

  def _empty                             = _op0("(+)")
  def _deadlock                          = _op0("(-)")
  def _neutral                           = _op0("(+-)")
  def _break                             = _op0("break")
  def _optionalBreak                     = _op0(".")
  def _optionalBreak_loop                = _op0("..")
  def _loop                              = _op0("...")
  def _if_inline                         = _op2("?")_
  def _if_else_inline                    = _op3("?:")_
  def _while  (_cond:       =>Boolean)   = T_0_ary_test("while", () => (here: N_while ) => _cond)
  def _while  (_cond:N_while=>Boolean)   = T_0_ary_test("while", () =>                     _cond)
  def _if     (_cond:       =>Boolean)(c0: TemplateNode) = T_1_ary_test("if", () => (here: N_if) => _cond, c0)
  def _if     (_cond:N_if   =>Boolean)(c0: TemplateNode) = T_1_ary_test("if", () =>                 _cond, c0)
  def _if_else(_cond:       =>Boolean)(c0: TemplateNode, c1: TemplateNode) = T_2_ary_test("if_else", () => (here: N_if) => _cond, c0, c1)
  def _if_else(_cond:N_if   =>Boolean)(c0: TemplateNode, c1: TemplateNode) = T_2_ary_test("if_else", () =>                 _cond, c0, c1)
 
  def _script(name: Symbol, p: FormalParameter_withName[_]*)(_t: TemplateNode): N_call=>Unit = ((_c: N_call) => _c.calls(T_script("script", name, _t), p:_*))
}