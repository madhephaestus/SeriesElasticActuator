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
	boolean showVitamins = false
	boolean showRightPrintedParts = true
	boolean showLeftPrintedParts = true
	
	HashMap<String , HashMap<String,ArrayList<CSG>>> map =  new HashMap<>();
	HashMap<String,ArrayList<CSG>> bodyMap =  new HashMap<>();
	LengthParameter thickness 				= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter printerOffset 			= new LengthParameter("printerOffset",0.5,[1.2,0])
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))
	StringParameter bearingSizeParam 			= new StringParameter("Encoder Board Bearing","R8-60355K505",Vitamins.listVitaminSizes("ballBearing"))
	StringParameter gearAParam 			 	= new StringParameter("Gear A","HS60T",Vitamins.listVitaminSizes("vexGear"))
	StringParameter gearBParam 				= new StringParameter("Gear B","HS60T",Vitamins.listVitaminSizes("vexGear"))
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS60T",Vitamins.listVitaminSizes("vexGear"))
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS84T",Vitamins.listVitaminSizes("vexGear"))
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS36T",Vitamins.listVitaminSizes("vexGear"))
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS12T",Vitamins.listVitaminSizes("vexGear"))
     String springType = "Torsion-9271K133"
     HashMap<String, Object>  springData = Vitamins.getConfiguration("torsionSpring",springType)
	HashMap<String, Object>  bearingData = Vitamins.getConfiguration("ballBearing",bearingSizeParam.getStrValue())			
	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	HashMap<String, Object>  gearAMeasurments = Vitamins.getConfiguration( "vexGear",gearAParam.getStrValue())
	HashMap<String, Object>  gearBMeasurments = Vitamins.getConfiguration( "vexGear",gearBParam.getStrValue())
	
	double gearDistance  = (gearAMeasurments.diameter/2)+(gearBMeasurments.diameter/2) +2.75
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double springHeight = (1+springData.numOfCoils)*(springData.wireDiameter)
	
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	//pin https://www.mcmaster.com/#98381a514/=16s6brg
	// PN: 98381a514		
	double pinRadius = ((3/16)*25.4+printerOffset.getMM())/2
	double pinLength = 1.5*25.4 + printerOffset.getMM()
	// bushing
	//https://www.mcmaster.com/#6391k123/=16s6one
	//double brassBearingRadius = ((1/4)*25.4+printerOffset.getMM())/2
	double brassBearingRadius = pinRadius
	double brassBearingLength = (5/8)*25.4
	
	double linkMaterialThickness = pinLength/2-3
	// #8x 1-5/8 wood screw
	double screwDrillHole=((0.2010*25.4)+printerOffset.getMM())/2
	double screwthreadKeepAway= ((0.2570*25.4)+printerOffset.getMM())/2
	double screwHeadKeepaway =8.6/2 + printerOffset.getMM()
	double screwLength = 41.275 //1-5/8 
	
	//Encoder Cap mesurments
	double encoderCapRodRadius =7
	double cornerRadius = 1
	double capPinSpacing = gearAMeasurments.diameter*0.75+encoderCapRodRadius
	double pinOffset  =gearBMeasurments.diameter/2+encoderCapRodRadius*2
	double mountPlatePinAngle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
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
	CSG encoderCapCache=null
	CSG encoderServoPlate=null;
	HashMap<Double,CSG> springLinkBlockLocal=new HashMap<Double,CSG>();
	HashMap<Double,CSG> sidePlateLocal=new HashMap<Double,CSG>();
	
		
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
	CSG screwHole = new Cylinder(screwDrillHole,screwDrillHole,screwLength,(int)8).toCSG() // a one line Cylinder
					.toZMax()
     CSG screwHoleKeepaway = new Cylinder(screwthreadKeepAway,screwthreadKeepAway,screwLength/2,(int)8).toCSG() // a one line Cylinder
     					.toZMax()
	CSG screwHead= new Cylinder(screwHeadKeepaway,screwHeadKeepaway,screwLength*2,(int)8).toCSG() // a one line Cylinder

	CSG screwTotal = screwHead.union([screwHoleKeepaway,screwHole])
					.movez(screwLength/2)
     CSG screwSet =screwTotal
					.movex(-pinOffset)
					.rotz(mountPlatePinAngle)
					.union(screwTotal
						.movex(-pinOffset)
						.rotz(-mountPlatePinAngle))
	
	double encoderBearingHeight = encoderSimple.getMaxZ()
	double topPlateOffset = encoderToEncoderDistance*2-encoderBearingHeight*2
	double centerLinkToBearingTop = encoderToEncoderDistance-encoderBearingHeight
	double topOfGearToCenter = (centerLinkToBearingTop-gearBMeasurments.height)
	double totalSpringLength = springData.legLength+spring.getMaxY()
	double drivenLinkThickness =centerLinkToBearingTop+topOfGearToCenter
	double drivenLinkWidth = (spring.getMaxY()*2)+encoderCapRodRadius
	double drivenLinkX = totalSpringLength+encoderCapRodRadius
	double drivenLinkXFromCenter = springData.legLength+encoderCapRodRadius
	CSG armScrews = screwTotal
					.movey(-springData.od/2+screwHeadKeepaway)
					.union(screwTotal
						.movey(springData.od/2-screwHeadKeepaway))
					.roty(-90)
					.movex(springData.legLength+encoderCapRodRadius/2)
					.movez(centerLinkToBearingTop-screwHeadKeepaway)
	CSG loadBearingPinBearing =new Cylinder(	brassBearingRadius,
										brassBearingRadius,
										drivenLinkThickness+encoderBearingHeight,
										(int)30).toCSG() 
						.toZMin()
						.movez(-pinLength/2)
	CSG loadBearingPin =new Cylinder(pinRadius,pinRadius,pinLength,(int)30).toCSG() 
						.movez(-pinLength/2)
						.union(	loadBearingPinBearing)	
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
		CSG baseShapeCutter = new Cube(basexLength+(keepAwayDistance*2)+encoderKeepawayDistance,
							baseyLength+(keepAwayDistance*2),
							topLevel)
						.toCSG()		
						.toXMax()
						.toYMin()
						.toZMin()
						.movex(keepAwayDistance+encoderKeepawayDistance)
						
		CSG baseShape = new RoundedCube(basexLength+(keepAwayDistance*2)+encoderKeepawayDistance,
							baseyLength+(keepAwayDistance*2),
							topLevel)
						.cornerRadius(cornerRadius)
						.toCSG()
						.toZMin()
						.difference([keepawayBottomY,keepawayBottomX])
		CSG screws = screwSet
					.movez(topLevel)	
		CSG screwAcross = screwTotal.rotx(90)
						.movez(topLevel/2)

		screwAcross=screwAcross.union(
				screwAcross
					.movez(topLevel/2-(keepAwayDistance/2+screwHeadKeepaway)+2)
					.movex(baseShape.getMaxX()-(keepAwayDistance/2+screwHeadKeepaway)+3)
			).union(
				screwAcross
					
					.movex(baseShape.getMinX()+(keepAwayDistance/2+screwHeadKeepaway))
			).union(
				screwAcross
					.movez(topLevel/2-(keepAwayDistance/2+screwHeadKeepaway))
					.movex(screwHeadKeepaway)
			)		
		CSG bottomScrews = screwTotal.rotx(180)
		
		CSG bottomScrewSet =bottomScrews
					.movex(baseShape.getMaxX()-(keepAwayDistance/2+screwHeadKeepaway))
					.movey(baseShape.getMaxY()-(keepAwayDistance/2+screwHeadKeepaway))
					.union(
							bottomScrews
								.movex(baseShape.getMinX()+(keepAwayDistance/2+screwHeadKeepaway))
								.movey(baseShape.getMaxY()-(keepAwayDistance/2+screwHeadKeepaway))
						)
						.union(
							bottomScrews
								.movex(baseShape.getMinX()+(keepAwayDistance/2+screwHeadKeepaway))
								.movey(baseShape.getMinY()+(keepAwayDistance/2+screwHeadKeepaway))
						)
						.union(
							bottomScrews
								.movex(baseShape.getMaxX()-(keepAwayDistance/2+screwHeadKeepaway))
								.movey(baseShape.getMinY()+(keepAwayDistance/2+screwHeadKeepaway))
						)				
		baseShape = baseShape.difference([bottomScrewSet,screwAcross])		
			
		baseShape = baseShape				
				.toXMax()
				.toYMin()
				.movey(-servoCentering-keepAwayDistance)
				.movex(keepAwayDistance+encoderKeepawayDistance)
				.difference([encoderBaseKeepaway,servoReference,screws])

				
		CSG baseCap = getEncoderCap()
					.movez(topLevel)
		
			
		CSG baseShapeA = baseShape.difference(baseShapeCutter)
						.setColor(javafx.scene.paint.Color.CYAN);
		CSG baseShapeB = baseShape.intersect(baseShapeCutter)
						.setColor(javafx.scene.paint.Color.GREEN);
		baseCap.setColor(javafx.scene.paint.Color.LIGHTBLUE);			
		baseCap.setManufacturing({ toMfg ->
				return toMfg
						.roty(180)
						.toZMin()
			})
		baseShapeB.setManufacturing({ toMfg ->
				return toMfg
						.rotx(90)
						.toZMin()
			})
		baseShapeA.setManufacturing({ toMfg ->
				return toMfg
						.rotx(-90)
						.toZMin()
			})
		
		
		
		
		if(showLeftPrintedParts)attachmentParts.add(baseShapeA)
		if(showRightPrintedParts)attachmentParts.add(baseShapeB)
		attachmentParts.add(baseCap)
		return attachmentParts;
	}
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics sourceLimb, int linkIndex) {
		//Creating the horn
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		String legStr = sourceLimb.getXml()
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(linkIndex);
		LinkConfiguration nextLink=null;
		if(linkIndex<dhLinks.size()-1){
			nextLink=sourceLimb.getLinkConfiguration(linkIndex+1);
		}
		
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
		
		DHLink dh = dhLinks.get(linkIndex);

		CSG springBlockPart = springBlock(drivenLinkThickness)
								.rotz(-Math.toDegrees(dh.getTheta()))
		CSG springBlockPartGear = springBlock(gearBMeasurments.height)
								.rotx(180)
								.rotz(-Math.toDegrees(dh.getTheta()))
		// creating the servo
		
	
	
		CSG springMoved = moveDHValues(spring
									.rotz(-Math.toDegrees(dh.getTheta()))
									//.rotz(linkIndex==0?180:0)
									,dh)
		CSG tmpMyGear = gearB
					.rotz(5)
					.movez(-centerLinkToBearingTop)
		tmpMyGear = 	tmpMyGear	
					.difference(springBlockPartGear
								.intersect(tmpMyGear)
								.hull()
					)
					.union(springBlockPartGear)
		CSG myGearB = moveDHValues(tmpMyGear
								.difference(loadBearingPin)
		,dh)
					.setColor(javafx.scene.paint.Color.LIGHTGREEN);
		CSG myPin = moveDHValues(loadBearingPin,dh)
		
		CSG myspringBlockPart = moveDHValues(springBlockPart
										.difference(loadBearingPin)
										,dh)	
							.setColor(javafx.scene.paint.Color.BROWN);
		CSG handMountPart=null;
		CSG myArmScrews = moveDHValues(armScrews
											.rotz(-Math.toDegrees(dh.getTheta()))
											,dh)
		if(linkIndex<dhLinks.size()-1){
			HashMap<String, Object> shaftmap = Vitamins.getConfiguration(nextLink.getShaftType(),nextLink.getShaftSize())
			HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(nextLink.getElectroMechanicalType(),nextLink.getElectroMechanicalSize())
			//println conf.getShaftType() +" "+conf.getShaftSize()+" "+shaftmap
			double hornOffset = 	shaftmap.get("hornThickness")	
			double servoNub = servoMeasurments.tipOfShaftToBottomOfFlange - servoMeasurments.bottomOfFlangeToTopOfBody
		
			CSG servoReference=   Vitamins.get(nextLink.getElectroMechanicalType(),nextLink.getElectroMechanicalSize())
			.transformed(new Transform().rotZ(90))
			
			double servoTop = servoReference.getMaxZ()-servoNub
							
			CSG horn = Vitamins.get(nextLink.getShaftType(),nextLink.getShaftSize())	
						.rotx(180)
						.movez(hornOffset)
						.movey(-gearDistance)
			servoReference=servoReference
				.toZMax()
				.movez(servoNub-centerLinkToBearingTop)			
				.movey(-gearDistance)
				.rotz(90+Math.toDegrees(dh.getTheta()))
			CSG myGearA = gearA.clone()	
			for(int i=0;i<2;i++){
				myGearA=myGearA
					.difference(horn
								.movez(hornOffset*i)
								)
			}
			myGearA = myGearA
						.rotz(90+Math.toDegrees(dh.getTheta()))
						.movez(-centerLinkToBearingTop)	
						.setColor(javafx.scene.paint.Color.BLUE);
			if(linkIndex==0){
				CSG baseServo =servoReference.clone()
				CSG secondLinkServo =servoReference.clone()
				CSG baseForceSenseEncoder = encoder
										.rotz(180-Math.toDegrees(dh.getTheta()))
										.rotx(180)
				CSG baseEncoder = encoder.clone()
				
				previousEncoder = baseEncoder
				previousServo = baseServo
				CSG baseMyGearA = myGearA.clone()
								.setColor(javafx.scene.paint.Color.BLUE);
				baseMyGearA.setManufacturing({ toMfg ->
					return toMfg
							.toXMin()
							.toZMin()
				})
				add(csg,baseMyGearA,sourceLimb.getRootListener())
				if(showVitamins)add(csg,baseServo,sourceLimb.getRootListener())
				if(showVitamins)add(csg,baseEncoder,sourceLimb.getRootListener())
				if(showVitamins)add(csg,baseForceSenseEncoder,sourceLimb.getRootListener())
			}
			println "Link Hardware: using from index "+
					(linkIndex+1)+
					" "+nextLink.getElectroMechanicalSize() +
					" "+nextLink.getShaftSize()
					
			CSG forceSenseEncoder = encoder
								.rotz(180-Math.toDegrees(dh.getTheta()))
								.rotx(180)
			CSG baseEncoderCap = getEncoderCap().clone()
							.movez(-centerLinkToBearingTop)
							
			CSG thirdPlusLinkServo =servoReference.clone()
			CSG linkEncoder = encoder.clone()
								.rotz(-Math.toDegrees(dh.getTheta()))
			CSG esp = getLinkSideEncoderCap(nextLink)
			double linkCconnectorOffset = drivenLinkXFromCenter-(encoderCapRodRadius+bearingDiameter)/2
			def end = [-dh.getR()+linkCconnectorOffset,dh.getD()*0.98,0]
			def controlOne = [0,end.get(1)/2,0]
			def controlTwo = [end.get(0),end.get(1)/2,end.get(2)]

			CSG connectorArmCross = new RoundedCube(cornerRadius*2,
											encoderCapRodRadius+bearingDiameter ,
											 encoderBearingHeight)
					.cornerRadius(cornerRadius)
					.toCSG()
					
			def ribs = Extrude.moveBezier(	connectorArmCross,
					controlOne, // Control point one
					controlTwo, // Control point two
					end ,// Endpoint
					
					5
					)
			CSG mountLug = new RoundedCube(encoderCapRodRadius+bearingDiameter,drivenLinkWidth,drivenLinkThickness)
						.cornerRadius(cornerRadius)
						.toCSG()
						.toXMax()
						.toZMax()
						.movez(centerLinkToBearingTop)
						.movex(drivenLinkX)
						
			mountLug = moveDHValues(mountLug.rotz(-Math.toDegrees(dh.getTheta()))
											,dh)	
			mountLug=mountLug.union(
				mountLug
					.toZMax()
					.movez(centerLinkToBearingTop+encoderBearingHeight)
				)	
			CSG supportRib = ribs.get(ribs.size()-2)
							.movez(encoderBearingHeight -cornerRadius*2)
							//.union(ribs.get(ribs.size()-2))
							.union(ribs.get(ribs.size()-1))
							.toZMin()
							.movez(centerLinkToBearingTop-encoderBearingHeight+ cornerRadius*2)
							.union(mountLug)
							.hull()
			def linkParts = Extrude.bezier(	connectorArmCross,
					controlOne, // Control point one
					controlTwo, // Control point two
					end ,// Endpoint
					
					10
					)
			print "\r\nUnioning link..."
			CSG linkSection = linkParts.get(0)
						.union(linkParts)
						//
						.toZMin()
						.movez(centerLinkToBearingTop )
						
			linkSection = 	linkSection
							.union(supportRib)
							.movex(5)// offset to avoid hitting pervious link
							.movey(-2)// offset to avoid hitting pervious link
			double xSize= (-linkSection.getMinX()+linkSection.getMaxX())
			double ySize= (-linkSection.getMinY()+linkSection.getMaxY())
			double zSize= (-linkSection.getMinZ()+linkSection.getMaxZ())
			CSG bottomCut = new Cube(xSize, ySize,zSize).toCSG()
							.toZMax()
							.toXMin()
							//.movez(myspringBlockPart.getMinZ())
			bottomCut=moveDHValues(bottomCut
											.rotz(-Math.toDegrees(dh.getTheta()))
											,dh)				
			linkSection = 	linkSection				
							.union(myspringBlockPart
									.intersect(linkSection)
									.hull())
							.difference(baseEncoderCap.hull()
										.intersect(linkSection)
										.hull())
							.difference(myArmScrews)
							.difference(bottomCut)
			print "Done\r\n"
			baseEncoderCap=baseEncoderCap.union(linkSection)
			baseEncoderCap.setColor(javafx.scene.paint.Color.LIGHTBLUE);
			esp.setColor(javafx.scene.paint.Color.ORANGE);
			previousEncoder = linkEncoder
			previousServo = thirdPlusLinkServo
			myGearA.setManufacturing({ toMfg ->
				return toMfg
						.toXMin()
						.toZMin()
			})
			baseEncoderCap.setManufacturing({ toMfg ->
				return toMfg
						.roty(180)
						.toXMin()
						.toZMin()
			})
			esp.setManufacturing({ toMfg ->
				return toMfg
						.toXMin()
						.toZMin()
			})
			
			if(showRightPrintedParts)add(csg,myGearA,dh.getListener())
			if(showVitamins)add(csg,thirdPlusLinkServo,dh.getListener())
			if(showVitamins)add(csg,linkEncoder,dh.getListener())
			if(showRightPrintedParts)add(csg,esp,dh.getListener())
			if(showLeftPrintedParts)add(csg,baseEncoderCap,dh.getListener())
			
		}else{
			// load the end of limb
			// Target point
			handMountPart = handMount()
			CSG tipCalibrationPart= tipCalibration()
			
			double plateThickenss = (-handMountPart.getMinX()+handMountPart.getMaxX())
			double platewidth  = (-handMountPart.getMinY()+handMountPart.getMaxY())
			double plateOffset = Math.abs(handMountPart.getMaxX())

			double springBlockWidth =(-myspringBlockPart.getMinY()+myspringBlockPart.getMaxY())
			double linkLength = dh.getR() -plateOffset-plateThickenss -drivenLinkXFromCenter+3
			CSG connectorArmCross = new RoundedCube(plateThickenss,platewidth,drivenLinkThickness)
					.cornerRadius(cornerRadius)
					.toCSG()
					.toXMin()
			CSG section = connectorArmCross
					.union(connectorArmCross
							.movex(linkLength )
					)
					.hull()
					.toXMax()
					.toZMin()
					.movex(-plateOffset-plateThickenss+cornerRadius*2)
			handMountPart=handMountPart
						.union(section)
			handMountPart=handMountPart
							.difference(myspringBlockPart
									.intersect(handMountPart)
									.hull())
							.difference(myArmScrews	)
			tipCalibrationPart.setColor(javafx.scene.paint.Color.PINK);
			handMountPart.setColor(javafx.scene.paint.Color.WHITE);
			
			tipCalibrationPart.setManufacturing({ toMfg ->
				return toMfg
					.rotx(90)
					.roty(90)
					.toZMin()
					.toXMin()
			})
			handMountPart.setManufacturing({ toMfg ->
				return toMfg
					.rotx(90)
					.toXMin()
					.toZMin()
			})
			if(showRightPrintedParts)add(csg,tipCalibrationPart,dh.getListener())
			if(showLeftPrintedParts)add(csg,handMountPart,dh.getListener())
		}
		
		myGearB.setManufacturing({ toMfg ->
			return reverseDHValues(toMfg,dh)
					.roty(180)
					.toZMin()
					.rotz(-Math.toDegrees(dh.getTheta()))
					.toXMin()
		})
		myspringBlockPart.setManufacturing({ toMfg ->
			return reverseDHValues(toMfg,dh)
					.toZMin()
					.rotz(-Math.toDegrees(dh.getTheta()))
					.toXMin()
		})
		if(showLeftPrintedParts)add(csg,myspringBlockPart,dh.getListener())
		if(showVitamins)add(csg,myPin,dh.getListener())
		if(showRightPrintedParts)add(csg,myGearB,dh.getListener())
		if(showVitamins)add(csg,springMoved,dh.getListener())
		return csg;
	}
	private CSG tipCalibration(){
		CSG plate = handMount()
		double plateThickenss = (-plate.getMinX()+plate.getMaxX())
		double platewidth  = (-plate.getMinY()+plate.getMaxY())
		plate=plate.movex(plateThickenss)
		CSG pyramid = new Cylinder(	platewidth/2, // Radius at the bottom
                      		0, // Radius at the top
                      		Math.abs(plate.getMaxX()), // Height
                      		(int)6 //resolution
                      		).toCSG()//convert to CSG to display 
                      		.roty(-90)
                      		.movex(- Math.abs(plate.getMaxX()))                  			 
		plate=plate.union(pyramid)
		return plate
	}
	private CSG handMount(){
		
		CSG mountPlate = new RoundedCube(8,30,70)
					.cornerRadius(cornerRadius)
					.toCSG()
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

	private CSG springBlock(double thickness){
		if(springLinkBlockLocal.get(thickness)!=null)
			return springLinkBlockLocal.get(thickness).clone()
		CSG linkBlank = new RoundedCube(drivenLinkX,drivenLinkWidth,thickness)
						.cornerRadius(cornerRadius)
						.toCSG()
						.toXMin()
						.toZMax()
						.movez(centerLinkToBearingTop)
						
						.movex(-drivenLinkWidth/2)
		CSG springCut = spring
		for(int i=1;i<springData.numOfCoils;i++){
			springCut=springCut.union(springCut.movez(-springData.wireDiameter*i))
		}
		double magnetPinDiameter = bearingData.innerDiameter/2
		CSG magnetPin = new Cylinder(magnetPinDiameter,magnetPinDiameter,encoderBearingHeight-1,(int)30).toCSG()
						.movez(linkBlank.getMaxZ())
				
		linkBlank =linkBlank
					.union(magnetPin)
					.difference(encoder.rotx(180))
					.difference([springCut])
					.difference(armScrews)
		springLinkBlockLocal.put(thickness,linkBlank)
		return linkBlank
	}
	private CSG getEncoderCap(){
		if(encoderCapCache!=null)
			return encoderCapCache
		
		double bearingHolder = bearingDiameter/2 + encoderCapRodRadius
		CSG pin  =new Cylinder(encoderCapRodRadius,encoderCapRodRadius,encoderBearingHeight,(int)30).toCSG()
					.movex(-pinOffset)
		double mountPlatePinAngle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
		
		CSG capPinSet=pin
					.rotz(mountPlatePinAngle)
					.union(pin.rotz(-mountPlatePinAngle))
		
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
						.union(pinColumn.rotz(mountPlatePinAngle))
						.union(pinColumn.rotz(-mountPlatePinAngle))
						.difference(screwSet
									.scalez(5))
		encoderCapCache = bottomBlock
		return encoderCapCache
	}	
	private CSG getLinkSideEncoderCap(LinkConfiguration conf ){
		if(sidePlateLocal.get(conf.getXml())!=null)
			return sidePlateLocal.get(conf.getXml()).clone()
		HashMap<String, Object> shaftmap = Vitamins.getConfiguration(conf.getShaftType(),conf.getShaftSize())
		HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		
		double hornOffset = 	shaftmap.get("hornThickness")	
		double servoNub = servoMeasurments.tipOfShaftToBottomOfFlange - servoMeasurments.bottomOfFlangeToTopOfBody
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
			.transformed(new Transform().rotZ(90))
			.toZMax()
			.movez(servoNub-centerLinkToBearingTop)			
			.movey(-gearDistance)
			.rotz(90)
			
		double servoTop = servoReference.getMaxZ()-servoNub
		double bearingHolder = bearingDiameter/2 + encoderCapRodRadius
		
		CSG pin  =new Cylinder(encoderCapRodRadius,encoderCapRodRadius,encoderBearingHeight,(int)30).toCSG()
					.movex(-pinOffset)
		double mountPlatePinAngle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
		
		CSG capPinSet=pin
					.rotz(mountPlatePinAngle)
					.union(pin.rotz(-mountPlatePinAngle))
		
		CSG center  =new Cylinder(bearingHolder,bearingHolder,encoderBearingHeight,(int)30).toCSG()
		
		CSG baseShape = new Cube(servoMeasurments.servoThinDimentionThickness+encoderCapRodRadius,
							servoMeasurments.flangeLongDimention+encoderCapRodRadius,
							encoderBearingHeight)
						
						.toCSG()
						.toZMin()
						.toYMin()
						.movey(-servoMeasurments.shaftToShortSideFlandgeEdge-encoderCapRodRadius/2)
						.movex(-gearDistance)
		CSG bottomBlock = capPinSet.union([center,baseShape]).hull()
						.movez(-encoderToEncoderDistance)
						.difference(encoderKeepaway)
						.difference(screwSet.movez(-encoderBearingHeight))
						.difference(servoReference)
						.difference(servoReference.movez(-2))
		sidePlateLocal.put(conf.getXml(),bottomBlock) 
		return sidePlateLocal.get(conf.getXml())
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
}