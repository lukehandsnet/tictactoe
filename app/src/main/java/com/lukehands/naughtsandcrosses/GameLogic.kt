package com.lukehands.naughtsandcrosses

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