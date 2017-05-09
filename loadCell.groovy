//Your code here
LengthParameter printerOffset		= new LengthParameter("printerOffset",0.5,[2,0.001])
double barWidth = 12.7
double nubWidth = 14.5
double holeRad = 9.5/2.0
double holeOffsetEdgeToEdge = 16.5

CSG  loadHole =new Cylinder(holeRad,holeRad,nubWidth*2,(int)20).toCSG() // a one line Cylinder
				.movez(-nubWidth)
loadHole=loadHole
		.toXMax()
		.movex(holeOffsetEdgeToEdge/2)
		.union(loadHole
		.toXMin()
		.movex(-holeOffsetEdgeToEdge/2))
		.rotx(90)


CSG bar = new Cube(80,barWidth,barWidth).toCSG()
				
CSG nub = new RoundedCube(30,nubWidth,nubWidth)
					.cornerRadius(1)// sets the radius of the corner
					.toCSG()
				
bar=bar.union(nub)
		.difference(loadHole)
		.toXMin()
				
CSG baseBolt =Vitamins.get("capScrew","M5");
CSG endBolt =Vitamins.get("capScrew","M4");
baseBolt=baseBolt.union(baseBolt.movex(15))
			.movex(5)
endBolt=endBolt.union(endBolt.movex(15))
			.movex(60)
			
			
CSG bolts= endBolt.union(baseBolt)
			.makeKeepaway(printerOffset.getMM())
			.scalez(10)
			.movez(bar.getMaxZ())
			
bar=bar.union(bolts)
		.movex(-bar.getMaxX()/2)
		.movey(barWidth/2)
		.movez(barWidth/2)

return bar