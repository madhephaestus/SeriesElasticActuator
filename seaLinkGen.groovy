import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import eu.mihosoft.vrl.v3d.Transform;

Vitamins.setGitRepoDatabase("https://github.com/madhephaestus/Hardware-Dimensions.git")
CSGDatabase.clear()
return new ICadGenerator(){
	HashMap<String , HashMap<String,ArrayList<CSG>>> map =  new HashMap<>();
	HashMap<String,ArrayList<CSG>> bodyMap =  new HashMap<>();
	LengthParameter thickness 				= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter printerOffset 			= new LengthParameter("printerOffset",0.5,[1.2,0])
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))
	StringParameter bearingSizeParam 			= new StringParameter("Encoder Board Bearing","608zz",Vitamins.listVitaminSizes("ballBearing"))
	StringParameter gearAParam 			 	= new StringParameter("Gear A","HS60T",Vitamins.listVitaminSizes("vexGear"))
	StringParameter gearBParam 				= new StringParameter("Gear B","HS84T",Vitamins.listVitaminSizes("vexGear"))
	
     String springType = "Torsion-9271K133"
     HashMap<String, Object>  springData = Vitamins.getConfiguration("torsionSpring",springType)
	HashMap<String, Object>  bearingData = Vitamins.getConfiguration("ballBearing",bearingSizeParam.getStrValue())			
	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	HashMap<String, Object>  gearAMeasurments = Vitamins.getConfiguration( "vexGear",gearAParam.getStrValue())
	HashMap<String, Object>  gearBMeasurments = Vitamins.getConfiguration( "vexGear",gearBParam.getStrValue())
	
	double gearDistance  = (gearAMeasurments.diameter/2)+(gearBMeasurments.diameter/2) +2.75
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double springHeight = springData.numOfCoils*springData.wireDiameter
	double linkMaterialThickness = 19 
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	//https://www.mcmaster.com/#standard-dowel-pins/=16olhp3
	// PN: 93600A586		
	double pinRadius = (5.0+printerOffset.getMM())/2
	double pinLength = 36

	double encoderCapRodRadius =8
	double capPinSpacing = gearAMeasurments.diameter*0.75+encoderCapRodRadius
	double bearingDiameter = bearingData.outerDiameter
	double encoderToEncoderDistance = (springHeight/2)+linkMaterialThickness
	
	DHParameterKinematics neck=null;
	CSG gearA = Vitamins.get( "vexGear",gearAParam.getStrValue())
				.movey(-gearDistance)
	CSG gearB = Vitamins.get( "vexGear",gearBParam.getStrValue());
	CSG bolt = Vitamins.get( "capScrew",boltSizeParam.getStrValue());
	CSG spring = Vitamins.get( "torsionSpring",springType)	
				.movez(-springHeight/2)
	CSG previousServo = null;
	CSG previousEncoder = null
	CSG encoderCap=null;
	CSG loadBearingPin =new Cylinder(pinRadius,pinRadius,pinLength,(int)30).toCSG() 
						.movez(-pinLength/2)
						
	CSG encoderSimple = (CSG) ScriptingEngine
					 .gitScriptRun(
            "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
            "encoderBoard.groovy" , // file to load
            null// no parameters (see next tutorial)
            )
     CSG encoderKeepaway = (CSG) ScriptingEngine
					 .gitScriptRun(
			            "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
			            "encoderBoard.groovy" , // file to load
			            [10]// create a keepaway version
			            )
			            .movez(-encoderToEncoderDistance)
     CSG encoder =   encoderSimple .movez(-encoderToEncoderDistance)
	double encoderBearingHeight = encoderSimple.getMaxZ()
	double topPlateOffset = encoderToEncoderDistance*2-encoderBearingHeight*2
	double centerLinkToBearingTop = encoderToEncoderDistance-encoderBearingHeight
	/**
	 * Gets the all dh chains.
	 *
	 * @return the all dh chains
	 */
	public ArrayList<DHParameterKinematics> getLimbDHChains(MobileBase base) {
		ArrayList<DHParameterKinematics> copy = new ArrayList<DHParameterKinematics>();
		for(DHParameterKinematics l:base.getLegs()){
			copy.add(l);	
		}
		for(DHParameterKinematics l:base.getAppendages() ){
			copy.add(l);	
		}
		return copy;
	}
	@Override 
	public ArrayList<CSG> generateBody(MobileBase base ){
		ArrayList<CSG> attachmentParts = new ArrayList<CSG>()
		double maxz = 0.001
		double cornerRadius = 3
		for(DHParameterKinematics l:getLimbDHChains(base)){
			double thisZ = l.getRobotToFiducialTransform().getZ()
			if(thisZ>maxz)
				maxz=thisZ
		}
		DHParameterKinematics sourceLimb=base.getAppendages() .get(0)
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(0);
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		DHLink dh = dhLinks.get(0);
		HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		LengthParameter tailLength		= new LengthParameter("Cable Cut Out Length",maxz,[500,0.01])
		tailLength.setMM(maxz)
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
								.rotz(180+Math.toDegrees(dh.getTheta()))
		
		double servoNub = servoMeasurments.tipOfShaftToBottomOfFlange - servoMeasurments.bottomOfFlangeToTopOfBody
		double servoTop = servoReference.getMaxZ()-servoNub
		double topLevel = maxz -(springHeight/2)-linkMaterialThickness +encoderBearingHeight
		double servoPlane = topLevel - encoderBearingHeight
		double basexLength = gearDistance + servoMeasurments.servoThinDimentionThickness/2
		//double baseyLength = servoMeasurments.flangeLongDimention 
		double servoCentering = servoMeasurments.flangeLongDimention -servoMeasurments.shaftToShortSideFlandgeEdge
		double minimumWidth = (capPinSpacing-encoderCapRodRadius-cornerRadius)
		if(servoCentering<minimumWidth)
			servoCentering=minimumWidth
		double baseyLength = servoCentering*2
		double keepAwayDistance =10
		
		servoReference=servoReference
					.movez(servoPlane)
					.movex(-gearDistance)
		CSG encoderBaseKeepaway = (CSG) ScriptingEngine
					 .gitScriptRun(
			            "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
			            "encoderBoard.groovy" , // file to load
			            [topLevel+5]// create a keepaway version
			            )
			            .movez(servoPlane)
		double encoderKeepawayDistance= encoderBaseKeepaway.getMaxX()
		/*
		for (int i=1;i<3;i++){
			servoReference=servoReference
						.union(servoReference
								.movez(servoMeasurments.flangeThickness*i))
		}
		*/
		CSG keepawayBottomX = new Cube(basexLength+(keepAwayDistance*3)+encoderKeepawayDistance,
							baseyLength-(keepAwayDistance*2),
							keepAwayDistance)
						.toCSG()
						.toZMin()
		CSG keepawayBottomY = new Cube(basexLength+encoderKeepawayDistance-(keepAwayDistance*2),
							baseyLength+(keepAwayDistance*3),
							keepAwayDistance)
							.toCSG()
							.toZMin()
		CSG baseShape = new RoundedCube(basexLength+(keepAwayDistance*2)+encoderKeepawayDistance,
							baseyLength+(keepAwayDistance*2),
							topLevel)
						.cornerRadius(cornerRadius)
						.toCSG()
						.toZMin()
						.difference([keepawayBottomY,keepawayBottomX])
		baseShape=baseShape				
				.toXMax()
				.toYMin()
				.movey(-servoCentering-keepAwayDistance)
				.movex(keepAwayDistance+encoderKeepawayDistance)
				.difference([encoderBaseKeepaway,servoReference])
		CSG baseCap = getEncoderCap()
					.movez(topLevel)
		attachmentParts.add(baseShape)
		attachmentParts.add(baseCap)
		return attachmentParts;
	}
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics sourceLimb, int linkIndex) {
		
		String legStr = sourceLimb.getXml()
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(linkIndex);

		String linkStr =conf.getXml()
		ArrayList<CSG> csg = null;
		HashMap<String,ArrayList<CSG>> legmap=null;
		if(map.get(legStr)==null){
			map.put(legStr, new HashMap<String,ArrayList<CSG>>())	
			// now load the cad and return it. 
		}
		legmap=map.get(legStr)
		if(legmap.get(linkStr) == null ){
			legmap.put(linkStr,new ArrayList<CSG>())
		}
		csg = legmap.get(linkStr)
		if(csg.size()>linkIndex){
			// this link is cached
			println "This link is cached"
			return csg;
		}
		//Creating the horn
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		DHLink dh = dhLinks.get(linkIndex);
		HashMap<String, Object> shaftmap = Vitamins.getConfiguration(conf.getShaftType(),conf.getShaftSize())
		HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		println conf.getShaftType() +" "+conf.getShaftSize()+" "+shaftmap
		double hornOffset = 	shaftmap.get("hornThickness")	
		double servoNub = servoMeasurments.tipOfShaftToBottomOfFlange - servoMeasurments.bottomOfFlangeToTopOfBody
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		.transformed(new Transform().rotZ(90))
		
		double servoTop = servoReference.getMaxZ()-servoNub
						
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	
					.rotx(180)
					.movez(hornOffset)
					.movey(-gearDistance)
		servoReference=servoReference
			.movez(-springHeight/2-linkMaterialThickness)			
			.movey(-gearDistance)
			.rotz(90+Math.toDegrees(dh.getTheta()))
		for(int i=0;i<2;i++){
			gearA=gearA
				.difference(horn
							.movez(hornOffset*i)
							)
		}
		CSG myGearA = gearA
					.rotz(90+Math.toDegrees(dh.getTheta()))
					.movez(-(springHeight/2)-linkMaterialThickness+servoTop)	
		
		if(linkIndex==0){
			CSG baseServo =servoReference.clone()
			CSG secondLinkServo =servoReference.clone()
			CSG baseForceSenseEncoder = encoder
									.rotz(180-Math.toDegrees(dh.getTheta()))
									.rotx(180)
			CSG baseEncoder = encoder.clone()
			
			previousEncoder = baseEncoder
			previousServo = baseServo
			
			add(csg,myGearA,sourceLimb.getRootListener())
			//add(csg,baseServo,sourceLimb.getRootListener())
			//add(csg,baseEncoder,sourceLimb.getRootListener())
			//add(csg,baseForceSenseEncoder,sourceLimb.getRootListener())
		}
		if(linkIndex<dhLinks.size()-1){
				CSG forceSenseEncoder = encoder
									.rotz(180-Math.toDegrees(dh.getTheta()))
									.rotx(180)
				CSG baseEncoderCap = getEncoderCap().clone()
								.movez(-centerLinkToBearingTop)
				CSG thirdPlusLinkServo =servoReference.clone()
				CSG linkEncoder = encoder.clone()
									.rotz(-Math.toDegrees(dh.getTheta()))

				previousEncoder = linkEncoder
				previousServo = thirdPlusLinkServo
				add(csg,myGearA.clone(),dh.getListener())
				add(csg,thirdPlusLinkServo,dh.getListener())
				add(csg,linkEncoder,dh.getListener())
				//add(csg,forceSenseEncoder,dh.getListener())
				add(csg,baseEncoderCap,dh.getListener())
			}else{
				// load the end of limb
				// Target point
				CSG handMountPart = handMount()
				add(csg,handMountPart,dh.getListener())
				
			}
		
		
		CSG springMoved = moveDHValues(spring
									.rotz(-Math.toDegrees(dh.getTheta()))
									//.rotz(linkIndex==0?180:0)
									,dh)
		CSG myGearB = moveDHValues(gearB
					.rotz(5)
					.movez(-(springHeight/2)-linkMaterialThickness+servoTop)	
					,dh)
		CSG myPin = moveDHValues(loadBearingPin,dh)
		
		add(csg,myPin,dh.getListener())
		add(csg,myGearB,dh.getListener())
		add(csg,springMoved,dh.getListener())
		
		
		return csg;
	}

	private CSG handMount(){
		
		CSG mountPlate = new Cube(5,30,70).toCSG()
		CSG centerHole =new Cylinder(10.2/2,10.2/2,10,(int)30)
							.toCSG()
							.movez(-5)
							.roty(90)
							
		
		mountPlate=mountPlate
					.toXMin()
					.difference(centerHole)
					.difference(bolt
								.roty(90)
								.toZMin()
								.movez(55.4/2)
								.toYMin()
								.movey(17.2/2)
					)
					.difference(bolt
								.roty(90)
								.toZMax()
								.movez(-55.4/2)
								.toYMin()
								.movey(17.2/2)
					)
					.difference(bolt
								.roty(90)
								.toZMax()
								.movez(-55.4/2)
								.toYMax()
								.movey(-17.2/2)
					)
					.difference(bolt
								.roty(90)
								.toZMin()
								.movez(55.4/2)
								.toYMax()
								.movey(-17.2/2)
					)
		// offset the claw mount so the tip is at the kinematic center
		mountPlate=mountPlate.movex(-54.4)
		return mountPlate
	}

	private CSG getEncoderCap(){
		if(encoderCap!=null)
			return encoderCap
		double pinOffset  =gearBMeasurments.diameter/2+encoderCapRodRadius*2
		double bearingHolder = bearingDiameter/2 + encoderCapRodRadius
		CSG pin  =new Cylinder(encoderCapRodRadius,encoderCapRodRadius,encoderBearingHeight,(int)30).toCSG()
					.movex(-pinOffset)
		double angle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
		
		CSG capPinSet=pin
					.rotz(angle)
					.union(pin.rotz(-angle))
		
		CSG center  =new Cylinder(bearingHolder,bearingHolder,encoderBearingHeight,(int)30).toCSG()
		CSG pinColumn =pin .union(pin
									.movez(topPlateOffset))
								.hull() 
		CSG bottomBlock = capPinSet.union(center).hull()
						//.toZMax()
						.movez(topPlateOffset)
						.difference(encoderKeepaway
								.rotx(180)
								.movez(encoderToEncoderDistance-encoderBearingHeight)
								
								)
						.union(pinColumn.rotz(angle))
						.union(pinColumn.rotz(-angle))
		encoderCap = bottomBlock
		return encoderCap
	}
	private CSG reverseDHValues(CSG incoming,DHLink dh ){
		println "Reversing "+dh
		TransformNR step = new TransformNR(dh.DhStep(0))
		Transform move = TransformFactory.nrToCSG(step)
		return incoming.transformed(move)
	}
	
	private CSG moveDHValues(CSG incoming,DHLink dh ){
		TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
		Transform move = TransformFactory.nrToCSG(step)
		return incoming.transformed(move)
		
	}


	private add(ArrayList<CSG> csg ,CSG object, Affine dh ){
		object.setManipulator(dh);
		csg.add(object);
		BowlerStudioController.addCsg(object);
	}
};
