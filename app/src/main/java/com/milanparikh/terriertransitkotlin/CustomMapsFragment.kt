package com.milanparikh.terriertransitkotlin


import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 */
class CustomMapsFragment : Fragment(), OnMapReadyCallback{
    private lateinit var mMap: GoogleMap
    lateinit var requestQueue: RequestQueue
    var shuttleHashMap:HashMap<String, Marker> = HashMap()
    var inboundStopsHashMap:HashMap<Int, Marker> = HashMap()
    var outboundStopsHashMap:HashMap<Int, Marker> = HashMap()
    var capStopsHashMap:HashMap<Int, Marker> = HashMap()
    var permissionsGranted:Boolean = false
    lateinit var mGeoDataClient:GeoDataClient
    lateinit var mPlaceDetectionClient:PlaceDetectionClient
    lateinit var mFusedLocationProviderClient:FusedLocationProviderClient
    var mLastKnownLocation:Location? = null
    var timesHashMap: HashMap<String, MutableList<Long>> = HashMap()
    var countdown: InfoWindowCountdown? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view:View = inflater.inflate(R.layout.fragment_map, container, false)
        mGeoDataClient = Places.getGeoDataClient(activity, null)
        mPlaceDetectionClient = Places.getPlaceDetectionClient(activity, null)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        requestQueue = Volley.newRequestQueue(activity)
        getLocationPermission()
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnInfoWindowClickListener { this }
        mMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
            if(countdown!=null){
                countdown?.cancel()
            }
            countdown = InfoWindowCountdown(600000, 1000, marker)
            countdown?.start()
            true
        })
        mMap.setOnInfoWindowClickListener ( GoogleMap.OnInfoWindowClickListener {marker ->
            //Do something here
        } )
        mMap.setOnInfoWindowCloseListener ( GoogleMap.OnInfoWindowCloseListener {marker ->
            countdown?.cancel()
        } )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(42.350500, -71.105399), 15.0f))
        getStopData()
        getShuttleRoute()
        createShuttleMarkers()
        updateLocationUI()
        getDeviceLocation()
        var refreshShuttleHandler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                createShuttleMarkers()
                getStopData()
                refreshShuttleHandler.postDelayed(this, 5000)            }

        }
        refreshShuttleHandler.postDelayed(runnable, 5000)

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

    fun getOutboundStopMarkers() {
        var outboundLocations = ArrayList<LatLng>()
        var outboundTitles = ArrayList<String>()
        var outboundStopIds = ArrayList<Int>()
        outboundTitles.add("Huntington Ave.")
        outboundLocations.add(LatLng(42.342399, -71.084142))
        outboundStopIds.add(4160718)
        outboundTitles.add("Danielsen Hall")
        outboundLocations.add(LatLng(42.350745, -71.090213))
        outboundStopIds.add(4160722)
        outboundTitles.add("Myles Standish")
        outboundLocations.add(LatLng(42.349620, -71.094383))
        outboundStopIds.add(4160726)
        outboundTitles.add("Silber Way")
        outboundLocations.add(LatLng(42.349506, -71.100746))
        outboundStopIds.add(4160730)
        outboundTitles.add("Marsh Plaza")
        outboundLocations.add(LatLng(42.350166, -71.106300))
        outboundStopIds.add(4160734)
        outboundTitles.add("College of Fine Arts")
        outboundLocations.add(LatLng(42.351061, -71.113889))
        outboundStopIds.add(4160738)

        for(i in 0..5){
            var outboundSecondsList:MutableList<Long>? = timesHashMap.get(outboundStopIds.get(i).toString())
            outboundSecondsList?.sort()
            var outboundMilliseconds:Long = 0
            var outboundTimeText = "No Stop Information"
            if(outboundSecondsList?.get(0)!=null){
                outboundMilliseconds = outboundSecondsList.get(0)
                outboundTimeText = "ETA: " + getTimeString(outboundMilliseconds)
            }
            if(outboundStopsHashMap.get(outboundStopIds.get(i))!=null){
                var updateMarker:Marker? = outboundStopsHashMap.get(outboundStopIds.get(i))
                updateMarker?.snippet=outboundTimeText
            }
            else {
                var marker: Marker = mMap.addMarker(MarkerOptions()
                        .position(outboundLocations.get(i))
                        .title(outboundTitles.get(i))
                        .snippet(outboundTimeText)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                outboundStopsHashMap.put(outboundStopIds.get(i), marker)
            }
            //createMarker(outboundLocations.get(i), outboundTitles.get(i), outboundTimeText, type = 0)
        }
    }

    fun getInboundStopMarkers() {
        var inboundLocations = ArrayList<LatLng>()
        var inboundTitles = ArrayList<String>()
        var inboundStopIds = ArrayList<Int>()
        inboundTitles.add("Amory Street")
        inboundLocations.add(LatLng(42.350665, -71.113570))
        inboundStopIds.add(4114006)
        inboundTitles.add("St. Mary's Street")
        inboundLocations.add(LatLng(42.349816, -71.106422))
        inboundStopIds.add(4149154)
        inboundTitles.add("Blandford Street")
        inboundLocations.add(LatLng(42.349082,-71.100355 ))
        inboundStopIds.add(4068466)
        inboundTitles.add("Hotel Commonwealth")
        inboundLocations.add(LatLng(42.348697, -71.095134))
        inboundStopIds.add(4068470)
        inboundTitles.add("Huntington Ave.")
        inboundLocations.add(LatLng(42.342511, -71.084728))
        inboundStopIds.add(4160714)

        for(i in 0..4){
            var inboundSecondsList:MutableList<Long>? = timesHashMap.get(inboundStopIds.get(i).toString())
            inboundSecondsList?.sort()
            var inboundMilliseconds:Long = 0
            var inboundTimeText = "No Stop Information"
            if(inboundSecondsList?.get(0)!=null){
                inboundMilliseconds = inboundSecondsList.get(0)
                inboundTimeText = "ETA: " + getTimeString(inboundMilliseconds)
            }
            if(inboundStopsHashMap.get(inboundStopIds.get(i))!=null){
                var updateMarker:Marker? = inboundStopsHashMap.get(inboundStopIds.get(i))
                updateMarker?.snippet=inboundTimeText
            }
            else {
                var marker: Marker = mMap.addMarker(MarkerOptions()
                        .position(inboundLocations.get(i))
                        .title(inboundTitles.get(i))
                        .snippet(inboundTimeText)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                outboundStopsHashMap.put(inboundStopIds.get(i), marker)
            }
            //createMarker(inboundLocations.get(i), inboundTitles.get(i), inboundTimeText, type = 1)
        }
    }

    fun getCapMarkers() {
        var capLocations = ArrayList<LatLng>()
        var capTitles = ArrayList<String>()
        var capStopIds = ArrayList<Int>()
        capTitles.add("710 Albany Street")
        capLocations.add(LatLng(42.335177, -71.071026))
        capStopIds.add(4068482)
        capTitles.add("Student Village 2")
        capLocations.add(LatLng(42.353117, -71.117747))
        capStopIds.add(4160714)

        for(i in 0..1){
            var capSecondsList:MutableList<Long>? = timesHashMap.get(capStopIds.get(i).toString())
            capSecondsList?.sort()
            var capMilliseconds:Long = 0
            var capTimeText = "No Stop Information"
            if(capSecondsList?.get(0)!=null){
                capMilliseconds = capSecondsList.get(0)
                capTimeText = "ETA: " + getTimeString(capMilliseconds)
            }
            if(capStopsHashMap.get(capStopIds.get(i))!=null){
                var updateMarker:Marker? = capStopsHashMap.get(capStopIds.get(i))
                updateMarker?.snippet=capTimeText
            }
            else {
                var marker: Marker = mMap.addMarker(MarkerOptions()
                        .position(capLocations.get(i))
                        .title(capTitles.get(i))
                        .snippet(capTimeText)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                outboundStopsHashMap.put(capStopIds.get(i), marker)
            }
            //createMarker(capLocations.get(i), capTitles.get(i), capTimeText, type = 2)
        }
    }

    fun getTimeString(p0: Long):String{
        var minutes = (p0/1000)/60
        var seconds = (p0/1000)-(minutes*60)
        var timeString = minutes.toString()+ ":" + String.format("%02d", seconds)
        return timeString
    }

    fun createShuttleMarkers() {
        val url = "https://www.bu.edu/bumobile/rpc/bus/livebus.json.php"
        val objectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                // The third parameter Listener overrides the method onResponse() and passes
                //JSONObject as a parameter
                Response.Listener { response ->
                    // Takes the response from the JSON request
                    try {
                        var resultSetObject = response.getJSONObject("ResultSet")
                        var resultArray = resultSetObject.getJSONArray("Result")
                        val shuttleKeyList = mutableListOf<String>()
                        for(i in 0.. resultArray.length() -1){
                            var shuttleObject = resultArray.getJSONObject(i)
                            var shuttleNumber = shuttleObject.getString("call_name")
                            var shuttleLocation = LatLng(shuttleObject.getDouble("lat"), shuttleObject.getDouble("lng"))
                            var shuttleRouteType = "Route"
                            when(shuttleObject.getString("route")){
                                "weekday" -> shuttleRouteType = "Weekday"
                                "caloop" -> shuttleRouteType = "Comm Ave Loop"
                            }
                            var shuttleRoute = "Route: " + shuttleRouteType
                            if(shuttleObject!=null){
                                if(shuttleHashMap.get(shuttleNumber)!=null){
                                    var updateMarker:Marker? = shuttleHashMap.get(shuttleNumber)
                                    updateMarker?.position = shuttleLocation
                                    shuttleKeyList.add(i, shuttleNumber)
                                }else{
                                    var marker: Marker = mMap.addMarker(MarkerOptions()
                                            .position(shuttleLocation)
                                            .title("Bus Number: " + shuttleNumber)
                                            .snippet(shuttleRoute)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                                    marker.tag = shuttleNumber
                                    shuttleKeyList.add(i, shuttleNumber)
                                    shuttleHashMap.put(shuttleNumber, marker)
                                }
                                //createShuttleMarker(shuttleLocation, "Bus Number: " + shuttleNumber, shuttleRoute, shuttleNumber)
                            }
                        }
                        shuttleHashMap.keys.retainAll(shuttleKeyList)


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

    fun getStopData() {
        val url = "https://www.bu.edu/bumobile/rpc/bus/livebus.json.php"
        var objectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                // The third parameter Listener overrides the method onResponse() and passes
                //JSONObject as a parameter
                Response.Listener { response ->
                    // Takes the response from the JSON request
                    try {
                        timesHashMap.clear()
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
                        getInboundStopMarkers()
                        getOutboundStopMarkers()
                        getCapMarkers()

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
        val dateformat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        var arrivalDate: Date = dateformat.parse(arrivalTime)
        var currentDate = Date()
        var secondsUntil:Long = (arrivalDate.time - currentDate.time)
        return secondsUntil
    }

    fun getLocationPermission(){
        if(ContextCompat.checkSelfPermission(activity.applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            permissionsGranted = true
        }else{
            val permArray:Array<String> = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(activity, permArray, 1);
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        permissionsGranted = false
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = true
                }
            }
        }
        //updateLocationUI()
    }

    private fun updateLocationUI() {
        try {
            if (permissionsGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    fun getDeviceLocation(){
        try{
            if(permissionsGranted){
                var locationResult:Task<Location> = mFusedLocationProviderClient.lastLocation

                locationResult.addOnCompleteListener(activity, OnCompleteListener { task ->
                    if(task.isSuccessful){
                        mLastKnownLocation = task.getResult()
                        mLastKnownLocation?.let { location ->
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.0f))
                        }
                    }else{
                    }
                })
            }
        } catch (e:SecurityException){
            Log.e("Exception: %s", e.message)
        }
    }

    class InfoWindowCountdown:CountDownTimer{
        var stopMarker:Marker?
        constructor(millisInFuture: Long, countDownInterval: Long, stopMarker: Marker?) : super(millisInFuture, countDownInterval){
            this.stopMarker = stopMarker
        }

        override fun onFinish() {
        }

        override fun onTick(p0: Long) {
            stopMarker?.showInfoWindow()
        }

    }

}
