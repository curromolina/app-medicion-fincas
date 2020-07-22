package fmolinadev.medicion_fincas

import android.graphics.Color
import android.os.Bundle
import android.os.Process
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    var perimetro: PolylineOptions? = null
    var calculoPerim = false
    var close = false
    var initCoord: Marker? = null
    private var vistaId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtenemos una instacia de la clase SupportMapFragment
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val Toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(Toolbar)
        supportActionBar!!.setIcon(R.mipmap.app_icon)
        val botonVista = findViewById(R.id.botonVista) as Button
        botonVista.setOnClickListener {
            var vista: String? = null
            // establece la siguiente a la actual
            vistaId = ++vistaId % 4
            when (vistaId) {
                0 -> {
                    mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
                    vista = "normal"
                }
                1 -> {
                    mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
                    vista = "híbrida"
                }
                2 -> {
                    mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    vista = "de satélite"
                }
                3 -> {
                    mMap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    vista = "de terreno"
                }
            }
            // Muestra su nombre
            Toast.makeText(this@MapsActivity, "Vista $vista", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val spain = LatLng(40.40, -3.82)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(spain))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(5.0f))

        // CÓDIGO APP *****************************************************************
        // Enlaza los eventos del mapa
        /**
         * gestiona el evento OnMapClick
         */
        mMap!!.setOnMapClickListener { point ->
            val proj = mMap!!.projection
            val coord = proj.toScreenLocation(point)
            if (!calculoPerim) {
                Toast.makeText(
                        this@MapsActivity,
                        """
                            Posición:
                            Lat: ${String.format("%.3f", point.latitude)}
                            Lng: ${String.format("%.3f", point.longitude)}
                            """.trimIndent(), Toast.LENGTH_SHORT)
                        .show()
            } else {
                if (close) {
                    Toast.makeText(this@MapsActivity, "Ya ha cerrado la parcela.\nBorre e inicie una nueva", Toast.LENGTH_SHORT).show()
                } else {
                    perimetro!!.add(point)
                    mMap!!.addPolyline(perimetro)
                }
            }
        }
        /**
         * gestiona el evento onMapLongClick
         */
        mMap!!.setOnMapLongClickListener { point ->
            if (calculoPerim) {
                Toast.makeText(this@MapsActivity, "Ya tiene una parcela iniciada", Toast.LENGTH_SHORT).show()
            } else {
                val proj = mMap!!.projection
                val coord = proj.toScreenLocation(point)
                Toast.makeText(this@MapsActivity, """
     Inicio de polígono
     Coordenadas agregadas correctamente:
     Lat: ${String.format("%.3f", point.latitude)}
     Lng: ${String.format("%.3f", point.longitude)}
     """.trimIndent(), Toast.LENGTH_LONG).show()
                initCoord = mMap!!.addMarker(MarkerOptions().position(point).title("Parcela"))
                perimetro = PolylineOptions()
                perimetro!!.add(point)
                perimetro!!.width(5f)
                perimetro!!.color(Color.RED)
                calculoPerim = true
                close = false
            }
        }
        /**
         * gestiona el evento onMarkerClick
         */
        mMap!!.setOnMarkerClickListener { marker ->
            Toast.makeText(this@MapsActivity,
                    """
                        Marcador pulsado:
                        ${marker.title}
                        """.trimIndent(),
                    Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.area -> if (!calculoPerim) {
                Toast.makeText(this, "No hay ninguna parcela creada", Toast.LENGTH_SHORT).show()
            } else {
                perimetro!!.add(initCoord!!.position)
                perimetro!!.color(Color.BLUE)
                mMap!!.addPolyline(perimetro)
                val area = SphericalUtil.computeArea(perimetro!!.points)
                Toast.makeText(this, "Área: " + String.format("%.2f", area) + " m²", Toast.LENGTH_SHORT).show()
                close = true
            }
            R.id.perimetro -> if (!calculoPerim) {
                Toast.makeText(this, "No hay perímetro creado", Toast.LENGTH_SHORT).show()
            } else {
                perimetro!!.color(Color.BLUE)
                mMap!!.addPolyline(perimetro)
                val longit = SphericalUtil.computeLength(perimetro!!.points)
                Toast.makeText(this, "Longitud: " + String.format("%.2f", longit) + " m", Toast.LENGTH_SHORT).show()
            }
            R.id.borrar -> if (!calculoPerim) {
                Toast.makeText(this, "No hay ningún perímetro ni parcela creada", Toast.LENGTH_SHORT).show()
            } else {
                mMap!!.clear()
                calculoPerim = false
                Toast.makeText(this, "Superficie / ruta borrada\nInicie una nueva", Toast.LENGTH_SHORT).show()
            }
            R.id.salir -> {
                Process.killProcess(Process.myPid())
                System.exit(1)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}