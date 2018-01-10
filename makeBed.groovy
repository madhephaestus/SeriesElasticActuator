import javafx.scene.paint.Color;

def base =DeviceManager.getSpecificDevice( "HephaestusWorkCell",{
	//If the device does not exist, prompt for the connection
	
	MobileBase m = BowlerStudio.loadMobileBaseFromGit(
		"https://github.com/madhephaestus/SeriesElasticActuator.git",
		"seaArm.xml"
		)
	if(m==null)
		throw new RuntimeException("Arm failed to assemble itself")
	println "Connecting new device robot arm "+m
	return m
})

/**
 * This function should generate the bed or beds or parts to be used in manufacturing
 * If parts are to be ganged up to make print beds then this should happen here
 * @param base the base to generate
 * @return simulatable CAD objects
 */
ArrayList<CSG> arrangeBed(MobileBase b ){
	double size =260
	CSG bedA = new Cube(size,size,5).toCSG()
						.toXMin()
						.toYMin()
						.toZMax().setColor(Color.WHITE)
	CSG bedB = new Cube(size,size,5).toCSG()
						.toXMax()
						.movex(-5)
						.toYMin()
						.toZMax()
						.setColor(Color.WHITE)					

	
	ArrayList<DHParameterKinematics> limbs = b.getAllDHChains();
	double numLimbs = limbs.size();
	int i;
	// Start by generating the legs using the DH link based generator
	ArrayList<CSG> totalAssembly = new ArrayList<>();
	double offset = 0;
	for (i = 0; i < limbs.size(); i += 1) {
		DHParameterKinematics l = limbs.get(i);
		ArrayList<CSG> parts = MobileBaseCadManager.get(b).getDHtoCadMap().get(l);
		for (int j = 0; j < parts.size(); j++) {
			CSG csg = parts.get(j);
			String name = csg.getName();
			try {
				CSG tmp = csg.prepForManufacturing();
				tmp.setName(name)
				if (csg != null) {
					totalAssembly.add(tmp);
				}
			} catch (Exception ex) {
				BowlerStudio.printStackTrace(ex, getCadScript());
			}
		}
	}
	int link = 0;
	// now we genrate the base pieces
	for (CSG csg : MobileBaseCadManager.get(b).getBasetoCadMap().get(b)) {
		String name = csg.getName();
		try {

			csg = csg.prepForManufacturing();
			
			if (csg != null) {
				csg.setName(name)
				totalAssembly.add(csg);
			}
		} catch (Exception ex) {
			BowlerStudio.printStackTrace(ex, null);
		}
	}
	HashMap<String,CSG> namedPart = []
	svgParts=[]
	for(CSG c:totalAssembly){
		tmp=c
		if(c.getName().contains("SVG")){
			tmp.addExportFormat("svg")
			svgParts.add(tmp)
		}else{
			tmp=c.toYMin().toXMin()
			tmp.setName(c.getName())
			namedPart.put(c.getName(),tmp)
		}
		
		
		
	}
	
	return svgParts
	double delta=2
	int numLinks =3
	
	
	baseRight = namedPart.baseRight
	baseLeft = namedPart.baseLeft
					.movex(baseRight.getMaxX()+delta)
	//Washers
	ArrayList<CSG> washers = []		
	for(int j=0;j<numLinks;j++){
		washers.add(namedPart.washer
			.movey(namedPart.washer.getMaxY()*j+delta*j)
			.movex(baseLeft.getMaxX()+delta))
			
	}
	washer=  CSG.unionAll(washers)
	//Big Gears
	ArrayList<CSG> gears = []	
	for(int j=0;j<numLinks-1;j++){
		gears.add(namedPart.drivenGear
			.movey(namedPart.drivenGear.getMaxY()*j+delta*j)
			.movex(bedB.getMinX()+delta))
			
	}
	gears.add(namedPart.drivenGear
			.movex(bedB.getMinX()+delta*2+namedPart.drivenGear.getMaxX() ))
	drivenGear=  CSG.unionAll(gears)
	//Small Gears
	ArrayList<CSG> sgears = []	
	for(int j=0;j<numLinks;j++){
		sgears.add(namedPart.servoGear
			.movey(namedPart.servoGear.getMaxY()*j+delta*j)
			.movex(bedB.getMaxX()-delta -namedPart.servoGear.getMaxX() ))
			
	}
	servoGear= CSG.unionAll(sgears)
	//LoadCell Block
	ArrayList<CSG> blocks = []
	loadCellBlock= namedPart.loadCellBlock	
	for(int j=0;j<numLinks-1;j++){
		blocks.add(loadCellBlock
			.movey((loadCellBlock.getMaxY()*j+delta*j)+
				namedPart.drivenGear.getMaxX()	+delta
			)
			.movex(bedB.getMinX()+delta*2 +namedPart.drivenGear.getMaxX() ))
			
	}
	CSG otherBlock = loadCellBlock
			.rotz(20)
			.toXMax()
			.toYMax()
			.movey(bedA.getMaxY()-35)
			.movex(bedA.getMaxX())
	
			
	loadCellBlock= CSG.unionAll(blocks)
	
	ArrayList<CSG> sencoderStandoff = []	
	for(int j=0;j<numLinks;j++){
		sencoderStandoff.add(namedPart.encoderStandoff
			.toXMax()
			.movey(namedPart.encoderStandoff.getMaxY()*j+delta*j+delta)
			.movex(bedB.getMaxX()-delta -namedPart.servoGear.getMaxX() ))
			
	}
	encoderStandoff= CSG.unionAll(sencoderStandoff)
	//encoderStandoff= namedPart.encoderStandoff
	sidePlate0= namedPart.sidePlate0
				.rotz(-112)
				.toYMax()
				.toXMax()
				.movey(bedB.getMaxY())
				.movex(bedB.getMaxX())
	baseCap= namedPart.baseCap
			.rotz(-75)
			.movey(baseRight.getMaxY()+delta)
			.movex(baseRight.getMaxX()+delta)
	sidePlate1= namedPart.sidePlate1
			.movey(baseRight.getMaxY()+delta-10)
			.movex(baseRight.getMaxX()+delta)
	lastLink= namedPart.lastLink
			.rotz(-160)
			.toXMin()
			.toYMax()
			.movey(otherBlock.getMinY()+delta+40)
			.movex(baseCap.getMaxX()+delta-10)
	//calibrationTip= namedPart.calibrationTip
	
	baseEncoderCap0= namedPart.baseEncoderCap0
					.rotz(107)
					.toYMax()
					.toXMax()
					.movey(bedA.getMaxY())
					.movex(bedA.getMaxX())
	
	baseEncoderCap1= namedPart.baseEncoderCap1
					.rotz(-99)
					.toYMax()
					.toXMin()
					.movey(bedA.getMaxY())
					.movex(bedA.getMinX())
	cup = namedPart.cupr
			.toXMax()
			.movex(bedA.getMaxX())
			.movey(sidePlate1.getMaxY()-15)
			.toZMin()
	def otherCup = cup.toYMin().movey(cup.getMaxY()+1)
	def beda =[washer,baseRight,baseLeft,baseCap,sidePlate1,lastLink,baseEncoderCap0,baseEncoderCap1,otherBlock,cup,otherCup]as ArrayList<CSG>

	def bedb = [encoderStandoff,loadCellBlock,sidePlate0,servoGear,drivenGear]as ArrayList<CSG>

	println "Making bed A "
	BowlerStudioController.setCsg(beda , null);
	//CSG A = CSG.unionAll(beda).toYMax()
	//A.setName("BedA")

	println "Making bed B "
	BowlerStudioController.setCsg(bedb , null);
	//CSG B = CSG.unionAll(bedb).toYMax()
	//B.setName("BedB")
	parts=[]
	
	parts.addAll(beda)
	parts.addAll(bedb)
	//parts.add(A);parts.add(B);
	return parts
}

ThreadUtil.wait(100)
while(MobileBaseCadManager.get( base).getProcesIndictor().get()<1){
	ThreadUtil.wait(1000)
	println "Waiting for cad to get to 1, currently = "+MobileBaseCadManager.get(base).getProcesIndictor().get()
}
File baseDirForFiles = com.neuronrobotics.nrconsole.util.FileSelectionFactory.GetDirectory(new File(System.getProperty("user.home")))
List<CSG> totalAssembly = arrangeBed(base) ;
BowlerStudioController.setCsg(totalAssembly , null);
//return null
File dir = new File(baseDirForFiles.getAbsolutePath() + "/" + base.getScriptingName() );
if (!dir.exists())
	dir.mkdirs();

new CadFileExporter().generateManufacturingParts(totalAssembly, dir);


return totalAssembly