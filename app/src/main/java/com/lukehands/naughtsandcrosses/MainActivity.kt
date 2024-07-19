/*
 * Naughts and Crosses (Tic-Tac-Toe) game for Android
 * Developed by Luke Hands
 * Date: 2021-08-02
 */

package com.lukehands.naughtsandcrosses

// Import necessary classes

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val gameLogic = GameLogic()
    private var currentPlayer = 1 // 1: player 1, 2: player 2
    private val mainScope = MainScope()
    private var isAnimating = false // Flag to indicate if an animation is in progress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        if (gridLayout == null) {
            Toast.makeText(this, "Error: GridLayout not found", Toast.LENGTH_LONG).show()
            return
        }

        var buttonIndex = 0
        for (i in 0 until gridLayout.childCount) {
            val view = gridLayout.getChildAt(i)
            if (view is Button) {
                val row = buttonIndex / 3
                val col = buttonIndex % 3
                buttonIndex++
                view.setOnClickListener {
                    if (!isAnimating && gameLogic.makeMove(row, col, currentPlayer)) {
                        // Update UI
                        val drawableRes = if (currentPlayer == 1) R.drawable.x else R.drawable.o
                        view.setBackgroundResource(drawableRes)

                        if (gameLogic.checkWin(currentPlayer)) {
                            isAnimating = true
                            highlightAndShowWinner(gameLogic.getWinningLine(), currentPlayer)
                        } else if (gameLogic.isDraw()) {
                            showDrawDialog()
                        } else {
                            currentPlayer = if (currentPlayer == 1) 2 else 1
                        }
                    }
                }
            }
        }
    }

    private fun highlightAndShowWinner(line: List<Pair<Int, Int>>, player: Int) {
        mainScope.launch {
            setAllButtonsToHalfTransparent()
            highlightWinningLine(line)
            delay(2000) // Flash for 2 seconds
            showWinnerDialog(player)
        }
    }

    private fun setAllButtonsToHalfTransparent() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        for (i in 0 until gridLayout.childCount) {
            val view = gridLayout.getChildAt(i)
            if (view is Button) {
                view.alpha = 0.5f // Set transparency to 50%
            }
        }
    }

    private suspend fun highlightWinningLine(line: List<Pair<Int, Int>>) {
        repeat(5) { // Flash 5 times
            line.forEach { (row, col) ->
                val button = findViewById<Button>(resources.getIdentifier("button_${row}${col}", "id", packageName))
                button.alpha = 1.0f // Set transparency to 100%
            }
            delay(200)
            line.forEach { (row, col) ->
                val button = findViewById<Button>(resources.getIdentifier("button_${row}${col}", "id", packageName))
                button.alpha = 0.5f // Set transparency to 50%
            }
            delay(200)
        }
        // Set the winning line to fully opaque at the end
        line.forEach { (row, col) ->
            val button = findViewById<Button>(resources.getIdentifier("button_${row}${col}", "id", packageName))
            button.alpha = 1.0f // Set transparency to 100%
        }
    }

    private fun showWinnerDialog(player: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Over")
        builder.setMessage("Player $player wins!")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            resetGame()
        }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDrawDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Over")
        builder.setMessage("It's a draw!")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            resetGame()
        }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

    private fun resetGame() {
        gameLogic.resetGame()
        resetUI()
        isAnimating = false // Reset animation flag
    }

    private fun resetUI() {
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)
        for (i in 0 until gridLayout.childCount) {
            val view = gridLayout.getChildAt(i)
            if (view is Button) {
                view.setBackgroundResource(android.R.color.transparent)
                view.alpha = 1.0f // Reset transparency
            }
        }
        currentPlayer = 1
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

class GameLogic {
    private val board = Array(3) { IntArray(3) } // 0: empty, 1: player 1, 2: player 2
    private var winningLine: List<Pair<Int, Int>> = emptyList()

    fun checkWin(player: Int): Boolean {
        // Check rows, columns, and diagonals
        for (i in 0..2) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                winningLine = listOf(Pair(i, 0), Pair(i, 1), Pair(i, 2))
                return true
            }
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) {
                winningLine = listOf(Pair(0, i), Pair(1, i), Pair(2, i))
                return true
            }
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
            winningLine = listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
            return true
        }
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
            winningLine = listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
            return true
        }
        return false
    }

    fun getWinningLine(): List<Pair<Int, Int>> {
        return winningLine
    }

    fun getPlayer(row: Int, col: Int): Int {
        return board[row][col]
    }

    fun isDraw(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == 0) return false
            }
        }
        return true
    }

    fun resetGame() {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = 0
            }
        }
        winningLine = emptyList()
    }

    fun makeMove(row: Int, col: Int, player: Int): Boolean {
        if (board[row][col] == 0) {
            board[row][col] = player
            return true
        }
        return false
    }
}
