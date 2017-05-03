//Your code here

CSG bar = new Cube(80,12.7,12.7).toCSG()
				.toXMin()
CSG baseBolt =Vitamins.get("capScrew","M5");
CSG endBolt =Vitamins.get("capScrew","M4");
baseBolt=baseBolt.union(baseBolt.movex(15))
			.movex(5)
endBolt=endBolt.union(endBolt.movex(15))
			.movex(60)
			
CSG bolts= endBolt.union(baseBolt)
			.scalez(5)
			.movez(bar.getMaxZ())
			
bar=bar.union(bolts)

return [bar]