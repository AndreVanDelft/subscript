package subscript.test

import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm.TemplateNode

object Test {
  
  //implicit def PRINTLN(s: String) = println("!!!"+s)
  //def $(those: Any*) = {var result=this.toString; those.foreach(result+="%%"+_); result}
  //def  main(args: Array[String]): Unit = {implicit val s ="Hello"; s; println(this $ null)}
  
  
  
  def PRINTLN(s: String*) = println("!!!"+s)
  def  main(args: Array[String]): Unit = {implicit val s ="Hello"; PRINTLN(s,"a",s)}
  
  // Enter..Escape =  {_seq(_seq(_optionalBreak_loop, _vkey(Key.Enter)),_vkey(Key.Escape))}

}

/*
 val people: Array[Person] = ...
 val (minors, adults) =	people partition (_.age<18)	

case class User(var firstName:String, var lastName:String, 
                var email:String, var password:Password)
*/