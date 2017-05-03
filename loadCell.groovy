//Your code here
LengthParameter printerOffset		= new LengthParameter("printerOffset",0.5,[2,0.001])
CSG bar = new Cube(80,12.7,12.7).toCSG()
				.toXMin()
CSG baseBolt =Vitamins.get("capScrew","M5");
CSG endBolt =Vitamins.get("capScrew","M4");
baseBolt=baseBolt.union(baseBolt.movex(15))
			.movex(5)
endBolt=endBolt.union(endBolt.movex(15))
			.movex(60)
			.rotx(180)
			
CSG bolts= endBolt.union(baseBolt)
			.makeKeepaway(printerOffset.getMM())
			.scalez(10)
			.movez(bar.getMaxZ())
			
bar=bar.union(bolts)
		.movex(-bar.getMaxX()/2)

return bar