
//Code by Adrian Altermatter & Noah Waldner
package com.example.user.schrittzaehler;

import android.content.pm.PackageManager;

//Code by Adrian Altermatter & Noah Waldner
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int numActions, startStation, endStation;
    private Button scan, button, button2;
    private String message;
    private TextToSpeech myTTS;
    private ArrayList<String> list;
    private JSONObject obj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scan = (Button) findViewById(R.id.scan);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            public void onInit(int status) {

                if (status != TextToSpeech.ERROR) {
                    myTTS.setLanguage(Locale.GERMAN);
                }
            }

        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        scan.setOnClickListener(this);
        button.setOnClickListener(this);
        button2.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                takeQrCodePicture();
                button.setEnabled(true);
                scan.setEnabled(false);
                break;

            case R.id.button:
                if(list==null){
                    try {
                        obj = new JSONObject(this.message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    list = StringtoArray(obj);
                    numActions = 0;

                }
                if (numActions < list.size()) {
                    String s = list.get(numActions);
                    numActions++;
                    if (s.matches("[-+]?\\d*\\.?\\d+")) {
                        speakWords(s + "Schritte");
                        Intent intent = new Intent(this, CountSteps.class);
                        intent.putExtra("stepsToGo", Integer.valueOf(s));
                        startActivity(intent);
                    } else {
                        speakWords("Drehen Sie sich nach: " + s);
                        Intent turnActivity = new Intent(this, TurnActivity.class);
                        turnActivity.putExtra("rol", s);
                        startActivity(turnActivity);
                    }
                }else{
                    list = null;
                    speakWords("Scannen Sie den QR-Code am Ziel");
                    takeQrCodePicture();
                    button.setEnabled(false);
                    scan.setEnabled(false);
                    button2.setEnabled(true);
                }
                break;
            case R.id.button2:
                try {
                    endStation = new JSONObject(this.message).getInt("endStation");
                    JSONObject log = new JSONObject();
                    log.put("task", "Schrittzaehler");
                    log.put("startStation", startStation);
                    log.put("endStation", endStation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                log("{\"task\": \"Schrittzaehler\"," +
                        "\"startStation\": " + startStation + "," +
                        "\"endStation\": " + endStation + "}");

                break;
        }

    }


    private void speakWords(String speech) {
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    public ArrayList StringtoArray(JSONObject obj) {
        list = new ArrayList<>();
        JSONArray jsonArray;
        try {
            jsonArray = obj.getJSONArray("input");
            startStation = obj.getInt("startStation");
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i = 0; i < len; i++) {
                    list.add(jsonArray.get(i).toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }


    public void takeQrCodePicture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(false);
        integrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE
                && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();

            // Ein Bitmap zur Darstellung erhalten wir so:
            // Bitmap bmp = BitmapFactory.decodeFile(path)

            message = extras.getString(
                    Intents.Scan.RESULT);
        }

    }

    private void log(String solution) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        intent.putExtra("ch.appquest.logmessage", solution);
        startActivity(intent);
    }


}


//Code by Adrian Altermatter & Noah Waldner