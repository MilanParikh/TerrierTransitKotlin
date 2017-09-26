package com.milanparikh.terriertransitkotlin


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.DrawFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_stops.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 */
class CustomMapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    lateinit var requestQueue: RequestQueue
    var shuttleHashMap:HashMap<String, Marker> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(R.layout.fragment_map, container, false)
        requestQueue = Volley.newRequestQueue(activity)
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getShuttleRoute()
        getOutboundStopMarkers()
        getInboundStopMarkers()
        getCapMarkers()
        getShuttleMarkers()
        //scheduleDataRefresh()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(42.350500, -71.105399), 14.0f))
    }

    fun getShuttleRoute(): Polyline{
        return mMap.addPolyline(PolylineOptions()
                .add(LatLng(42.348973, -71.096996))
                .add(LatLng(42.351598, -71.118548))
                .add(LatLng(42.353611, -71.118054))
                .add(LatLng(42.353690, -71.117861))
                .add(LatLng(42.353645, -71.117668))
                .add(LatLng(42.352445, -71.115479))
                .add(LatLng(42.351032, -71.115817))
                .add(LatLng(42.348767, -71.097262))
                .add(LatLng(42.348733, -71.092868))
                .add(LatLng(42.347494, -71.092428))
                .add(LatLng(42.346956, -71.092391))
                .add(LatLng(42.346737, -71.092160))
                .add(LatLng(42.346292, -71.090922))
                .add(LatLng(42.345442, -71.090626))
                .add(LatLng(42.344657, -71.090798))
                .add(LatLng(42.344343, -71.090733))
                .add(LatLng(42.343982, -71.090196))
                .add(LatLng(42.343312, -71.085867))
                .add(LatLng(42.340245, -71.081375))
                .add(LatLng(42.339319, -71.080530))
                .add(LatLng(42.333398, -71.073465))
                .add(LatLng(42.335927, -71.070053))
                .add(LatLng(42.338907, -71.073626))
                .add(LatLng(42.336549, -71.076960))
                .add(LatLng(42.339216, -71.080200))
                .add(LatLng(42.340286, -71.081228))
                .add(LatLng(42.343315, -71.085691))
                .add(LatLng(42.343442, -71.085814))
                .add(LatLng(42.350857, -71.089448))
                .add(LatLng(42.350201, -71.091956))
                .add(LatLng(42.349768, -71.093565))
                .add(LatLng(42.349039, -71.096301))
                .add(LatLng(42.348997, -71.096612))
                .add(LatLng(42.348973, -71.096996))
                .color(ContextCompat.getColor(activity, R.color.BURed)))
    }

    fun createMarker(latlng: LatLng, title: String, snippet:String, type: Int): Marker {
        var iconColor:Float = BitmapDescriptorFactory.HUE_RED
        when(type){
            0-> {
                iconColor = BitmapDescriptorFactory.HUE_AZURE
            }
            1->{
                iconColor = BitmapDescriptorFactory.HUE_RED
            }
            2->{
                iconColor = BitmapDescriptorFactory.HUE_GREEN
            }
            3->{
                iconColor = BitmapDescriptorFactory.HUE_YELLOW
            }
        }
        return mMap.addMarker(MarkerOptions()
                .position(latlng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(iconColor)))
    }

    fun createShuttleMarker(latlng: LatLng, title: String, snippet: String, tag: String){
        if(shuttleHashMap.get(tag)!=null){
            var removedMarker:Marker? = shuttleHashMap.get(tag)
            removedMarker?.remove()
        }
        var marker: Marker = mMap.addMarker(MarkerOptions()
                .position(latlng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        marker.tag = tag
        shuttleHashMap.put(tag, marker)

    }

    fun getOutboundStopMarkers() {
        var outboundLocations = ArrayList<LatLng>()
        var outboundTitles = ArrayList<String>()
        outboundTitles.add("Huntington Ave.")
        outboundLocations.add(LatLng(42.342399, -71.084142))
        outboundTitles.add("Danielsen Hall")
        outboundLocations.add(LatLng(42.350745, -71.090213))
        outboundTitles.add("Myles Standish")
        outboundLocations.add(LatLng(42.349620, -71.094383))
        outboundTitles.add("Silber Way")
        outboundLocations.add(LatLng(42.349506, -71.100746))
        outboundTitles.add("Marsh Plaza")
        outboundLocations.add(LatLng(42.350166, -71.106300))
        outboundTitles.add("College of Fine Arts")
        outboundLocations.add(LatLng(42.351061, -71.113889))

        for(i in 0..5)
            createMarker(outboundLocations.get(i), outboundTitles.get(i), "", type = 0)
    }

    fun getInboundStopMarkers() {
        var inboundLocations = ArrayList<LatLng>()
        var inboundTitles = ArrayList<String>()
        inboundTitles.add("Amory Street")
        inboundLocations.add(LatLng(42.350665, -71.113570))
        inboundTitles.add("St. Mary's Street")
        inboundLocations.add(LatLng(42.349816, -71.106422))
        inboundTitles.add("Blandford Street")
        inboundLocations.add(LatLng(42.349082,-71.100355 ))
        inboundTitles.add("Hotel Commonwealth")
        inboundLocations.add(LatLng(42.348697, -71.095134))
        inboundTitles.add("Huntington Ave.")
        inboundLocations.add(LatLng(42.342511, -71.084728))

        for(i in 0..4)
            createMarker(inboundLocations.get(i), inboundTitles.get(i), "", type = 1)
    }

    fun getCapMarkers() {
        var capLocations = ArrayList<LatLng>()
        var capTitles = ArrayList<String>()
        capTitles.add("710 Albany Street")
        capLocations.add(LatLng(42.335177, -71.071026))
        capTitles.add("Student Village 2")
        capLocations.add(LatLng(42.353117, -71.117747))

        for(i in 0..1)
            createMarker(capLocations.get(i), capTitles.get(i), "", type = 2)
    }

    fun getShuttleMarkers() {
        val url = "https://www.bu.edu/bumobile/rpc/bus/livebus.json.php"
        val objectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                // The third parameter Listener overrides the method onResponse() and passes
                //JSONObject as a parameter
                Response.Listener { response ->
                    // Takes the response from the JSON request
                    try {
                        var resultSetObject = response.getJSONObject("ResultSet")
                        var resultArray = resultSetObject.getJSONArray("Result")
                        for(i in 0.. resultArray.length() -1){
                            var shuttleObject = resultArray.getJSONObject(i)
                            var shuttleNumber = shuttleObject.getString("call_name")
                            var shuttleLocation = LatLng(shuttleObject.getDouble("lat"), shuttleObject.getDouble("lng"))
                            var shuttleRoute = "Route: " + shuttleObject.getString("route")
                            if(shuttleObject!=null){
                                createShuttleMarker(shuttleLocation, "Bus Number: " + shuttleNumber, shuttleRoute, shuttleNumber)
                            }
                        }


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

    /*fun scheduleDataRefresh(){
        var handler = Handler()
        var runnable = Runnable {
            getShuttleMarkers()
            Toast.makeText(activity, "Refreshed", Toast.LENGTH_SHORT)
        }
        handler.postDelayed(runnable, 5000)
    }*/
}
