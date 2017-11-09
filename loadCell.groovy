//Your code here
LengthParameter printerOffset		= new LengthParameter("printerOffset",0.5,[2,0.001])
double barWidth = 12.7
double nubWidth = 16.5
double holeRad = 9.5/2.0-printerOffset.getMM()*2
double holeOffsetEdgeToEdge = 16.5-printerOffset.getMM()*2

CSG  loadHole =new Cylinder(holeRad,holeRad,nubWidth*2,(int)20).toCSG() // a one line Cylinder
				.movez(-nubWidth)
loadHole=loadHole
		.toXMax()
		.movex(holeOffsetEdgeToEdge/2)
		.union(loadHole
		.toXMin()
		.movex(-holeOffsetEdgeToEdge/2))
		.rotx(90)

double nubOffset = ((-barWidth+nubWidth)/2)
double barLength = 80
CSG bar = new Cube(barLength+2,barWidth+nubOffset+printerOffset.getMM(),barWidth+printerOffset.getMM()).toCSG()
			.movey(-nubOffset/2+printerOffset.getMM()/2)
				
CSG nub = new RoundedCube(30,nubWidth,nubWidth)
					.cornerRadius(1)// sets the radius of the corner
					.toCSG()
				
bar=bar.union(nub)
		.difference(loadHole)
		//.toXMin()
HashMap<String, Object>  m5boltMeasurments = Vitamins.getConfiguration( "capScrew","M5")
HashMap<String, Object>  m4boltMeasurments = Vitamins.getConfiguration( "capScrew","M4")

CSG baseBolt =new Cylinder(m5boltMeasurments.outerDiameter/2,
					m5boltMeasurments.outerDiameter/2,100,(int)30).toCSG()
			.movez(-50)
CSG endBolt =new Cylinder(m4boltMeasurments.outerDiameter/2,
					m4boltMeasurments.outerDiameter/2,100,(int)30).toCSG()
			.movez(-50)
baseBolt=baseBolt.union(baseBolt.movex(15))
			.movex(5)
endBolt=endBolt.union(endBolt.movex(15))
			.movex(60)
			
			
CSG bolts= endBolt.union(baseBolt)
			
			//.movez(bar.getMaxZ())
			
bar=bar.union(bolts.movex(-barLength/2))
		.movey(barWidth/2)
		//.movez(barWidth/2)

return bar