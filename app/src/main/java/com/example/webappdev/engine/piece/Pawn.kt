package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type
import kotlin.math.abs

class Pawn(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init { type = Type.PAWN }

    override fun copyForSim(): Piece {
        val p = Pawn(color, col, row)
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
        if (!isWithinBoard(targetCol, targetRow) || isSameSquare(targetCol, targetRow)) return false

        val moveValue = if (color == GamePanel.WHITE) -1 else 1

        // Edible Collisions
        hittingPiece = null
        for (p in GamePanel().simPieces) {
            if (p.col == targetCol && p.row == targetRow && p !== this) {
                hittingPiece = p
                break
            }
        }

        // 1-square forward
        if (targetCol == preCol && targetRow == preRow + moveValue && hittingPiece == null) return true

        // 2-square forward if first move and no blocking piece
        if (targetCol == preCol && targetRow == preRow + moveValue * 2 && hittingPiece == null && !moved && !pieceIsOnStraightLine(
                targetCol,
                targetRow
            )
        ) return true

        // Diagonal Capture
        val target = hittingPiece
        if (abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && target != null && target.color != color) return true

        // En-passant
        if (abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
            for (piece in GamePanel().simPieces) {
                if (piece.col == targetCol && piece.row == preRow && piece.twoStepped) {
                    hittingPiece = piece
                    return true
                }
            }
        }
        return false
    }
}
