package lam.project.foureventplannerdroid.utils.qr_code;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScannerActivity extends Activity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;
    private final String TAG = ScannerActivity.class.getName();

    @Override
    public void onCreate(Bundle state) {

        super.onCreate(state);

        //Inizializza programmaticamente lo scanner view
        mScannerView = new ZBarScannerView(this);

        //Setta lo scanner come content view
        setContentView(mScannerView);
    }

    //Avvia la camera al resume della Activity
    @Override
    public void onResume() {

        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    //Ferma la camera al pause della Activity
    @Override
    public void onPause() {

        super.onPause();
        mScannerView.stopCamera();
    }

    //Gestione del risultato
    @Override
    public void handleResult(Result rawResult) {

        Log.v(TAG, rawResult.getContents()); // Stampa i risultati dello scan
        Log.v(TAG, rawResult.getBarcodeFormat().getName()); // Stampa il format dello scan

        //Se si desidera fare il resume dello scanning (riavviarlo dopo aver catturato il risultato)
        mScannerView.resumeCameraPreview(this);
    }
}