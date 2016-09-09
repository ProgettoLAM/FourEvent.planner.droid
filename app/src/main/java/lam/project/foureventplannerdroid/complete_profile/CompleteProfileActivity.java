package lam.project.foureventplannerdroid.complete_profile;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.github.fcannizzaro.materialstepper.style.DotStepper;

public class CompleteProfileActivity extends DotStepper {

    private int i = 1;
    private Activity mActivity;

    public static final int CAMERA_PERMISSION_REQUEST_ID = 1;
    public static final int EXTERNAL_DATA_PERMISSION_REQUEST_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTitle("Completa il tuo profilo");

        addStep(createFragment(new Step1Info()));
        addStep(createFragment(new Step2Credits()));

        super.onCreate(savedInstanceState);

        cameraPermission();
        externalDataPermission();
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

    private void externalDataPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(this)
                        .setTitle("Consentire a FourEvent di acquisire foto e registrare video?")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(mActivity,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        EXTERNAL_DATA_PERMISSION_REQUEST_ID);
                            }
                        })
                        .create()
                        .show();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        EXTERNAL_DATA_PERMISSION_REQUEST_ID);

            }
        }
    }

    private void cameraPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                new AlertDialog.Builder(this)
                        .setTitle("Consentire a FourEvent di acquisire foto e registrare video?")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(mActivity,
                                        new String[]{Manifest.permission.CAMERA},
                                        CAMERA_PERMISSION_REQUEST_ID);
                            }
                        })
                        .create()
                        .show();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_ID);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == CAMERA_PERMISSION_REQUEST_ID) {

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                cameraPermission();

            } else {

                new AlertDialog.Builder(this)
                        .setTitle("Errori di permessi")
                        .setMessage("E' necessario abilitare il permesso per utilizzare della camera per salvare la foto")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                finish();
                            }
                        })
                        .create()
                        .show();
            }

        } else if( requestCode == EXTERNAL_DATA_PERMISSION_REQUEST_ID) {

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                externalDataPermission();

            } else {

                new AlertDialog.Builder(this)
                        .setTitle("Errori di permessi")
                        .setMessage("E' necessario abilitare il permesso per utilizzare della camera per salvare la foto")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                finish();
                            }
                        })
                        .create()
                        .show();
            }
        }
    }

}
