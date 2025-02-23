package com.lukehands.naughtsandcrosses

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.random.Random

class Connect4Activity : AppCompatActivity() {
    private lateinit var boardGridLayout: GridLayout
    private lateinit var statusTextView: TextView
    private lateinit var orientationGuide: TextView
    private lateinit var planetsContainer: FrameLayout
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
    private lateinit var winPlayer: MediaPlayer
    private lateinit var drawPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect4)

        // Initialize MediaPlayers
        dinkPlayer = MediaPlayer.create(this, R.raw.dink)
        donkPlayer = MediaPlayer.create(this, R.raw.donk)
        winPlayer = MediaPlayer.create(this, R.raw.win)
        drawPlayer = MediaPlayer.create(this, R.raw.draw)

        boardGridLayout = findViewById(R.id.boardGridLayout)
        statusTextView = findViewById(R.id.statusTextView)
        orientationGuide = findViewById(R.id.orientationGuide)
        planetsContainer = findViewById(R.id.planetsContainer)

        findViewById<Button>(R.id.resetButton).setOnClickListener { resetGame() }

        // Start floating planets animation
        startFloatingPlanetsAnimation()

        initializeBoard()
        checkOrientation(resources.configuration.orientation)
    }

    private fun startFloatingPlanetsAnimation() {
        mainScope.launch {
            while (true) {
                addFloatingPlanet()
                delay(Random.nextLong(2000, 5000))
            }
        }
    }

    private fun addFloatingPlanet() {
        val planet = ImageView(this).apply {
            setImageResource(R.drawable.planet_${Random.nextInt(1, 5)})
            alpha = 0.6f
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val size = Random.nextInt(40, 100)
        val params = FrameLayout.LayoutParams(size, size)
        
        // Random starting position
        val startX = Random.nextInt(-50, planetsContainer.width + 50)
        val startY = planetsContainer.height + 100
        
        planet.x = startX.toFloat()
        planet.y = startY.toFloat()
        
        planetsContainer.addView(planet, params)

        // Create animation set
        val animSet = AnimationSet(true).apply {
            // Translate animation
            val translateAnim = TranslateAnimation(
                0f, Random.nextFloat() * 200 - 100,  // Random X movement
                0f, -planetsContainer.height.toFloat() - 200  // Move up
            ).apply {
                duration = Random.nextLong(10000, 20000)
            }

            // Rotation animation
            val rotateAnim = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = Random.nextLong(5000, 15000)
                repeatCount = Animation.INFINITE
            }

            addAnimation(translateAnim)
            addAnimation(rotateAnim)

            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    planetsContainer.removeView(planet)
                }
            })
        }

        planet.startAnimation(animSet)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkOrientation(newConfig.orientation)
    }

    private fun checkOrientation(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientationGuide.visibility = View.VISIBLE
            orientationGuide.postDelayed({
                orientationGuide.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction { orientationGuide.visibility = View.GONE }
            }, 3000)
        } else {
            orientationGuide.visibility = View.GONE
        }
    }

    private fun initializeBoard() {
        cells = Array(ROWS) { Array(COLS) { ImageView(this) } }
        board = Array(ROWS) { Array(COLS) { 0 } }

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val cell = ImageView(this).apply {
                    setBackgroundResource(R.drawable.cell_empty)
                    layoutParams = GridLayout.LayoutParams().apply {
                        val cellSize = resources.displayMetrics.density.toInt() * 48
                        width = cellSize
                        height = cellSize
                        setMargins(4, 4, 4, 4)
                    }
                    tag = "$row,$col"
                    setOnClickListener { makeMove(col) }
                    elevation = resources.displayMetrics.density * 4
                }
                cells[row][col] = cell
                boardGridLayout.addView(cell)

                // Add entry animation
                cell.alpha = 0f
                cell.scaleX = 0.5f
                cell.scaleY = 0.5f
                cell.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay((row * COLS + col) * 50L)
                    .setDuration(300)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }
        }
        updateBoard()
    }

    private fun makeMove(col: Int) {
        if (!gameActive || isAnimating) return

        val row = getLowestEmptyRow(col)
        if (row == -1) return

        isAnimating = true
        board[row][col] = currentPlayer

        // Animate the piece dropping
        val cell = cells[row][col]
        cell.y = -cell.height.toFloat() * (row + 1)
        cell.alpha = 1f
        cell.setBackgroundResource(if (currentPlayer == 1) R.drawable.cell_red else R.drawable.cell_yellow)
        
        cell.animate()
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(BounceInterpolator())
            .withEndAction {
                isAnimating = false
                
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
                    winPlayer.start()
                    return@withEndAction
                }

                if (checkDraw()) {
                    showDrawDialog()
                    gameActive = false
                    drawPlayer.start()
                    return@withEndAction
                }

                currentPlayer = if (currentPlayer == 1) 2 else 1
                statusTextView.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction {
                        statusTextView.text = if (currentPlayer == 1) "Red's Turn" else "Yellow's Turn"
                        statusTextView.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            }
            .start()
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
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("🎉 Game Over 🎉")
        builder.setMessage(if (player == 1) "Red Wins! 🔴" else "Yellow Wins! 🟡")
        builder.setPositiveButton("Play Again! 🎮") { dialog, _ ->
            dialog.dismiss()
            resetGame()
        }
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDrawDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("🤝 Game Over 🤝")
        builder.setMessage("It's a Draw! Well played! 🌟")
        builder.setPositiveButton("Play Again! 🎮") { dialog, _ ->
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
        // Stop all animations
        mainScope.launch {
            // First fade out all cells
            for (row in 0 until ROWS) {
                for (col in 0 until COLS) {
                    val cell = cells[row][col]
                    cell.clearAnimation()
                    cell.animate()
                        .alpha(0f)
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(200)
                        .setStartDelay((row * COLS + col) * 30L)
                        .start()
                }
            }
            
            delay(500) // Wait for fade out

            // Reset the game state
            board = Array(ROWS) { Array(COLS) { 0 } }
            currentPlayer = 1
            gameActive = true
            isAnimating = false
            
            // Update status with animation
            statusTextView.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    statusTextView.text = "Red's Turn"
                    statusTextView.animate()
                        .alpha(1f)
                        .setDuration(150)
                        .start()
                }
                .start()

            // Fade in all cells with new state
            for (row in 0 until ROWS) {
                for (col in 0 until COLS) {
                    val cell = cells[row][col]
                    cell.setBackgroundResource(R.drawable.cell_empty)
                    cell.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .setStartDelay((row * COLS + col) * 50L)
                        .setInterpolator(OvershootInterpolator())
                        .start()
                }
            }
        }
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