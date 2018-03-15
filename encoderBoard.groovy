import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.parametrics.*;

CSG makeEncoder(){
	LengthParameter printerOffset 		= new LengthParameter("printerOffset",0.5,[1.2,0])
	StringParameter bearingSizeParam 			= new StringParameter("Encoder Board Bearing","R8-60355K505",Vitamins.listVitaminSizes("ballBearing"))
	LengthParameter encoderBoltKeepaway 		= new LengthParameter("encoderBoltKeepaway",printerOffset.getMM(),[1.2,0])
	HashMap<String, Object>  bearingData = Vitamins.getConfiguration("ballBearing",bearingSizeParam.getStrValue())
	
	
	double magnetDiameter =6.0+ printerOffset.getMM()
	double magnetThickness = 2.5+printerOffset.getMM()
	double magnetOffset =1.5
	double mountHoleRadius = 2.0/2
	double chipToShortside = 5.5
	double chipToLongSide  = 9.0
	double boardLong = 28
	double boardShort = 22
	double cornerOffset = (boardShort-(chipToLongSide*2))/2
	double PCBsurfaceTobearing = -6
	
	double bearingOffset = magnetThickness+magnetOffset+(-PCBsurfaceTobearing)+1
	double bearingHole =  bearingData.innerDiameter
	double bearingHoleWithOffset =(bearingHole )/2
	
	
	
	CSG bearing = Vitamins.get("ballBearing",bearingSizeParam.getStrValue())
				.makeKeepaway(printerOffset.getMM()/2)
				.movez(bearingOffset)
	
	CSG bearingCutterSlot = new Cube(bearingHole,bearingHole,bearingOffset +bearingData.width).toCSG()
	CSG bearingCutterSlotHole = new Cube(bearingHole*2/3,bearingHole,bearingOffset +bearingData.width).toCSG()
							.movez(1)
	CSG bearingHoleInner = new Cylinder(bearingHoleWithOffset,bearingHoleWithOffset,bearingOffset +bearingData.width,(int)30).toCSG() // a one line Cylinder
	CSG bearingHoleOuter = new Cylinder(bearingHole/2+2,bearingHole/2+2,bearingOffset +bearingData.width,(int)30).toCSG() // a one line Cylinder
						.difference(bearingHoleInner)
	
	bearingCutterSlot= bearingCutterSlot
					.difference(bearingCutterSlotHole)
					.toZMin()
					.union(bearingHoleOuter)
	
	CSG magnet =new Cylinder(magnetDiameter/2,magnetDiameter/2,magnetThickness+magnetOffset,(int)30).toCSG() // a one line Cylinder
	CSG chipClearence =new Cylinder(bearingHole/2+2,bearingHole/2+2,magnetOffset+0.5,(int)30).toCSG() // a one line Cylinder
	magnet=magnet.union(chipClearence)
	CSG board = new Cube(	boardShort,// X dimention
				boardLong,// Y dimention
				1//  Z dimention
				).toCSG()
				.toZMax()
				.toYMin()
				.movey(-chipToShortside-cornerOffset)
				
	CSG chipCkearence = new Cube(14.5,16.3,1.9).toCSG()
					.toXMin()
					.toYMin()
					.toZMin()
					
					.movey(-3.5)
					.movex(-6.5)
	double boardEdgeX = 23.14
	double boardEdgeY = 15.86
	CSG servoHeader = new Cube(12.5,8,2.5).toCSG()
					.toXMax()
					.toYMin()
					.toZMin()
					.movex(9.5-boardEdgeX)
					.movey(-36.57+boardEdgeY)
	CSG hdmiHeader = new Cube(7.1,11.4,3.29).toCSG()
					.toXMax()
					.toYMin()
					.toZMin()
					.movex(9.8-boardEdgeX)
					.movey(-25.7+boardEdgeY)
	CSG loadHeader = new Cube(11.2,10,3.54).toCSG()
					.toXMax()
					.toYMin()
					.toZMin()
					.movex(11.72-boardEdgeX)
					.movey(-12.25+boardEdgeY)				
	chipCkearence=chipCkearence.union([servoHeader,hdmiHeader,loadHeader])
	
	CSG bolt =new Cylinder(mountHoleRadius,mountHoleRadius,16,(int)30).toCSG() // a one line Cylinder
							//.movez(-2)
							.union(
								new Cylinder(	mountHoleRadius+encoderBoltKeepaway.getMM(),
											mountHoleRadius+encoderBoltKeepaway.getMM(),
											PCBsurfaceTobearing,(int)30).toCSG() 
											.movez(-PCBsurfaceTobearing)
								)
	/*												
	CSG boardCad = (CSG)ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
	                              "encoderBoard.stl" , // file to load
	                              null
                        )
                        .rotz(90)
                        .movex(-10.8)
                        .movey(10.4)
                        .rotz(-180)
      */                  
 	CSG boltSet = bolt
					.movex(chipToLongSide)
					.movey(chipToShortside)
					
			.union(bolt
					.movex(-chipToLongSide)
					.movey(chipToShortside)
					)
			.union(bolt
					.movex(chipToLongSide)
					.movey(-chipToShortside)
					)
			.union(bolt
					.movex(-chipToLongSide)
					.movey(-chipToShortside)
					)
	board=CSG.unionAll([
				magnet,
				boltSet,
				boltSet.rotz(90),
				bearing,
				bearingCutterSlot,
				//boardCad.minkowski(new Cube(2,2,0.01).toCSG().toZMax()),
				//boardCad,
				]	)
	if(encoderBoltKeepaway.getMM()>0){
		board=			board.union(chipCkearence)	
	}
	
	board.setParameter(printerOffset)
		.setRegenerate({makeEncoder()})
			
	double shadowy = -chipToShortside-cornerOffset-3
	CSG cordCutOut = new Cube(	6,// X dimention
				6,// Y dimention
				board.getMaxZ()//  Z dimention
				).toCSG()
				.toZMin()
				.toXMax()
				.movex(-30)
	cordCutOut=cordCutOut.movey(6)
				.union(
					cordCutOut.movey(-13)
					)
	
	board=board
		.union(	cordCutOut)
		//.union(fullBezier)
		.movez(PCBsurfaceTobearing)
		
	if (args ==  null)	return board
	
	if(args == null)
		args = [100]
	
	CSG shaddow = new Cube(	boardShort+6,// X dimention
				boardLong+6,// Y dimention
				args.get(0)//  Z dimention
				).toCSG()
				.toZMax()
				
				.toYMin()
				.movey(shadowy)
		
	board=board
		.union([bearingHoleInner,bearingHoleInner.movez(bearingHoleInner.getMaxZ()),shaddow])
	
	
	return board

}

return makeEncoder();
