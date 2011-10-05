package subscript.vm

abstract class        FormalParameter {def name: String}
case class       FormalInputParameter(name: String) extends FormalParameter
case class      FormalOutputParameter(name: String) extends FormalParameter
case class FormalConstrainedParameter(name: String) extends FormalParameter

trait ActualParameterTrait[T<:Any] {
  def originalValue: T
  def value: T
  def transfer {}  
  def matches: Boolean  
}
abstract class ActualParameter[T<:Any] extends ActualParameterTrait[T] {
  var value=originalValue
  def matches = true  
}
trait ParameterTransferrerTrait[T<:Any] extends ActualParameterTrait[T] {
  def transferFunction: T=>Unit
  override def transfer {transferFunction.apply(value)}
}
case class   ActualInputParameter[T<:Any](originalValue:T)                            extends ActualParameter[T]
case class  ActualOutputParameter[T<:Any](originalValue:T, transferFunction: T=>Unit) extends ActualParameter[T] with ParameterTransferrerTrait[T]
case class ActualForcingParameter[T<:Any](originalValue:T)                            extends ActualParameter[T] {
  override def matches = value==originalValue  
}
case class ActualConstrainedParameter[T<:Any](originalValue:T, transferFunction: T=>Unit, constraint: T=>Boolean) extends 
    ActualParameter[T] with ParameterTransferrerTrait[T] {
  override def matches = constraint.apply(value)  
}
// adapting parameters, as in script a(i:Int??) = b(i??)
case class ActualAdaptingParameter[T<:Any](originalValue:T, transferFunction: T=>Unit, adaptee: ActualParameter[T]) 
   extends ActualParameter[T] with ParameterTransferrerTrait[T] {
  override def matches = value==adaptee.originalValue
}
case class LocalVariable(name: String, var value: Any)

