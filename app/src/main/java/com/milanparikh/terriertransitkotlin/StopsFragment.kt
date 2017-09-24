package com.milanparikh.terriertransitkotlin


import android.os.Bundle
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
import org.json.JSONObject
import com.android.volley.VolleyError
import org.json.JSONException
import android.R.attr.data
import android.util.Log
import org.json.JSONArray


/**
 * A simple [Fragment] subclass.
 */
class StopsFragment : Fragment()  {
    var inboundID:Int? = 4160714
    var outboundID:Int = 4160714
    var inboundName:String = "Student Village 2"
    var outboundName:String = "Nickerson Field"
    var jsonData:String = ""
    lateinit var requestQueue:RequestQueue

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
                selectedStop = p2
                getShuttleStopIDs(selectedStop)
                inbound_time_text.setText(inboundID.toString())
                inbound_text.setText("Inbound: " + inboundName)
                outbound_time_text.setText(outboundID.toString())
                outbound_text.setText("Outbound: " + outboundName)
                var url:String = "https://www.terriertransit.com/api/shuttle/" + outboundID + "/" + inboundID + "?updateRequest=true"
                getShuttleData(url)
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
                inboundID = null
                inboundName = "Danielson"
                outboundID = 4160722
                outboundName = "Danielson"
            }
            6->{
                //Huntington
                inboundID = 4160714
                inboundName = "Huntington"
                outboundID = 4160714
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

    fun getShuttleData(url:String) {
        val objectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                // The third parameter Listener overrides the method onResponse() and passes
                //JSONObject as a parameter
                Response.Listener { response ->
                    // Takes the response from the JSON request
                    try {
                        var inboundObj = response.getJSONObject("inbound")
                        var outboundObj = response.getJSONObject("outbound")
                        var inboundData:JSONArray = inboundObj.getJSONArray("incoming")
                        var outboundData:JSONArray = outboundObj.getJSONArray("incoming")
                        var inboundTime = inboundData.get(0)

                        //outbound_minutes_text.setText(outboundData.getString("rawTimeAway"))
                        //inbound_minutes_text.setText(inboundData.getString("rawTimeAway"))


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

}
