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
	CSG bedA = new Cube(250,250,5).toCSG()
						.toXMin()
						.toYMin()
						.toZMax().setColor(Color.WHITE)
	CSG bedB = new Cube(250,250,5).toCSG()
						.toXMax()
						.movex(-5)
						.toYMin()
						.toZMax()
						.setColor(Color.WHITE)					
	ArrayList<CSG> beds = [bedA,
						bedB
	]
	
	
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
	for(CSG c:totalAssembly){
		if(c.getName().contains("SVG")){
			//c.addExportFormat("svg")
		}else{
			//beds.add(c)
			tmp=c.toYMin().toXMin()
			tmp.setName(c.getName())
			namedPart.put(c.getName(),tmp)
			
		}
		
	}
	double delta=1.5
	int numLinks =3
	baseRight = namedPart.baseRight
	baseLeft = namedPart.baseLeft
					.movex(baseRight.getMaxX()+delta)
	loadCellBlock= namedPart.loadCellBlock
	encoderStandoff= namedPart.encoderStandoff
	sidePlate0= namedPart.sidePlate0
	baseCap= namedPart.baseCap
	sidePlate1= namedPart.sidePlate1
	lastLink= namedPart.lastLink
	calibrationTip= namedPart.calibrationTip
	//Washers
	ArrayList<CSG> washers = []		
	for(int j=0;j<numLinks;j++){
		washers.add(namedPart.washer
			.movey(namedPart.washer.getMaxY()*j+delta*j)
			.movex(baseLeft.getMaxX()+delta
			
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
			.movex(bedB.getMinX()+delta-))
	drivenGear=  CSG.unionAll(gears)
	servoGear= namedPart.servoGear
	baseEncoderCap0= namedPart.baseEncoderCap0
	
	baseEncoderCap1= namedPart.baseEncoderCap1
	
	beds.add(baseRight)
	beds.add(baseLeft)
	beds.add(loadCellBlock)
	beds.add(encoderStandoff)
	beds.add(sidePlate0)
	beds.add(baseCap)
	beds.add(sidePlate1)
	beds.add(lastLink)
	beds.add(calibrationTip)
	beds.add(washer)
	beds.add(servoGear)
	beds.add(baseEncoderCap0)
	beds.add(drivenGear)
	beds.add(baseEncoderCap1)
	return beds
}

ThreadUtil.wait(100)
while(MobileBaseCadManager.get( base).getProcesIndictor().getProgress()<1){
	ThreadUtil.wait(1000)
	println "Waiting for cad to get to 1:"+MobileBaseCadManager.get(base).getProcesIndictor().getProgress()
}

List<CSG> totalAssembly = arrangeBed(base) ;
BowlerStudioController.setCsg(totalAssembly , null);

/*
File baseDirForFiles = com.neuronrobotics.nrconsole.util.FileSelectionFactory.GetDirectory(new File("/home/hephaestus/Desktop/armLinks/"))

File dir = new File(baseDirForFiles.getAbsolutePath() + "/" + base.getScriptingName() );
if (!dir.exists())
	dir.mkdirs();

CadFileExporter.generateManufacturingParts(totalAssembly, dir);
*/

return totalAssembly