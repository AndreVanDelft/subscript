package subscript.vm

object DSL {
  def _param[T<:Any](p:       FormalInputParameter[T], n:Symbol) = {p.bindToFormalInputParameter      ; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}
  def _param[T<:Any](p:      FormalOutputParameter[T], n:Symbol) = {p.bindToFormalOutputParameter     ; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}
  def _param[T<:Any](p: FormalConstrainedParameter[T], n:Symbol) = {p.bindToFormalConstrainedParameter; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}

  def execute(_script: N_call => Unit) = {val executer = new BasicExecuter; _script(executer.anchorNode); executer.run}
  
  implicit def codeFragment_to_T_0_ary_code(codeFragment: => Unit): T_0_ary_code[N_code_normal] =
    T_0_ary_code("{}", (_here:N_code_normal) => codeFragment)
    
  implicit def codeFragment_here_to_T_0_ary_code(codeFragment: (N_code_normal => Unit)): T_0_ary_code[N_code_normal] =
    T_0_ary_code("{}", codeFragment)
    
  implicit def scriptCall_to_T_0_ary_code(_scriptCall: N_call=>Unit) =  T_0_ary_code("call", (_here: N_call) => {_scriptCall.apply(_here)})

  implicit def annotation_to_T_1_ary_code[N<:CallGraphNodeTrait[T],T<:TemplateNode](_annotation: N_annotation[N] => Unit, _child: T) =
    T_1_ary_code("@:", _annotation, _child)

  
  def _seq     (children: TemplateNode*) = T_n_ary(";" , children:_*)
  def _alt     (children: TemplateNode*) = T_n_ary("+" , children:_*)
  def _par     (children: TemplateNode*) = T_n_ary("&" , children:_*)
  def _par_or  (children: TemplateNode*) = T_n_ary("|" , children:_*)
  def _par_and2(children: TemplateNode*) = T_n_ary("&&", children:_*)
  def _par_or2 (children: TemplateNode*) = T_n_ary("||", children:_*)
  def _disrupt (children: TemplateNode*) = T_n_ary("/" , children:_*)

  def _empty                             = T_0_ary("(+)")
  def _deadlock                          = T_0_ary("(-)")
  def _neutral                           = T_0_ary("(+-)")
  def _break                             = T_0_ary("break")
  def _optionalBreak                     = T_0_ary(".")
  def _loop_optionalBreak                = T_0_ary("..")
  def _loop                              = T_0_ary("...")
  def _while(_cond:       =>Boolean)     = T_0_ary_test("while", (here: N_while ) => _cond)
  def _while(_cond:N_while=>Boolean)     = T_0_ary_test("while",                     _cond)
  
  def _script(name: Symbol, child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List())
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2,p3))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              p6: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              p6: FormalParameter_withName[_], 
              p7: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6,p7))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              p6: FormalParameter_withName[_], 
              p7: FormalParameter_withName[_], 
              p8: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6,p7,p8))
  def _script(name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              p6: FormalParameter_withName[_], 
              p7: FormalParameter_withName[_], 
              p8: FormalParameter_withName[_], 
              p9: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = (_c: N_call) => _c.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9))

}