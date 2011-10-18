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