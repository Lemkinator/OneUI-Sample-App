package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.picker.app.SeslDatePickerDialog
import androidx.picker.app.SeslTimePickerDialog
import androidx.picker.widget.SeslDatePicker
import androidx.picker.widget.SeslNumberPicker
import androidx.picker.widget.SeslSpinningDatePicker
import androidx.picker.widget.SeslTimePicker
import androidx.picker3.app.SeslColorPickerDialog
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.dialog.StartEndTimePickerDialog
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.ActivityPickersBinding
import android.widget.Toast
import java.util.*


@AndroidEntryPoint
class PickersActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, SeslColorPickerDialog.OnColorSetListener {
    private lateinit var binding: ActivityPickersBinding
    private var currentColor = 0
    private val recentColors: MutableList<Int> = ArrayList()
    private lateinit var numberPickers: LinearLayout
    private lateinit var timePicker: SeslTimePicker
    private lateinit var datePicker: SeslDatePicker
    private lateinit var spinningDatePicker: SeslSpinningDatePicker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickersBinding.inflate(layoutInflater)
        binding.toolbarLayout.setNavigationButtonOnClickListener { finish() }
        binding.toolbarLayout.tooltipText = getString(R.string.sesl_navigate_up)
        setContentView(binding.root)
        currentColor = -16547330 // #0381fe
        recentColors.add(currentColor)
        initNumberPicker()
        initTimePicker()
        initDatePickers()
        initSpinner()
        initBNV()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        when (position) {
            0 -> {
                numberPickers.visibility = View.VISIBLE
                timePicker.visibility = View.GONE
                datePicker.visibility = View.GONE
                spinningDatePicker.visibility = View.GONE
            }

            1 -> {
                numberPickers.visibility = View.GONE
                timePicker.visibility = View.VISIBLE
                timePicker.startAnimation(200, null)
                datePicker.visibility = View.GONE
                spinningDatePicker.visibility = View.GONE
            }

            2 -> {
                numberPickers.visibility = View.GONE
                timePicker.visibility = View.GONE
                datePicker.visibility = View.VISIBLE
                spinningDatePicker.visibility = View.GONE
            }

            3 -> {
                numberPickers.visibility = View.GONE
                timePicker.visibility = View.GONE
                datePicker.visibility = View.GONE
                spinningDatePicker.visibility = View.VISIBLE
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private fun initNumberPicker() {
        numberPickers = binding.pickersNumber
        val numberPickerThree = numberPickers.findViewById<SeslNumberPicker>(R.id.picker_number_3)
        numberPickerThree.setTextTypeface(ResourcesCompat.getFont(this, R.font.samsungsharpsans_bold))
        numberPickerThree.minValue = 0
        numberPickerThree.maxValue = 2
        numberPickerThree.setTextSize(40f)
        numberPickerThree.displayedValues = arrayOf("A", "B", "C")
        val et3 = numberPickerThree.editText
        et3.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                numberPickerThree.isEditTextMode = false
            }
            false
        }
        val numberPickerTwo = numberPickers.findViewById<SeslNumberPicker>(R.id.picker_number_2)
        numberPickerTwo.setTextTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL))
        numberPickerTwo.minValue = 0
        numberPickerTwo.maxValue = 10
        numberPickerTwo.value = 8
        numberPickerTwo.setTextSize(50f)
        val et2 = numberPickerTwo.editText
        et2.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN or EditorInfo.IME_ACTION_NEXT
        et2.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                numberPickerTwo.isEditTextMode = false
                numberPickerThree.isEditTextMode = true
                numberPickerThree.requestFocus()
            }
            false
        }
        val numberPickerOne = numberPickers.findViewById<SeslNumberPicker>(R.id.picker_number_1)
        numberPickerOne.minValue = 1
        numberPickerOne.maxValue = 100
        numberPickerOne.value = 50
        numberPickerOne.setTextSize(40f)
        val et1 = numberPickerOne.editText
        et1.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN or EditorInfo.IME_ACTION_NEXT
        et1.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                numberPickerOne.isEditTextMode = false
                numberPickerTwo.isEditTextMode = true
                numberPickerTwo.requestFocus()
            }
            false
        }
    }

    private fun initTimePicker() {
        timePicker = binding.pickerTime
        timePicker.setIs24HourView(DateFormat.is24HourFormat(this))
    }

    private fun initDatePickers() {
        datePicker = binding.pickerDate
        spinningDatePicker = binding.pickerSpinningDate
        val calendar = Calendar.getInstance()
        datePicker.init(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH], null
        )
        spinningDatePicker.init(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH], null
        )
        spinningDatePicker.showMarginRight(true)
    }

    private fun initSpinner() {
        val spinner = binding.pickersSpinner
        val categories: MutableList<String> = ArrayList()
        categories.add("NumberPicker")
        categories.add("TimePicker")
        categories.add("DatePicker")
        categories.add("SpinningDatePicker")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    private fun initBNV() {
        binding.pickersBnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.pickers_bnv_date -> {
                    openDatePickerDialog()
                    return@setOnItemSelectedListener true
                }

                R.id.pickers_bnv_time -> {
                    openTimePickerDialog()
                    return@setOnItemSelectedListener true
                }

                R.id.pickers_bnv_start_end_time -> {
                    openStartEndTimePickerDialog()
                    return@setOnItemSelectedListener true
                }

                R.id.pickers_bnv_color -> {
                    openColorPickerDialog()
                    return@setOnItemSelectedListener true
                }

                else -> return@setOnItemSelectedListener false
            }
        }
    }

    private fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dialog = SeslDatePickerDialog(
            this,
            { _: SeslDatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                Toast.makeText(this, "Year: $year\nMonth: $monthOfYear\nDay: $dayOfMonth", Toast.LENGTH_SHORT).show()
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        dialog.show()
    }

    private fun openTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val dialog = SeslTimePickerDialog(
            this,
            { _: SeslTimePicker?, hourOfDay: Int, minute: Int ->
                Toast.makeText(this, "Hour: $hourOfDay\nMinute: $minute", Toast.LENGTH_SHORT).show()
            },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            DateFormat.is24HourFormat(this)
        )
        dialog.show()
    }

    private fun openStartEndTimePickerDialog() {
        val dialog = StartEndTimePickerDialog(
            this,
            0, 600,
            DateFormat.is24HourFormat(this)
        ) { startTime: Int, endTime: Int ->
            //print start time and end time in HH:MM format
            Toast.makeText(
                this,
                "Start time: " + String.format(
                    "%02d:%02d",
                    startTime / 60,
                    startTime % 60
                ) + "\nEnd time: " + String.format(
                    "%02d:%02d",
                    endTime / 60,
                    endTime % 60
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
        dialog.show()
    }

    private fun openColorPickerDialog() {
        val dialog = SeslColorPickerDialog(
            this, this,
            currentColor, buildIntArray(recentColors), true
        )
        dialog.setTransparencyControlEnabled(true)
        dialog.show()
    }

    override fun onColorSet(color: Int) {
        currentColor = color
        if (recentColors.size == 6) {
            recentColors.removeAt(5)
        }
        recentColors.add(0, color)
    }

    private fun buildIntArray(integers: List<Int>): IntArray {
        val ints = IntArray(integers.size)
        var i = 0
        for (n in integers) {
            ints[i++] = n
        }
        return ints
    }
}