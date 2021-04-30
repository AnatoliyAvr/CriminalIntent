package com.bignerdranch.android.criminalintent.ui.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.criminalintent.R
import com.bignerdranch.android.criminalintent.model.Crime
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeListFragment"
private const val VIEW_TYPE_DEFAULT = 0
private const val VIEW_TYPE_1 = 1

class CrimeListFragment : Fragment() {

  interface Callbacks {
    fun onCrimeSelected(crimeId: UUID)
  }

  private var callbacks: Callbacks? = null
  private lateinit var crimeRecyclerView: RecyclerView
  private lateinit var tvText: TextView
  private lateinit var btn: FloatingActionButton
  private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())
  private val crimeListViewModel: CrimeListViewModel by lazy {
    ViewModelProvider(this).get(CrimeListViewModel::class.java)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    callbacks = context as Callbacks?
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
    crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
    tvText = view.findViewById(R.id.tv_text) as TextView
    btn = view.findViewById(R.id.btn) as FloatingActionButton
    crimeRecyclerView.layoutManager = LinearLayoutManager(context)
    crimeRecyclerView.adapter = adapter
    return view
  }

  override fun onDetach() {
    super.onDetach()
    callbacks = null
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.fragment_crime_list, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.new_crime -> {
        val crime = Crime()
        crimeListViewModel.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
        true
      }
      else -> return super.onOptionsItemSelected(item)
    }
  }


  private fun updateUI(crimes: List<Crime>) {
    adapter = CrimeAdapter(crimes)
    crimeRecyclerView.adapter = adapter
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner, { crimes ->
      crimes?.let {
        Log.i(TAG, "Got crimes ${crimes.size}")
        if (crimes.isNotEmpty()) {
          btn.visibility = View.GONE
          tvText.visibility = View.GONE
        } else {
          btn.visibility = View.VISIBLE
          tvText.visibility = View.VISIBLE
        }
        updateUI(crimes)
      }
    })

    btn.setOnClickListener {
      val crime = Crime()
      crimeListViewModel.addCrime(crime)
      callbacks?.onCrimeSelected(crime.id)
    }
  }

  private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
    private lateinit var crime: Crime

    private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
    private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
    private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)
    private val sdf = SimpleDateFormat("EEEE, dd.MM.yyyy hh:mm", Locale.ROOT)

    init {
      itemView.setOnClickListener(this)
    }

    fun bind(crime: Crime) {
      this.crime = crime
      titleTextView.text = this.crime.title
      dateTextView.text = sdf.format(this.crime.date)
      solvedImageView.visibility = if (crime.isSolved) {
        View.VISIBLE
      } else {
        View.GONE
      }
    }

    override fun onClick(v: View) {
      callbacks?.onCrimeSelected(crime.id)
    }
  }

  private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
      val view = when (viewType) {
        VIEW_TYPE_1 -> layoutInflater.inflate(R.layout.list_item_crime_police, parent, false)
        else -> layoutInflater.inflate(R.layout.list_item_crime, parent, false)
      }
      return CrimeHolder(view)
    }

    override fun getItemCount() = crimes.size

    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
      val crime = crimes[position]
      holder.bind(crime)
    }

    override fun getItemViewType(position: Int): Int {
      return if (crimes[position].requiresPolice)
        VIEW_TYPE_1
      else
        VIEW_TYPE_DEFAULT
    }

    fun updateData(newData: List<Crime>) {

      val diffCallback = object : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
          return crimes[oldItemPosition].id == newData[newItemPosition].id
        }

        override fun getOldListSize(): Int = crimes.size

        override fun getNewListSize(): Int = newData.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
          return crimes[oldItemPosition] == newData[newItemPosition]
        }
      }

      val diffResult = DiffUtil.calculateDiff(diffCallback)
      crimes = newData
      diffResult.dispatchUpdatesTo(this)

    }
  }

  companion object {
    fun newInstance(): CrimeListFragment {
      return CrimeListFragment()
    }
  }
}