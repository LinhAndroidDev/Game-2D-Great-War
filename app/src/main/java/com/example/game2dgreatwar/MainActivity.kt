package com.example.game2dgreatwar

import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.game2dgreatwar.databinding.ActivityMainBinding
import com.example.game2dgreatwar.dialog.DialogGameOver

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val items = listOf("Item 1", "Item 2", "Item 3")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.btnFps?.adapter = adapter

        binding?.gameView?.gameOverListener = Game.GameOverListener {
            runOnUiThread {
                val dialogGameOver = DialogGameOver()
                dialogGameOver.setCancelable(false)
                dialogGameOver.show(supportFragmentManager, "gameOver")
                dialogGameOver.onConfirmListener = object : DialogGameOver.OnClickListener {
                    override fun onConfirm() {
                        binding?.gameView?.resetGame()
                    }

                }
            }
        }
        setUpFullScreen()
    }

    private fun setUpFullScreen() {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
    }
}