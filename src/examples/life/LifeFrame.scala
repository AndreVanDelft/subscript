package life

import scala.math._
import scala.swing._
import scala.swing.event._
import subscript.Predef._
import subscript.swing.SimpleSubscriptApplication
import subscript.swing.Scripts._
import subscript._
import subscript.DSL._
import subscript.Predef._
import subscript.vm._

object LifeFrame extends LifeFrameApplication
class LifeFrameApplication extends BasicLifeFrameApplication {

    //////////////////////////////////////////////
    // speed control
    //////////////////////////////////////////////
    
    def getSleep_ms = pow(2, 12-speed).toInt // logarithmic scale
    
    def sleep = 
      try {
        val sleepPart_ms = 10        
        for (slept_ms <- 0 until getSleep_ms by sleepPart_ms)
        {
          Thread.sleep(sleepPart_ms)
        }
      }
      catch { case _ => }

    //////////////////////////////////////////////
    // confirm exit dialog
    //////////////////////////////////////////////
    def confirmExit: Boolean = Dialog.showConfirmation(null, "Are you sure?", "About to exit")==Dialog.Result.Yes
                            
  /* the following subscript code has manually been compiled into Scala; see below
 script..
	implicit  key(c: Char     ??) =  key(top, c??)
	implicit vkey(k: Key.Value??) = vkey(top, k??)

	 randomizeCommand  = randomizeButton + 'r'
	     clearCommand  =     clearButton + 'c'
	      stepCommand  =      stepButton + ' '
	      exitCommand  =      exitButton + windowClosing
	multiStepStartCmd  =     startButton + Key.Enter
	 multiStepStopCmd  =      stopButton + Key.Enter
	
	exit               =   exitCommand var r: Boolean @gui: {r=confirmExit} while (!r)
	
   canvasOperations    = ...; (..singleStep) multiStep || clear || randomize

      do1Step          = {*canvas.calculateGeneration*} @gui: {!canvas.repaint!}
      
      randomize        =   randomizeCommand @gui: {!canvas.doRandomize!}
      clear            =       clearCommand @gui: {!canvas.doClear!}
      singleStep       =        stepCommand do1Step
       multiStep       = multiStepStartCmd; ...do1Step {*sleep*} / multiStepStopCmd

    setSpeed(s: Int)   = @gui: {!setSpeed(s)!}

      speedChanges     = ...; speedKeyInput + speedButtonInput + speedSliderInput
                    
      speedKeyInput    = for(c <- '0' to '9' ) + private c key,c setSpeed(digit2Speed(c))
                              
   speedButtonInput = if (speed>minSpeed) speedDecButton
                    + if (speed<maxSpeed) speedIncButton
    
     speedDecButton = minSpeedButton setSpeed(minSpeed)
                    +   slowerButton setSpeed(speed-1)
     
     speedIncButton = maxSpeedButton setSpeed(maxSpeed)
                    +   fasterButton setSpeed(speed+1)
     
   speedSliderInput = speedSlider setSpeed(speedSlider.value)

      mouseInput    = mousePressInput & mouseDragInput

    mousePressInput = mousePresses  (canvas, (me: MouseEvent) => canvas.mouseDownToggle(me))
    mouseDragInput  = mouseDraggings(canvas, (me: MouseEvent) => canvas.mouseDragToggle(me))  

    live            = canvasOperations 
                   || mouseInput 
                   || speedChanges  
                   || exit
*/
  def _vkey(_k:FormalConstrainedParameter[Key.Value]) = _script(this, 'vkey, _k~??'k) {subscript.swing.Scripts._vkey(top, _k~??)}
  def  _key(_c:FormalConstrainedParameter[Char     ]) = _script(this,  'key, _c~??'c) {subscript.swing.Scripts._key (top, _c~??)}
               
