package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.Type
import kotlin.math.abs

class Queen(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init { type = Type.QUEEN }

    override fun copyForSim(): Piece {
        val p = Queen(color, col, row)
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
        val straight = (targetCol == preCol || targetRow == preRow)
        val diag = abs(targetCol - preCol) == abs(targetRow - preRow)
        if (!straight && !diag) return false
        if (straight && !isClearStraight(targetCol, targetRow)) return false
        if (diag && !isClearDiagonal(targetCol, targetRow)) return false
        val hit = panel.findPieceAt(targetCol, targetRow)
        if (hit == null) { panel.validMoves.add(targetCol to targetRow); return true }
        if (hit.color != color) { hittingPiece = hit; panel.captureMoves.add(targetCol to targetRow); return true }
        return false
    }
}
