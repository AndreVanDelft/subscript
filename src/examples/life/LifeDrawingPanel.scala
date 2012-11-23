package life

import java.awt.{Graphics, Image}
import javax.swing.JPanel
import scala.swing._
import scala.swing.event._

class LifeDrawingPanel(var cellColumns: Int = 100, var cellRows: Int = 70) extends Panel {
    type CellState = Boolean
    val cellSizeX = 3
    val cellSizeY = 3
    var currentX  = -1
    var currentY  = -1
    
    var cells        : Array[CellState] = null
    var bufferedImage: Image    = null
    var imageGC      : Graphics = null
    
    doClear

    //////////////////////////////////////////////
    // functions for mouse input
    //////////////////////////////////////////////

    def mouseDownToggle (e: MouseEvent) {currentX = -1; toggleMousePoint (e.point.x,e.point.y)}
    def mouseDragToggle (e: MouseEvent) {               toggleMousePoint (e.point.x,e.point.y)}

    def toggleMousePoint (x: Int, y: Int) {
        if (bufferedImage==null) createCells
        if (bufferedImage==null) return
        invertAt(x,y)
    }

    //////////////////////////////////////////////
    // functions for painting and the like
    //////////////////////////////////////////////
    def validate {

        //super.validate
        val newWidth  = size.width
        val newHeight = size.height;

        if (newWidth==0 
        || newHeight==0)  return;

        if (bufferedImage == null) {
            createCells (newWidth, newHeight)
            repaint ()
        } else {
            if (   bufferedImage.getWidth(null)  != newWidth 
                || bufferedImage.getHeight(null) != newHeight) {
                resizeCells (newWidth, newHeight)
                repaint
            }
        }
    }

    def cleanUp {
        if (bufferedImage == null) return
        imageGC.dispose
        bufferedImage.flush
        bufferedImage = null
    }

    /** randomize the canvas with the given density */
    def doRandomize(density: Double = 0.3): Unit = {
        if (bufferedImage==null) createCells
        if (bufferedImage==null) return
        imageGC.clearRect (0,0,bufferedImage.getWidth (null)-1,
                               bufferedImage.getHeight(null)-1)
        cells.zipWithIndex.foreach {
          case (cell, i) =>
            if (density >  0 
            &&  density >= scala.math.random) {
                cells(i) = true
                paintCellInImage(i%cellColumns, i/cellColumns, true)
            } else {
                cells(i) = false
            }
        }
        repaint
    }
    def doClear          = doRandomize(0.0)
    def getPreferredSize = new Dimension (cellSizeX*cellColumns, cellSizeY*cellRows);

    def update(g: Graphics2D) {
      paint (g);
    }
    
    override def paint (g: Graphics2D) {
        if (bufferedImage == null) createCells
        if (bufferedImage == null) {
            g.setColor(java.awt.Color.red)
            g.fillRect(0,0,size.width-1,size.height-1)
            return
        }
        g.drawImage (bufferedImage, 0, 0, null)
    }

    def paintCellInImage(cellX: Int, cellY: Int, cellState: CellState) {
        if (cellState)
             imageGC. fillRect(cellX*cellSizeX, cellY*cellSizeY, cellSizeX, cellSizeY)
        else imageGC.clearRect(cellX*cellSizeX, cellY*cellSizeY, cellSizeX, cellSizeY)
    }

    //////////////////////////////////////////////
    // functions implementing the life algorithm
    //////////////////////////////////////////////

    def createCells: Unit = createCells(size.width, size.height)
    def createCells (width: Int, height: Int): Unit = {
        if (width==0
        || height==0)
        {
            return 
        }
        //cellColumns = width  / cellSizeX
        //cellRows    = height / cellSizeY

        cells       = new Array(cellColumns * cellRows)

        bufferedImage = peer.createImage (width, height)
        if (bufferedImage == null) return

        imageGC     = bufferedImage.getGraphics
    }


