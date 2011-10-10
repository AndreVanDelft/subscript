package subscript
import scala.swing._
import scala.swing.event._
import subscript.vm._;

// Predefined scripts. times, delta, epsilon, nu
//
// The swing stuff will have to be moved to object subscript.swing.Scripts or something like that
object Predef {
  implicit def valueToActualValueParameter[T<:Any](value: T) = new ActualValueParameter(value)
  def pass(implicit node: CallGraphNodeTrait[_]): Int = node.pass
  
//  scripts
//    times(n:Int) = while(pass<n)
//    delta        = (-)
//    epsilon      = (+)
//    nu           = (+-)
}