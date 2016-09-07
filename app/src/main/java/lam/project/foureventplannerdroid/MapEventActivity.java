package lam.project.foureventplannerdroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import java.util.Locale;

import lam.project.foureventplannerdroid.model.Event;

import static lam.project.foureventplannerdroid.CreateEventActivity.mCurrentLocation;

public class MapEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private final static float DEFAULT_ZOOM = 15.0f;

    private ViewGroup view;
    private SearchView searchView;
    private MenuItem searchItem;

    private LatLng currentLatLng;
    private String resultAddress;

    private FloatingActionButton mapFab;

    private Geocoder geocoder;
    private List<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_event);

        initView();

    }

    /**
     * Metodo per inizializzare gli elementi della view
     */
    private void initView() {

        setTitle("Scegli l'indirizzo");

        view = (ViewGroup) getWindow().getDecorView();

        mapFab = (FloatingActionButton) findViewById(R.id.map_fab);

        final Intent srcIntent = getIntent();

        if(srcIntent.hasExtra(Event.Keys.ADDRESS))
            currentLatLng = getLocationFromAddress(this,srcIntent.getStringExtra(Event.Keys.ADDRESS));

        //Se si è a conoscenza della location corrente del planner
        if(mCurrentLocation != null && currentLatLng == null) {

            //Si salvano le coordinate e in una lista si salvano le informazioni
            currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            List<Address> result = getLocationName(currentLatLng);

            //Si preleva l'indirizzo e la città e si imposta come risultato
            String address = result.get(0).getAddressLine(0);
            String splitAddress = address.replace(",", "");
            String city = result.get(0).getLocality();

            resultAddress = splitAddress + ", " + city;
        }

        //Per ottenere il SupportMapFragment e notificare quando la mappa è pronta per l'uso
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //region Google Maps

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

    /**
     * Visualizzazione della mappa in tutta la Activity
     * @param position localizzazione del planner
     */
    private void showMap(LatLng position) {

        //Se la posizione è uguale a null, il planner non è localizzato
        if (mMap == null || position == null) {
            Snackbar.make(view, "Localizzazione non disponibile", Snackbar.LENGTH_LONG).show();
            return;
        }

        mMap.clear();

        //Si aggiunge alla mappa un marker
        mMap.addMarker(new MarkerOptions().position(position));

        //Si fa lo zoom della fotocamera sulla posizione
        final CameraUpdate cameraUpdate = CameraUpdateFactory
                .newLatLngZoom(position, DEFAULT_ZOOM);

        mMap.moveCamera(cameraUpdate);
    }

    /**
     * Ricavare la localizzazione da un indirizzo
     * @param context contesto della Activity
     * @param strAddress indirizzo
     * @return coordinate
     */
    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;

        try {
            //Si prende il nome dall'indirizzo passato
            address = coder.getFromLocationName(strAddress, 5);

            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            //Si salvano in una variabile latitudine e longitudine
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) { ex.printStackTrace();}

        return currentLatLng;
    }

    /**
     * Metodo del fab per ritornare l'address, se presente, nei dettagli di un evento
     * @param view view del fab
     */
    public void onClickAddress(final View view) {

        Intent resultIntent = new Intent();

        if(resultAddress == null) {
            setResult(Activity.RESULT_CANCELED, resultIntent);
        }

        //Invio del risultato nella creazione di un evento
        else {
            resultIntent.putExtra("Address", resultAddress);
            setResult(Activity.RESULT_OK, resultIntent);
        }
        finish();

    }

    /**
     * Metodo per prendere il nome di un indirizzo dalle coordinate
     * @param currentLatLng coordinate
     * @return lista di indirizzi (solo uno)
     */
    private List<Address> getLocationName(LatLng currentLatLng) {
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        mapFab.setVisibility(View.VISIBLE);

        try {

            return addresses = geocoder.getFromLocation(currentLatLng.latitude,
                    currentLatLng.longitude, 1);

        } catch (IOException e) {

            e.printStackTrace();
            return null;
        }

    }

    //endregion

    @Override
    protected void onRestart() {
        super.onRestart();

        startActivity(new Intent(this,CreateEventActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_menu_map, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        //Listener dell'icon del search nella toolbar
        if(searchItem != null) {

            //Al submit del testo inserito nel search
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    //Se il risultato è nullo, si mostrano le coordinate iniziali del planner
                    if(query.equals("")) {
                        showMap(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    }
                    //Altrimenti si prende la query dalla quale si ricava l'indirizzo con le coordinate
                    else if(!query.equals("")) {
                        currentLatLng = getLocationFromAddress(getApplicationContext(), query);

                        //Se la ricerca non produce nessun risultato, l'indirizzo non esiste
                        if(currentLatLng == null) {

                            Snackbar snackbar = Snackbar.make(view, "L' indirizzo selezionato non esiste",
                                    Snackbar.LENGTH_LONG);
                            snackbar.getView().setBackgroundColor(getResources()
                                    .getColor(R.color.lightRed));
                            snackbar.show();
                        }
                        //Altrimenti si inserisce la localizzazione in una lista di indirizzi e si
                        //ricava indirizzo e città da ritornare come risultato
                        else {
                            List<Address> result = getLocationName(currentLatLng);

                            String address = result.get(0).getAddressLine(0);
                            String splitAddress = address.replace(",", "");
                            String city = result.get(0).getLocality();

                            showMap(currentLatLng);
                            resultAddress = splitAddress + ", "+ city;
                        }
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) { return false;}
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

}
