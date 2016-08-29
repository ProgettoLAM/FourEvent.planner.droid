package lam.project.foureventplannerdroid.complete_profile;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.fcannizzaro.materialstepper.AbstractStep;

import java.util.Calendar;

import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.PlannerManager;

/**
 * Created by Vale on 11/08/2016.
 */

public class Step1Info extends AbstractStep{

    private int i = 1;
    public static TextView dateInfo;

    private EditText txtName;
    private EditText txtSurname;
    private EditText txtRole;
    private EditText txtLocation;
    private RadioGroup radioGroup;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    @Override
    public String name() {
        return "Completa profilo utente";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return initView(inflater.inflate(R.layout.step1_info, container, false));
    }

    private View initView(final View rootView) {

        LinearLayout birth_date = (LinearLayout) rootView.findViewById(R.id.birth_date);
        dateInfo = (TextView) rootView.findViewById(R.id.date_info);
        txtName = (EditText) rootView.findViewById(R.id.name_info);
        txtRole = (EditText) rootView.findViewById(R.id.role_info);
        txtSurname = (EditText) rootView.findViewById(R.id.surname_info);
        txtLocation = (EditText) rootView.findViewById(R.id.location_info);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_info);
        txtLocation = (EditText) rootView.findViewById(R.id.location_info);


        birth_date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "DatePicker");
            }
        });

        return rootView;
    }

    @Override
    public boolean nextIf() {


        boolean isNotEmptyName = !txtName.getText().toString().matches("") &&
                !txtSurname.getText().toString().matches("");

        if(isNotEmptyName){

            //setto il nome dell'utente
            Planner mCurrentPlanner = PlannerManager.get().getUser();
            mCurrentPlanner.addName(txtName.getText().toString()+ " "
                    + txtSurname.getText().toString());

            //controllo che esista il ruolo
            String role = txtRole.getText().toString();

            if(!role.matches("")) {

                mCurrentPlanner.addRole(role);
            }

            //controllo che esista la location
            String location = txtLocation.getText().toString();

            if(!location.matches("")) {

                mCurrentPlanner.addLocation(location);
            }

            //controllo che esista il giorno di nascita
            String birthDate = dateInfo.getText().toString();
            if(!birthDate.matches("")) {

                mCurrentPlanner.addBirthDate(birthDate);
            }

            //controllo che esista il sesso
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if(selectedId != -1) {
                RadioButton genderField = (RadioButton) getActivity().findViewById(selectedId);
                mCurrentPlanner.addGender(genderField.getText().toString());
            }

            getStepDataFor(1).putParcelable(Planner.Keys.USER, mCurrentPlanner);

        }

        return isNotEmptyName;
    }

    @Override
    public String error() {

        return "Inserisci nome e cognome";
    }

    //Classe relativa alla visualizzazione del dialog del calendario per la selezione della mData di nascita
    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }
        public void populateSetDate(int year, int month, int day) {
            dateInfo.setText(month+"/"+day+"/"+year);
        }

    }

}

