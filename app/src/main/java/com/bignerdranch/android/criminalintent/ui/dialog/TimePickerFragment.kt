package com.bignerdranch.android.criminalintent.ui.dialog

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_TIME = "time"

class TimePickerFragment : DialogFragment() {

  interface Callbacks {
    fun onTimeSelected(date: Date)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val date = arguments?.getSerializable(ARG_TIME) as Date
    val calendar = Calendar.getInstance()
    calendar.time = date
    val initialHour = calendar.get(Calendar.HOUR)
    val initialMinute = calendar.get(Calendar.MINUTE)

    val initialYear = calendar.get(Calendar.YEAR)
    val initialMount = calendar.get(Calendar.MONTH)
    val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

    val dateListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hour: Int, minutes: Int ->
//      val resultDate: Date = Calendar.getInstance().time
      val resultDate: Date = GregorianCalendar(initialYear, initialMount, initialDay, hour, minutes).time
      Log.d("AAA", "$resultDate")
      targetFragment?.let { fragment ->
        (fragment as Callbacks).onTimeSelected(resultDate)
      }
    }

    return TimePickerDialog(
      requireContext(),
      dateListener,
      initialHour,
      initialMinute,
      true
    )
  }

  companion object {
    fun newInstance(date: Date): TimePickerFragment {
      val args = Bundle().apply {
        putSerializable(ARG_TIME, date)
      }
      return TimePickerFragment().apply {
        arguments = args
      }
    }
  }
}