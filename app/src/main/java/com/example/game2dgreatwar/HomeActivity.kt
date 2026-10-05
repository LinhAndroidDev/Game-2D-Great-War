package com.example.game2dgreatwar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.game2dgreatwar.databinding.ActivityHomeBinding
import com.example.game2dgreatwar.save.GameSaveRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeActivity : AppCompatActivity() {
    private var binding: ActivityHomeBinding? = null
    private lateinit var saveRepository: GameSaveRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        saveRepository = GameSaveRepository(this)

        binding?.btnContinue?.setOnClickListener { startGame(continueSavedGame = true) }
        binding?.btnNewGame?.setOnClickListener { onNewGameClicked() }
        binding?.btnExit?.setOnClickListener { finishAffinity() }

        setUpFullScreen()
    }

    override fun onResume() {
        super.onResume()
        // The save may have been created or deleted while playing
        refreshContinueButton()
    }

    private fun refreshContinueButton() {
        val hasSave = saveRepository.hasSave()
        binding?.btnContinue?.isEnabled = hasSave
        binding?.btnContinue?.alpha = if (hasSave) 1f else 0.4f
    }

    private fun onNewGameClicked() {
        if (!saveRepository.hasSave()) {
            startGame(continueSavedGame = false)
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Chơi mới")
            .setMessage("Bản lưu hiện tại sẽ bị xoá. Bạn có chắc muốn bắt đầu game mới?")
            .setNegativeButton("Huỷ", null)
            .setPositiveButton("Đồng ý") { _, _ ->
                saveRepository.delete()
                startGame(continueSavedGame = false)
            }
            .show()
    }

    private fun startGame(continueSavedGame: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_CONTINUE, continueSavedGame)
        startActivity(intent)
    }

    private fun setUpFullScreen() {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
    }
}
