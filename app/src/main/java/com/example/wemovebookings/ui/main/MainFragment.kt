package com.example.wemovebookings.ui.main

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.android.external.store4.StoreResponse
import com.example.wemovebookings.R
import com.example.wemovebookings.api.Status
import com.example.wemovebookings.api.request.FlightRequestBody
import com.example.wemovebookings.di.Injectable
import com.example.wemovebookings.room.entities.AirlineSchedule
import com.example.wemovebookings.room.entities.Airport
import com.example.wemovebookings.ui.map.MapsActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class MainFragment : Fragment(), Injectable {

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    var airports: List<Airport> = emptyList()

    var airportsText = emptyList<String>()

    val flightsAdapter = FlightsAdapter()

    var selectedOriginAirportIndex = -1

    var selectedDestAirportIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLiveData()
        flightList.adapter = flightsAdapter
        next.setOnClickListener {
            getFlights()
        }
        swipe.setOnRefreshListener {
            getFlights()
        }
        if (savedInstanceState == null) {
            viewModel.getAirports(airports.size)
        }

    }

    private fun getFlights() {
        if (originEditText.text.isNotEmpty() && destinationEditText.text.isNotEmpty()) {
            try {
                selectedOriginAirportIndex = airportsText.indexOf(originEditText.text.toString())
                selectedDestAirportIndex = airportsText.indexOf(destinationEditText.text.toString())
                val originCode = airports[selectedOriginAirportIndex].AirportCode
                val destCode = airports[selectedDestAirportIndex].AirportCode
                val date = DateFormat.format("yyyy-MM-dd", Calendar.getInstance())
                viewModel.getFlights(FlightRequestBody(originCode, destCode, date.toString()))
            } catch (e: ArrayIndexOutOfBoundsException) {
                Toast.makeText(context!!, "Unknown Airport Code", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(context!!, "One or more airport codes missing", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeLiveData() {
        viewModel.airportLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
//                    if (airports.size != it.data?.size ){
//                        viewModel.getAirports(it.data?.size ?: 0 ) //fetch next page
//                    }
                    airports = if (it.data.isNullOrEmpty().not()) it.data!! else emptyList()
                    freshLoader.visibility = View.GONE
                    contentContainer.visibility = View.VISIBLE
                    setUpDropDowns()
                }
                Status.ERROR -> {
                    if (airports.isEmpty()) {
                        Snackbar.make(
                            container,
                            "Failed to load data. ${it.message}",
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction("RETRY") {
                                viewModel.getAirports(airports.size)
                            }.show()
                        freshLoader.visibility = View.GONE
                        contentContainer.visibility = View.GONE
                    } else {
                        Toast.makeText(context!!, it.message, Toast.LENGTH_LONG).show()
                    }

                }
                Status.LOADING -> {
                    if (airports.isEmpty()) {
                        freshLoader.visibility = View.VISIBLE
                        contentContainer.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.flightsLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    flightList.visibility = View.VISIBLE
                    swipe.isRefreshing = false
                    emptyText.visibility = View.GONE
                    flightsAdapter.flights = it.data!!
                    flightsAdapter.notifyDataSetChanged()
                }
                Status.ERROR -> {
                    swipe.isRefreshing = false
                    if (flightsAdapter.itemCount == 0) {
                        flightList.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                    }
                    Toast.makeText(context!!, it.message, Toast.LENGTH_LONG).show()
                }
                Status.LOADING -> {
                    flightList.visibility = View.VISIBLE
                    swipe.isRefreshing = true
                    emptyText.visibility = View.GONE

                }
            }
        })
    }

    private fun setUpDropDowns() {
        airportsText = airports.map {
            "${it.Name.Name} (${it.AirportCode})"
        }
        originEditText.setAdapter(
            ArrayAdapter(
                context!!,
                android.R.layout.simple_list_item_1,
                airportsText
            )
        )
        destinationEditText.setAdapter(
            ArrayAdapter(
                context!!,
                android.R.layout.simple_list_item_1,
                airportsText
            )
        )
        val onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && v is AutoCompleteTextView) {
                when (v.id) {
                    R.id.originEditText or R.id.destinationEditText -> {
                        if (!airportsText.contains(v.text.toString())) v.setText("", false)
                    }
                }
            }
        }
        originEditText.onFocusChangeListener = onFocusChangeListener
        destinationEditText.onFocusChangeListener = onFocusChangeListener
    }


    inner class FlightsAdapter(var flights: List<AirlineSchedule> = emptyList()) :
        RecyclerView.Adapter<FlightsAdapter.ViewHolder>() {


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val toCode: TextView = itemView.findViewById(R.id.toCode)
            private val toTime: TextView = itemView.findViewById(R.id.toTime)
            private val fromCode: TextView = itemView.findViewById(R.id.fromCode)
            private val fromTime: TextView = itemView.findViewById(R.id.fromTime)
            private val airline: TextView = itemView.findViewById(R.id.airline)

            init {
                itemView.setOnClickListener {
                    startActivity(Intent(context!!, MapsActivity::class.java).apply {
                        putExtras(
                            bundleOf(
                                "lat1" to airports[selectedOriginAirportIndex].Position.Coordinate.Latitude,
                                "long1" to airports[selectedOriginAirportIndex].Position.Coordinate.Longitude,
                                "lat2" to airports[selectedDestAirportIndex].Position.Coordinate.Latitude,
                                "long2" to airports[selectedDestAirportIndex].Position.Coordinate.Longitude
                            )
                        )
                    })
                }
            }

            fun bind(position: Int) {
                val flight = flights[position]
                fromCode.text = flight.schedule.Flight.Departure.AirportCode
                fromTime.text = flight.schedule.Flight.Departure.ScheduledTimeLocal.DateTime
                toCode.text = flight.schedule.Flight.Arrival.AirportCode
                toTime.text = flight.schedule.Flight.Arrival.ScheduledTimeLocal.DateTime
                airline.text = flight.schedule.Flight.OperatingCarrier?.AirlineID
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.list_item_flight,
                    parent,
                    false
                )
            )
        }

        override fun getItemCount() = flights.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }
    }
}
