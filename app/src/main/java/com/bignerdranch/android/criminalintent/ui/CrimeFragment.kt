package com.bignerdranch.android.criminalintent.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bignerdranch.android.criminalintent.R
import com.bignerdranch.android.criminalintent.model.Crime
import com.bignerdranch.android.criminalintent.ui.dialog.DatePickerFragment
import com.bignerdranch.android.criminalintent.ui.dialog.TimePickerFragment
import com.bignerdranch.android.criminalintent.utils.getScaledBitmap
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHONE = 3
private const val REQUEST_PHOTO = 4
private const val DATE_FORMAT = "EEE, MMM, dd"


class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

  private lateinit var crime: Crime
  private lateinit var photoFile: File
  private lateinit var photoUri: Uri
  private lateinit var titleField: EditText
  private lateinit var dateButton: Button
  private lateinit var timeButton: Button
  private lateinit var solvedCheckBox: CheckBox
  private lateinit var reportButton: Button
  private lateinit var suspectButton: Button
  private lateinit var photoButton: ImageButton
  private lateinit var photoView: ImageView
  private lateinit var callButton: Button
  private val dateFormat = DateFormat.getMediumDateFormat(context)
  private val sdfT = SimpleDateFormat("hh:mm", Locale.ROOT)
  private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
    ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_crime, container, false)
    crime = Crime()
    val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
    crimeDetailViewModel.crimeLiveData
    crimeDetailViewModel.loadCrime(crimeId)

    titleField = view.findViewById(R.id.crime_title) as EditText
    dateButton = view.findViewById(R.id.crime_date) as Button
    timeButton = view.findViewById(R.id.crime_time) as Button
    solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
    reportButton = view.findViewById(R.id.crime_report) as Button
    suspectButton = view.findViewById(R.id.crime_suspect) as Button
    callButton = view.findViewById(R.id.call_suspect) as Button
    photoButton = view.findViewById(R.id.crime_camera) as ImageButton
    photoView = view.findViewById(R.id.crime_photo) as ImageView

    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    crimeDetailViewModel.crimeLiveData.observe(
      viewLifecycleOwner, { crime ->
        crime?.let {
          this.crime = crime
          photoFile = crimeDetailViewModel.getPhotoFile(crime)
          photoUri = FileProvider.getUriForFile(
            requireActivity(),
            "com.bignerdranch.android.criminalintent.fileprovider",
            photoFile
          )
          updateUI()
        }
      })
  }

  override fun onStart() {
    super.onStart()
    val titleWatcher = object : TextWatcher {
      override fun beforeTextChanged(
        sequence: CharSequence?, start: Int, count: Int, after: Int
      ) {        // Это пространство оставлено пустым специально
      }

      override fun onTextChanged(
        sequence: CharSequence?, start: Int, before: Int, count: Int
      ) {
        crime.title = sequence.toString()
      }

      override fun afterTextChanged(sequence: Editable?) {        // И это
      }
    }
    titleField.addTextChangedListener(titleWatcher)

    solvedCheckBox.apply {
      setOnCheckedChangeListener { _, isChecked ->
        crime.isSolved = isChecked
      }
    }

    dateButton.setOnClickListener {
      DatePickerFragment.newInstance(crime.date).apply {
        setTargetFragment(this@CrimeFragment, REQUEST_DATE)
        show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
      }
    }

    timeButton.setOnClickListener {
      TimePickerFragment.newInstance(crime.date).apply {
        setTargetFragment(this@CrimeFragment, REQUEST_TIME)
        show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
      }
    }

    reportButton.setOnClickListener {
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, getCrimeReport())
        putExtra(
          Intent.EXTRA_SUBJECT,
          getString(R.string.crime_report_subject)
        )
      }.also { intent ->
        val chooserIntent =
          Intent.createChooser(intent, getString(R.string.send_report))
        startActivity(chooserIntent)
      }
    }

    suspectButton.apply {
      val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
      setOnClickListener {
        startActivityForResult(pickContactIntent, REQUEST_CONTACT)

        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
          packageManager.resolveActivity(
            pickContactIntent,
            PackageManager.MATCH_DEFAULT_ONLY
          )
        if (resolvedActivity == null) {
          isEnabled = false
        }

      }
    }

    callButton.apply {
      val pickContactIntent = Intent(Intent.ACTION_PICK)
      pickContactIntent.type = Phone.CONTENT_TYPE
      setOnClickListener {
        startActivityForResult(pickContactIntent, REQUEST_PHONE)

        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
          packageManager.resolveActivity(
            pickContactIntent,
            PackageManager.MATCH_DEFAULT_ONLY
          )
        if (resolvedActivity == null) {
          isEnabled = false
        }

      }
    }

    photoButton.apply {
      val packageManager: PackageManager = requireActivity().packageManager
      val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      val resolvedActivity: ResolveInfo? =
        packageManager.resolveActivity(
          captureImage,
          PackageManager.MATCH_DEFAULT_ONLY
        )
      if (resolvedActivity == null) {
        isEnabled = false
      }
      setOnClickListener {
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        val cameraActivities: List<ResolveInfo> =
          packageManager.queryIntentActivities(
            captureImage,
            PackageManager.MATCH_DEFAULT_ONLY
          )
        for (cameraActivity in cameraActivities) {
          requireActivity().grantUriPermission(
            cameraActivity.activityInfo.packageName,
            photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
          )
        }
        startActivityForResult(
          captureImage, REQUEST_PHOTO
        )
      }
    }

    photoView.apply {
      setOnClickListener {
        if (photoFile.freeSpace > 0L) {
          val bitmap = getScaledBitmap(photoFile.path, requireActivity())
          val bitmapDrawable = BitmapDrawable(context.resources, bitmap)

          val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

          val layout: View = inflater.inflate(R.layout.dialog_photo, null)
          val image = layout.findViewById<View>(R.id.dialog_photo_image_view) as ImageView
          image.setImageDrawable(bitmapDrawable)

          AlertDialog.Builder(context).apply {
            setView(layout)
            create()
            show()
          }
        }
      }
    }

