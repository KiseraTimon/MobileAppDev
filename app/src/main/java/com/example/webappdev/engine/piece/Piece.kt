package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.Board
import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type
import kotlin.math.abs

open class Piece(var color: Int, var col: Int, var row: Int) {
    lateinit var panel: GamePanel
    var type: Type? = null

    var x: Int = getX(col)
    var y: Int = getY(row)
    var preCol: Int = col
    var preRow: Int = row

    var hittingPiece: Piece? = null
    var moved: Boolean = false
    var twoStepped: Boolean = false

    open fun copyForSim(): Piece {
        val p = Piece(color, col, row)
        p.panel = this.panel
        p.type = this.type
        p.x = this.x; p.y = this.y
        p.preCol = this.preCol; p.preRow = this.preRow
        p.hittingPiece = this.hittingPiece
        p.moved = this.moved; p.twoStepped = this.twoStepped
        return p
    }

    fun getX(col: Int) = col * Board.SQUARE_SIZE
    fun getY(row: Int) = row * Board.SQUARE_SIZE
    fun getCol(px: Int) = (px + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
    fun getRow(py: Int) = (py + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE

    fun updatePosition() {
        if (type != null && type == Type.PAWN && kotlin.math.abs(row - preRow) == 2) {
            twoStepped = true
        }
        x = getX(col); y = getY(row)
        preCol = col; preRow = row
        moved = true
    }

    fun resetPosition() {
        col = preCol; row = preRow
        x = getX(col); y = getY(row)
    }

    fun isWithinBoard(c: Int, r: Int) = (c in 0..7 && r in 0..7)
    fun isSameSquare(c: Int, r: Int) = (c == preCol && r == preRow)

    open fun canMove(targetCol: Int, targetRow: Int): Boolean = false

    // helpers for sliding pieces
    protected fun isClearStraight(targetCol: Int, targetRow: Int): Boolean {
        if (targetCol == preCol) {
            val step = if (targetRow > preRow) 1 else -1
            var r = preRow + step
            while (r != targetRow) {
                if (panel.simPieces.any { it.col == preCol && it.row == r }) return false
                r += step
            }
            return true
        }
        if (targetRow == preRow) {
            val step = if (targetCol > preCol) 1 else -1
            var c = preCol + step
            while (c != targetCol) {
                if (panel.simPieces.any { it.col == c && it.row == preRow }) return false
                c += step
            }
            return true
        }
        return false
    }

    protected fun isClearDiagonal(targetCol: Int, targetRow: Int): Boolean {
        val dc = if (targetCol > preCol) 1 else -1
        val dr = if (targetRow > preRow) 1 else -1
        var c = preCol + dc
        var r = preRow + dr
        while (c != targetCol && r != targetRow) {
            if (panel.simPieces.any { it.col == c && it.row == r }) return false
            c += dc; r += dr
        }
        return true
    }
}
