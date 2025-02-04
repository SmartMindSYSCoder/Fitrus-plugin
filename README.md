# sm_fitrus

Fitrus Plugin.

## Getting Started
This plugin enable you connect and read data from Fitrus device

To add this plugin to your project you can add below tow lines in your project pubspec

    sm_fitrus:
      git: https://github.com/SmartMindSYSCoder/Fitrus-plugin.git

To import it:
    
    import 'package:sm_fitrus/sm_fitrus.dart';

Now you can create instance of plugin :

     final smFitrus = SmFitrus();


To start use this plugin must be call getPermissions() like below:
         

    smFitrus.getPermissions();


Then call init  method like below:


                  smFitrus.init();

Now you can start listen to stream event  like:

                     StreamBuilder(stream: smFitrus.getEvents(), builder: (bc,event){



                return Column(children: [


                  Text(event.data.toString())

                ],);

              }),


or 


        smFitrus.getEvents().listen((event){



       print("********************    event    $event");




      var data=jsonDecode(event);

     });




      

   when status is service discovered  you can call startBFP  like below:

// 'gender' must be 'M' or 'F'
// 'birth'  must be like yyyyMMdd format ex:199901203


      await  smFitrus.startBFP(height: 165,weight: 55.5,gender: "M",birth:'19901203' );


Note that :  the stream listen to check the update of state of connection and retrieve data


You can also see the example

I hope this clear 