package de.lemke.oneuisample.ui.fragments

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.Companion.PRIVATE
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.picker.app.SeslDatePickerDialog
import androidx.picker.app.SeslTimePickerDialog
import androidx.picker.widget.SeslDatePicker
import androidx.picker.widget.SeslTimePicker
import androidx.picker3.app.SeslColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.FragmentTabPickerBinding
import de.lemke.oneuisample.ui.util.autoCleared
import de.lemke.oneuisample.ui.util.suggestiveSnackBar
import dev.oneuiproject.oneui.dialog.StartEndTimePickerDialog
import dev.oneuiproject.oneui.ktx.setEntries
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.Locale

@AndroidEntryPoint
class TabPickerFragment : AbsBaseFragment(R.layout.fragment_tab_picker) {
    private val binding by autoCleared { FragmentTabPickerBinding.bind(requireView()) }
    private var currentColor = -16547330 // #0381fe
    private var recentColors: List<Int> = listOf(currentColor)
    private var colorPickerDialog: SeslColorPickerDialog? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("UsePropertyAccessSyntax")
        binding.timePicker.setIs24HourView(is24HourFormat(requireContext()))
        initNumberPicker()
        initDatePicker()
        initSpinner()
        binding.dateButton.setOnClickListener { openDatePickerDialog() }
        binding.timeButton.setOnClickListener { openTimePickerDialog() }
        binding.startEndTimeButton.setOnClickListener { openStartEndTimePickerDialog() }
        binding.colorButton.setOnClickListener { openColorPickerDialog() }
    }

    private fun initNumberPicker() {
        binding.numberPicker3.apply {
            setTextTypeface(ResourcesCompat.getFont(requireContext(), R.font.samsungsharpsans_bold))
            minValue = 0
            maxValue = 2
            setTextSize(40f)
            displayedValues = arrayOf("A", "B", "C")
            editText.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                onNumberPicker3EditorAction(actionId)
            }
        }
        binding.numberPicker2.apply {
            setTextTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL))
            minValue = 0
            maxValue = 10
            value = 8
            setTextSize(50f)
            editText.imeOptions = IME_FLAG_NO_FULLSCREEN or IME_ACTION_NEXT
            editText.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                onNumberPicker2EditorAction(actionId)
            }
        }
        binding.numberPicker1.apply {
            minValue = 1
            maxValue = 100
            value = 50
            setTextSize(40f)
            editText.imeOptions = IME_FLAG_NO_FULLSCREEN or IME_ACTION_NEXT
            editText.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                onNumberPicker1EditorAction(actionId)
            }
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onNumberPicker3EditorAction(actionId: Int): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) binding.numberPicker3.isEditTextMode = false
        return false
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onNumberPicker2EditorAction(actionId: Int): Boolean {
        if (actionId == IME_ACTION_NEXT) {
            binding.numberPicker2.isEditTextMode = false
            binding.numberPicker3.isEditTextMode = true
            binding.numberPicker3.requestFocus()
        }
        return false
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onNumberPicker1EditorAction(actionId: Int): Boolean {
        if (actionId == IME_ACTION_NEXT) {
            binding.numberPicker1.isEditTextMode = false
            binding.numberPicker2.isEditTextMode = true
            binding.numberPicker2.requestFocus()
        }
        return false
    }

    private fun initDatePicker() {
        val calendar = Calendar.getInstance()
        binding.datePicker.init(calendar[YEAR], calendar[MONTH], calendar[DAY_OF_MONTH], null)
        binding.spinningDatePicker.init(calendar[YEAR], calendar[MONTH], calendar[DAY_OF_MONTH], null)
        binding.spinningDatePicker.showMarginRight(true)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onSpinnerItemSelected(position: Int?) {
        position?.let {
            binding.numberPicker.isVisible = position == 0
            binding.timePicker.isVisible = position == 1
            binding.datePicker.isVisible = position == 2
            binding.spinningDatePicker.isVisible = position == 3
            binding.sleepPicker.isVisible = position == 4
        }
    }

    private fun initSpinner() {
        binding.pickerSpinner.setEntries(
            listOf("NumberPicker", "TimePicker", "DatePicker", "SpinningDatePicker", "SleepTimePicker"),
        ) { position, _ -> onSpinnerItemSelected(position) }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        SeslDatePickerDialog(
            requireContext(),
            { _: SeslDatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int -> onDatePicked(year, monthOfYear, dayOfMonth) },
            calendar[YEAR],
            calendar[MONTH],
            calendar[DAY_OF_MONTH],
        ).show()
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onDatePicked(
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
    ) {
        suggestiveSnackBar(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth))
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun openTimePickerDialog() {
        val calendar = Calendar.getInstance()
        SeslTimePickerDialog(
            requireContext(),
            { _: SeslTimePicker?, hourOfDay: Int, minute: Int -> onTimePicked(hourOfDay, minute) },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            is24HourFormat(requireContext()),
        ).show()
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onTimePicked(
        hourOfDay: Int,
        minute: Int,
    ) {
        suggestiveSnackBar("$hourOfDay:$minute")
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun openStartEndTimePickerDialog() {
        StartEndTimePickerDialog(requireContext(), 0, 600, is24HourFormat(requireContext())) { startTime, endTime ->
            onStartEndTimePicked(startTime, endTime)
        }.show()
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onStartEndTimePicked(
        startTime: Int,
        endTime: Int,
    ) {
        val startFormatted = String.format(Locale.getDefault(), "%02d:%02d", startTime / 60, startTime % 60)
        val endFormatted = String.format(Locale.getDefault(), "%02d:%02d", endTime / 60, endTime % 60)
        suggestiveSnackBar("Start time: $startFormatted\nEnd time: $endFormatted")
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun openColorPickerDialog() {
        colorPickerDialog =
            SeslColorPickerDialog(
                requireContext(),
                { color: Int -> onColorPicked(color) },
                currentColor,
                recentColors.toIntArray(),
                true,
            ).apply {
                setTransparencyControlEnabled(true)
                show()
                requireView().post {
                    setOnBitmapSetListener { captureScreenBitmap() }
                }
            }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun onColorPicked(color: Int) {
        currentColor = color
        recentColors = (listOf(color) + recentColors).distinct().take(6)
    }

    @NoCoverage
    internal fun captureScreenBitmap(): Bitmap {
        val rootView = requireActivity().window.decorView.rootView
        val bitmap = createBitmap(rootView.width, rootView.height)
        rootView.draw(Canvas(bitmap))
        return bitmap
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        colorPickerDialog?.apply {
            if (isShowing) {
                dismiss()
                openColorPickerDialog()
            }
        }
    }
}
