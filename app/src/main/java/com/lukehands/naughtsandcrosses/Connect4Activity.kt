package com.lukehands.naughtsandcrosses

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Connect4Activity : AppCompatActivity() {
    private lateinit var boardGridLayout: GridLayout
    private lateinit var statusTextView: TextView
    private lateinit var cells: Array<Array<ImageView>>
    private lateinit var board: Array<Array<Int>>
    private var currentPlayer = 1 // 1 for red, 2 for yellow
    private var gameActive = true
    private val ROWS = 6
    private val COLS = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect4)

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
                    setBackgroundResource(android.R.drawable.btn_default)
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = resources.displayMetrics.density.toInt() * 48
                        height = resources.displayMetrics.density.toInt() * 48
                        setMargins(4, 4, 4, 4)
                    }
                    tag = "$row,$col"
                    setOnClickListener { makeMove(col) }
                }
                cells[row][col] = cell
                boardGridLayout.addView(cell)
            }
        }
        updateBoard()
    }

    private fun makeMove(col: Int) {
        if (!gameActive) return

        val row = getLowestEmptyRow(col)
        if (row == -1) return

        board[row][col] = currentPlayer
        updateBoard()

        if (checkWin(row, col)) {
            statusTextView.text = if (currentPlayer == 1) "Red Wins!" else "Yellow Wins!"
            gameActive = false
            return
        }

        if (checkDraw()) {
            statusTextView.text = "It's a Draw!"
            gameActive = false
            return
        }

        currentPlayer = if (currentPlayer == 1) 2 else 1
        statusTextView.text = if (currentPlayer == 1) "Red's Turn" else "Yellow's Turn"
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
                    0 -> cell.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                    1 -> cell.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                    2 -> cell.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
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
            }
        }
        currentPlayer = 1
        gameActive = true
        statusTextView.text = "Red's Turn"
        updateBoard()
    }
}