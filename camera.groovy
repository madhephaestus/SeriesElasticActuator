//Your code here
CSG pcb =new Cube(	32.16,// X dimention
			32.16,// Y dimention
			1.7//  Z dimention
			).toCSG()
			.toZMin()
double boltHolePattern = (25.5+2.5)/2
double keepawayZ = 27*2
double keepawayX = 22.85
double keepawayY = 32.16
//Your code here
CSG keepaway =new Cube(	keepawayX,// X dimention
			keepawayY,// Y dimention
			keepawayZ//  Z dimention
			).toCSG()
double holeRadius = 2.5/2.0

CSG hole =new Cylinder(holeRadius,holeRadius,keepawayZ,(int)8).toCSG() // a one line Cylinder
				.movex(boltHolePattern)
				.movey(boltHolePattern)
				.movez(-keepawayZ/2)
CSG assembly = pcb
			.union(keepaway)
for (int i=0;i<=360;i+=90){
	assembly=assembly.union(hole
						.rotz(i))
}


return assembly