  override def _live     = _script(this,  'live            ) {_par_or2(_canvasOperations, _mouseInput, _speedChanges, _exit)}
  def  _randomizeCommand = _script(this,  'randomizeCommand) {_alt(_clicked(randomizeButton), _key('r'))} 
  def      _clearCommand = _script(this,      'clearCommand) {_alt(_clicked(    clearButton), _key('c'))}
  def       _stepCommand = _script(this,       'stepCommand) {_alt(_clicked(     stepButton), _key(' '))}
  def       _exitCommand = _script(this,       'exitCommand) {_alt(_clicked(     exitButton), _windowClosing(top))}
  def _multiStepStartCmd = _script(this, 'multiStepStartCmd) {_alt(_clicked(    startButton), _vkey(Key.Enter))}
  def  _multiStepStopCmd = _script(this,  'multiStepStopCmd) {_alt(_clicked(     stopButton), _vkey(Key.Enter))}
  
  def   _exit            = {val _r = _declare[Boolean]('r)
                           _script(this, 'exit) {_seq(_var(_r, (here:N_localvar[_]) => false), 
                                                      _exitCommand,
                                                      _at{gui} (_normal{here => _r.at(here).value = confirmExit}),
                                                      _while{here=> {! _r.at(here).value}})}
                            }

  def  _canvasOperations = _script(this, 'canvasOperations) {_seq(_loop, _par_or2(_seq(_seq(_optionalBreak_loop, _singleStep), _multiStep), _randomize, _clear))} 
  def  _do1Step          = _script(this, 'do1Step         ) {_seq(_threaded0{canvas.calculateGeneration}, _at{gui}(_tiny0{canvas.validate}))} 

  def  _randomize        = _script(this, 'randomize       ) {_seq( _randomizeCommand, _at{gui}(_tiny0{canvas.doRandomize()}))} 
  def  _clear            = _script(this, 'clear           ) {_seq(     _clearCommand, _at{gui}(_tiny0{canvas.doClear}))} 
  def  _singleStep       = _script(this, 'singleStep      ) {_seq(      _stepCommand, _do1Step)} 
  def  _multiStep        = _script(this, 'multiStep       ) {_seq(_multiStepStartCmd, _disrupt(_seq(_loop, _do1Step, _threaded0{sleep}), _multiStepStopCmd))} 
      
  def  _setSpeed(_s:FormalInputParameter[Int])  = _script(this, 'setSpeed, _s~'s) {_at{gui}(_tiny0{setSpeed(_s.value)})} 
  
  def  _speedChanges     = _script(this, 'speedChanges    ) {_seq(_loop, _alt(_speedKeyInput, _speedButtonInput, _speedSliderInput))} 

//def  _speedKeyInput    = _script(this, 'speedKeyInput   ) {_alt(_while{here=>pass(here)<10}, _seq(_key('0'), _setSpeed(digit2Speed('0'))))} 
  def  _speedKeyInput    = {val _c = _declare[Char]('r)
                           _script(this, 'speedKeyInput   ) {_alt(_times(10), _seq(_val(_c, (here:N_localvar[_]) => (pass(here.n_ary_op_ancestor)+'0').asInstanceOf[Char]), 
                                                                                   _call{here=>_key(_c.at(here).value)(here)}, 
                                                                                   _call{here=>_setSpeed(digit2Speed(_c.at(here).value))(here)} ))}
                           }
  def  _speedButtonInput = _script(this, 'speedButtonInput) {_alt(_if0{speed>minSpeed}(_buttonSpeedDec), _if0{speed<maxSpeed}(_buttonSpeedInc))} 

  def  _buttonSpeedDec   = _script(this, 'buttonSpeedDec  ) {_alt(_seq(_clicked(minSpeedButton), _setSpeed(minSpeed)), _seq(_clicked(slowerButton), _setSpeed(speed-1)))} 
  def  _buttonSpeedInc   = _script(this, 'buttonSpeedInc  ) {_alt(_seq(_clicked(maxSpeedButton), _setSpeed(maxSpeed)), _seq(_clicked(fasterButton), _setSpeed(speed+1)))} 
  
  def  _speedSliderInput = _script(this, 'speedSliderInput) {_seq(_stateChange(speedSlider), _setSpeed(speedSlider.value))} 
     
  def  _mouseInput       = _script(this, 'mouseInput      ) {_par(_mousePressInput, _mouseDragInput)} 

  def  _mousePressInput  = _script(this, 'mousePressInput ) {_mousePresses  (canvas, (me: MouseEvent) => canvas.mouseDownToggle(me))} 
  def  _mouseDragInput   = _script(this, 'mouseDragInput  ) {_mouseDraggings(canvas, (me: MouseEvent) => canvas.mouseDragToggle(me))} 

  // bridge method   
  override def live = _execute(_live)

  def _times(_n: FormalInputParameter[Int]) = _script(this, 'mouseDragInput, _n~'n) {_while{here=>pass(here)<_n.value}}
}


