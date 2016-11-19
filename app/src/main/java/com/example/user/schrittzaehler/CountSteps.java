package com.example.user.schrittzaehler;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class CountSteps extends AppCompatActivity implements StepListener{

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView textView;
    private int stepsToGo, steps;
    private StepCounter stepCounter;
    MainActivity main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_steps);
        stepCounter = new StepCounter(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = (Sensor) sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        textView = (TextView) findViewById(R.id.textView);
        Bundle extras = getIntent().getExtras();
        stepsToGo = extras.getInt("stepsToGo");
        textView.setText("Schritt: " + 0 + " von " + stepsToGo);
        steps = 0;
        main = new MainActivity();
    }

    @Override
    public void onStep() {
        steps++;
        textView.setText("Schritt: " + steps + " von " + stepsToGo);
        if(stepsToGo == steps){
            Intent result = new Intent();
            result.putExtra("finished", true);
            setResult(1, result);

            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stepCounter != null) {
            sensorManager.unregisterListener(stepCounter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounter != null) {
            sensorManager.registerListener(stepCounter, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
}
