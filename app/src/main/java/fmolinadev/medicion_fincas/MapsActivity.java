package fmolinadev.medicion_fincas;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    PolylineOptions perimetro = null;
    boolean calculoPerim, close = false;
    Marker initCoord = null;
    private int vistaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtenemos una instacia de la clase SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar Toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(Toolbar);
        getSupportActionBar().setIcon(R.mipmap.app_icon);
        Button botonVista = (Button) findViewById(R.id.botonVista);
        botonVista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vista = null;
                // establece la siguiente a la actual
                vistaId = ++vistaId % 4;
                switch (vistaId) {
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        vista = "normal";
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        vista = "híbrida";
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        vista = "de satélite";
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        vista = "de terreno";
                        break;
                }
                // Muestra su nombre
                Toast.makeText(MapsActivity.this, "Vista " + vista, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng spain = new LatLng(40.40, -3.82);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(spain));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5.0f));

        // CÓDIGO APP *****************************************************************
        // Enlaza los eventos del mapa

        /**
         * gestiona el evento OnMapClick
         */
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                Projection proj = mMap.getProjection();
                Point coord = proj.toScreenLocation(point);

                if (!calculoPerim) {
                    Toast.makeText(
                            MapsActivity.this,
                            "Posición:\n" + "Lat: " + String.format("%.3f", point.latitude) + "\n" + "Lng: "
                                    + String.format("%.3f", point.longitude), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    if (close) {
                        Toast.makeText(MapsActivity.this, "Ya ha cerrado la parcela.\nBorre e inicie una nueva", Toast.LENGTH_SHORT).show();
                    } else {
                        perimetro.add(point);
                        mMap.addPolyline(perimetro);
                    }
                }
            }
        });

        /**
         * gestiona el evento onMapLongClick
         */
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {
                if (calculoPerim) {
                    Toast.makeText(MapsActivity.this, "Ya tiene una parcela iniciada", Toast.LENGTH_SHORT).show();
                } else {
                    Projection proj = mMap.getProjection();
                    Point coord = proj.toScreenLocation(point);

                    Toast.makeText(MapsActivity.this, "Inicio de polígono\nCoordenadas agregadas correctamente:\n"
                            + "Lat: " + String.format("%.3f", point.latitude) + "\n"
                            + "Lng: " + String.format("%.3f", point.longitude), Toast.LENGTH_LONG).show();

                    initCoord = mMap.addMarker(new MarkerOptions().position(point).title("Parcela"));
                    perimetro = new PolylineOptions();
                    perimetro.add(point);
                    perimetro.width(5);
                    perimetro.color(Color.RED);
                    calculoPerim = true;
                    close = false;
                }
            }
        });

        /**
         * gestiona el evento onMarkerClick
         */
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapsActivity.this,
                        "Marcador pulsado:\n" + marker.getTitle(),
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.area:
                if (!calculoPerim) {
                    Toast.makeText(this, "No hay ninguna parcela creada", Toast.LENGTH_SHORT).show();
                } else {
                    perimetro.add(initCoord.getPosition());
                    perimetro.color(Color.BLUE);
                    mMap.addPolyline(perimetro);
                    double area = SphericalUtil.computeArea(perimetro.getPoints());
                    Toast.makeText(this, "Área: " + String.format("%.2f", area) + " m²", Toast.LENGTH_SHORT).show();
                    close = true;
                }
                break;
            case R.id.perimetro:
                if (!calculoPerim) {
                    Toast.makeText(this, "No hay perímetro creado", Toast.LENGTH_SHORT).show();
                } else {
                    perimetro.color(Color.BLUE);
                    mMap.addPolyline(perimetro);
                    double longit = SphericalUtil.computeLength(perimetro.getPoints());
                    Toast.makeText(this, "Longitud: " + String.format("%.2f", longit) + " m", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.borrar:
                if (!calculoPerim) {
                    Toast.makeText(this, "No hay ningún perímetro ni parcela creada", Toast.LENGTH_SHORT).show();
                } else {
                    mMap.clear();
                    calculoPerim = false;
                    Toast.makeText(this, "Superficie / ruta borrada\nInicie una nueva", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.salir:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
