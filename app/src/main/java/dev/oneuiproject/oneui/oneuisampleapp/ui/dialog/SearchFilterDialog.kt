package dev.oneuiproject.oneui.oneuisampleapp.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.domain.GetUserSettingsUseCase
import dev.oneuiproject.oneui.oneuisampleapp.domain.UpdateUserSettingsUseCase
import javax.inject.Inject

@AndroidEntryPoint
class SearchFilterDialog(private val onDismissListener: DialogInterface.OnDismissListener) : DialogFragment() {

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var updateUserSettings: UpdateUserSettingsUseCase

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener.onDismiss(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rootView = layoutInflater.inflate(R.layout.dialog_search_filter, null)
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(R.string.search_filter)
            .setView(rootView)
            .setNeutralButton(R.string.ok) { _, _ -> dismiss() }
            .setOnDismissListener { onDismissListener.onDismiss(it) }
        return builder.create()
    }
}