package com.example.webappdev.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webappdev.engine.main.Board
import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.GamePanel.Companion.WHITE
import com.example.webappdev.model.User
import com.example.webappdev.ui.components.Footer
import com.example.webappdev.ui.components.Navbar

@Composable
fun GameScreen(user: User, onExitGame: () -> Unit, onRestart: () -> Unit) {
    val controller = remember { GamePanel() }
    var board by remember { mutableStateOf(controller.getCurrentLayout()) }
    var status by remember { mutableStateOf("Choose color to start") }
    var playerColor by remember { mutableStateOf<Int?>(null) } // WHITE or BLACK
    var showPromotion by remember { mutableStateOf(false) }
    var capturedPieces by remember { mutableStateOf(mutableListOf<String>()) }

    LaunchedEffect(playerColor) {
        if (playerColor != null) {
            controller.initialize()
            board = controller.getCurrentLayout()
            status = "White to move"
        }
    }

    Scaffold(topBar = { Navbar(user = user, isInGame = true) },
        bottomBar = { Footer(isInGame = playerColor != null, onExitGame = onExitGame, onRestart = { controller.resetGame(); board = controller.getCurrentLayout(); status = "White to move"; capturedPieces.clear(); onRestart() }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (playerColor == null) {
                Text("Choose color", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { playerColor = WHITE }) { Text("Play White") }
                    Button(onClick = { playerColor = GamePanel.BLACK }) { Text("Play Black") }
                }
                Spacer(Modifier.height(8.dp))
                Text("Your color sits closest to the footer.")
                return@Column
            }

            Text(status, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            ChessBoard(controller = controller, engineBoard = board, playerColor = playerColor!!, onMoveComplete = {
                // refresh board and status
                val before = board.flatten().count { it.isNotEmpty() }
                board = controller.getCurrentLayout()
                val after = board.flatten().count { it.isNotEmpty() }
                if (after < before) {
                    // simple capture increment (improve later)
                    capturedPieces.add("bP")
                }
                if (controller.promotion) {
                    showPromotion = true
                    return@ChessBoard
                }
                if (controller.gameOver) { status = "Checkmate!" ; return@ChessBoard }
                if (controller.stalemate) { status = "Stalemate!" ; return@ChessBoard }
                status = if (controller.currentColor == WHITE) "White to move" else "Black to move"
            })

            Spacer(Modifier.height(12.dp))
            if (capturedPieces.isNotEmpty()) {
                Text("Captured: ${capturedPieces.joinToString(" "){ pieceToSymbol(it)}}")
            }

            if (showPromotion) {
                PromotionDialog { idx ->
                    controller.promoteTo(idx)
                    board = controller.getCurrentLayout()
                    showPromotion = false
                }
            }
        }
    }
}

@Composable
fun ChessBoard(
    controller: GamePanel,
    engineBoard: Array<Array<String>>,
    playerColor: Int,
    onMoveComplete: () -> Unit
) {
    val light = Color(0xFFEEEED2); val dark = Color(0xFF769656)
    val moveH = Color(0x8034C759); val capH = Color(0x80FF0000); val selH = Color(0x803A86FF)

    var selectedEngine by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // mapping functions (full 180 rotation when player is BLACK)
    fun uiToEngine(uiCol: Int, uiRow: Int): Pair<Int, Int> {
        val engineRow = 7 - uiRow
        val engineCol = if (playerColor == WHITE) uiCol else 7 - uiCol
        return engineCol to engineRow
    }

    Column(modifier = Modifier.size(360.dp)) {
        for (uiRow in 0 until 8) {
            Row(modifier = Modifier.weight(1f)) {
                for (uiCol in 0 until 8) {
                    val (eCol,eRow) = uiToEngine(uiCol, uiRow)
                    val isSel = selectedEngine == (eCol to eRow)
                    val isMove = controller.validMoves.contains(eCol to eRow)
                    val isCap = controller.captureMoves.contains(eCol to eRow)
                    val base = if ((uiRow + uiCol) % 2 == 0) light else dark
                    val bg = when {
                        isSel -> selH
                        isCap -> capH
                        isMove -> moveH
                        else -> base
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(bg).clickable {
                        val piece = controller.findPieceAt(eCol, eRow)
                        if (selectedEngine == null) {
                            if (piece != null && piece.color == controller.currentColor) {
                                selectedEngine = eCol to eRow
                                val px = eCol * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
                                val py = eRow * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
                                controller.touchDown(px, py)
                                controller.touchMove(px, py) // force simulate to populate highlights
                            }
                        } else {
                            // attempt move
                            val from = selectedEngine!!
                            val to = eCol to eRow
                            val fromPx = from.first * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
                            val fromPy = from.second * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
                            val toPx = to.first * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
                            val toPy = to.second * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
                            controller.touchDown(fromPx, fromPy)
                            controller.touchMove(toPx, toPy)
                            controller.touchUp(toPx, toPy)
                            selectedEngine = null
                            controller.clearHighlights()
                            onMoveComplete()
                        }
                    }, contentAlignment = Alignment.Center) {
                        val pc = engineBoard[eRow][eCol]
                        if (pc.isNotEmpty()) {
                            Text(text = pieceToSymbol(pc), fontSize = 26.sp, fontWeight = FontWeight.Bold,
                                color = if (pc.startsWith("w")) Color.Black else Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun pieceToSymbol(code: String) = when (code) {
    "wK" -> "♔"; "wQ" -> "♕"; "wR" -> "♖"; "wB" -> "♗"; "wN" -> "♘"; "wP" -> "♙"
    "bK" -> "♚"; "bQ" -> "♛"; "bR" -> "♜"; "bB" -> "♝"; "bN" -> "♞"; "bP" -> "♟"
    else -> ""
}

@Composable
fun PromotionDialog(onSelect: (Int)->Unit) {
    AlertDialog(onDismissRequest = {}, title = { Text("Promote Pawn") }, text = {
        Column { listOf("Queen","Rook","Bishop","Knight").forEachIndexed { i,n -> TextButton(onClick = { onSelect(i) }){ Text(n) } } }
    }, confirmButton = {})
}
