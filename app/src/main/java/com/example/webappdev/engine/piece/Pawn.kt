package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type
import kotlin.math.abs

class Pawn(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init { type = Type.PAWN }

    override fun copyForSim(): Piece {
        val p = Pawn(color, col, row)
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
        if (isSameSquare(targetCol, targetRow)) return false

        val dir = if (color == GamePanel.WHITE) -1 else 1
        val one = preRow + dir
        val two = preRow + dir * 2
        val left = preCol - 1
        val right = preCol + 1

        // forward one
        if (targetCol == preCol && targetRow == one) {
            if (panel.findPieceAt(preCol, one) == null) {
                panel.validMoves.add(preCol to one)
                return true
            }
        }
        // forward two
        if (!moved && targetCol == preCol && targetRow == two) {
            if (panel.findPieceAt(preCol, one) == null && panel.findPieceAt(preCol, two) == null) {
                panel.validMoves.add(preCol to two)
                return true
            }
        }
        // captures
        if (targetRow == one) {
            val hitL = panel.findPieceAt(left, one)
            if (targetCol == left && hitL != null && hitL.color != color) {
                hittingPiece = hitL; panel.captureMoves.add(left to one); return true
            }
            val hitR = panel.findPieceAt(right, one)
            if (targetCol == right && hitR != null && hitR.color != color) {
                hittingPiece = hitR; panel.captureMoves.add(right to one); return true
            }
        }
        // en-passant
        for (p in panel.pieces) {
            if (p.type == Type.PAWN && p.color != color && p.twoStepped && p.row == preRow) {
                if (p.col == left && targetCol == left && targetRow == one) {
                    hittingPiece = p; panel.captureMoves.add(left to one); return true
                }
                if (p.col == right && targetCol == right && targetRow == one) {
                    hittingPiece = p; panel.captureMoves.add(right to one); return true
                }
            }
        }
        return false
    }
}
