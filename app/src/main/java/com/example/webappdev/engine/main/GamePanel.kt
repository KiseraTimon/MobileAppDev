package com.example.webappdev.engine.main

import com.example.webappdev.engine.piece.*
import kotlin.math.abs

class GamePanel {

    companion object { const val WHITE = 0; const val BLACK = 1 }

    val pieces = ArrayList<Piece>()
    val simPieces = ArrayList<Piece>()
    var castlingPiece: Piece? = null
    var activePiece: Piece? = null

    var currentColor = WHITE
    var promotion = false
    var gameOver = false
    var stalemate = false

    var validSquare = false
    var canMove = false

    // highlights
    val validMoves = mutableListOf<Pair<Int, Int>>()
    val captureMoves = mutableListOf<Pair<Int, Int>>()

    private val mouse = Mouse()

    fun clearHighlights() {
        validMoves.clear(); captureMoves.clear()
    }

    fun initialize() {
        pieces.clear(); simPieces.clear()
        activePiece = null; castlingPiece = null
        promotion = false; gameOver = false; stalemate = false
        currentColor = WHITE

        // White
        for (c in 0 until 8) pieces.add(Pawn(WHITE, c, 6))
        pieces.add(Rook(WHITE,0,7)); pieces.add(Rook(WHITE,7,7))
        pieces.add(Knight(WHITE,1,7)); pieces.add(Knight(WHITE,6,7))
        pieces.add(Bishop(WHITE,2,7)); pieces.add(Bishop(WHITE,5,7))
        pieces.add(Queen(WHITE,3,7)); pieces.add(King(WHITE,4,7))

        // Black
        for (c in 0 until 8) pieces.add(Pawn(BLACK, c, 1))
        pieces.add(Rook(BLACK,0,0)); pieces.add(Rook(BLACK,7,0))
        pieces.add(Knight(BLACK,1,0)); pieces.add(Knight(BLACK,6,0))
        pieces.add(Bishop(BLACK,2,0)); pieces.add(Bishop(BLACK,5,0))
        pieces.add(Queen(BLACK,3,0)); pieces.add(King(BLACK,4,0))

        // attach panel refs
        for (p in pieces) p.panel = this

        copyPieces(pieces, simPieces)
    }

    // Input wrappers (pixel coords)
    fun touchDown(px: Int, py: Int) {
        mouse.press(px, py)
        updateSelectionOnPress()
    }

    fun touchMove(px: Int, py: Int) {
        mouse.move(px, py)
        simulate()
    }

    fun touchUp(px: Int, py: Int) {
        mouse.release(px, py)
        finalizeMove()
    }

    // Adapter functions used by older code (kept for compatibility)
    fun selectSquare(col: Int, row: Int) {
        val px = col * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        val py = row * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        touchDown(px, py)
        touchMove(px, py)
    }

    fun moveSelectedTo(col: Int, row: Int) {
        val px = col * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        val py = row * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        touchMove(px, py); touchUp(px, py)
    }

    private fun updateSelectionOnPress() {
        if (activePiece == null) {
            val c = mouse.x / Board.SQUARE_SIZE
            val r = mouse.y / Board.SQUARE_SIZE
            for (p in simPieces) {
                if (p.col == c && p.row == r && p.color == currentColor) {
                    activePiece = p
                    break
                }
            }
        } else {
            // if already active just call simulate
            simulate()
        }
    }

    private fun simulate() {
        canMove = false; validSquare = false
        clearHighlights()
        copyPieces(pieces, simPieces)

        // reset castlingPiece placeholder
        if (castlingPiece != null) {
            castlingPiece!!.col = castlingPiece!!.preCol
            castlingPiece!!.x = castlingPiece!!.getX(castlingPiece!!.col)
            castlingPiece = null
        }

        val p = activePiece ?: return

        // move active piece with mouse
        p.x = mouse.x - Board.HALF_SQUARE_SIZE
        p.y = mouse.y - Board.HALF_SQUARE_SIZE
        p.col = p.getCol(p.x); p.row = p.getRow(p.y)

        if (p.canMove(p.col, p.row)) {
            canMove = true
            if (p.hittingPiece != null) simPieces.remove(p.hittingPiece)
            validSquare = true
        }
    }

