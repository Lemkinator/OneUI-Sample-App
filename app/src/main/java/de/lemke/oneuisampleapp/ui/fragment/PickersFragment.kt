package de.lemke.oneuisampleapp.ui.fragment

import de.lemke.oneuisampleapp.ui.BaseFragment
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.picker.widget.SeslTimePicker
import androidx.picker.widget.SeslDatePicker
import androidx.picker.widget.SeslSpinningDatePicker
import android.os.Bundle
import de.lemke.oneuisampleapp.R
import androidx.picker.widget.SeslNumberPicker
import androidx.core.content.res.ResourcesCompat
import android.widget.TextView
import android.view.inputmethod.EditorInfo
import android.graphics.Typeface
import android.text.format.DateFormat
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.AppCompatSpinner
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatButton
import androidx.picker.app.SeslDatePickerDialog
import androidx.picker.app.SeslTimePickerDialog
import androidx.picker3.app.SeslColorPickerDialog
import dev.oneuiproject.oneui.widget.Toast
import java.util.*

class PickersFragment : BaseFragment(), AdapterView.OnItemSelectedListener, SeslColorPickerDialog.OnColorSetListener {
    private var currentColor = 0
    private val recentColors: MutableList<Int> = ArrayList()
    private lateinit var numberPickers: LinearLayout
    private lateinit var timePicker: SeslTimePicker
    private lateinit var datePicker: SeslDatePicker
    private lateinit var spinningDatePicker: SeslSpinningDatePicker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentColor = -16547330 // #0381fe
        recentColors.add(currentColor)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNumberPicker(view)
        initTimePicker(view)
        initDatePickers(view)
        initSpinner(view)
        initDialogBtns(view)
    }

    override val layoutResId: Int
        get() = R.layout.fragment_pickers
    override val iconResId: Int
        get() = dev.oneuiproject.oneui.R.drawable.ic_oui_calendar_next_shedules
    override val title: CharSequence
        get() = "Pickers"

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
    private fun initNumberPicker(view: View) {
        numberPickers = view.findViewById(R.id.pickers_number)
        val numberPickerThree = numberPickers.findViewById<SeslNumberPicker>(R.id.picker_number_3)
        numberPickerThree.setTextTypeface(ResourcesCompat.getFont(context!!, R.font.samsungsharpsans_bold))
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

    private fun initTimePicker(view: View) {
        timePicker = view.findViewById(R.id.picker_time)
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))
    }

    private fun initDatePickers(view: View) {
        datePicker = view.findViewById(R.id.picker_date)
        spinningDatePicker = view.findViewById(R.id.picker_spinning_date)
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

    private fun initSpinner(view: View) {
        val spinner = view.findViewById<AppCompatSpinner>(R.id.pickers_spinner)
        val categories: MutableList<String> = ArrayList()
        categories.add("NumberPicker")
        categories.add("TimePicker")
        categories.add("DatePicker")
        categories.add("SpinningDatePicker")
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    private fun initDialogBtns(view: View) {
        val dateBtn = view.findViewById<AppCompatButton>(R.id.pickers_dialog_date)
        dateBtn.setOnClickListener { openDatePickerDialog() }
        val timeBtn = view.findViewById<AppCompatButton>(R.id.pickers_dialog_time)
        timeBtn.setOnClickListener { openTimePickerDialog() }
        val colorBtn = view.findViewById<AppCompatButton>(R.id.pickers_dialog_color)
        colorBtn.setOnClickListener { openColorPickerDialog() }
    }

    private fun openDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dialog = SeslDatePickerDialog(
            context!!,
            { _: SeslDatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                Toast.makeText(
                    context,
                    "Year: $year\nMonth: $monthOfYear\nDay: $dayOfMonth",
                    Toast.LENGTH_SHORT
                ).show()
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
            context,
            { _: SeslTimePicker?, hourOfDay: Int, minute: Int ->
                Toast.makeText(
                    context,
                    "Hour: $hourOfDay\nMinute: $minute",
                    Toast.LENGTH_SHORT
                ).show()
            },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            DateFormat.is24HourFormat(context)
        )
        dialog.show()
    }

    private fun openColorPickerDialog() {
        val dialog = SeslColorPickerDialog(
            context, this,
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