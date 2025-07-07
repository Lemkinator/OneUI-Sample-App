package de.lemke.oneuisample.ui.fragments

import android.content.res.Configuration
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
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.picker.app.SeslDatePickerDialog
import androidx.picker.app.SeslTimePickerDialog
import androidx.picker.widget.SeslDatePicker
import androidx.picker.widget.SeslTimePicker
import androidx.picker3.app.SeslColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
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
class TabPicker : Fragment(R.layout.fragment_tab_picker) {
    private val binding by autoCleared { FragmentTabPickerBinding.bind(requireView()) }
    private var currentColor = -16547330 // #0381fe
    private var recentColors: List<Int> = listOf(currentColor)
    private var colorPickerDialog: SeslColorPickerDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                if (actionId == EditorInfo.IME_ACTION_DONE) isEditTextMode = false
                false
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
                if (actionId == IME_ACTION_NEXT) {
                    isEditTextMode = false
                    binding.numberPicker3.isEditTextMode = true
                    binding.numberPicker3.requestFocus()
                }
                false
            }
        }
        binding.numberPicker1.apply {
            minValue = 1
            maxValue = 100
            value = 50
            setTextSize(40f)
            editText.imeOptions = IME_FLAG_NO_FULLSCREEN or IME_ACTION_NEXT
            editText.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == IME_ACTION_NEXT) {
                    isEditTextMode = false
                    binding.numberPicker2.isEditTextMode = true
                    binding.numberPicker2.requestFocus()
                }
                false
            }
        }
    }

    private fun initDatePicker() {
        val calendar = Calendar.getInstance()
        binding.datePicker.init(calendar[YEAR], calendar[MONTH], calendar[DAY_OF_MONTH], null)
        binding.spinningDatePicker.init(calendar[YEAR], calendar[MONTH], calendar[DAY_OF_MONTH], null)
        binding.spinningDatePicker.showMarginRight(true)
    }

    private fun initSpinner() {
        binding.pickerSpinner.setEntries(
            listOf("NumberPicker", "TimePicker", "DatePicker", "SpinningDatePicker", "SleepTimePicker")
        ) { position, _ ->
            position?.let {
                binding.numberPicker.isVisible = position == 0
                binding.timePicker.isVisible = position == 1
                binding.datePicker.isVisible = position == 2
                binding.spinningDatePicker.isVisible = position == 3
                binding.sleepPicker.isVisible = position == 4
            }
        }
    }

    private fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        SeslDatePickerDialog(
            requireContext(),
            { _: SeslDatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                suggestiveSnackBar(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth))
            },
            calendar[YEAR], calendar[MONTH], calendar[DAY_OF_MONTH]
        ).show()
    }

    private fun openTimePickerDialog() {
        val calendar = Calendar.getInstance()
        SeslTimePickerDialog(
            requireContext(),
            { _: SeslTimePicker?, hourOfDay: Int, minute: Int -> suggestiveSnackBar("$hourOfDay:$minute") },
            calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], is24HourFormat(requireContext())
        ).show()
    }

    private fun openStartEndTimePickerDialog() {
        StartEndTimePickerDialog(requireContext(), 0, 600, is24HourFormat(requireContext())) { startTime, endTime ->
            val startFormatted = String.format(Locale.getDefault(), "%02d:%02d", startTime / 60, startTime % 60)
            val endFormatted = String.format(Locale.getDefault(), "%02d:%02d", endTime / 60, endTime % 60)
            suggestiveSnackBar("Start time: $startFormatted\nEnd time: $endFormatted")
        }.show()
    }

    private fun openColorPickerDialog() {
        colorPickerDialog = SeslColorPickerDialog(
            requireContext(),
            { color: Int -> currentColor = color; recentColors = (listOf(color) + recentColors).distinct().take(6) },
            currentColor, recentColors.toIntArray(), true
        ).apply {
            setTransparencyControlEnabled(true)
            show()
            requireView().post {
                setOnBitmapSetListener {
                    val rootView = requireActivity().window.decorView.rootView
                    val bitmap = createBitmap(rootView.width, rootView.height)
                    rootView.draw(Canvas(bitmap))
                    bitmap
                }
            }
        }
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