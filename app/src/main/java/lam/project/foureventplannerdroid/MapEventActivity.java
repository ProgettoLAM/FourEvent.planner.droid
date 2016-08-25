package lam.project.foureventplannerdroid;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import static lam.project.foureventplannerdroid.CreateEventActivity.mCurrentLocation;

public class MapEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private final static float DEFAULT_ZOOM = 15.0f;

    private ViewGroup view;
    private SearchView searchView;
    private MenuItem searchItem;

    private LatLng currentLatLng;

    private FloatingActionButton mapFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_event);

        setTitle("Scegli l'indirizzo");

        view = (ViewGroup) getWindow().getDecorView();

        if(mCurrentLocation != null) {

            currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }

        mapFab = (FloatingActionButton) findViewById(R.id.map_fab);

        //Per ottenere il SupportMapFragment e notificare quando la mappa è pronta per l'uso
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        showMap(currentLatLng);

    }

    private void showMap(LatLng position) {

        if (mMap == null || position == null) {
            Snackbar.make(view, "Localizzazione non disponibile", Snackbar.LENGTH_LONG).show();
            return;
        }

        //currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        mMap.addMarker(new MarkerOptions().position(position));
        final CameraUpdate cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(position, DEFAULT_ZOOM);

        mMap.moveCamera(cameraUpdate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        if(searchItem != null) {
            searchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("MapEvent", "Search cliccato!");
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    Log.d("MapEvent", "Search chiuso!");
                    return false;
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("MapEvent", "Search cliccato!");
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("MapEvent", "Query text submit!" + query);
                    currentLatLng = getLocationFromAddress(getApplicationContext(), query);
                    showMap(currentLatLng);
                    mapFab.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.d("MapEvent", "Query text changed!" + newText);
                    currentLatLng = getLocationFromAddress(getApplicationContext(), newText);
                    showMap(currentLatLng);

                    if(newText.equals("")) {
                        showMap(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    }
                    return false;
                }
            });
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    return false;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
        return currentLatLng;
    }

    public void onClickAddress(final View view) {
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtra("Address", currentLatLng);
        startActivity(intent);
        finish();
    }

}
