/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui.fragments

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
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

    @VisibleForTesting(otherwise = PRIVATE)
    internal var currentColor = 0xFF0381FE.toInt()

    @VisibleForTesting(otherwise = PRIVATE)
    internal var recentColors: List<Int> = listOf(currentColor)

    @VisibleForTesting(otherwise = PRIVATE)
    internal var colorPickerDialog: SeslColorPickerDialog? = null

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

    override fun onDestroyView() {
        colorPickerDialog?.dismiss()
        colorPickerDialog = null
        super.onDestroyView()
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

    private fun initNumberPicker() {
        binding.numberPicker3.apply {
            setTextTypeface(ResourcesCompat.getFont(requireContext(), R.font.samsungsharpsans_bold))
            minValue = 0
            maxValue = 2
            setTextSize(PICKER_TEXT_SIZE_SP)
            displayedValues = arrayOf("A", "B", "C")
        }
        binding.numberPicker2.apply {
            setTextTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL))
            minValue = 0
            maxValue = PICKER2_MAX_VALUE
            value = PICKER2_DEFAULT_VALUE
            setTextSize(PICKER2_TEXT_SIZE_SP)
            editText.imeOptions = IME_FLAG_NO_FULLSCREEN or IME_ACTION_NEXT
        }
        binding.numberPicker1.apply {
            minValue = 1
            maxValue = PICKER1_MAX_VALUE
            value = PICKER1_DEFAULT_VALUE
            setTextSize(PICKER_TEXT_SIZE_SP)
            editText.imeOptions = IME_FLAG_NO_FULLSCREEN or IME_ACTION_NEXT
        }
        binding.numberPicker3.editText.setOnEditorActionListener { _, actionId, _ -> onNumberPicker3EditorAction(actionId) }
        binding.numberPicker2.editText.setOnEditorActionListener { _, actionId, _ -> onNumberPicker2EditorAction(actionId) }
        binding.numberPicker1.editText.setOnEditorActionListener { _, actionId, _ -> onNumberPicker1EditorAction(actionId) }
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
            binding.numberPicker.isVisible = position == SPINNER_NUMBER_PICKER
            binding.timePicker.isVisible = position == SPINNER_TIME_PICKER
            binding.datePicker.isVisible = position == SPINNER_DATE_PICKER
            binding.spinningDatePicker.isVisible = position == SPINNER_SPINNING_DATE_PICKER
            binding.sleepPicker.isVisible = position == SPINNER_SLEEP_PICKER
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
        StartEndTimePickerDialog(requireContext(), 0, DEFAULT_END_TIME_MINUTES, is24HourFormat(requireContext())) { startTime, endTime ->
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
        suggestiveSnackBar(getString(R.string.start_end_time_result, startFormatted, endFormatted))
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
        recentColors = (listOf(color) + recentColors).distinct().take(MAX_RECENT_COLORS)
    }

    @NoCoverage
    internal fun captureScreenBitmap(): Bitmap {
        val act = activity ?: return createBitmap(1, 1)
        val rootView = act.window.decorView.rootView
        val bitmap = createBitmap(rootView.width.coerceAtLeast(1), rootView.height.coerceAtLeast(1))
        rootView.draw(Canvas(bitmap))
        return bitmap
    }

    companion object {
        private const val PICKER_TEXT_SIZE_SP = 40f
        private const val PICKER2_TEXT_SIZE_SP = 50f
        private const val PICKER2_MAX_VALUE = 10
        private const val PICKER2_DEFAULT_VALUE = 8
        private const val PICKER1_MAX_VALUE = 100
        private const val PICKER1_DEFAULT_VALUE = 50
        private const val SPINNER_NUMBER_PICKER = 0
        private const val SPINNER_TIME_PICKER = 1
        private const val SPINNER_DATE_PICKER = 2
        private const val SPINNER_SPINNING_DATE_PICKER = 3
        private const val SPINNER_SLEEP_PICKER = 4
        private const val DEFAULT_END_TIME_MINUTES = 600
        private const val MAX_RECENT_COLORS = 6
    }
}
