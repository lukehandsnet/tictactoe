package com.lukehands.naughtsandcrosses

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class TicTacToeActivity : AppCompatActivity() {
    private val gameLogic = GameLogic()
    private var currentPlayer = 1 // 1: player 1, 2: player 2
    private val mainScope = MainScope()
    private var isAnimating = false // Flag to indicate if an animation is in progress

    // Initialize MediaPlayers
    private lateinit var dinkPlayer: MediaPlayer
    private lateinit var donkPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac_toe)

        // Initialize MediaPlayers
        dinkPlayer = MediaPlayer.create(this, R.raw.dink)
        donkPlayer = MediaPlayer.create(this, R.raw.donk)

        if (!::dinkPlayer.isInitialized || !::donkPlayer.isInitialized) {
            Log.e("TicTacToeActivity", "MediaPlayer initialization failed")
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
                            Log.d("TicTacToeActivity", "Playing dink sound")
                            dinkPlayer.start()
                        } else {
                            Log.d("TicTacToeActivity", "Playing donk sound")
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