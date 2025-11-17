package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type
import kotlin.math.abs

class King(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init { type = Type.KING }

    override fun copyForSim(): Piece {
        val p = King(color, col, row)
        p.panel = this.panel
        p.type = this.type
        p.x = this.x; p.y = this.y
        p.preCol = this.preCol; p.preRow = this.preRow
        p.hittingPiece = this.hittingPiece
        p.moved = this.moved; p.twoStepped = this.twoStepped
        return p
    }

    override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        panel.clearHighlights()
        if (!isWithinBoard(targetCol, targetRow)) return false
        val dx = abs(targetCol - preCol)
        val dy = abs(targetRow - preRow)

        // normal one-step moves
        if ((dx <= 1 && dy <= 1) && !(dx == 0 && dy == 0)) {
            val hit = panel.findPieceAt(targetCol, targetRow)
            if (hit == null) { panel.validMoves.add(targetCol to targetRow); return true }
            if (hit.color != color) { hittingPiece = hit; panel.captureMoves.add(targetCol to targetRow); return true }
            return false
        }

        // castling (visual/placement only; full legality requires check tests)
        if (!moved && dy == 0) {
            // king side
            if (targetCol == preCol + 2) {
                // check rook exists at preCol+3 and hasn't moved
                val r = panel.findPieceAt(preCol + 3, preRow)
                if (r != null && r.type == Type.ROOK && !r.moved) {
                    panel.validMoves.add(targetCol to targetRow)
                    panel.castlingPiece = r
                    return true
                }
            }
            // queen side
            if (targetCol == preCol - 2) {
                val r = panel.findPieceAt(preCol - 4, preRow)
                if (r != null && r.type == Type.ROOK && !r.moved) {
                    panel.validMoves.add(targetCol to targetRow)
                    panel.castlingPiece = r
                    return true
                }
            }
        }
        return false
    }
}
