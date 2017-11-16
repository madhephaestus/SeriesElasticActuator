double holeDiam = 3.2
double standoff =10
LengthParameter thickness 				= new LengthParameter("Material Thickness",11.88,[10,1])
CSG boltHole =new Cylinder(holeDiam/2,holeDiam/2,thickness.getMM()*4,(int)30).toCSG()
			.toXMin()
			.toYMin()
			.movez(-thickness.getMM()*2)
double standoffThickness = 2
CSG standoffShaft =new Cylinder((holeDiam/2)+standoffThickness,(holeDiam/2)+standoffThickness,standoff,(int)30).toCSG()
			.toXMin()
			.toYMin()
CSG standoffs = CSG.unionAll([
			standoffShaft
				.movey(9.0-standoffThickness)
				.movex(26.3-standoffThickness),
			standoffShaft
				.movey(57.4-standoffThickness)
				.movex(27.7-standoffThickness),
			standoffShaft
				.movey(56.6-standoffThickness)
				.movex(103.0-standoffThickness),
			standoffShaft
				.movey(10.7-standoffThickness)
				.movex(103.0-standoffThickness)
			])					
CSG bolts = CSG.unionAll([
			boltHole
				.movey(9.0)
				.movex(26.3),
			boltHole
				.movey(57.4)
				.movex(27.7),
			boltHole
				.movey(56.6)
				.movex(103.0),
			boltHole
				.movey(10.7)
				.movex(103.0)
			])
CSG board = new Cube(133.5,70,1.6).toCSG()
			.toXMin()
			.toYMin()
			.movez(standoff)
CSG finalboard=board.union(bolts)
CSG bottom = board.toZMin()
			.union(standoffs)
			.difference(bolts)
return [finalboard,bottom]