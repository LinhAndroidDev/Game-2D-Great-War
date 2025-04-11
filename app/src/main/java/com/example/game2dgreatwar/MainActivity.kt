package com.example.game2dgreatwar

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.game2dgreatwar.dialog.DialogGameOver

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set content view to game, so that objects in the Game class can be rendered to the screen
        val game = Game(this)
        game.gameOverListener = Game.GameOverListener {
            runOnUiThread {
                val dialogGameOver = DialogGameOver()
                dialogGameOver.setCancelable(false)
                dialogGameOver.show(supportFragmentManager, "gameOver")
                dialogGameOver.onConfirmListener = object : DialogGameOver.OnClickListener {
                    override fun onConfirm() {
                        game.resetGame()
                    }

                }
            }
        }
        setContentView(game)
        setUpFullScreen()
    }

    private fun setUpFullScreen() {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
    }
}