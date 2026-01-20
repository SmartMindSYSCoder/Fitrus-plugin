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
    public static FitrusHandler fitrusHandler;
    private static EventChannel.EventSink eventSink;
    public Context applicationContext;
    public Activity activity;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "sm_fitrus");
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "sm_fitrus_status");
        channel.setMethodCallHandler(this);
        
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                android.util.Log.e("SmFitrusPlugin", "StreamHandler onListen. Capture Sink: " + events);
                eventSink = events;
                if (fitrusHandler != null) {
                    fitrusHandler.onListen(arguments, events);
                }
            }

            @Override
            public void onCancel(Object arguments) {
                android.util.Log.e("SmFitrusPlugin", "StreamHandler onCancel. Release Sink.");
                eventSink = null;
                if (fitrusHandler != null) {
                    fitrusHandler.onCancel(arguments);
                }
            }
        });

        this.applicationContext = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        eventChannel.setStreamHandler(null);
    }
    
    // ... [rest of methods]

    public void init(String apiUrl, String apiKey, MethodChannel.Result result) {
        android.util.Log.e("SmFitrusPlugin", "init() method called from Dart. apiUrl=" + apiUrl);
        if (fitrusHandler != null) {
            android.util.Log.e("SmFitrusPlugin", "fitrusHandler is valid. Calling handler.init().");
            fitrusHandler.init(apiUrl, apiKey, result);
        } else {
            android.util.Log.e("SmFitrusPlugin", "CRITICAL: fitrusHandler is NULL in init()!");
            if (result != null) result.error("HANDLER_NULL", "FitrusHandler is null", null);
        }
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPermissions")) {
            getPermissions();
        } else if (call.method.equals("init")) {
            String apiUrl = null;
            String apiKey = null;
            if (call.arguments() != null) {
                Map<String, String> args = call.arguments();
                apiUrl = args.get("apiUrl");
                apiKey = args.get("apiKey");
            }
            init(apiUrl, apiKey, result);
        } else if (call.method.equals("dispose")) {
            dispose();
        } else if (call.method.equals("startBFP")) {
            final Map<String, String> arguments = call.arguments();
            String gender = (String) arguments.get("gender");
            String birth = (String) arguments.get("birth");
            Double height = Double.parseDouble((String) arguments.get("height"));
            Double weight = Double.parseDouble((String) arguments.get("weight"));

            if (fitrusHandler != null) {
                fitrusHandler.startBFP(birth, height, weight, gender);
            }
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        android.util.Log.e("SmFitrusPlugin", "onAttachedToActivity. Instance: " + System.identityHashCode(this));
        this.activity = activityPluginBinding.getActivity();
        if (fitrusHandler == null) {
            fitrusHandler = new FitrusHandler(activity, applicationContext);
        } else {
            fitrusHandler.setActivity(activity);
        }
        
        if (eventSink != null) {
            android.util.Log.e("SmFitrusPlugin", "onAttachedToActivity: Injecting existing eventSink: " + eventSink);
            fitrusHandler.onListen(null, eventSink);
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        android.util.Log.e("SmFitrusPlugin", "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        android.util.Log.e("SmFitrusPlugin", "onReattachedToActivityForConfigChanges");
        onAttachedToActivity(activityPluginBinding);
    }

    @Override
    public void onDetachedFromActivity() {
        android.util.Log.e("SmFitrusPlugin", "onDetachedFromActivity");
        this.activity = null;
        if (fitrusHandler != null) {
            fitrusHandler.setActivity(null);
        }
    }

    public void getPermissions() {
        if (fitrusHandler != null) {
            fitrusHandler.checkPermissions();
        }
    }

    public void dispose() {
        if (fitrusHandler != null) {
             fitrusHandler.dispose();
        }
    }
}
