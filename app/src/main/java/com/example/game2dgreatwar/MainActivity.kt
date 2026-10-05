package com.example.game2dgreatwar

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.game2dgreatwar.databinding.ActivityMainBinding
import com.example.game2dgreatwar.dialog.DialogGameOver
import com.example.game2dgreatwar.dialog.DialogVictory
import com.example.game2dgreatwar.save.GameSaveRepository
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var saveRepository: GameSaveRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        saveRepository = GameSaveRepository(this)
        setupPause()

        binding?.gameView?.gameOverListener = Game.GameOverListener {
            // A lost game can't be continued
            saveRepository.delete()
            runOnUiThread {
                binding?.btnPause?.isVisible = false
                val dialogGameOver = DialogGameOver()
                dialogGameOver.setCancelable(false)
                dialogGameOver.show(supportFragmentManager, "gameOver")
                dialogGameOver.onConfirmListener = object : DialogGameOver.OnClickListener {
                    override fun onConfirm() {
                        binding?.gameView?.resetGame()
                        binding?.btnPause?.isVisible = true
                    }
                }
            }
        }

        binding?.gameView?.victoryListener = Game.VictoryListener {
            // A won game can't be continued
            saveRepository.delete()
            runOnUiThread {
                binding?.btnPause?.isVisible = false
                val dialogVictory = DialogVictory()
                dialogVictory.setCancelable(false)
                dialogVictory.show(supportFragmentManager, "victory")
                dialogVictory.onConfirmListener = object : DialogVictory.OnClickListener {
                    override fun onConfirm() {
                        binding?.gameView?.resetGame()
                        binding?.btnPause?.isVisible = true
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })

        // Also load when the activity is recreated (process killed in background, config change):
        // the Game view is brand new then, and would otherwise overwrite the save on next pause
        val isRecreated = savedInstanceState != null
        if (intent.getBooleanExtra(EXTRA_CONTINUE, false) || (isRecreated && saveRepository.hasSave())) {
            loadSavedGame()
        }

        setUpFullScreen()
    }

    override fun onPause() {
        super.onPause()
        // Freeze and save when leaving the app (Home, screen off, Recents, being killed...);
        // the pause overlay waits for the player on return
        binding?.gameView?.pause()
        saveGameIfPossible()
    }

    private fun setupPause() {
        binding?.gameView?.pauseStateListener = Game.PauseStateListener { paused ->
            runOnUiThread { binding?.layoutPause?.isVisible = paused }
        }
        binding?.btnPause?.setOnClickListener {
            binding?.gameView?.pause()
            saveGameIfPossible()
        }
        binding?.btnResume?.setOnClickListener { binding?.gameView?.resume() }
        binding?.btnRestart?.setOnClickListener {
            saveRepository.delete()
            binding?.gameView?.resetGame()
        }
        binding?.btnHome?.setOnClickListener { goHome() }
    }

    private fun loadSavedGame() {
        val state = saveRepository.load()
        val restored = state != null && binding?.gameView?.restoreSaveState(state) == true
        if (!restored) {
            // Unusable save: drop it and start a fresh game instead
            Log.w(TAG, "Saved game could not be loaded, starting a new game")
            saveRepository.delete()
            binding?.gameView?.resetGame()
        }
    }

    private fun saveGameIfPossible() {
        val gameView = binding?.gameView ?: return
        if (!gameView.canBeSaved()) {
            return
        }
        try {
            saveRepository.save(gameView.createSaveState())
        } catch (e: JSONException) {
            Log.e(TAG, "Could not save game", e)
        }
    }

    private fun goHome() {
        binding?.gameView?.pause()
        saveGameIfPossible()
        // HomeActivity is still below us in the back stack
        finish()
    }

    private fun setUpFullScreen() {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
    }

    companion object {
        const val EXTRA_CONTINUE = "extra_continue"
        private const val TAG = "MainActivity"
    }
}
