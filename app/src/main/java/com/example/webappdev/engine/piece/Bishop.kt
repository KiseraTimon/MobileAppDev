package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type
import kotlin.math.abs

class Bishop(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init { type = Type.BISHOP }

    override fun copyForSim(): Piece {
        val p = Bishop(color, col, row)
        p.type = this.type
        p.x = this.x
        p.y = this.y
        p.preCol = this.preCol
        p.preRow = this.preRow
        p.hittingPiece = this.hittingPiece
        p.moved = this.moved
        p.twoStepped = this.twoStepped
        return p
    }

    override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        if (!isWithinBoard(targetCol, targetRow)) return false
        if (isSameSquare(targetCol, targetRow)) return false

        if (abs(targetCol - preCol) == abs(targetRow - preRow)) {
            if (!pieceIsOnDiagonalLine(targetCol, targetRow) && isValidSquare(targetCol, targetRow)) return true
        }
        return false
    }
}
