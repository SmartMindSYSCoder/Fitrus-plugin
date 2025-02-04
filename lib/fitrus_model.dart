class FitrusModel{

 final bool isConnected,hasData,hasProgress;
 final String connectionState, progress;

final BodyFat? bodyFat;

 FitrusModel({this.isConnected=false,this.hasData=false,this.hasProgress=false,this.connectionState='',this.progress='',this.bodyFat});


}

class BodyFat{

 final double bmi,bmr,waterPercentage,fatMass,fatPercentage,muscleMass,minerals,protein,calorie;

  BodyFat({this.bmi=0,this.bmr=0,this.fatMass=0,this.fatPercentage=0,this.muscleMass=0,this.waterPercentage=0,this.calorie=0.0,this.minerals=0,this.protein=0});

  @override
  String toString() {

    return "BMI:$bmi, \tBMR:$bmr, \tBodyFat:$fatPercentage, \tFatMas:$fatMass, \tMuscleMass:$muscleMass";
  }

}