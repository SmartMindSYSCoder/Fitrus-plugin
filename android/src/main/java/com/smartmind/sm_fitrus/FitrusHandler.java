package com.smartmind.sm_fitrus;
import android.Manifest;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.Toast;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.ArrayList;
import android.content.Context;
import android.app.Activity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.smartmind.sm_fitrus.Interfaces.DeviceServiceBinder;
import com.smartmind.sm_fitrus.Interfaces.FitrusServiceInterface;
import com.smartmind.sm_fitrus.Results.FitrusLtResultData;
import io.flutter.plugin.common.EventChannel;
import org.json.JSONObject;


public class FitrusHandler implements EventChannel.StreamHandler{

  private final   Activity activity;
  private final   Context applicationContext;
    private EventChannel.EventSink events;

    private String connectState = "Disconnected";
    private int mCurrTlMsurModeIndex = 0;


    private String gender ="M",birth="199901203";
    private double height=160.5 ,weight=60.5;


    FitrusHandler( Activity activity, Context applicationContext){

        this.activity=activity;
        this.applicationContext =applicationContext;
    };





    @Override
    public void onListen(Object args, EventChannel.EventSink events) {


        this.events = events;

      //  Log.i("onListen","this is from onListen  ******************************"+events);

        if (events != null) {

            try {
                JSONObject inputObject = new JSONObject();

                inputObject.put("connectState", connectState);
                events.success(inputObject.toString());

            } catch (Exception var5) {

                Log.i("erorr",var5.getMessage());
            }

        }



    }

    @Override
    public void onCancel(Object args) {
    }


    public void checkPermissions() {

       final int PERMISSIONS_REQUEST_CODE = 1;
        if(Build.VERSION.SDK_INT < 31){
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        PERMISSIONS_REQUEST_CODE);
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        PERMISSIONS_REQUEST_CODE);
            }
        }


    }
    public boolean isPermissionsGranted() {

       final int PERMISSIONS_REQUEST_CODE = 1;
        if(Build.VERSION.SDK_INT < 31){
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return  false;
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
              return  false;
            }
        }
