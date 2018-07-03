package com.example.datacollector;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.instacart.library.truetime.TrueTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {


    File path;
    File accelFile;
    File gyroFile;
    File magnetFile;
    File pressureFile;
    File accelLinFile;
    File gyroLinFile;
    File magnetLinFile;
    File gravityLinFile;

    private Sensor accel;
    private Sensor gyro;
    private Sensor magnet;
    private Sensor pressure;
    private Sensor accelLin;
    private Sensor gyroscpe;
    private Sensor magneticfield;
    private Sensor gravity;


    private FileOutputStream streamAccel;
    private FileOutputStream streamGyro;
    private FileOutputStream streamMagnet;
    private FileOutputStream streamPressure;
    private SimpleDateFormat timeFormat;


    private Date oldDatePress = new Date(0);
    public SensorEventListener pressureListener = new SensorEventListener() {
        boolean firstrun = true;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                String[] s;
                if (event.values.length > 1){
                    s =new String[] {"V1","V2","V3"};
                } else{
                    s =new String[] {"V1"};
                }
                writeHeader(streamPressure,s);
                firstrun = false;
            }

            writeToFile(streamPressure,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Date oldDateMagnet= new Date(0);
    public SensorEventListener magnetListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamMagnet,new String[]{"X","Y","Z","E_X","E_Y","E_Z"});
                firstrun = false;
            }
            writeToFile(streamMagnet,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private FileOutputStream streamAccelLin;
    private FileOutputStream streamGyroCalib;
    private FileOutputStream streamMagnetCalib;
    private FileOutputStream streamGravity;
    private FileOutputStream streamAccelCalib;
    private Date oldDate = new Date(0L);

    private SensorEventListener testListener  = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            //Log.d(TAG, "onSensorChanged: time: " +(event.timestamp - old));
            //oldDate = temp;
            //old = event.timestamp;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener accelCalibListener = new SensorEventListener() {
        public boolean firstrun = true;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamAccelCalib,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            writeToFile(streamAccelCalib,printEvent(event));
        }
        public void resetListener(){
            firstrun = true;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Timer t;
    private TextView textView;
    private PowerManager.WakeLock wl;
    private Sensor accelC;
    private File accelCFile;


    private String printEvent(SensorEvent event) {
        StringBuilder sb = new StringBuilder();
        for (float val : event.values){
            sb.append(val);
            sb.append(" ");
        }
        sb.append(timeFormat.format(TrueTime.now()).replace("/","T"));
        sb.append("\r\n");
        return sb.toString();
    }

    private SensorEventListener magnetCalibListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamMagnetCalib,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            writeToFile(streamMagnetCalib,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener gyroCalibListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamGyroCalib,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            writeToFile(streamGyroCalib,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Date oldDateAccel = new Date(0);
    private SensorEventListener accelLinListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamAccelLin,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            writeToFile(streamAccelLin,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener gravityListener = new SensorEventListener() {
        public boolean firstrun = true;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamGravity,new String[]{"X","Y","Z"});
                firstrun = false;
            }
            writeToFile(streamGravity,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Date oldDateGyro= new Date(0);

    public SensorEventListener gyroListener = new SensorEventListener() {
        private boolean firstrun = true;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamGyro,new String[]{"X","Y","Z","E_X","E_Y","E_Z"});
                firstrun = false;
            }
            writeToFile(streamGyro,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Date oldDateAccelUn= new Date(0);
    public SensorEventListener accelListener = new SensorEventListener() {
        private boolean firstrun = true;
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event==null){
                firstrun = true;
                return;
            }
            if (firstrun){
                writeHeader(streamAccel,new String[]{"X","Y","Z","E_X","E_Y","E_Z"});
                firstrun = false;
            }
            writeToFile(streamAccel,printEvent(event));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private Context ctx;


    private String TAG = "DataCollector";
    private ToggleButton button;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> arrayAdapter;
    private CharSequence type = "Car";

    private SensorManager sensorManager;
    private int requestCode = 1234;



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode){

        }
    }

    long old = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings,menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                    if (TrueTime.isInitialized()){
                        return;
                    }
                    syncTime(0);
                    //TrueTime.build().initialize();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            button.setEnabled(true);
                        }
                    });

                    Log.d(TAG, "run: initialized");

            }
        });

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"CollectingSensorData");

        timeFormat = new SimpleDateFormat("YYYY-MM-dd/kk:mm:ss.SSS");
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.toggleButton);
        button.setEnabled(false);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    // start recording
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            startRecording();
                        }
                    });

                } else {
                    //stop recording
                    stopRecording();
                }
            }
        });
        spinner = findViewById(R.id.selectionItem);
        arrayAdapter = ArrayAdapter.createFromResource(this,R.array.spinner_array,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = arrayAdapter.getItem(position);
                Log.d(TAG, "onItemSelected: is now "+type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(arrayAdapter.getPosition("Car"));

        textView = findViewById(R.id.textView);

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WAKE_LOCK},requestCode);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //List<Sensor> sens = sensorManager.getSensorList(Sensor.TYPE_ALL);

        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);

        accelC = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        if (gyro == null){
            gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if (magnet == null ){
            magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        }

        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        accelLin = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroscpe = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magneticfield = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        //sensorManager.registerListener(testListener,accel,2500);
        //createFile("sensorTest");
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ctx,msg,Toast.LENGTH_LONG);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void stopRecording() {
        unrigisterAllSensors();
        closeAllStreams();
        addFile(accelCFile);
        addFile(accelFile);
        addFile(gyroFile);
        addFile(magnetFile);
        addFile(pressureFile);
        addFile(accelLinFile);
        addFile(gravityLinFile);
        addFile(magnetLinFile);
        addFile(gyroLinFile);
        t.cancel();
        t.purge();
        wl.release();


    }

    private void closeAllStreams() {
        try {
            streamAccel.close();
            streamGyro.close();
            streamMagnet.close();
            streamPressure.close();
            streamAccelLin.close();
            streamMagnetCalib.close();
            streamGravity.close();
            streamGyroCalib.close();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (NullPointerException e){
            Log.d(TAG, "closeAllStreams: No Files written because streams are null");
        }
    }

    private void startRecording() {

        wl.acquire();

        syncTime(0);

        //preparation
        path = new File(Environment.getExternalStorageDirectory().getPath(),"SensorData");
        path.setReadable(true);
        path.setWritable(true);
        path.mkdirs();
        if (accelC != null){
            accelCFile = createFile("acc");
        }
        accelFile = createFile("acc_un");
        gyroFile = createFile("gyro_un");
        magnetFile = createFile("mag_un");
        pressureFile = createFile("pres");
        accelLinFile= createFile("acc_lin");
        gyroLinFile = createFile("gyro");
        magnetLinFile= createFile("mag");
        gravityLinFile = createFile("grav");

        try {
            if (accelC != null){
                streamAccelCalib = new FileOutputStream(accelCFile);
            }
            streamAccel = new FileOutputStream(accelFile);
            //writeHeader(streamAccel,accel);
            streamGyro = new FileOutputStream(gyroFile);
            //writeHeader(streamGyro,gyro);
            streamMagnet = new FileOutputStream(magnetFile);
            //writeHeader(streamMagnet,magnet);
            streamPressure = new FileOutputStream(pressureFile);
            streamAccelLin = new FileOutputStream(accelLinFile);
            streamGyroCalib =new FileOutputStream(gyroLinFile);
            streamMagnetCalib = new FileOutputStream(magnetLinFile);
            streamGravity = new FileOutputStream(gravityLinFile);

            //writeHeader(streamPressure,pressure);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setChecked(false);
                    textView.setText("Couldnt create FileStreams to write data");
                }
            });
        }

        startCountDown();
    }

    Calendar calendar;
    SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
    private void startCountDown() {
        t = new Timer();
        Date date = new Date(0L);
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR,-1);

        //TODO: make countdown for recording data; Not really nessesary because we have almost no deleay in getting the actual time
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                calendar.add(Calendar.SECOND,1);
                calendar.getTime();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(timerFormat.format(calendar.getTime()));
                    }
                });

            }
        },0,1000);
        registerAllSensors();
    }

    private File createFile(String fileName) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");

        File file = new File(path,type+"_"+fileName+"_"+format.format(TrueTime.now())+".txt");
