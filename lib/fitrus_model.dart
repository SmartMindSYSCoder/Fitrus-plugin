class FitrusModel{

 final bool isConnected,hasData,hasProgress;
 final String connectionState, progress;

final BodyFat? bodyFat;

 FitrusModel({this.isConnected=false,this.hasData=false,this.hasProgress=false,this.connectionState='',this.progress='',this.bodyFat});


}

class BodyFat{

 final double bmi,bmr,waterPercentage,fatMass,fatPercentage,muscleMass;

  BodyFat({this.bmi=0,this.bmr=0,this.fatMass=0,this.fatPercentage=0,this.muscleMass=0,this.waterPercentage=0});

}