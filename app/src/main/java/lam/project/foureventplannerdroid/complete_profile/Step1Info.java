package lam.project.foureventplannerdroid.complete_profile;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.fcannizzaro.materialstepper.AbstractStep;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.DateConverter;
import lam.project.foureventplannerdroid.utils.ImageManager;
import lam.project.foureventplannerdroid.utils.PlannerManager;
import lam.project.foureventplannerdroid.utils.Utility;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.MultipartRequest;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;

/**
 *
 *
 */
public class Step1Info extends AbstractStep{

    public static final int REQUEST_CODE = 1;

    private TextView dateInfo;

    private CircleImageView imgUser;
    private EditText txtName;
    private EditText txtSurname;
    private EditText txtRole;
    private EditText txtLocation;
    private RadioGroup radioGroup;
    private Planner mCurrentPlanner = PlannerManager.get().getUser();

    private String mImageUri;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;

    private Fragment thisFragment;

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

        thisFragment = this;
        return initView(inflater.inflate(R.layout.step1_info, container, false));
    }

    /**
     *
     * @param rootView
     * @return
     */
    private View initView(final View rootView) {

        dateInfo = (TextView) rootView.findViewById(R.id.date_info);
        txtName = (EditText) rootView.findViewById(R.id.name_info);
        txtRole = (EditText) rootView.findViewById(R.id.role_info);
        txtSurname = (EditText) rootView.findViewById(R.id.surname_info);
        txtLocation = (EditText) rootView.findViewById(R.id.location_info);
        radioGroup = (RadioGroup) rootView.findViewById(R.id.radio_info);
        txtLocation = (EditText) rootView.findViewById(R.id.location_info);
        imgUser = (CircleImageView) rootView.findViewById(R.id.profile_image);

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        dateInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DialogFragment newFragment = new SelectDateFragment();

                newFragment.setTargetFragment(thisFragment,REQUEST_CODE);
                newFragment.show(getFragmentManager(), "DatePicker");
            }
        });

        return rootView;
    }

    @Override
    public boolean nextIf() {


        boolean isNotEmptyFields = !txtName.getText().toString().matches("") &&
                !txtSurname.getText().toString().matches("");

        if(isNotEmptyFields){

            //setto il nome dell'utente
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

        return isNotEmptyFields;
    }

    @Override
    public String error() {

        return "Inserisci nome e cognome obbligatori";
    }

    //region Upload image

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == SELECT_FILE)

                onSelectFromGalleryResult(data);

            else if (requestCode == REQUEST_CAMERA)

                onCaptureImageResult(data);

        } else if(requestCode == REQUEST_CODE) {

            dateInfo.setText(data.getStringExtra(SelectDateFragment.DATE_RESULT));
        }
    }

    /**
     *
     *
     */
    private void selectImage() {

        final CharSequence[] items = { "Scatta una foto", "Scegli dalla galleria", "Annulla" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Aggiungi un'immagine");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(getContext());
                if (items[item].equals("Scatta una foto")) {
                    userChoosenTask = "Scatta una foto";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Scegli dalla galleria")) {
                    userChoosenTask = "Scegli dalla galleria";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Annulla")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     *
     *
     */
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     *
     *
     */
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    /**
     *
     *
     * @param data
     */
    private void onSelectFromGalleryResult(Intent data) {

        try {
            Bitmap thumbnail = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
            File createdImage = ImageManager.get().writeImage(mCurrentPlanner.email,thumbnail);

            if(createdImage != null){
                imgUser.setImageBitmap(thumbnail);
                uploadImage(createdImage);
            }

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     * @param data
     */
    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        File createdImage = ImageManager.get().writeImage(mCurrentPlanner.email,thumbnail);

        if(createdImage != null){
            imgUser.setImageBitmap(thumbnail);
            uploadImage(createdImage);
        }
    }

    /**
     *
     *
     * @param toUploadFile
     */
    private void uploadImage(File toUploadFile) {

        String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                .appendPath("img").appendEncodedPath(PlannerManager.get(getContext()).getUser().email).getUri();

        final ProgressDialog loading = ProgressDialog.show(getContext(), "Immagine del profilo", "Caricamento in corso..", false, false);

        MultipartRequest mMultipartRequest = new MultipartRequest(url,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                            Snackbar.make(imgUser, "Errore nel caricamento dell'immagine", Snackbar.LENGTH_SHORT)
                                    .show();
                            loading.dismiss();
                            }
                },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Snackbar successSnackBar = Snackbar.make(imgUser, "Immagine caricata!", Snackbar.LENGTH_SHORT);
                        successSnackBar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightGreen));
                        successSnackBar.show();

                        mImageUri = response;
                        loading.dismiss();

                    }
                },toUploadFile,"filename");

        VolleyRequest.get(getContext()).add(mMultipartRequest);

    }

    //endregion

    //region Date

    /**
     *
     *
     */
    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        public static String DATE_RESULT = "date";
        private String mDate;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR,-18);
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), this, yy, mm, dd);
            pickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            pickerDialog.setTitle("");

            return pickerDialog;
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }
        public void populateSetDate(int year, int month, int day) {

            final Calendar calendar = Calendar.getInstance();
            calendar.set(year,month,day);
            mDate = DateConverter.dateFromCalendar(calendar);

            sendResult(REQUEST_CODE);
        }

        private void sendResult(int REQUEST_CODE) {

            Intent intent = new Intent();
            intent.putExtra(DATE_RESULT,mDate);
            getTargetFragment().onActivityResult(getTargetRequestCode(), REQUEST_CODE, intent);
        }
    }

    //endregion

}

