package com.example.game2dgreatwar.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.game2dgreatwar.databinding.DialogGameOverBinding

class DialogGameOver : DialogFragment() {
    private var binding: DialogGameOverBinding? = null
    var onConfirmListener: OnClickListener? = null

    interface OnClickListener {
        fun onConfirm()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogGameOverBinding.inflate(inflater, container, false)
        binding?.btnOK?.setOnClickListener {
            dismiss()
            onConfirmListener?.onConfirm()
        }
        return binding?.root
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}