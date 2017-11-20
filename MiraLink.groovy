//Your code here

if(args == null){
	def base =DeviceManager.getSpecificDevice( "HephaestusArm",{
		//If the device does not exist, prompt for the connection
		
		MobileBase m = BowlerStudio.loadMobileBaseFromGit(
			"https://github.com/osh1996/SeriesElasticActuator.git",
			"seaArm.xml"
			)
		if(m==null)
			throw new RuntimeException("Arm failed to assemble itself")
		println "Connecting new device robot arm "+m
		return m
	})
	DHParameterKinematics arm = base.getAllDHChains().get(0)
	ArrayList<DHLink> dhLinks=arm.getChain().getLinks();
	DHLink dh = dhLinks.get(0);
	args=[dh,(int)2]
}

 CSG reverseDHValues(CSG incoming,DHLink dh ){
	println "Reversing "+dh
	TransformNR step = new TransformNR(dh.DhStep(0))
	Transform move = TransformFactory.nrToCSG(step)
	return incoming.transformed(move)
}
 CSG moveDHValues(CSG incoming,DHLink dh ){
	TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
	Transform move = TransformFactory.nrToCSG(step)
	return incoming.transformed(move)
	
}

CSG DummyStandInForLink = new Cube(20).toCSG()