import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.parametrics.*;

CSG makeEncoder(){
	LengthParameter printerOffset 		= new LengthParameter("printerOffset",0.5,[1.2,0])
	StringParameter bearingSizeParam 			= new StringParameter("Encoder Board Bearing","608zz",Vitamins.listVitaminSizes("ballBearing"))
	
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
	CSG bolt =new Cylinder(mountHoleRadius,mountHoleRadius,16,(int)30).toCSG() // a one line Cylinder
							.movez(-5)
	CSG boardCad = (CSG)ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
	                              "encoderBoard.stl" , // file to load
	                              null
                        )
                        .rotz(90)
                        .movex(-10.8)
                        .movey(10.4)
                        
 
	board=magnet.union(bolt
					.movex(chipToLongSide)
					.movey(chipToShortside)
					)
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
			.union(magnet)
			.union(bearing)
			.union(bearingCutterSlot)
			.union(boardCad.minkowski(new Cube(2,2,0.1).toCSG().toZMax()))
			.union(boardCad)
			.setParameter(printerOffset)
			.setRegenerate({makeEncoder()})
			
	double shadowy = -chipToShortside-cornerOffset-3
	CSG cordCutOut = new Cube(	6,// X dimention
				6,// Y dimention
				board.getMaxZ()//  Z dimention
				).toCSG()
				.toZMin()
				.toXMax()
				.movex(-bearing.getMaxX()-2)
	board=board
		.union(	cordCutOut)
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
