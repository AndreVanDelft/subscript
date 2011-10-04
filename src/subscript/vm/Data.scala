package subscript.vm

abstract class        FormalParameter(n: String) {def name = n}
case class       FormalInputParameter(n: String) extends FormalParameter(n)
case class      FormalOutputParameter(n: String) extends FormalParameter(n)
case class FormalConstrainedParameter(n: String) extends FormalParameter(n)

trait ActualParameterTrait[T<:Any] {
  def originalValue: T
  def value: T
  def transfer {}  
  def matches: Boolean  
}
abstract class ActualParameter[T<:Any](originalValue:T) extends ActualParameterTrait[T] {
  var value=originalValue
  def matches = true  
}
trait ParameterTransferrerTrait[T<:Any] extends ActualParameterTrait[T] {
  def transferFunction: T=>Unit
  override def transfer {transferFunction.apply(value)}
}
case class   ActualInputParameter[T<:Any](originalValue:T)                            extends ActualParameter[T](originalValue)
case class  ActualOutputParameter[T<:Any](originalValue:T, transferFunction: T=>Unit) extends ActualParameter[T](originalValue) with ParameterTransferrerTrait[T]
case class ActualForcingParameter[T<:Any](originalValue:T, transferFunction: T=>Unit) extends ActualParameter[T](originalValue) with ParameterTransferrerTrait[T] {
  override def matches = value==originalValue  
}
case class ActualConstrainedParameter[T<:Any](originalValue:T, transferFunction: T=>Unit, constraint: T=>Boolean) extends 
    ActualParameter[T](originalValue) with ParameterTransferrerTrait[T] {
  override def matches = constraint.apply(value)  
}
case class LocalVariable(name: String, var value: Any)


