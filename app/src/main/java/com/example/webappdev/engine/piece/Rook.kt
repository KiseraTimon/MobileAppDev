package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type

class Rook(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init { type = Type.ROOK }

    override fun copyForSim(): Piece {
        val p = Rook(color, col, row)
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

        // Rook Moves
        if (targetCol == preCol || targetRow == preRow) {
            if (!pieceIsOnStraightLine(targetCol, targetRow) && isValidSquare(targetCol, targetRow)) {
                return true
            }
        }
        return false
    }
}
