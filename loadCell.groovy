//Your code here
LengthParameter printerOffset		= new LengthParameter("printerOffset",0.5,[2,0.001])
double barWidth = 12.7+printerOffset.getMM()
double nubOffset = 1.9
double nubWidth = barWidth+(nubOffset*2)
double holeRad = 9.5/2.0-printerOffset.getMM()*2
double holeOffsetEdgeToEdge = 16.5-printerOffset.getMM()*2
double loadCellStandoff = 2
CSG  loadHole =new Cylinder(holeRad,holeRad,nubWidth*2,(int)20).toCSG() // a one line Cylinder
				.movez(-nubWidth)
loadHole=loadHole
		.toXMax()
		.movex(holeOffsetEdgeToEdge/2)
		.union(loadHole
		.toXMin()
		.movex(-holeOffsetEdgeToEdge/2))
		.rotx(90)


double barLength = 80
CSG bar = new Cube(barLength+2,barWidth+nubOffset+printerOffset.getMM()+loadCellStandoff,barWidth+printerOffset.getMM()).toCSG()
			.movey(printerOffset.getMM()/2-nubOffset/2)
CSG backKeepaway = new Cube(barLength/2+1,barWidth+nubOffset+printerOffset.getMM()+loadCellStandoff,barWidth+printerOffset.getMM()).toCSG()
			.movey((nubOffset))	
			.toXMax()			
CSG nub = new Cube(30,nubWidth+loadCellStandoff,nubWidth)
					.toCSG()
					//.movey(nubOffset)
				
bar=CSG.unionAll([bar,nub,backKeepaway])
		.difference(loadHole)
		//.toXMin()
HashMap<String, Object>  m5boltMeasurments = Vitamins.getConfiguration( "capScrew","M5")
HashMap<String, Object>  m4boltMeasurments = Vitamins.getConfiguration( "capScrew","M4")

CSG baseBolt =new Cylinder((m5boltMeasurments.outerDiameter+printerOffset.getMM())/2,
					(m5boltMeasurments.outerDiameter+printerOffset.getMM())/2,100,(int)30).toCSG()
			.movez(-50)
CSG endBolt =new Cylinder((m4boltMeasurments.outerDiameter+printerOffset.getMM())/2,
					(m4boltMeasurments.outerDiameter+printerOffset.getMM())/2,100,(int)30).toCSG()
			.movez(-50)
baseBolt=baseBolt.union(baseBolt.movex(15))
			.movex(5)
endBolt=endBolt.union(endBolt.movex(15))
			.movex(60)
			
			
CSG bolts= endBolt.union(baseBolt)
			
			//.movez(bar.getMaxZ())
			
bar=bar.union(bolts.movex(-barLength/2))
		.movey(barWidth/2-nubOffset+loadCellStandoff)

return bar