return  true;

    }


    public void init(){


//        checkPermissions();

        if(isPermissionsGranted()) {

            Log.d("init","init ****************************");

            if(mFitLtServiceInterface ==null) {
                activity.bindService(new Intent(activity, DeviceService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

            }
            else{
                LocalBroadcastManager.getInstance(activity)
                        .registerReceiver(mGattUpdateReceiver, mFitLtServiceInterface.getGattUpdateIntentFilter());
                mFitLtServiceInterface.startFitrusScan(ScanSettings.SCAN_MODE_LOW_LATENCY, 10000);
            }


        }
        else{
                Toast.makeText(applicationContext, " Permission not granted\nPlease check permission first", Toast.LENGTH_SHORT).show();

        }





    }



    private FitrusServiceInterface mFitLtServiceInterface = null;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

       // Log.d("ServiceConnection","ServiceConnection *********************");

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mFitLtServiceInterface = (FitrusServiceInterface) ((DeviceServiceBinder) iBinder).getService();
            LocalBroadcastManager.getInstance(activity)
                    .registerReceiver(mGattUpdateReceiver, mFitLtServiceInterface.getGattUpdateIntentFilter());

            mFitLtServiceInterface.startFitrusScan(ScanSettings.SCAN_MODE_LOW_LATENCY, 10000);

            Log.d("connection","*****************   service connected ");
//            mBtnDisc.setEnabled(true);
//            mBtnConn.setEnabled(false);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mFitLtServiceInterface = null;
//            mBtnDisc.setEnabled(false);
//            mBtnConn.setEnabled(true);

        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                JSONObject inputObject = new JSONObject();



//                Log.i("action","******************   "+action);
//                Log.i("events","******************   "+events);


                switch (action) {
                    case FitrusServiceInterface.ACTION_SCAN_SEARCHED:
                        connectState = "Searched";
//                        events.success(connectState);
                        if (events != null) {

                            try {

                                inputObject.put("connectState", connectState);
                                events.success(inputObject.toString());

                            } catch (Exception var5) {

                                Log.i("erorr",var5.getMessage());
                            }

                        }
//                        mTvConnStat.setText(connectState);
//                        mBtnDisc.setEnabled(false);
//                        mBtnConn.setEnabled(true);
                        break;

                    case FitrusServiceInterface.ACTION_SCAN_FAILED:
                        connectState = "Scan Failed";
//                        mTvConnStat.setText(connectState);
//                        mBtnDisc.setEnabled(false);
//                        mBtnConn.setEnabled(true);
                            if (events != null) {

                                try {

                                    inputObject.put("connectState", connectState);
                                    events.success(inputObject.toString());

                                } catch (Exception var5) {

                                }

                            }

                        break;

                    case FitrusServiceInterface.ACTION_GATT_CONNECTED:
                        connectState = "Connected";
//                        mTvConnStat.setText(connectState);
//                        mBtnConn.setEnabled(false);
//                        mBtnDisc.setEnabled(true);




                      //  mTvConnDevice.setText(mFitLtServiceInterface.getFitrusName());
                            if (events != null) {

                                try {

                                    inputObject.put("connectState", connectState);
                                    events.success(inputObject.toString());

                                } catch (Exception var5) {
                                    Log.i("erorr",var5.getMessage());

                                }

                            }


                        break;

                    case FitrusServiceInterface.ACTION_GATT_DISCONNECTED:
                        connectState = "Disconnected";
//                        mTvConnStat.setText(connectState);
//                        mTvConnDevice.setText("--");
//                        mBtnStartMsur.setEnabled(false);
//                        mBtnDisc.setEnabled(false);
//                        mBtnConn.setEnabled(true);
                            if (events != null) {

                                try {

                                    inputObject.put("connectState", connectState);
                                    events.success(inputObject.toString());

                                } catch (Exception var5) {
                                    Log.i("erorr",var5.getMessage());

                                }

                            }

                        break;

                    case FitrusServiceInterface.ACTION_GATT_SERVICES_DISCOVERED:
                        connectState = "Service Discovered";
//                        mTvConnStat.setText(connectState);


//                        mBtnStartMsur.setEnabled(true);
                            if (events != null) {

                                try {

                                    inputObject.put("connectState", connectState);
                                    events.success(inputObject.toString());

                                } catch (Exception var5) {
                                    Log.i("erorr",var5.getMessage());

                                }

                            }

                        break;

                    case FitrusServiceInterface.ACTION_DATA_AVAILABLE:
                        handleData(intent);
                        break;
                }
            }
        }
    };


    private void handleData(final Intent intent) {
        String type = intent.getStringExtra(FitrusServiceInterface.EXTRA_TYPE);

        JSONObject inputObject = new JSONObject();

//Log.d("type","type **********************  :"+type);
        switch (type) {
            case FitrusServiceInterface.EXTRA_TYPE_MEASURE_PROGRESS:
                FitrusLtResultData.Progress progress = (FitrusLtResultData.Progress) intent.getSerializableExtra(FitrusServiceInterface.EXTRA_DATA);
              //  mTvResultLog.setText(progress.toString());
             //   Log.i("progress",progress.toString());

//                events.success(progress.toString());
                if (events != null) {

                    try {

                        inputObject.put("connectState", connectState);
                        inputObject.put("hasProgress", true);

                        inputObject.put("progress", progress.progressValue);
                        events.success(inputObject.toString());

                    } catch (Exception var5) {
                        Log.i("erorr",var5.getMessage());

                    }

                }

                break;

            case FitrusServiceInterface.EXTRA_TYPE_BFP:
                FitrusLtResultData.Body bfpResult = (FitrusLtResultData.Body) intent.getSerializableExtra(FitrusServiceInterface.EXTRA_DATA);

             //   Log.i("data",bfpResult.toString());
                if (events != null) {

                    try {

                        inputObject.put("connectState", connectState);
                        inputObject.put("data", bfpResult.toString());
                        inputObject.put("hasData", true);
                        inputObject.put("bmi", bfpResult.bmi);
                        inputObject.put("bmr", bfpResult.bmr);
                        inputObject.put("waterPercentage", bfpResult.waterPercentage);
                        inputObject.put("fatMass", bfpResult.fatMass);
                        inputObject.put("fatPercentage", bfpResult.fatPercentage);
                        inputObject.put("muscleMass", bfpResult.muscleMass);
                        inputObject.put("protein", bfpResult.protein);
                        inputObject.put("calorie", bfpResult.calorie);
                        inputObject.put("minerals", bfpResult.minerals);
                        events.success(inputObject.toString());

                    } catch (Exception var5) {

                    }

                }

                //   mTvResultLog.setText(bfpResult.toString());

                break;

            case FitrusServiceInterface.EXTRA_TYPE_HRV:
                FitrusLtResultData.HRV hrvResult = (FitrusLtResultData.HRV) intent.getSerializableExtra(FitrusServiceInterface.EXTRA_DATA);
               // mTvResultLog.setText(hrvResult.toString());
                break;

            case FitrusServiceInterface.EXTRA_TYPE_BATTERY:
                int battResult = intent.getIntExtra(FitrusServiceInterface.EXTRA_DATA, 999);
             //   mTvResultLog.setText(battResult == 999 ? "Read Error" : "Battery : " + battResult);
                break;

            case FitrusServiceInterface.EXTRA_TYPE_STRESS:
                FitrusLtResultData.Stress stressResult = (FitrusLtResultData.Stress) intent.getSerializableExtra(FitrusServiceInterface.EXTRA_DATA);
               // mTvResultLog.setText(stressResult.toString());
                break;

            case FitrusServiceInterface.EXTRA_TYPE_SKIN_TEMP:
            case FitrusServiceInterface.EXTRA_TYPE_OBJECT_TEMP:
                FitrusLtResultData.Temperature tempResult = (FitrusLtResultData.Temperature) intent.getSerializableExtra(FitrusServiceInterface.EXTRA_DATA);
              //  mTvResultLog.setText(tempResult.toString());
                break;

            case FitrusServiceInterface.EXTRA_TYPE_BP:
                FitrusLtResultData.BP BpResult = (FitrusLtResultData.BP) intent.getSerializableExtra(FitrusServiceInterface.EXTRA_DATA);
               // mTvResultLog.setText(BpResult.toString());
                break;

            case FitrusServiceInterface.EXTRA_TYPE_ERROR:
                String errorMsg = intent.getStringExtra(FitrusServiceInterface.EXTRA_DATA);
             //   mTvResultLog.setText(errorMsg);
                break;
        }
    }


  public void   startBFP(String birth, double height, double weight, String gender){
      int code;

      code = mFitLtServiceInterface.startBFP(
              birth,
              height,
              weight,
              gender,
              "P",

              "3"
      );


  }

  public void dispose(){


      if (mFitLtServiceInterface != null)
          mFitLtServiceInterface.closeFitrus();

    activity.  unbindService(mServiceConnection);
      LocalBroadcastManager.getInstance(activity).unregisterReceiver(mGattUpdateReceiver);


  }

}
