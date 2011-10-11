package subscript.vm

/*
Overview of formal and actual parameter use

|| Formal declaration   ||        Formal type              ||  Actual call          || Value of _p ||
||` p: P               `||`       FormalInputParameter[P] `||` expr                `||`  ActualValueParameter     (   expr) `||
||` p: P?              `||`      FormalOutputParameter[P] `||` varExpr?            `||` ActualOutputParameter     (varExpr, {=>varExpr=_) `||
||` p: P??             `||` FormalConstrainedParameter[P] `||` expr                `||`  ActualValueParameter     (   expr, {=>   expr=_) `||
||`                    `||`                               `||` varExpr?            `||` ActualOutputParameter     (varExpr, {=>varExpr=_) `||
||`                    `||`                               `||` varExpr if(c)?      `||` ActualConstrainedParameter(   expr, {=>   expr=_}, {_=>c}) `||
||`                    `||`                               `||` formalParam??       `||`    ActualAdaptingParameter(_formalParam) `||
||`                    `||`                               `||` formalParam if(c)?? `||`    ActualAdaptingParameter(_formalParam, {=>c}) `||

 */


trait FormalParameter[T<:Any] {
  def name: String; 
  def value: T
  def matches(aValue: T, doIsForcing: Boolean = isForcing): Boolean
  def isInput      : Boolean
  def isOutput     : Boolean
  def isForcing    : Boolean
  def isConstrained: Boolean
}
trait       FormalInputParameter[T<:Any] extends FormalParameter[T]
trait      FormalOutputParameter[T<:Any] extends FormalParameter[T]
trait FormalConstrainedParameter[T<:Any] extends FormalParameter[T] {var value: T; def setAsFormalConstrainedParameter}

trait ActualParameterTrait[T<:Any] extends FormalParameter[T] {
  def originalValue: T
  def value: T
  def transfer {}  
  def matches      : Boolean = matches(value)
  def isInput      : Boolean  
  def isOutput     : Boolean  
  def isForcing    : Boolean
  def isConstrained: Boolean
}
abstract class ActualParameter[T<:Any] extends ActualParameterTrait[T] {
  var name: String = null
  var value=originalValue
}
trait ParameterTransferrerTrait[T<:Any] extends ActualParameterTrait[T] {
  def transferFunction: T=>Unit
  override def transfer {transferFunction.apply(value)}
}
case class   ActualValueParameter[T<:Any](originalValue:T) extends ActualParameter[T] 
  with FormalInputParameter      [T]
  with FormalConstrainedParameter[T] {
  def setAsFormalConstrainedParameter = isForcing=true
  def matches(aValue: T, doIsForcing: Boolean = isForcing) = if (doIsForcing) aValue==originalValue else true 
  def isInput       = !isForcing  
  def isOutput      = false  
  var isForcing     = false // var, not def!!!
  def isConstrained = false
}
case class  ActualOutputParameter[T<:Any](originalValue:T, transferFunction: T=>Unit) extends ActualParameter[T] 
  with ParameterTransferrerTrait [T]
  with FormalInputParameter      [T] 
  with FormalConstrainedParameter[T] {
  def setAsFormalConstrainedParameter {}
  def matches(aValue: T, doIsForcing: Boolean = isForcing) = true  
  def isInput       = false  
  def isOutput      = true  
  def isForcing     = false
  def isConstrained = false
}

case class ActualConstrainedParameter[T<:Any](originalValue:T, transferFunction: T=>Unit, constraint: T=>Boolean) extends ActualParameter[T] 
  with ParameterTransferrerTrait [T]
  with FormalConstrainedParameter[T] {
  def matches(aValue: T, doIsForcing: Boolean = isForcing) = constraint.apply(aValue)  
  def setAsFormalConstrainedParameter {}
  def isInput       = false  
  def isOutput      = false  
  def isForcing     = false
  def isConstrained = true
}
// adapting parameters, as in script a(i:Int??) = b(i??)
case class ActualAdaptingParameter[T<:Any](adaptee: FormalConstrainedParameter[T], constraint: T=>Boolean=null) 
  extends ActualParameter        [T] 
  with ParameterTransferrerTrait [T]
  with FormalConstrainedParameter[T] {
  val rootAdaptee: ActualParameter[T] = adaptee match {case a:ActualAdaptingParameter[_]=>a.rootAdaptee case _ => adaptee.asInstanceOf[ActualParameter[T]]}
  def setAsFormalConstrainedParameter = isForcing=rootAdaptee.isInput
  val originalValue  = adaptee.value // val, not def !!
  value = originalValue
  def transferFunction: T=>Unit = {adaptee.value = _}
  def matches(aValue: T, doIsForcing: Boolean = isForcing) = (constraint==null||constraint.apply(aValue))&&
                            adaptee.matches(aValue, doIsForcing)
  def isInput       = adaptee.isInput && !isForcing
  def isOutput      = adaptee.isOutput&&constraint==null
  var isForcing     = adaptee.isForcing
  def isConstrained = adaptee.isConstrained || adaptee.isOutput && constraint!=null
}
//case class LocalVariable[T<:Any](name: String, var value: T)  cannot get this compiled, yet
case class LocalVariable(name: String, var value: Any)

