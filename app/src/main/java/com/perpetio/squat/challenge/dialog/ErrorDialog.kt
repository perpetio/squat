package com.perpetio.squat.challenge.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ErrorDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity)
            .setMessage(requireArguments().getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.ok) { _, _ -> requireActivity().finish() }
            .create()

    companion object {

        @JvmStatic
        private val ARG_MESSAGE = "message"

        @JvmStatic
        fun newInstance(message: String): ErrorDialog = ErrorDialog()
            .apply {
            arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
        }
    }
}