    def resizeCells (newWidth: Int, newHeight: Int) {
        if (newWidth==0
        || newHeight==0)
        {
            println("resizeCells fails: newWidth = "+newWidth+", newHeight= "+newHeight)
            return
        }

        val newBufferedImage = peer.createImage (newWidth, newHeight)
        if (newBufferedImage == null) return

        val g = newBufferedImage.getGraphics

        val newCellColumns      = newWidth  / cellSizeX
        val newCellRows         = newHeight / cellSizeY;
        val newCells            = new Array[Boolean](newCellColumns * newCellRows)

        val newCellColumnOffset = (newCellColumns - cellColumns) / 2
        val newCellRowOffset    = (newCellRows    - cellRows)    / 2

        var cellIndex = 0
        for (cellY <- 0 until cellRows) {
            val newCellY = cellY + newCellRowOffset
            if (newCellY >= 0 && newCellY < newCellRows) {

              var newCellIndex = newCellY * newCellColumns + newCellColumnOffset
              for (cellX <- 0 until cellColumns) {
                val newCellX = cellX + newCellColumnOffset
                if (newCellX >= 0 && newCellX < newCellColumns
                && cells(cellIndex)) {
                  newCells(newCellIndex) = true
                  g.fillRect (newCellX*cellSizeX, newCellY*cellSizeY, cellSizeX, cellSizeY);
                }
                cellIndex    += 1
                newCellIndex += 1
              }
            } else {
                cellIndex += cellColumns
            }
        }

        cells       = newCells
        cellColumns = newCellColumns
        cellRows    = newCellRows

        if (newBufferedImage != null) {
            imageGC.dispose()
            bufferedImage.flush()
            bufferedImage = newBufferedImage
            imageGC     = g;
        }
    }

    def invertAt (x: Int, y: Int)  = invertCell (x/cellSizeX, y/cellSizeY)

    def invertCell (cellX: Int, cellY: Int) {
        if (     cellX < 0 || cellX >= cellColumns 
              || cellY < 0 || cellY >= cellRows)
            return;

        if (cellX == currentX 
        &&  cellY == currentY) return;

        val cellIndex     = cellColumns*cellY + cellX
        val oldCellState  = cells (cellIndex)
        val newCellState  = !oldCellState
        cells(cellIndex)  = newCellState
        currentX = cellX
        currentY = cellY
        if (bufferedImage == null) return

        paintCellInImage (cellX, cellY, newCellState)
        repaint(new Rectangle(cellX*cellSizeX, cellY*cellSizeY, cellSizeX, cellSizeY))
    }

    def calculateGeneration {
        if (cellRows <= 2 || cellColumns <= 2) return
    
        val neighbors = new Array[Int](cellColumns * cellRows)

        var prevLineIndex = 0
        var thisLineIndex = cellColumns
        var nextLineIndex = 2 * cellColumns

        var cellIndex = cellColumns + 1
        for (cellY <- 2 until cellRows - 2) {
            for (cellX <- 2 until cellColumns - 2) {
                if (cells(cellIndex)) {
                    neighbors(prevLineIndex    ) += 1
                    neighbors(prevLineIndex + 1) += 1
                    neighbors(prevLineIndex + 2) += 1
                    neighbors(thisLineIndex    ) += 1
                    neighbors(thisLineIndex + 2) += 1
                    neighbors(nextLineIndex    ) += 1
                    neighbors(nextLineIndex + 1) += 1
                    neighbors(nextLineIndex + 2) += 1
                }
                cellIndex     += 1
                prevLineIndex += 1
                thisLineIndex += 1
                nextLineIndex += 1
            }
            cellIndex     += 2
            prevLineIndex += 2
            thisLineIndex += 2
            nextLineIndex += 2
        }

        for (cellY <- 0 until cellRows;
             cellX <- 0 until cellColumns) {
             val cellIndex = cellX + cellColumns*cellY
             cells(cellIndex) = neighbors(cellIndex) == 3
        }
        for (cellY <- 0 until cellRows;
             cellX <- 0 until cellColumns) {
             val cellIndex = cellX + cellColumns*cellY
             paintCellInImage (cellX, cellY, cells(cellIndex))
        }
    }
}
