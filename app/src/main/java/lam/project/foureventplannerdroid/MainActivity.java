package lam.project.foureventplannerdroid;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import lam.project.foureventplannerdroid.complete_profile.CompleteProfileActivity;
import lam.project.foureventplannerdroid.complete_profile.StepManager;
import lam.project.foureventplannerdroid.fragment.FragmentEvent;
import lam.project.foureventplannerdroid.fragment.FragmentProfile;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.PlannerManager;

public class MainActivity extends AppCompatActivity {

    private Fragment selectedFragment;
    public static Planner mCurrentPlanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //se il profilo è completo
        if (StepManager.get(this).getStep() == StepManager.COMPLETE) {

            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setTitle(R.string.app_name);
            setSupportActionBar(toolbar);

            mCurrentPlanner = PlannerManager.get(this).getUser();

            BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);

            bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
                @Override
                public void onTabSelected(@IdRes int tabId) {

                    if (tabId == R.id.tab_events) {
                        selectedFragment = new FragmentEvent();
                        // The tab with id R.id.tab_favorites was selected,
                        // change your content accordingly.
                    } else if (tabId == R.id.tab_profile) {
                        selectedFragment = new FragmentProfile();

                    } else if (tabId == R.id.tab_wallet) {

                    }
                    //Setto la pagina principale come quella di ricerca degli eventi
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.anchor_point, selectedFragment)
                            .commit();
                }
            });

        } else {

            Intent completeProfileIntent = new Intent(this, CompleteProfileActivity.class);
            startActivity(completeProfileIntent);
            finish();
        }
    }

}

