package com.milanparikh.terriertransitkotlin


import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.milanparikh.terriertransitkotlin.R.array.shuttle_stops
import kotlinx.android.synthetic.main.fragment_stops.*
import org.json.JSONException
import android.util.Log
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class StopsFragment : Fragment()  {
    var inboundID:Int? = 4160714
    var outboundID:Int = 4160714
    var inboundName:String = "Student Village 2"
    var outboundName:String = "Nickerson Field"
    lateinit var requestQueue:RequestQueue
    var timesHashMap:HashMap<String, MutableList<Long>> = HashMap()
    var inboundCount:countDown? = null
    var outboundCount:countDown? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_stops, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        requestQueue = Volley.newRequestQueue(activity)
        var stopsArrayAdapter:ArrayAdapter<String> = ArrayAdapter(activity, android.R.layout.simple_spinner_item, resources.getStringArray(shuttle_stops))
        stopsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stop_spinner.adapter = stopsArrayAdapter

        var selectedStop:Int = stop_spinner.selectedItemPosition
        stop_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(inboundCount!=null && outboundCount!=null){
                    inboundCount?.cancel()
                    outboundCount?.cancel()
                }
                inbound_time_text.setText("Loading")
                outbound_time_text.setText("Loading")
                getShuttleStopIDs(p2)
                getShuttleData()
                inbound_text.setText("Inbound: " + inboundName)
                outbound_text.setText("Outbound: " + outboundName)
            }

        }
        getShuttleStopIDs(selectedStop)

    }



    fun getShuttleStopIDs(pos:Int){
        when(pos){
            0->{
                //StuVi 2/Nickerson Field
                inboundID = 4160714
                inboundName = "StuVi 2"
                outboundID = 4160714
                outboundName = "Nickerson Field"
            }
            1->{
                //Amory St./College of Fine Arts
                inboundID = 4114006
                inboundName = "Amory Street"
                outboundID = 4160738
                outboundName = "College of Fine Arts"
            }
            2->{
                //St. Mary's St./Marsh Chapel
                inboundID = 4149154
                inboundName = "St. Mary\'s St."
                outboundID = 4160734
                outboundName = "Marsh Chapel"
            }
            3->{
                //Blandford/Silber Way
                inboundID = 4068466
                inboundName = "Blandford"
                outboundID = 4160730
                outboundName = "Silber Way"
            }
            4->{
                //Hotel Commonwealth/Myles Standish
                inboundID = 4068470
                inboundName = "Hotel Commonwealth"
                outboundID = 4160726
                outboundName = "Myles Standish"
            }
            5->{
                //Danielsen
                inboundID = 4110206
                inboundName = "Danielson"
                outboundID = 4160722
                outboundName = "Danielson"
            }
            6->{
                //Huntington
                inboundID = 4160714
                inboundName = "Huntington"
                outboundID = 4160718
                outboundName = "Huntington"
            }
            7->{
                //710 Albany St.
                inboundID = 4068482
                inboundName = "710 Albany Street"
                outboundID = 4068482
                outboundName = "710 Albany Street"
            }
        }
    }

    fun getShuttleData() {
        val url = "https://www.bu.edu/bumobile/rpc/bus/livebus.json.php"
        var objectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                // The third parameter Listener overrides the method onResponse() and passes
                //JSONObject as a parameter
                Response.Listener { response ->
                    // Takes the response from the JSON request
                    try {
                        var resultSetObject = response.getJSONObject("ResultSet")
                        var resultArray = resultSetObject.getJSONArray("Result")
                        for(i in 0.. resultArray.length() - 1){
                            var shuttleObject = resultArray.getJSONObject(i)
                            if(shuttleObject.has("arrival_estimates")){
                                var arrivalEstimatesArray = shuttleObject.getJSONArray("arrival_estimates")
                                for (j in 0..arrivalEstimatesArray.length() - 1){
                                    var arrivalEstimateObject = arrivalEstimatesArray.getJSONObject(j)
                                    var stopID = arrivalEstimateObject.getString("stop_id")
                                    var arrivalTime = arrivalEstimateObject.getString("arrival_at")
                                    var secondsUntil = getMillisecondsUntil(arrivalTime)
                                    //TODO: format time and clear it before current, remove from hashmap
                                    if(timesHashMap.get(stopID)==null){
                                        var timeList = mutableListOf<Long>()
                                        timeList.add(secondsUntil)
                                        timesHashMap.put(stopID, timeList)
                                    }else{
                                        var timeList:MutableList<Long>? = timesHashMap.get(stopID)
                                        timeList?.add(secondsUntil)
                                        timesHashMap.put(stopID, timeList!!)
                                    }
                                }
                            }
                        }
                        updateTimes()

                    } catch (e: JSONException) {
                        // If an error occurs, this prints the error to the log
                        e.printStackTrace()
                    }
                    // Try and catch are included to handle any errors due to JSON
                },
                // The final parameter overrides the method onErrorResponse() and passes VolleyError
                //as a parameter
                Response.ErrorListener // Handles errors that occur due to Volley
                { Log.e("Volley", "Error") }
        )
        // Adds the JSON object request "obreq" to the request queue
        requestQueue.add(objectRequest)

    }

    fun getMillisecondsUntil(arrivalTime:String): Long{
        val dateformat:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        var arrivalDate:Date = dateformat.parse(arrivalTime)
        var currentDate = Date()
        var secondsUntil:Long = (arrivalDate.time - currentDate.time)
        return secondsUntil
    }

    fun updateTimes(){
        var inboundSecondsList:MutableList<Long>? = timesHashMap.get(inboundID.toString())
        inboundSecondsList?.sort()
        var outboundSecondsList:MutableList<Long>? = timesHashMap.get(outboundID.toString())
        outboundSecondsList?.sort()
        var inboundMilliseconds:Long = 0
        var outboundMilliseconds:Long = 0
        if(inboundSecondsList?.get(0)!=null && outboundSecondsList?.get(0)!=null){
            inboundMilliseconds = inboundSecondsList.get(0)
            outboundMilliseconds = outboundSecondsList.get(0)
            inboundCount = countDown(inboundMilliseconds, 1000, inbound_time_text)
            outboundCount = countDown(outboundMilliseconds, 1000, outbound_time_text)
            inboundCount?.start()
            outboundCount?.start()
        }
    }

    class countDown:CountDownTimer{
        val timeText:TextView
        constructor(millisInFuture: Long, countDownInterval: Long, timeTextView: TextView) : super(millisInFuture, countDownInterval){
            this.timeText = timeTextView
        }


        override fun onFinish() {
        }

        override fun onTick(p0: Long) {
            var minutes = (p0/1000)/60
            var seconds = (p0/1000)-(minutes*60)
            var timeString = minutes.toString()+ ":" + String.format("%02d", seconds)
            timeText.setText(timeString)
        }

    }

}
