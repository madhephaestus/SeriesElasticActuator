double holeDiam = 3.2
double standoff =8
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
				.movey(2.54-standoffThickness)
				.movex(2.54-standoffThickness),
			standoffShaft
				.movey(118.68-standoffThickness)
				.movex(2.54-standoffThickness),
			standoffShaft
				.movey(2.54-standoffThickness)
				.movex(139.84-standoffThickness),
			standoffShaft
				.movey(118.68-standoffThickness)
				.movex(139.84-standoffThickness)
			])					
CSG bolts = CSG.unionAll([
			boltHole
				.movey(2.54)
				.movex(2.54),
			boltHole
				.movey(118.68)
				.movex(2.54),
			boltHole
				.movey(2.54)
				.movex(139.84),
			boltHole
				.movey(118.68)
				.movex(139.84)
			])
CSG board = new Cube(146.05,124.89,1.6).toCSG()
			.toXMin()
			.toYMin()
			.movez(standoff)
CSG finalboard=board.union(bolts)
CSG bottom = board.toZMin()
			.union(standoffs)
			.difference(bolts)
return [finalboard,bottom]