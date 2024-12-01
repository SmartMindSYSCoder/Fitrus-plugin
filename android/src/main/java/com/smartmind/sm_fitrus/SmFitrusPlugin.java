package com.smartmind.sm_fitrus;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * SmFitrusPlugin
 */
public class SmFitrusPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private EventChannel eventChannel;
    public Context applicationContext;
    public Activity activity;

    public FitrusHandler fitrusHandler;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "sm_fitrus");
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "sm_fitrus_status");
        channel.setMethodCallHandler(this);

        this.applicationContext = flutterPluginBinding.getApplicationContext();


    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {


        if (call.method.equals("getPermissions")) {

            getPermissions();


        } else if (call.method.equals("init")) {

            init();


        } else if (call.method.equals("startBFP")) {

            final Map<String, String> arguments = call.arguments();
            String gender = (String) arguments.get("gender");
            String birth = (String) arguments.get("birth");
            Double height = Double.parseDouble((String) arguments.get("height"));
            Double weight = Double.parseDouble((String) arguments.get("weight"));


            fitrusHandler.startBFP(birth, height, weight, gender);


        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
        fitrusHandler.dispose();

    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        // TODO: your plugin is now attached to an Activity
//    this.activity = activityPluginBinding.getActivity();
        this.activity = activityPluginBinding.getActivity();
//    this.applicationContext = activityPluginBinding.getApplicationContext();
        fitrusHandler = new FitrusHandler(activity, applicationContext);


    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        // This call will be followed by onReattachedToActivityForConfigChanges().
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    }

    @Override
    public void onDetachedFromActivity() {

    }



    public void getPermissions() {

        fitrusHandler.checkPermissions();
//    FitrusHandler fitrusHandler=new FitrusHandler(activity,applicationContext);

//    android.widget.Toast.makeText(applicationContext, "Check Permission", Toast.LENGTH_SHORT).show();

    }

    public void init() {

//    FitrusHandler fitrusHandler=new FitrusHandler(activity,applicationContext);

        //  Log.i("fitrusHandler","**************************  "+fitrusHandler);
        eventChannel.setStreamHandler(fitrusHandler);
//    Log.i("SmFitrusPlugin", "EventChannel StreamHandler set");


        fitrusHandler.init();
//    android.widget.Toast.makeText(applicationContext, "Check Permission", Toast.LENGTH_SHORT).show();

    }


}
