package subscript.vm

trait FormalParameter[T<:Any] {
  def name: String; 
  def value: T
  def matches(aValue: T): Boolean
  def isInput      : Boolean
  def isOutput     : Boolean
  def isForcing    : Boolean
  def isConstrained: Boolean
}
trait       FormalInputParameter[T<:Any] extends FormalParameter[T]
trait      FormalOutputParameter[T<:Any] extends FormalParameter[T]
trait FormalConstrainedParameter[T<:Any] extends FormalParameter[T] {var value: T}

trait ActualParameterTrait[T<:Any] extends FormalParameter[T] {
  def originalValue: T
  def value: T
  def transfer {}  
  def matches      : Boolean = matches(value)
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
  def matches(aValue: T) = if (isForcing) aValue==originalValue else true 
  def isInput       = !isForcing  
  def isOutput      = false  
  var isForcing     = false // var, not def!!!
  def isConstrained = false
}
case class  ActualOutputParameter[T<:Any](originalValue:T, transferFunction: T=>Unit) extends ActualParameter[T] 
  with ParameterTransferrerTrait [T]
  with FormalInputParameter      [T] 
  with FormalConstrainedParameter[T] {
  def matches(aValue: T) = true  
  def isInput       = false  
  def isOutput      = true  
  def isForcing     = false
  def isConstrained = false
}

case class ActualConstrainedParameter[T<:Any](originalValue:T, transferFunction: T=>Unit, constraint: T=>Boolean) extends ActualParameter[T] 
  with ParameterTransferrerTrait [T]
  with FormalConstrainedParameter[T] {
  def matches(aValue: T) = constraint.apply(aValue)  
  def isInput       = false  
  def isOutput      = false  
  def isForcing     = false
  def isConstrained = true
}
// adapting parameters, as in script a(i:Int??) = b(i??)
case class ActualAdaptingParameter[T<:Any](adaptee: FormalConstrainedParameter[T]) 
  extends ActualParameter        [T] 
  with ParameterTransferrerTrait [T]
  with FormalConstrainedParameter[T] {
  val originalValue  = adaptee.value // val, not def !!
  def transferFunction: T=>Unit = {adaptee.value = _}
  def matches(aValue: T) = adaptee.matches(aValue)
  def isInput       = adaptee.isInput  
  def isOutput      = adaptee.isOutput
  def isForcing     = adaptee.isForcing
  def isConstrained = adaptee.isConstrained
}
case class LocalVariable(name: String, var value: Any)

