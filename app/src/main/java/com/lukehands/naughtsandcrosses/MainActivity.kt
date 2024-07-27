package com.lukehands.naughtsandcrosses

// Import necessary classes
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val gameLogic = GameLogic()
    private var currentPlayer = 1 // 1: player 1, 2: player 2
    private val mainScope = MainScope()
    private var isAnimating = false // Flag to indicate if an animation is in progress

    // Initialize MediaPlayers
    private lateinit var dinkPlayer: MediaPlayer
    private lateinit var donkPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize MediaPlayers
        dinkPlayer = MediaPlayer.create(this, R.raw.dink)
        donkPlayer = MediaPlayer.create(this, R.raw.donk)

        if (!::dinkPlayer.isInitialized || !::donkPlayer.isInitialized) {
            Log.e("MainActivity", "MediaPlayer initialization failed")
        }

        val buttons = arrayOf(
            arrayOf(findViewById<Button>(R.id.button_00), findViewById<Button>(R.id.button_01), findViewById<Button>(R.id.button_02)),
            arrayOf(findViewById<Button>(R.id.button_10), findViewById<Button>(R.id.button_11), findViewById<Button>(R.id.button_12)),
            arrayOf(findViewById<Button>(R.id.button_20), findViewById<Button>(R.id.button_21), findViewById<Button>(R.id.button_22))
        )

        for (row in buttons.indices) {
            for (col in buttons[row].indices) {
                buttons[row][col].setOnClickListener {
                    if (!isAnimating && gameLogic.makeMove(row, col, currentPlayer)) {
                        // Update UI
                        val drawableRes = if (currentPlayer == 1) R.drawable.x else R.drawable.o
                        buttons[row][col].setBackgroundResource(drawableRes)

                        // Play sound
                        if (currentPlayer == 1) {
                            Log.d("MainActivity", "Playing dink sound")
                            dinkPlayer.start()
                        } else {
                            Log.d("MainActivity", "Playing donk sound")
                            donkPlayer.start()
                        }

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
        val buttons = arrayOf(
            arrayOf(findViewById<Button>(R.id.button_00), findViewById<Button>(R.id.button_01), findViewById<Button>(R.id.button_02)),
            arrayOf(findViewById<Button>(R.id.button_10), findViewById<Button>(R.id.button_11), findViewById<Button>(R.id.button_12)),
            arrayOf(findViewById<Button>(R.id.button_20), findViewById<Button>(R.id.button_21), findViewById<Button>(R.id.button_22))
        )

        for (row in buttons.indices) {
            for (col in buttons[row].indices) {
                buttons[row][col].alpha = 0.5f // Set transparency to 50%
            }
        }
    }

    private suspend fun highlightWinningLine(line: List<Pair<Int, Int>>) {
        val buttons = arrayOf(
            arrayOf(findViewById<Button>(R.id.button_00), findViewById<Button>(R.id.button_01), findViewById<Button>(R.id.button_02)),
            arrayOf(findViewById<Button>(R.id.button_10), findViewById<Button>(R.id.button_11), findViewById<Button>(R.id.button_12)),
            arrayOf(findViewById<Button>(R.id.button_20), findViewById<Button>(R.id.button_21), findViewById<Button>(R.id.button_22))
        )

        repeat(5) { // Flash 5 times
            line.forEach { (row, col) ->
                buttons[row][col].alpha = 1.0f // Set transparency to 100%
            }
            delay(200)
            line.forEach { (row, col) ->
                buttons[row][col].alpha = 0.5f // Set transparency to 50%
            }
            delay(200)
        }
        // Set the winning line to fully opaque at the end
        line.forEach { (row, col) ->
            buttons[row][col].alpha = 1.0f // Set transparency to 100%
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
        val buttons = arrayOf(
            arrayOf(findViewById<Button>(R.id.button_00), findViewById<Button>(R.id.button_01), findViewById<Button>(R.id.button_02)),
            arrayOf(findViewById<Button>(R.id.button_10), findViewById<Button>(R.id.button_11), findViewById<Button>(R.id.button_12)),
            arrayOf(findViewById<Button>(R.id.button_20), findViewById<Button>(R.id.button_21), findViewById<Button>(R.id.button_22))
        )

        for (row in buttons.indices) {
            for (col in buttons[row].indices) {
                buttons[row][col].setBackgroundResource(android.R.color.transparent)
                buttons[row][col].alpha = 1.0f // Reset transparency
            }
        }
        currentPlayer = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayers
        if (::dinkPlayer.isInitialized) {
            dinkPlayer.release()
        }
        if (::donkPlayer.isInitialized) {
            donkPlayer.release()
        }
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
