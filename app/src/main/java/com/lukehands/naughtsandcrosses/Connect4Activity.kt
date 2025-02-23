package com.lukehands.naughtsandcrosses

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class Connect4Activity : AppCompatActivity() {
    private lateinit var boardGridLayout: GridLayout
    private lateinit var statusTextView: TextView
    private lateinit var cells: Array<Array<ImageView>>
    private lateinit var board: Array<Array<Int>>
    private var currentPlayer = 1 // 1 for red, 2 for yellow
    private var gameActive = true
    private var isAnimating = false
    private val ROWS = 6
    private val COLS = 7
    private val mainScope = MainScope()

    // Initialize MediaPlayers
    private lateinit var dinkPlayer: MediaPlayer
    private lateinit var donkPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect4)

        // Initialize MediaPlayers
        dinkPlayer = MediaPlayer.create(this, R.raw.dink)
        donkPlayer = MediaPlayer.create(this, R.raw.donk)

        boardGridLayout = findViewById(R.id.boardGridLayout)
        statusTextView = findViewById(R.id.statusTextView)
        findViewById<Button>(R.id.resetButton).setOnClickListener { resetGame() }

        initializeBoard()
    }

    private fun initializeBoard() {
        cells = Array(ROWS) { Array(COLS) { ImageView(this) } }
        board = Array(ROWS) { Array(COLS) { 0 } }

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val cell = ImageView(this).apply {
                    background = ContextCompat.getDrawable(context, R.drawable.button_background)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = resources.displayMetrics.density.toInt() * 48
                        height = resources.displayMetrics.density.toInt() * 48
                        setMargins(4, 4, 4, 4)
                    }
                    tag = "$row,$col"
                    setOnClickListener { makeMove(col) }
                    elevation = resources.displayMetrics.density * 4 // Add elevation for 3D effect
                }
                cells[row][col] = cell
                boardGridLayout.addView(cell)
            }
        }
        updateBoard()
    }

    private fun makeMove(col: Int) {
        if (!gameActive || isAnimating) return

        val row = getLowestEmptyRow(col)
        if (row == -1) return

        board[row][col] = currentPlayer
        updateBoard()

        // Play sound based on current player
        if (currentPlayer == 1) {
            dinkPlayer.start()
        } else {
            donkPlayer.start()
        }

        if (checkWin(row, col)) {
            isAnimating = true
            val winningCells = getWinningCells(row, col)
            highlightAndShowWinner(winningCells, currentPlayer)
            gameActive = false
            return
        }

        if (checkDraw()) {
            showDrawDialog()
            gameActive = false
            return
        }

        currentPlayer = if (currentPlayer == 1) 2 else 1
        statusTextView.text = if (currentPlayer == 1) "Red's Turn" else "Yellow's Turn"
    }

    private fun getWinningCells(row: Int, col: Int): List<Pair<Int, Int>> {
        val directions = arrayOf(
            Pair(0, 1),  // Horizontal
            Pair(1, 0),  // Vertical
            Pair(1, 1),  // Diagonal /
            Pair(1, -1)  // Diagonal \
        )

        for ((deltaRow, deltaCol) in directions) {
            val cells = mutableListOf(Pair(row, col))
            var count = 1

            // Check forward direction
            var r = row + deltaRow
            var c = col + deltaCol
            while (r in 0 until ROWS && c in 0 until COLS && board[r][c] == currentPlayer) {
                cells.add(Pair(r, c))
                count++
                r += deltaRow
                c += deltaCol
            }

            // Check backward direction
            r = row - deltaRow
            c = col - deltaCol
            while (r in 0 until ROWS && c in 0 until COLS && board[r][c] == currentPlayer) {
                cells.add(Pair(r, c))
                count++
                r -= deltaRow
                c -= deltaCol
            }

            if (count >= 4) {
                return cells
            }
        }
        return emptyList()
    }

    private fun highlightAndShowWinner(winningCells: List<Pair<Int, Int>>, player: Int) {
        mainScope.launch {
            setAllCellsToHalfTransparent()
            highlightWinningCells(winningCells)
            delay(2000) // Flash for 2 seconds
            showWinnerDialog(player)
        }
    }

    private fun setAllCellsToHalfTransparent() {
        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                cells[row][col].alpha = 0.5f
            }
        }
    }

    private suspend fun highlightWinningCells(winningCells: List<Pair<Int, Int>>) {
        repeat(5) { // Flash 5 times
            winningCells.forEach { (row, col) ->
                cells[row][col].alpha = 1.0f
            }
            delay(200)
            winningCells.forEach { (row, col) ->
                cells[row][col].alpha = 0.5f
            }
            delay(200)
        }
        // Set the winning cells to fully opaque at the end
        winningCells.forEach { (row, col) ->
            cells[row][col].alpha = 1.0f
            // Add continuous pulsing animation
            val pulseAnimation = AlphaAnimation(1.0f, 0.5f).apply {
                duration = 1000
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
            }
            cells[row][col].startAnimation(pulseAnimation)
        }
    }

    private fun showWinnerDialog(player: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Over")
        builder.setMessage(if (player == 1) "Red Wins!" else "Yellow Wins!")
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
        builder.setMessage("It's a Draw!")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            resetGame()
        }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

    private fun getLowestEmptyRow(col: Int): Int {
        for (row in ROWS - 1 downTo 0) {
            if (board[row][col] == 0) return row
        }
        return -1
    }

    private fun updateBoard() {
        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val cell = cells[row][col]
                when (board[row][col]) {
                    0 -> {
                        cell.setBackgroundResource(R.drawable.button_background)
                        cell.alpha = 1.0f
                    }
                    1 -> {
                        cell.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                        cell.alpha = 1.0f
                    }
                    2 -> {
                        cell.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
                        cell.alpha = 1.0f
                    }
                }
            }
        }
    }

    private fun checkWin(row: Int, col: Int): Boolean {
        val directions = arrayOf(
            Pair(0, 1),  // Horizontal
            Pair(1, 0),  // Vertical
            Pair(1, 1),  // Diagonal /
            Pair(1, -1)  // Diagonal \
        )

        for ((deltaRow, deltaCol) in directions) {
            var count = 1
            count += countInDirection(row, col, deltaRow, deltaCol)
            count += countInDirection(row, col, -deltaRow, -deltaCol)
            if (count >= 4) return true
        }
        return false
    }

    private fun countInDirection(row: Int, col: Int, deltaRow: Int, deltaCol: Int): Int {
        var count = 0
        var currentRow = row + deltaRow
        var currentCol = col + deltaCol
        val player = board[row][col]

        while (currentRow in 0 until ROWS && 
               currentCol in 0 until COLS && 
               board[currentRow][currentCol] == player) {
            count++
            currentRow += deltaRow
            currentCol += deltaCol
        }
        return count
    }

    private fun checkDraw(): Boolean {
        return board.all { row -> row.all { it != 0 } }
    }

    private fun resetGame() {
        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                board[row][col] = 0
                cells[row][col].clearAnimation() // Clear any running animations
                cells[row][col].alpha = 1.0f
            }
        }
        currentPlayer = 1
        gameActive = true
        isAnimating = false
        statusTextView.text = "Red's Turn"
        updateBoard()
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
        // Cancel any running coroutines
        mainScope.cancel()
    }
}