package lam.project.foureventplannerdroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import lam.project.foureventplannerdroid.complete_profile.CompleteProfileActivity;
import lam.project.foureventplannerdroid.complete_profile.StepManager;
import lam.project.foureventplannerdroid.fragment.EventFragment;
import lam.project.foureventplannerdroid.fragment.ProfileFragment;
import lam.project.foureventplannerdroid.fragment.WalletFragment;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.shared_preferences.PlannerManager;

import static lam.project.foureventplannerdroid.EventDetailActivity.OPEN_FRAGMENT_WALLET;

public class MainActivity extends AppCompatActivity {

    private Fragment selectedFragment;
    public static Planner mCurrentPlanner;

    private static int clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Se il profilo è completo
        if (StepManager.get(this).getStep() == StepManager.COMPLETE) {

            setContentView(R.layout.activity_main);
            initView();

        //Altrimenti il planner è reindirizzato al completamento del profilo
        } else {

            Intent completeProfileIntent = new Intent(this, CompleteProfileActivity.class);
            startActivity(completeProfileIntent);
            finish();
        }
    }

    /**
     * Metodo per inizializzare gli elementi della view
     */
    private void initView() {

        //Si setta il titolo dell'Activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        //Si salva in una variabile il planner corrente
        mCurrentPlanner = PlannerManager.get(this).getUser();

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        //Alla selezione di una tab si apre un fragment diverso
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                if (tabId == R.id.tab_events) {

                    selectedFragment = new EventFragment();

                } else if (tabId == R.id.tab_profile) {

                    selectedFragment = new ProfileFragment();

                } else if (tabId == R.id.tab_wallet) {

                    selectedFragment = new WalletFragment();
                }

                //Setto la pagina principale come quella di ricerca degli eventi
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.anchor_point, selectedFragment)
                        .commit();

            }
        });

        //Se la MainActivity è il risultato di un Intent precedente, si prende il codice del risultato
        //e si apre la tab del portafoglio in questo caso
        if (getIntent().hasExtra(OPEN_FRAGMENT_WALLET)) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.anchor_point, new WalletFragment())
                    .commit();
        }

    }

    @Override
    public void onBackPressed() {

        //Quando si clicca il pulsante Back, se il numero di volte è 0, appare un messaggio di
        //cliccare nuovamente
        if(clicked == 0) {

            Toast.makeText(this,"Clicca ancora per chiudere l'app",Toast.LENGTH_LONG).show();
            clicked ++;

        } else { super.onBackPressed();}

    }

    @Override
    protected void onResume() {

        super.onResume();
        //Al resume della Activity, il numero dei click al pulsante Back è resettato
        clicked = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Se si preme il pulsante di logout presente nella toolbar
        switch (item.getItemId()) {

            case R.id.logout:

                logout();
                break;

            default:
                break;
        }

        return true;
    }

    /**
     * Metodo per eseguire il logout del planner
     */
    public void logout() {

        //Creazione di un dialog tramite il Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Logout");
        builder.setMessage("Vuoi uscire da FourEvent?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Rimozione del planner corrente in locale
                PlannerManager.get(MainActivity.this).remove();

                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                finish();
            }
        })
        .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }

}