//    val observer = photoView.viewTreeObserver
//    observer.addOnGlobalLayoutListener {
//      Log.d("AAA!", "addOnGlobalLayoutListener")
//    }
  }

  override fun onStop() {
    super.onStop()
    crimeDetailViewModel.saveCrime(crime)
  }

  override fun onDetach() {
    super.onDetach()
    requireActivity().revokeUriPermission(
      photoUri,
      Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
  }

  override fun onDateSelected(date: Date) {
    crime.date = date
    updateUI()
  }

  override fun onTimeSelected(date: Date) {
    crime.date = date
    updateUI()
  }

  private fun updateUI() {
    titleField.setText(crime.title)
    dateButton.text = dateFormat.format(crime.date)
    timeButton.text = sdfT.format(crime.date)
    solvedCheckBox.apply {
      isChecked = crime.isSolved
      jumpDrawablesToCurrentState()
    }
    if (crime.suspect.isNotEmpty()) {
      suspectButton.text = crime.suspect
    }
    updatePhotoView()
  }

  private fun updatePhotoView() {
    if (photoFile.exists()) {
      val bitmap = getScaledBitmap(photoFile.path, requireActivity())
      photoView.setImageDrawable(null)
      photoView.setImageBitmap(bitmap)
    }
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when {
      resultCode != Activity.RESULT_OK -> return

      requestCode == REQUEST_CONTACT && data != null -> {
        val contactUri: Uri? = data.data
        // Указать, для каких полей ваш запрос должен возвращать значения.
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        // Выполняемый здесь запрос — contactUri похож на предложение "where"
        val cursor = requireActivity().contentResolver.query(contactUri!!, queryFields, null, null, null)
        cursor?.use {
          // Verify cursor contains at least one result
          if (it.count == 0) {
            return
          }
          // Первый столбец первой строки данных —
          // это имя вашего подозреваемого.
          it.moveToFirst()
          val suspect = it.getString(0)
          crime.suspect = suspect
          crimeDetailViewModel.saveCrime(crime)
          suspectButton.text = suspect
        }
      }

      requestCode == REQUEST_PHONE && data != null -> {
        val contactUri: Uri? = data.data
        val queryFields = arrayOf(Phone.NUMBER, Phone.CONTACT_ID)
        val cursor = requireActivity().contentResolver.query(contactUri!!, queryFields, null, null, null)

        cursor?.use {
          if (it.count == 0) {
            return
          }
          it.moveToFirst()
          val contactNumber = it.getString(0)

          val intent = Intent(Intent.ACTION_DIAL)
          intent.data = Uri.parse("tel:$contactNumber")
          startActivity(intent)
        }
      }

      requestCode == REQUEST_PHOTO -> {
        requireActivity().revokeUriPermission(
          photoUri,
          Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        updatePhotoView()
      }

    }
  }

  private fun getCrimeReport(): String {
    val solvedString = if (crime.isSolved) {
      getString(R.string.crime_report_solved)
    } else {
      getString(R.string.crime_report_unsolved)
    }
    val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
    val suspect = if (crime.suspect.isBlank()) {
      getString(R.string.crime_report_no_suspect)
    } else {
      getString(R.string.crime_report_suspect, crime.suspect)
    }
    return getString(
      R.string.crime_report,
      crime.title, dateString, solvedString, suspect
    )
  }

  companion object {
    fun newInstance(crimeId: UUID): CrimeFragment {
      val args = Bundle().apply {
        putSerializable(ARG_CRIME_ID, crimeId)
      }
      return CrimeFragment().apply {
        arguments = args
      }
    }
  }
}