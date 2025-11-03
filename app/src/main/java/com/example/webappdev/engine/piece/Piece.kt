package com.example.webappdev.engine.piece

import com.example.webappdev.engine.main.Board
import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.Type
import kotlin.math.abs

// Piece Engine Logic + Position & Movement Helpers
open class Piece(var color: Int, var col: Int, var row: Int) {
    var type: Type? = null

    // Pixel offsets kept (for compatibility)
    var x: Int = getX(col)
    var y: Int = getY(row)

    var preCol: Int = col
    var preRow: Int = row

    var hittingPiece: Piece? = null
    var moved: Boolean = false
    var twoStepped: Boolean = false

    // Simulation Copy
    open fun copyForSim(): Piece {
        val p = Piece(color, col, row)
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

    fun getX(col: Int): Int = col * Board.SQUARE_SIZE
    fun getY(row: Int): Int = row * Board.SQUARE_SIZE
    fun getCol(px: Int): Int = (px + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
    fun getRow(py: Int): Int = (py + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE

    // Position Updates
    fun updatePosition() {
        // en-passant detection for pawns (two-stepped)
        if (type == Type.PAWN) {
            if (abs(row - preRow) == 2) {
                twoStepped = true
            }
        }
        x = getX(col)
        y = getY(row)
        preCol = getCol(x)
        preRow = getRow(y)
        moved = true
    }

    // Position Resets
    fun resetPosition() {
        col = preCol
        row = preRow
        x = getX(col)
        y = getY(row)
    }

    // Checks
    open fun canMove(targetCol: Int, targetRow: Int): Boolean = false

    fun isWithinBoard(targetCol: Int, targetRow: Int): Boolean =
        targetCol in 0..7 && targetRow in 0..7

    fun isSameSquare(targetCol: Int, targetRow: Int): Boolean =
        targetCol == preCol && targetRow == preRow

    fun getHittingPiece(targetCol: Int, targetRow: Int): Piece? {
        for (piece in GamePanel().simPieces) {
            if (piece.col == targetCol && piece.row == targetRow && piece !== this) return piece
        }
        return null
    }

    fun isValidSquare(targetCol: Int, targetRow: Int): Boolean {
        hittingPiece = null
        for (p in GamePanel().simPieces) {
            if (p.col == targetCol && p.row == targetRow && p !== this) {
                hittingPiece = p
                break
            }
        }
        if (hittingPiece == null) return true
        if (hittingPiece!!.color != this.color) return true
        // same color - not valid
        hittingPiece = null
        return false
    }

    fun pieceIsOnStraightLine(targetCol: Int, targetRow: Int): Boolean {
        // when moving left
        if (targetCol < preCol) {
            for (c in preCol - 1 downTo targetCol + 1) {
                for (piece in GamePanel().simPieces) {
                    if (piece.col == c && piece.row == targetRow) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        // moving right
        if (targetCol > preCol) {
            for (c in preCol + 1 until targetCol) {
                for (piece in GamePanel().simPieces) {
                    if (piece.col == c && piece.row == targetRow) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        // moving up
        if (targetRow < preRow) {
            for (r in preRow - 1 downTo targetRow + 1) {
                for (piece in GamePanel().simPieces) {
                    if (piece.col == targetCol && piece.row == r) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        // moving down
        if (targetRow > preRow) {
            for (r in preRow + 1 until targetRow) {
                for (piece in GamePanel().simPieces) {
                    if (piece.col == targetCol && piece.row == r) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        return false
    }

    fun pieceIsOnDiagonalLine(targetCol: Int, targetRow: Int): Boolean {
        if (targetRow < preRow) {
            // up-left
            for (c in preCol - 1 downTo targetCol + 1) {
                val diff = abs(c - preCol)
                for (piece in GamePanel().simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
            // up-right
            for (c in preCol + 1 until targetCol) {
                val diff = abs(c - preCol)
                for (piece in GamePanel().simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        if (targetRow > preRow) {
            // down-left
            for (c in preCol - 1 downTo targetCol + 1) {
                val diff = abs(c - preCol)
                for (piece in GamePanel().simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
            // down-right
            for (c in preCol + 1 until targetCol) {
                val diff = abs(c - preCol)
                for (piece in GamePanel().simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        return false
    }
}
