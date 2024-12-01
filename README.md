# sm_fitrus

A new Flutter project.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/to/develop-plugins),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

to add this plugin to your project you can add below tow lines in your project pubspec

    sm_fitrus:
      git: https://github.com/SmartMindSYSCoder/Fitrus-plugin

Now you can create instance of plugin :

     final smFitrus = SmFitrus();


To start use this plugin must be call getPermissions() like below:
         

    smFitrus.getPermissions();


Then call init  method like below:


                  smFitrus.init();
                  smFitrus.statusStream.listen((data){

                  if(data is FitrusModel){
                  fitrusModel=data;

                  }
                  setState(() {
                  });


                });

   when status is service discovered  you can call startBFP  like below:

// 'gender' must be 'M' or 'F'
// 'birth'  must be like yyyyMMdd format ex:199901203


      await  smFitrus.startBFP(height: 165,weight: 55.5,gender: "M",birth:'19901203' );


Note that :  the stream listen to check the update of state of connection and retrieve data


You can also see the example

I hope this clear 