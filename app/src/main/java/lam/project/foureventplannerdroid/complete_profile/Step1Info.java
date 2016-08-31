package lam.project.foureventplannerdroid.complete_profile;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.PlannerManager;
import lam.project.foureventplannerdroid.utils.Utility;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.MultipartRequest;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;

/**
 * Created by Vale on 11/08/2016.
 */

public class Step1Info extends AbstractStep{

    private int i = 1;
    public static TextView dateInfo;

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
        imgUser = (CircleImageView) rootView.findViewById(R.id.profile_image);

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


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
            mCurrentPlanner.addName(txtName.getText().toString()+ " "
                    + txtSurname.getText().toString());

            if(mImageUri != null) {
                mCurrentPlanner.updateImage(mImageUri);
            }

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

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    //Permessi per scattare una foto/scegliere un'immagine dalla galleria
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //Codice per negare i permessi
                }
                break;
        }
    }

    /*Risultato della scelta dell'immagine in base al codice che ritorna:
      - se ritorna "SELECT_FILE" si richiama il metodo per la scelta dalla galleria
      - se ritorna "REQUEST_CAMERA" si richiama il metodo per la scelta dalla fotocamera
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    //Risultato dell'immagine scelta dalla galleria
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                mImageUri = mCurrentPlanner.email + ".jpg";

                File destination = new File(Environment.getExternalStorageDirectory(), mImageUri);
                FileOutputStream fo;
                destination.createNewFile();
                fo = new FileOutputStream(destination);

                fo.write(bytes.toByteArray());
                fo.close();
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                        destination.getAbsolutePath(), destination.getName(), null);

                imgUser.setImageBitmap(bm);
                uploadImage(destination);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    //Risultato dell'immagine scattata dalla fotocamera
    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        mImageUri =  mCurrentPlanner.email + ".jpg";

        File destination = new File(Environment.getExternalStorageDirectory(),
                mImageUri);
        FileOutputStream fo;
        try {

            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
            MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                    destination.getAbsolutePath(), destination.getName(), null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgUser.setImageBitmap(thumbnail);
        uploadImage(destination);
    }

    private void uploadImage(File toUploadFile) {

        String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                .appendPath("img").appendEncodedPath(PlannerManager.get(getContext()).getUser().email).getUri();

        final ProgressDialog loading = ProgressDialog.show(getContext(), "Immagine dell'evento", "Caricamento in corso..", false, false);

        MultipartRequest mMultipartRequest = new MultipartRequest(url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(getView(), "Errore nel caricamento dell'immagine", Snackbar.LENGTH_SHORT)
                        .show();
                loading.dismiss();
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Snackbar.make(getView(), "Immagine caricata!", Snackbar.LENGTH_SHORT)
                        .show();
                mImageUri = response;
                loading.dismiss();

            }
        },toUploadFile,"filename");

        VolleyRequest.get(getContext()).add(mMultipartRequest);

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

