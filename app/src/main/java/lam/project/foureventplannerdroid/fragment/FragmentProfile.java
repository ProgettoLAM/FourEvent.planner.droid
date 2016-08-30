package lam.project.foureventplannerdroid.fragment;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.hdodenhof.circleimageview.CircleImageView;
import lam.project.foureventplannerdroid.R;
import lam.project.foureventplannerdroid.model.Planner;
import lam.project.foureventplannerdroid.utils.PlannerManager;
import lam.project.foureventplannerdroid.utils.Utility;
import lam.project.foureventplannerdroid.utils.connection.CustomRequest;
import lam.project.foureventplannerdroid.utils.connection.FourEventUri;
import lam.project.foureventplannerdroid.utils.connection.MultipartRequest;
import lam.project.foureventplannerdroid.utils.connection.VolleyRequest;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentProfile extends Fragment {

    private String oldPassword;
    private String newPassword;
    private Snackbar snackbar;
    private CircleImageView imgProfile;

    private String mImageUri;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;

    private Planner planner;

    public FragmentProfile() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profilo");

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        planner = PlannerManager.get().getUser();

        ImageView editPass = (ImageView) view.findViewById(R.id.change_pass);

        TextView emailProfile = (TextView) view.findViewById(R.id.email_profile);
        TextView nameProfile = (TextView) view.findViewById(R.id.name_profile);
        TextView birthDateProfile = (TextView) view.findViewById(R.id.birth_date_profile);
        TextView locationProfile = (TextView) view.findViewById(R.id.location_profile);
        TextView genderProfile = (TextView) view.findViewById(R.id.gender_profile);
        imgProfile = (CircleImageView) view.findViewById(R.id.profile_image);


        /*if(planner.image == null) {
            if(planner.gender.equals("F")) {
                imgProfile.setImageResource(R.drawable.img_female);
            }
        }
        else {
            String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                    .appendPath("img").appendEncodedPath(planner.email).getUri();

            Picasso.with(getContext()).load(url).into(imgProfile);
        }*/

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        emailProfile.setText(planner.email);
        nameProfile.setText(planner.name);
        birthDateProfile.setText(planner.birthDate);
        locationProfile.setText(planner.location);

        if(planner.gender != null) {

            if(planner.gender.equals("F")) {

                genderProfile.setText("Femmina");

            } else if(planner.gender.equals("M")) {

                genderProfile.setText("Maschio");
            }
        }

        editPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Cambia la password");

                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, (ViewGroup) getView(), false);

                final EditText oldPasswordField = (EditText) viewInflated.findViewById(R.id.old_password);
                final EditText newPasswordField = (EditText) viewInflated.findViewById(R.id.new_password);

                builder.setView(viewInflated);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                        oldPassword = oldPasswordField.getText().toString();
                        newPassword = newPasswordField.getText().toString();

                        if(newPassword.length() >= 8) {

                            try {
                                String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                                        .appendPath("changepassword").appendEncodedPath(planner.email)
                                        .getUri();

                                JSONObject obj = new JSONObject("{'oldPassword':'"+oldPassword+"', 'newPassword':'"+newPassword+"'}");

                                CustomRequest request = new CustomRequest(Request.Method.POST, url, obj,

                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try{

                                                    snackbar = Snackbar
                                                            .make(getActivity().findViewById(R.id.container),
                                                                  response.getString("message"), Snackbar.LENGTH_LONG);

                                                    snackbar.getView()
                                                            .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightGreen));
                                                    snackbar.show();

                                                    dialog.dismiss();

                                                } catch (JSONException e) {

                                                    e.printStackTrace();
                                                }


                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {


                                        try {
                                            String responseBody = new String( error.networkResponse.data, "utf-8" );
                                            JSONObject jsonObject = new JSONObject( responseBody );

                                            String errorText = (String) jsonObject.get("message");

                                            snackbar = Snackbar
                                                    .make(getActivity().findViewById(R.id.container),
                                                          errorText, Snackbar.LENGTH_LONG);

                                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightRed));

                                            snackbar.show();

                                        } catch (NullPointerException | UnsupportedEncodingException | JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                VolleyRequest.get(getContext()).add(request);

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }
                        }
                        else if(newPassword.length() < 8) {
                            snackbar = Snackbar
                                    .make(getActivity().findViewById(R.id.container),
                                            "La password deve essere almeno di 8 caratteri", Snackbar.LENGTH_LONG);
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightRed));
                            snackbar.show();
                        }

                        else {
                            snackbar = Snackbar
                                    .make(getActivity().findViewById(R.id.container),
                                            "Password errata, riprova!", Snackbar.LENGTH_LONG);
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lightRed));
                            snackbar.show();

                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        return view;

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
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                mImageUri = System.currentTimeMillis() + ".jpg";

                File destination = new File(Environment.getExternalStorageDirectory(),
                        mImageUri);
                FileOutputStream fo;
                destination.createNewFile();
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();

                imgProfile.setImageBitmap(bm);
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

        mImageUri = System.currentTimeMillis() + ".jpg";

        File destination = new File(Environment.getExternalStorageDirectory(),
                mImageUri);
        FileOutputStream fo;
        try {

            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imgProfile.setImageBitmap(thumbnail);
        uploadImage(destination);
    }

    private void uploadImage(File toUploadFile) {

        String url = FourEventUri.Builder.create(FourEventUri.Keys.PLANNER)
                .appendPath("img").appendEncodedPath(planner.email).getUri();

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
                planner.updateImage(mImageUri);
                loading.dismiss();

                //refresh immagine


            }
        },toUploadFile,"filename");

        VolleyRequest.get(getContext()).add(mMultipartRequest);

    }

}

