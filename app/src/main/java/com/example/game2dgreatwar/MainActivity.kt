package com.example.game2dgreatwar

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.game2dgreatwar.databinding.ActivityMainBinding
import com.example.game2dgreatwar.dialog.DialogGameOver
import com.example.game2dgreatwar.dialog.DialogVictory

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private var ignoreSpinnerCallback = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupLevelSpinner()
        setupPause()

        binding?.gameView?.gameOverListener = Game.GameOverListener {
            runOnUiThread {
                binding?.btnPause?.isVisible = false
                val dialogGameOver = DialogGameOver()
                dialogGameOver.setCancelable(false)
                dialogGameOver.show(supportFragmentManager, "gameOver")
                dialogGameOver.onConfirmListener = object : DialogGameOver.OnClickListener {
                    override fun onConfirm() {
                        binding?.gameView?.resetGame()
                        syncLevelSpinner(1)
                        binding?.btnPause?.isVisible = true
                    }
                }
            }
        }

        binding?.gameView?.victoryListener = Game.VictoryListener {
            runOnUiThread {
                binding?.btnPause?.isVisible = false
                val dialogVictory = DialogVictory()
                dialogVictory.setCancelable(false)
                dialogVictory.show(supportFragmentManager, "victory")
                dialogVictory.onConfirmListener = object : DialogVictory.OnClickListener {
                    override fun onConfirm() {
                        binding?.gameView?.resetGame()
                        syncLevelSpinner(1)
                        binding?.btnPause?.isVisible = true
                    }
                }
            }
        }

        binding?.gameView?.levelChangedListener = Game.LevelChangedListener { level ->
            runOnUiThread { syncLevelSpinner(level) }
        }

        setUpFullScreen()
    }

    override fun onPause() {
        super.onPause()
        // Freeze the game when leaving the app; the pause overlay waits for the player on return
        binding?.gameView?.pause()
    }

    private fun setupPause() {
        binding?.gameView?.pauseStateListener = Game.PauseStateListener { paused ->
            runOnUiThread { binding?.layoutPause?.isVisible = paused }
        }
        binding?.btnPause?.setOnClickListener { binding?.gameView?.pause() }
        binding?.btnResume?.setOnClickListener { binding?.gameView?.resume() }
        binding?.btnRestart?.setOnClickListener {
            binding?.gameView?.resetGame()
            syncLevelSpinner(1)
        }
    }

    private fun setupLevelSpinner() {
        val items = listOf("Cấp độ 1", "Cấp độ 2", "Cấp độ 3", "Cấp độ 4")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinnerLevel?.adapter = adapter

        binding?.spinnerLevel?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (ignoreSpinnerCallback) {
                    ignoreSpinnerCallback = false
                    return
                }
                val level = position + 1
                binding?.gameView?.startAtLevel(level)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun syncLevelSpinner(level: Int) {
        ignoreSpinnerCallback = true
        binding?.spinnerLevel?.setSelection((level - 1).coerceIn(0, 3))
    }

    private fun setUpFullScreen() {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
    }
}
