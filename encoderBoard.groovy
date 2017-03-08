import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;

double magnetDiameter =6.0
double magnetThickness = 2.5
double magnetOffset =1.5
double mountHoleRadius = 2.0/2
double chipToShortside = 5.5
double chipToLongSide  = 9.0
double boardLong = 28
double boardShort = 22
double cornerOffset = (boardShort-(chipToLongSide*2))/2
double bearingOffset = magnetThickness+magnetOffset+1

CSG bearing = Vitamins.get("ballBearing","608zz")
			.movez(bearingOffset)
HashMap<String, Object>  bearingData = Vitamins.getConfiguration("ballBearing","608zz")
//println bearingData
double bearingHole =  bearingData.innerDiameter

CSG bearingCutterSlot = new Cube(bearingHole,bearingHole,bearingOffset +bearingData.width).toCSG()
CSG bearingCutterSlotHole = new Cube(bearingHole*2/3,bearingHole,bearingOffset +bearingData.width).toCSG()
						.movez(1)
CSG bearingHoleInner = new Cylinder(bearingHole/2,bearingHole/2,bearingOffset +bearingData.width,(int)30).toCSG() // a one line Cylinder
CSG bearingHoleOuter = new Cylinder(bearingHole/2+2,bearingHole/2+2,bearingOffset +bearingData.width,(int)30).toCSG() // a one line Cylinder
					.difference(bearingHoleInner)

bearingCutterSlot= bearingCutterSlot
				.difference(bearingCutterSlotHole)
				.toZMin()
				.union(bearingHoleOuter)

CSG magnet =new Cylinder(magnetDiameter/2,magnetDiameter/2,magnetThickness+magnetOffset,(int)30).toCSG() // a one line Cylinder

CSG board = new Cube(	boardShort,// X dimention
			boardLong,// Y dimention
			1//  Z dimention
			).toCSG()
			.toZMax()
			.toYMin()
			.movey(-chipToShortside-cornerOffset)
CSG bolt =new Cylinder(mountHoleRadius,mountHoleRadius,10,(int)30).toCSG() // a one line Cylinder
						.movez(-5)

board=board.union(bolt
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
return [board,bearingCutterSlot]