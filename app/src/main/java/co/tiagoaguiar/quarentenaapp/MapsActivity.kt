package co.tiagoaguiar.quarentenaapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RawRes

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import org.json.JSONArray
import org.json.JSONException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val usa = LatLng(31.51073, -96.4247)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(usa))
        mMap.setMinZoomPreference(3.9f)
        mMap.setMaxZoomPreference(5.79f)
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.styles_json))

        addHeatMap()
    }

    private fun addHeatMap() {
        var list: List<WeightedLatLng>? = null

        val jsonArray = readJson(R.raw.counties_data)
        val counties = readCounties(jsonArray)

        try {
            list = readItems(readJson(R.raw.us_county), counties)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val colors = intArrayOf(
            Color.rgb(255, 0, 255),
            Color.rgb(255, 0, 128)
        )
        val startPoints = floatArrayOf(0.2f, 1f)
        val gradient = Gradient(colors, startPoints)

        mMap.addTileOverlay(
            TileOverlayOptions().tileProvider(
                HeatmapTileProvider.Builder()
                    .weightedData(list)
                    .radius(30)
                    .gradient(gradient)
                    .build()
            )
        )
    }

    @Throws(JSONException::class)
    private fun readItems(
        array: JSONArray,
        jsonCounty: List<Triple<Double, Double, String>>
    ): ArrayList<WeightedLatLng> {
        val list = arrayListOf<WeightedLatLng>()
        val size = array.length()

        for (i in 0 until size) {
            val ob = array.getJSONObject(i)

            val county = ob.getString("county")
            val cases = ob.getString("cases")

            jsonCounty.firstOrNull {
                it.third == county
            }?.apply {
                val lat = this.first
                val lng = this.second
                list.add(WeightedLatLng(LatLng(lat, lng), cases.toDouble()))
            }
        }

        return list
    }

    @Throws(JSONException::class)
    private fun readCounties(array: JSONArray): ArrayList<Triple<Double, Double, String>> {
        val list = arrayListOf<Triple<Double, Double, String>>()
        val size = array.length()

        for (i in 0 until size) {
            val ob = array.getJSONObject(i)

            val latitude = ob.getDouble("latitude")
            val longitude = ob.getDouble("longitude")
            val county = ob.getString("county")

            list.add(Triple(latitude, longitude, county))
        }

        return list
    }

    private fun readJson(@RawRes resource: Int): JSONArray {
        val inputStream: InputStream = resources.openRawResource(resource)
        val json: String = Scanner(inputStream).useDelimiter("\\A").next()
        return JSONArray(json)
    }

}
