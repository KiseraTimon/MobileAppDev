package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type
import kotlin.math.abs

class King(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init { type = Type.KING }

    override fun copyForSim(): Piece {
        val p = King(color, col, row)
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

        // Basic one-square moves
        if (abs(targetCol - preCol) + abs(targetRow - preRow) == 1 ||
            abs(targetCol - preCol) * abs(targetRow - preRow) == 1
        ) {
            if (isValidSquare(targetCol, targetRow)) return true
        }

        // Castling logic
        if (!moved) {
            // Right castling
            if (targetCol == preCol + 2 && targetRow == preRow && !pieceIsOnStraightLine(targetCol, targetRow)) {
                for (piece in GamePanel().pieces) {
                    if (piece.col == preCol + 3 && piece.row == preRow && !piece.moved) {
                        GamePanel().castlingPiece = piece
                        return true
                    }
                }
            }
            // Left castling
            if (targetCol == preCol - 2 && targetRow == preRow && !pieceIsOnStraightLine(targetCol, targetRow)) {
                val pArray = arrayOfNulls<Piece>(2)
                for (piece in GamePanel().simPieces) {
                    if (piece.col == preCol - 3 && piece.row == targetRow) pArray[0] = piece
                    if (piece.col == preCol - 4 && piece.row == targetRow) pArray[1] = piece
                }
                if (pArray[0] == null && pArray[1] != null && !pArray[1]!!.moved) {
                    GamePanel().castlingPiece = pArray[1]
                    return true
                }
            }
        }
        return false
    }
}
