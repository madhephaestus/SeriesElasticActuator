
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
	ArrayList<CSG> beds = [new Cube(250,250,5).toCSG()
						.toXMin()
						.toYMin()
						.toZMax(),
						new Cube(250,250,5).toCSG()
						.toXMax()
						.movex(-5)
						.toYMin()
						.toZMax()
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
			namedPart.put(c.getName(),c.toXMin().toXMax())
			
		}
		
	}
	for(String c:namedPart.keySet())
		beds.add(namedPart.get(c))
	
	return beds
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