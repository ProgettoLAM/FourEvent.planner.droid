package lam.project.foureventplannerdroid.complete_profile;

import android.os.Bundle;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.github.fcannizzaro.materialstepper.style.DotStepper;

public class CompleteProfileActivity extends DotStepper {

    private int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTitle("Completa il tuo profilo");

        addStep(createFragment(new Step1Info()));
        addStep(createFragment(new Step2Credits()));

        super.onCreate(savedInstanceState);

    }

    /**
     * Creazione dei 2 fragments, ai quali si assegna ad ognuno una posizione
     * @param fragment fragment di uno step
     * @return fragment con una posizione
     */
    private AbstractStep createFragment(AbstractStep fragment) {

        Bundle b = new Bundle();
        b.putInt("position", i++);
        fragment.setArguments(b);
        return fragment;
    }

}
