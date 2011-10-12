package subscript.vm

object DSL {
  def param[T<:Any](p:       FormalInputParameter[T], n:Symbol) = {p.bindToFormalInputParameter      ; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}
  def param[T<:Any](p:      FormalOutputParameter[T], n:Symbol) = {p.bindToFormalOutputParameter     ; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}
  def param[T<:Any](p: FormalConstrainedParameter[T], n:Symbol) = {p.bindToFormalConstrainedParameter; p.asInstanceOf[FormalParameter_withName[T]].nameThis(n)}
  
  def _script(caller: N_call, name: Symbol, child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List())
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0))
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0,p1))
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2))
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2,p3))
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4))
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5))
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              p6: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6))
  def _script(caller: N_call, name: Symbol, 
              p0: FormalParameter_withName[_], 
              p1: FormalParameter_withName[_], 
              p2: FormalParameter_withName[_], 
              p3: FormalParameter_withName[_], 
              p4: FormalParameter_withName[_], 
              p5: FormalParameter_withName[_], 
              p6: FormalParameter_withName[_], 
              p7: FormalParameter_withName[_], 
              child0: TemplateNode) 
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6,p7))
  def _script(caller: N_call, name: Symbol, 
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
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6,p7,p8))
  def _script(caller: N_call, name: Symbol, 
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
      = caller.calls(T_script("script", name, child0), List(p0,p1,p2,p3,p4,p5,p6,p7,p8,p9))

}