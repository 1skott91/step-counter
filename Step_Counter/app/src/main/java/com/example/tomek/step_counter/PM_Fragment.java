package com.example.tomek.step_counter;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class PM_Fragment extends Fragment implements SensorEventListener {
    Chronometer chronometer;
    Button start, stop, resume;
    private long lastPause;
    TextView tv_stepCounter, distCount;
    SensorManager sensorManager;

    boolean isRunning = false;
    boolean isTiming = false;
    boolean isCheckbox = false;
    int counter = 0;
    int soundCheck = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View v = inflater.inflate(R.layout.pm_fragment, container, false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        chronometer = (Chronometer) v.findViewById(R.id.chronometer); // initiate a chronometer
        start = (Button) v.findViewById(R.id.startButton);
        stop = (Button) v.findViewById(R.id.stopButton);
        tv_stepCounter = (TextView) v.findViewById(R.id.tv_stepCounter);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        distCount = (TextView) v.findViewById(R.id.distCount);

        // start button
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                isTiming = true;

                counter = 0;
                soundCheck = 0;
                tv_stepCounter.setText(String.valueOf(0));
                distCount.setText(String.valueOf(0));

                PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                isCheckbox = sharedPreferences.getBoolean("applicationSound", true);


            }
        });

        // stop button
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                lastPause = SystemClock.elapsedRealtime();
                chronometer.stop();
                isTiming = false;

            }
        });

        return v;

    }

    @Override
    public void onResume()
    {
        super.onResume();
        isRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (countSensor != null)
        {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else
        {
            Toast.makeText(getActivity(), "Sensor not found!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        isRunning = false;
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.toolbar_menu, menu);
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings){
            Intent i = new Intent(getActivity(), Preferences.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.action_favorite)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.BLUE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {

        counter+=1;
        soundCheck+=1;

        if (isCheckbox) {
            //play sound every 5 steps
            soundOnStep();
        }

        if (isRunning && isTiming) {

            tv_stepCounter.setText(String.valueOf(counter));
            distanceTravelled();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void soundOnStep()
    {
        if (soundCheck == 5) {
            MediaPlayer mp = MediaPlayer.create(getActivity(), R.raw.click);
            mp.start();
            soundCheck = 0;
        }
    }

    public void distanceTravelled()
    {
        double averegeStep = 0.762;
        double formula = counter * averegeStep;
        distCount.setText(String.format( "%.2f", formula ));

    }
}
