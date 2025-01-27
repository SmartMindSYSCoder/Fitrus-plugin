import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:sm_fitrus/fitrus_model.dart';
import 'package:sm_fitrus/sm_fitrus.dart';

void main() {

  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final smFitrus = SmFitrus();

  FitrusModel fitrusModel=FitrusModel(
    bodyFat: BodyFat()
  );
  String state="";
  @override
  void initState() {
    super.initState();

smFitrus.init();
  }


  // Platform messages are asynchronous, so we initialize in an async method.

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin SmartMind Fitrus'),
        ),
        body: Center(
          child: Column(
            children: [



              SizedBox(height: 30,),

              TextButton(onPressed: (){

                smFitrus.getPermissions();
              }, child: Text("Get Permissions")),
              SizedBox(height: 30,),



              StreamBuilder(stream: smFitrus.getEvents(), builder: (bc,event){



                return Column(children: [


                  Text(event.data.toString())

                ],);

              }),

              TextButton(onPressed: ()async{

                smFitrus.init();
                // smFitrus.statusStream.listen((data){
                //
                //
                //
                //   if(data is FitrusModel){
                //   fitrusModel=data;
                //
                //   }
                //   setState(() {
                //   });
                //
                //
                // });




              }, child: Text("Init")),




              TextButton(onPressed: ()async{

                // 'gender' must be 'M' or 'F'
               // 'birth'  must be like yyyyMMdd format ex:199901203
              await  smFitrus.startBFP(height: 165,weight: 55.5,gender: "M",birth:'19901203' );



              }, child: Text("startBFP")),


               Text("Connection State:\t${fitrusModel.connectionState}""\nisConnected : \t${fitrusModel.isConnected}"""),
               Text("Progress Value:\t${fitrusModel.progress}"),
               Text("Data:"
                   ""
                   ""
                   "\nBMI:${fitrusModel.bodyFat!.bmi}"
                   "\nBMR:${fitrusModel.bodyFat!.bmr}"


               ),

            ],
          ),
        ),
      ),
    );
  }
}
