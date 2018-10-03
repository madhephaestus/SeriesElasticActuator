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
    ArrayList<LinkConfiguration> links = l.getLinkConfigurations();
     for (LinkConfiguration lc:links) {
   	 ArrayList<CSG> cadForThisLink = MobileBaseCadManager.get(b).getLinktoCadMap().get(lc);
   	     CSG linUnified = CSG.unionAll(cadForThisLink);
    totalAssembly.add(linUnified);
     }

	}
	int link = 0;
	// now we genrate the base pieces
	totalAssembly.add(CSG.unionAll(MobileBaseCadManager.get(b).getBasetoCadMap().get(b)));
	return totalAssembly
	
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
