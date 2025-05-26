
import 'dart:async';
import 'dart:convert';

// import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:sm_fitrus/fitrus_model.dart';

// import 'sm_fitrus_platform_interface.dart';

class SmFitrus {


  final methodChannel = const MethodChannel('sm_fitrus');

  final eventChannel = const EventChannel("sm_fitrus_status");

  // Future<String?> getPlatformVersion() {
  //   return SmFitrusPlatform.instance.getPlatformVersion();
  // }
  //
  // Future<void> getUserName() {
  //   return SmFitrusPlatform.instance.getUserName();
  // }

  Future<void> getPermissions()async {
    await methodChannel.invokeMethod('getPermissions');

    // return SmFitrusPlatform.instance.getPermissions();
  }


  Future<void> init() async{
    methodChannel.invokeMethod('init');

   // _readStatus();
  }
  Future<void> dispose() async{
    methodChannel.invokeMethod('dispose');

  }

  Future<void> startBFP({required double height,required double weight,required String gender,required String birth}) async{

    Map<String,String>  args={
      'height':height.toStringAsFixed(1),  /// must be double inside String
      'weight':weight.toStringAsFixed(1),/// must be double inside String
      'gender':gender,  /// must be 'M' or 'F'
      'birth':birth,  /// must be like yyyyMMdd format ex:199901203

    };
    if(checkBodyCompositionArgs(args)) {
      methodChannel.invokeMethod('startBFP', args);
    }

    else{
      print("check your arguments  Height,Weight,Birth,Gender");
      return;

    }

    // return SmFitrusPlatform.instance.startBFP();
  }


  bool checkBodyCompositionArgs(Map args){


    if( !( args['height'] !=null &&  ( double.tryParse( args['height']) ?? 0 ) >0 )){

      print("Invalid Height ");

      return false;

    }
    if( !( args['weight'] !=null &&  ( double.tryParse( args['weight']) ?? 0 ) >0 )){

      print("Invalid Weight ");

      return false;

    }
    if( !( args['gender'] !=null &&  ( ['M','F'].contains( args['gender']) )  )){

      print("Invalid Gender");

      return false;

    }

    /// birth must be in yyyyMMdd format  ex:   19901203
    if( !( args['birth'] !=null &&  (  args['birth'].toString().length==8 )  )){

      print("Invalid Birth ");

      return false;

    }

    return true;


  }



  // final StreamController _controller = StreamController<dynamic>();

  // Stream<dynamic> get statusStream => _controller.stream;

//   Future<dynamic>  _readStatus() async{
//
//
//     eventChannel
//         .receiveBroadcastStream().listen((result){
//
// // print("************************   data :$data");
// final Map<String, dynamic> data = jsonDecode(result.toString());
//
//
//
//      FitrusModel fitrusModel=FitrusModel(
//
//        hasData: data['hasData'] ?? false,
//        hasProgress: data['hasProgress'] ?? false,
//        isConnected: data['connectState'] !=null  && data['connectState'].toString().toLowerCase().contains("data")  || ['Connected','Service Discovered'].contains(data['connectState'].toString())  ,
//        connectionState: data['connectState'] ??"",
//        progress:data['progress'] !=null ? data['progress'].toString() :"",
//        bodyFat: BodyFat(
//          bmi:double.tryParse( data['bmi'].toString()) ??0.0 ,
//          bmr: double.tryParse(data['bmr'].toString()) ??0.0,
//          waterPercentage: double.tryParse(data['waterPercentage'].toString()) ??0.0,
//          fatMass: double.tryParse(data['fatMass'].toString()) ??0.0,
//          fatPercentage: double.tryParse(data['fatPercentage'].toString()) ??0.0,
//          muscleMass: double.tryParse(data['muscleMass'].toString()) ??0.0,
//          protein: double.tryParse(data['protein'].toString()) ??0.0,
//          calorie: double.tryParse(data['calorie'].toString()) ??0.0,
//          minerals: double.tryParse(data['minerals'].toString()) ??0.0,
//
//
//        )
//
//
//
//
//      );
//
//
//
//       if (!_controller.isClosed) {
//         _controller.sink.add(fitrusModel);
//       }
//
//     },
//
//       onError: (error) {
//         debugPrint(' ***********************   Error in event stream: $error');
//       },
//     );
//   }

  Stream<String> getEvents() {
    return eventChannel.receiveBroadcastStream().map((event) => event.toString());
  }
}