    private fun finalizeMove() {
        val p = activePiece ?: return

        if (validSquare) {
            // apply simPieces into pieces
            copyPieces(simPieces, pieces)
            // find the moved piece in pieces with same pre coordinates? simpler: update position on moved piece reference
            // update position of the piece that matches the moved piece by type+preCol+preRow (robust engine would use IDs)
            for (piece in pieces) {
                if (piece.preCol == p.preCol && piece.preRow == p.preRow && piece.type == p.type && piece.color == p.color) {
                    piece.col = p.col; piece.row = p.row
                    piece.updatePosition()
                    break
                }
            }

            // handle castling's rook move
            if (castlingPiece != null) {
                // castlingPiece was set to the rook to be moved; adjust its preCol/preRow and final col in copyForSim logic
                castlingPiece!!.updatePosition()
            }

            // promotion?
            if (p.type != null && p.type!!.name == "PAWN") {
                if ((p.color == WHITE && p.row == 0) || (p.color == BLACK && p.row == 7)) {
                    promotion = true
                    // leave activePiece set so promoteTo can know location
                    activePiece = p
                    return
                }
            }

            // switch player
            changePlayer()
        } else {
            // revert piece
            p.resetPosition()
        }

        // cleanup
        activePiece = null
        clearHighlights()
        copyPieces(pieces, simPieces)
    }

    fun promoteTo(index: Int) {
        if (!promotion) return
        // find pawn in pieces matching activePiece pre coords
        val pawn = pieces.find { it.type?.name == "PAWN" && it.col == activePiece!!.col && it.row == activePiece!!.row && it.color == activePiece!!.color }
        if (pawn != null) pieces.remove(pawn)
        val col = activePiece!!.col; val row = activePiece!!.row; val color = activePiece!!.color
        val newPiece: Piece = when (index) {
            0 -> Queen(color, col, row)
            1 -> Rook(color, col, row)
            2 -> Bishop(color, col, row)
            3 -> Knight(color, col, row)
            else -> Queen(color, col, row)
        }
        newPiece.panel = this
        pieces.add(newPiece)
        promotion = false
        activePiece = null
        copyPieces(pieces, simPieces)
        changePlayer()
    }

    private fun changePlayer() {
        currentColor = if (currentColor == WHITE) BLACK else WHITE
        // reset twoStepped for that color's pieces
        for (p in pieces) if (p.color == currentColor) p.twoStepped = false
    }

    fun findPieceAt(col: Int, row: Int): Piece? {
        return pieces.find { it.col == col && it.row == row }
    }

    fun getCurrentLayout(): Array<Array<String>> {
        val grid = Array(8) { Array(8) { "" } }
        for (p in pieces) {
            val code = (if (p.color == WHITE) "w" else "b") + when (p.type) {
                com.example.webappdev.engine.main.Type.PAWN -> "P"
                com.example.webappdev.engine.main.Type.ROOK -> "R"
                com.example.webappdev.engine.main.Type.KNIGHT -> "N"
                com.example.webappdev.engine.main.Type.BISHOP -> "B"
                com.example.webappdev.engine.main.Type.QUEEN -> "Q"
                com.example.webappdev.engine.main.Type.KING -> "K"
                else -> "?"
            }
            if (p.row in 0..7 && p.col in 0..7) grid[p.row][p.col] = code
        }
        return grid
    }

    private fun copyPieces(source: ArrayList<Piece>, target: ArrayList<Piece>) {
        target.clear()
        for (p in source) {
            val cp = p.copyForSim()
            cp.panel = this
            target.add(cp)
        }
    }

    fun resetGame() = initialize()

    // helpers
    fun pixelToCol(px: Int) = (px + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
    fun pixelToRow(py: Int) = (py + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
}
