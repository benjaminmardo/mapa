package cl.ipgv.mapa;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private lateinit var mMap: GoogleMap;
    private lateinit var fusedLocationClient: FusedLocationProviderClient;
    private val markers = mutableListOf<Marker>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar el fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment;
        mapFragment.getMapAsync(this);

        // Guardar los puntos al hacer clic en el botón
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveMarkersToFirebase();
        };
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap;

        // Habilitar clic en el mapa para agregar marcadores
        mMap.setOnMapClickListener { latLng ->
                val marker = mMap.addMarker(MarkerOptions().position(latLng).title("Nuevo Punto"));
            if (marker != null) {
                markers.add(marker);
            }
        };

        // Obtener y mover la cámara a la ubicación actual
        getCurrentLocation();
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1
            );
            return;
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
            };
        };
    }

    private fun saveMarkersToFirebase() {
        val database = FirebaseDatabase.getInstance().getReference("Markers");
        for (marker in markers) {
            val markerData = mapOf(
                    "latitude" to marker.position.latitude,
                    "longitude" to marker.position.longitude,
                    "title" to marker.title
            );
            database.push().setValue(markerData);
        }
        markers.clear();
        showMessage("Puntos guardados en Firebase");
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}