//        Log.d(TAG, "createFile: File created" + file.getName());
        try {
            if (file.exists()){
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    static int attempt = 0;
    private void syncTime(int i) {
        try {
            switch (i) {
                case 0 : TrueTime.build().withNtpHost("0.de.pool.ntp.org").initialize(); break;
                case 1 : TrueTime.build().withNtpHost("1.de.pool.ntp.org").initialize(); break;
                case 2 : TrueTime.build().withNtpHost("2.de.pool.ntp.org").initialize(); break;
                case 3 : TrueTime.build().withNtpHost("3.de.pool.ntp.org").initialize(); break;
                default:  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Time did not sync, Restart app");
                    }
                });
            }

            //TrueTime.build().initialize();
        } catch (IOException e) {
            e.printStackTrace();
            syncTime(attempt++);
        }

    }

    private void trySecond() {
        try {
            TrueTime.build().withNtpHost("1.de.pool.ntp.org").initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(OutputStream stream, String text){
        try {
            stream.write(text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHeader(OutputStream stream, String[] headers){
        StringBuilder sb = new StringBuilder();
        for (String s: headers){
            sb.append(s);
            sb.append(" ");
        }
        sb.append("TS \r\n");
        writeToFile(stream,sb.toString());
    }

    private void unrigisterAllSensors(){
        if (accelC != null){
            sensorManager.unregisterListener(accelCalibListener);
            accelCalibListener.onSensorChanged(null);

        }
        if (accelC != null){
            sensorManager.unregisterListener(accelCalibListener);
            accelCalibListener.onSensorChanged(null);
        }
        sensorManager.unregisterListener(accelListener);
        accelListener.onSensorChanged(null);
        sensorManager.unregisterListener(gyroListener);
        gyroListener.onSensorChanged(null);
        sensorManager.unregisterListener(magnetListener);
        magnetListener.onSensorChanged(null);
        sensorManager.unregisterListener(pressureListener);
        pressureListener.onSensorChanged(null);
        sensorManager.unregisterListener(accelLinListener);
        accelLinListener.onSensorChanged(null);
        sensorManager.unregisterListener(gyroCalibListener);
        gyroCalibListener.onSensorChanged(null);
        sensorManager.unregisterListener(gravityListener);
        gravityListener.onSensorChanged(null);
        sensorManager.unregisterListener(magnetCalibListener);
        magnetCalibListener.onSensorChanged(null);
    }

    private void registerAllSensors(){

        if (accelC != null){
            sensorManager.registerListener(accelCalibListener,accelC,SensorManager.SENSOR_DELAY_FASTEST);
        }
        sensorManager.registerListener(accelListener,accel,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gyroListener,gyro,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magnetListener,magnet,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(pressureListener,pressure,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(accelLinListener,accelLin,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gyroCalibListener,gyroscpe,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magnetCalibListener,magneticfield,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gravityListener,gravity,SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void addFile(File file){
        MediaScannerConnection.scanFile(this,new String[]{file.getPath()},null,null);

    }

    public void deleteData(MenuItem item) {
        File[] files;
        path = new File(Environment.getExternalStorageDirectory().getPath(),"SensorData");
        files = path.listFiles();
        File file;
        if (files != null){
            for (int i = 0 ; i < files.length; i++){

                file = files[i].getAbsoluteFile();
                file.delete();
                addFile(file);

            }
        }

    }
}
