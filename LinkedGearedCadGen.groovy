import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;

import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import eu.mihosoft.vrl.v3d.Transform;
import javafx.scene.transform.Affine;
if(args==null){
	args=[	  [36,// Number of teeth gear a link 0
	            84],// Number of teeth gear b link 0
	            [36,// Number of teeth gear a link 1
	            84],// Number of teeth gear b link 1
	            [36,// Number of teeth gear a link 2
	            84],// Number of teeth gear b link 2
            ]
}

Vitamins.setGitRepoDatabase("https://github.com/madhephaestus/Hardware-Dimensions.git")
CSGDatabase.clear()
class MyCadGenerator implements ICadGenerator{
	double capPinSpacing 
	double pinOffset  
	double mountPlatePinAngle 	
	CSG screwSet
	double topOfGearToCenter
	double distanceToTopOfGear
	double drivenLinkThickness
	double gearDistance
	CSG armScrews
	CSG screwWithNut
	CSG gearScrew
	CSG LoadCellScrews 
	CSG loadBearingPinBearing 
	CSG loadBearingPin	
	//HashMap<String, Object>  gearAMeasurments 
	//HashMap<String, Object>  gearBMeasurments 
	double gearHeightValue=12.8
	CSG gearStandoff
	CSG gearKeepaway 
	CSG gearA
	CSG gearB
	def gearRatios 
	public MyCadGenerator(def args){
		
		gearRatios= args
	
		
	}
	
	public void setGearing(def ratio){
		println "\n\n\tSetting gearing to "+ratio+"\n\n"
		/*
		StringParameter gearAParam 			 	= new StringParameter("Gear A","HS"+ratio[0]+"T",Vitamins.listVitaminSizes("vexGear"))
		StringParameter gearBParam 				= new StringParameter("Gear B","HS"+ratio[1]+"T",Vitamins.listVitaminSizes("vexGear"))
		HashMap<String, Object>  gearAMeasurments = Vitamins.getConfiguration( "vexGear",gearAParam.getStrValue())
		HashMap<String, Object>  gearBMeasurments = Vitamins.getConfiguration( "vexGear",gearBParam.getStrValue())

		gearDistance  = (gearAMeasurments.diameter/2)+(gearBMeasurments.diameter/2) +2.75
		capPinSpacing = gearAMeasurments.diameter*0.75+encoderCapRodRadius
		pinOffset  =gearBMeasurments.diameter/2+encoderCapRodRadius*2
		topOfGearToCenter = (centerLinkToBearingTop-gearBMeasurments.height)
		gearA = Vitamins.get( "vexGear",gearAParam.getStrValue())
					.movex(-gearDistance)
		gearB = Vitamins.get( "vexGear",gearBParam.getStrValue());
		*/

		def bevelGears = ScriptingEngine.gitScriptRun(
		    "https://github.com/madhephaestus/GearGenerator.git", // git location of the library
		    "bevelGear.groovy" , // file to load
		    // Parameters passed to the funcetion
		    [	  ratio[0],// Number of teeth gear a
			    ratio[1],// Number of teeth gear b
			    gearHeightValue,// thickness of gear A
			    3.42303,//computeGearPitch(26.15,24),// gear pitch in arc length mm
			   60,// shaft angle, can be from 0 to 100 degrees
			    0// helical angle, only used for 0 degree bevels
		    ]
		    )
		
		gearDistance  = (bevelGears[2]/2)+(bevelGears[3]/2) +2.75
		capPinSpacing = bevelGears[2]*0.75+encoderCapRodRadius
		pinOffset  =bevelGears[3]/2+encoderCapRodRadius*2
		topOfGearToCenter = (centerLinkToBearingTop-gearHeightValue)//gearBMeasurments.height
		gearA = bevelGears[0]
		gearB = bevelGears[1].movex(gearDistance)
		
		mountPlatePinAngle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
		gearStandoff = new Cylinder(gearA.getMaxY(),gearA.getMaxY(),motorBackSetDistance+washerThickness,20).toCSG()
						.toZMax()
						.movex(-gearDistance)
						
	 	gearKeepaway = gearStandoff.toolOffset(1).getBoundingBox()

		screwSet =screwTotal
						.movex(-pinOffset)
						.rotz(mountPlatePinAngle)
						.union(screwTotal
							.movex(-pinOffset)
							.rotz(-mountPlatePinAngle))
		
		distanceToTopOfGear = topOfGearToCenter
		drivenLinkThickness =centerLinkToBearingTop+topOfGearToCenter-(washerThickness*2)
		screwWithNut = screwTotal.union(LockNutCentered.makeKeepaway(printerOffset.getMM())
									.rotx(180)
									.union(wrenchKeepaway)
									.rotz(90)
									)
									.movez(-58)
		armScrews = screwWithNut.rotz(-45)
					.movey(-screwCenterLine+screwHeadKeepaway)
					.union(screwWithNut.rotz(45)
						.movey(screwCenterLine-screwHeadKeepaway))
					.union(screwTotal
							.union(new Cylinder(boltHeadKeepaway/2,boltHeadKeepaway/2,screwLength*2,(int)8).toCSG() 
									.movez(thirdPinStandoff-drivenLinkThickness)
							)
							.movez(centerLinkToBearingTop-encoderBearingHeight)
							.movex(-thirdarmBoltBackSetDistance)
							.roty(90)
							)
					.union(screwTotal
							.movez((-centerLinkToBearingTop-encoderBearingHeight)*2)
							.union(new Cylinder(boltHeadKeepaway/2,boltHeadKeepaway/2,screwLength*2,(int)8).toCSG() 
									.movez(thirdPinStandoff-drivenLinkThickness+centerLinkToBearingTop-encoderBearingHeight)
							)
							.movex(-thirdarmBoltBackSetDistance+22)
							.roty(90)
							)
					.roty(-90)
					.movez(centerLinkToBearingTop-screwHeadKeepaway*1.5)
		 screwWithNut = screwTotal.union(LockNutCentered.makeKeepaway(printerOffset.getMM())
									.rotx(180)
									.union(wrenchKeepaway)
									.rotz(90)
									)
									.movez(-58)
		 gearScrew =screwTotal
					.union(nutAndDriverKeepaway.roty(180))
					.movez(-gearHeightValue+washerThickness+nutInsetDistance)
			
		 LoadCellScrews = gearScrew
					.movey(-screwCenterLine+screwHeadKeepaway)
					.union(gearScrew
						.movey(screwCenterLine-screwHeadKeepaway))
					.movex(loadCellBoltCenter)
					.movez(-distanceToTopOfGear+2)	
		 loadBearingPinBearing =new Cylinder(	brassBearingRadius,
									brassBearingRadius,
									drivenLinkThickness+encoderBearingHeight,
									(int)30).toCSG() 
						.toZMin()
						.movez(-pinLength/2)
		 loadBearingPin =new Cylinder(pinRadius,pinRadius,pinLength,(int)30).toCSG() 
						.movez(-pinLength/2)
						.union(	loadBearingPinBearing)	
	}
	double centerOfRobotToBackEdgeOfBoard = 222.357	
	int linkResolution = 6
	double boardX = 530*2
	double boardY = 330*2
	boolean showVitamins =false
	boolean showRightPrintedParts = true
	boolean showLeftPrintedParts = true
	int[] version = com.neuronrobotics.javacad.JavaCadBuildInfo.getBuildInfo();
	HashMap<String , HashMap<String,ArrayList<CSG>>> map =  new HashMap<>();
	HashMap<String,ArrayList<CSG>> bodyMap =  new HashMap<>();
	LengthParameter thickness 				= new LengthParameter("Material Thickness",6.1,[10,1])
	LengthParameter ballRadius = new LengthParameter("ballRadius",(1.1*25.4)/2,[20,0.001])
	LengthParameter ballCenter = new LengthParameter("ballCenter",30.72,[20,0.001])
	LengthParameter printerOffset 			= new LengthParameter("printerOffset",0.5,[1.2,0])
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M5",Vitamins.listVitaminSizes("capScrew"))
	StringParameter bearingSizeParam 			= new StringParameter("Encoder Board Bearing","R8-60355K505",Vitamins.listVitaminSizes("ballBearing"))
	
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS60T",Vitamins.listVitaminSizes("vexGear"))
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS84T",Vitamins.listVitaminSizes("vexGear"))
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS36T",Vitamins.listVitaminSizes("vexGear"))
	//StringParameter gearBParam 				= new StringParameter("Gear B","HS12T",Vitamins.listVitaminSizes("vexGear"))
     //String springType = "Torsion-9271K133"
     //HashMap<String, Object>  springData = Vitamins.getConfiguration("torsionSpring",springType)
	HashMap<String, Object>  bearingData = Vitamins.getConfiguration("ballBearing",bearingSizeParam.getStrValue())			
	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())

	double workcellSize = 700
	double cameraLocation =(workcellSize-20)/2
	TransformNR cameraLocationNR = new TransformNR(cameraLocation+20,0,cameraLocation+20,new RotationNR(0,-180,-35))
	Transform cameraLocationCSG =TransformFactory.nrToCSG(cameraLocationNR)
	
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double springHeight = 26
	double motorBackSetDistance =5
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double boltHeadThickness =boltMeasurments.headHeight
	double boltHeadKeepaway = boltMeasurments.headDiameter*1.25
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	//pin https://www.mcmaster.com/#98381a516/=19n8j9z
	// PN: 98381A516		
	double pinRadius = ((3/16)*25.4+printerOffset.getMM())/2
	double pinExtraDepth = 1.0/2.0*25.4
	double pinLength = (1.5*25.4)+pinExtraDepth + (printerOffset.getMM()*2)
	// bushing
	//https://www.mcmaster.com/#6391k123/=16s6one
	//double brassBearingRadius = ((1/4)*25.4+printerOffset.getMM())/2
	double brassBearingRadius = pinRadius
	double brassBearingLength = (5/8)*25.4
	
	double linkMaterialThickness = pinLength/2-3-(pinExtraDepth/2)
	// #8x 1-5/8 wood screw
	double screwDrillHole=((boltMeasurments.outerDiameter-1.2)+printerOffset.getMM())/2
	double screwthreadKeepAway= (boltMeasurments.outerDiameter+(printerOffset.getMM()*2))/2
	double screwHeadKeepaway =boltMeasurments.headDiameter/2 + printerOffset.getMM()
	double screwLength = 200 //1-5/8 
	
	//Encoder Cap mesurments
	double encoderCapRodRadius =7
	double cornerRadius =1
	//double capPinSpacing = gearAMeasurments.diameter*0.75+encoderCapRodRadius
	//double pinOffset  =gearBMeasurments.diameter/2+encoderCapRodRadius*2
	//double mountPlatePinAngle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
	double bearingDiameter = bearingData.outerDiameter
	double washerThickness = 2.0
	double washerOd = 17
	double washerId = 12.75
	double encoderToEncoderDistance = (springHeight/2)+linkMaterialThickness + (washerThickness*2)
	
	CSG washerInner =new Cylinder(washerId/2,washerId/2,washerThickness,(int)30).toCSG() // a one line Cylinder
	CSG washerOuter      =new Cylinder(washerOd/2,washerOd/2,washerThickness,(int)30).toCSG() // a one line Cylinder
	CSG washer = 		washerOuter.difference(washerInner)
	CSG washerWithKeepaway = washerOuter
							.difference(washerInner
										.makeKeepaway(printerOffset.getMM()*1.5
										))
	
	DHParameterKinematics neck=null;
	CSG  tmpNut= Vitamins.get( "lockNut",boltSizeParam.getStrValue())
					.rotz(30)
	
	
	
	CSG bolt = Vitamins.get( "capScrew",boltSizeParam.getStrValue());
	
	CSG previousServo = null;
	CSG previousEncoder = null
	CSG encoderCapCache=null
	CSG encoderServoPlate=null;
	HashMap<Double,CSG> springLinkBlockLocal=new HashMap<Double,CSG>();
	HashMap<Double,ArrayList<CSG>> sidePlateLocal=new HashMap<Double,ArrayList<CSG>>();
	
	
     double rectParam = 0.1*25.4
	CSG part = new Cube(rectParam,rectParam,2*rectParam).toCSG()
	def loadCellCutoutLocal = CSG.unionAll( Extrude.bezier(	part,
					[(double)20.0,(double)0.0,(double)0.0], // Control point one
					[(double)20.0,(double)0.0,(double)25.0], // Control point two
					[(double)0.0,(double)0.0,(double)(20.0+(3.0*rectParam)+4.0)], // Endpoint
					(int)10)
					).movex(-21.5)
					 .movez(-2.0-rectParam)
				
	CSG encoderSimple = (CSG) ScriptingEngine
					 .gitScriptRun(
            "https://github.com/NotOctogonapus/SeriesElasticActuator.git", // git location of the library
            "encoderBoard.groovy" , // file to load
            null// no parameters (see next tutorial)
            )
      List<CSG> nucleo = (List<CSG>) ScriptingEngine
					 .gitScriptRun(
			            "https://github.com/NotOctogonapus/SeriesElasticActuator.git", // git location of the library
			            "nucleo-144.groovy" , // file to load
			            null
			            ) 
     CSG encoderKeepaway = (CSG) ScriptingEngine
					 .gitScriptRun(
			            "https://github.com/NotOctogonapus/SeriesElasticActuator.git", // git location of the library
			            "encoderBoard.groovy" , // file to load
			            [10]// create a keepaway version
			            )
			            .union(loadCellCutoutLocal)
			            .movez(-encoderToEncoderDistance)
     CSG encoder1 =   encoderSimple.union(loadCellCutoutLocal).movez(-encoderToEncoderDistance)
	CSG screwHole = new Cylinder(screwDrillHole,screwDrillHole,screwLength,(int)8).toCSG() // a one line Cylinder
					.toZMax()
     CSG screwHoleKeepaway = new Cylinder(screwthreadKeepAway,screwthreadKeepAway,100+(washerThickness*4),(int)8).toCSG() // a one line Cylinder
     					.toZMin()
	CSG screwHead= new Cylinder(boltHeadKeepaway/2,boltHeadKeepaway/2,screwLength*2,(int)8).toCSG() // a one line Cylinder
						.movez(screwHoleKeepaway.getMaxZ())
	CSG screwChampher = new Cylinder(screwDrillHole,screwthreadKeepAway,printerOffset.getMM()*4,(int)8).toCSG() // a one line Cylinder
					.toZMax()
	CSG screwTotal = CSG.unionAll([screwHead,screwChampher,screwHoleKeepaway,screwHole])
					//.movez()
     
					//.movez(-encoderSimple.getMaxZ())
	double centerHoleRad=(20)/2
	CSG centerHole =new Cylinder(centerHoleRad,centerHoleRad,20,(int)30)
							.toCSG()
							.movez(-10)
							.roty(90)
	double loadCellwidth = 12.7
	double screwOffsetFromSides = 5
	double screwCenterLine = boltHeadKeepaway+loadCellwidth-screwOffsetFromSides
	double encoderBearingHeight = encoderSimple.getMaxZ()
	double topPlateOffset = encoderToEncoderDistance*2-encoderBearingHeight*2
	double centerLinkToBearingTop = encoderToEncoderDistance-encoderBearingHeight
	
	double totalSpringLength = 47.5
	
	double drivenLinkWidth = screwCenterLine*1.5+encoderCapRodRadius+screwOffsetFromSides
	double drivenLinkX = totalSpringLength+encoderCapRodRadius
	double legLength = totalSpringLength
	double drivenLinkXFromCenter = legLength+encoderCapRodRadius
	double loadCellBoltCenter = -(40.0-5.0-(15.0/2))
	double thirdarmBoltBackSetDistance = 16.0
	CSG	LockNutKeepaway = tmpNut.movey(thickness.getMM())
				.union(tmpNut.movey(-thickness.getMM()))
				.hull()
	CSG	LockNutCentered = LockNutKeepaway.movey(thickness.getMM())	
	double nutDriverRadius = (0.75*25.4)/2
	double nutInsetDistance =tmpNut.getMaxZ();
	CSG nutDriver = new Cylinder(nutDriverRadius,nutInsetDistance*2).toCSG()
	CSG nutAndDriverKeepaway = nutDriver.union(tmpNut.movez(-1))
	
	CSG wrenchKeepaway=new Cube(20,40,6).toCSG()
					.toYMax()
					.movey(3)
					.toZMax()
					.movez(printerOffset.getMM())
					.union(tmpNut
							.makeKeepaway(printerOffset.getMM())
							.rotx(180)	
							.movez(1)
							)
	
	double thirdPinStandoff =37								
	
	// Loading the gear bolts for the load cell
	
	
									

	CSG loadCell = (CSG) ScriptingEngine
					 .gitScriptRun(
            "https://github.com/NotOctogonapus/SeriesElasticActuator.git", // git location of the library
            "loadCell.groovy" , // file to load
            null// no parameters (see next tutorial)
            )
            .rotx(-90)
            //.movey(-drivenLinkWidth/2)
            .movez(1)
            
     CSG standoffBLock=null
     ArrayList<CSG> manipulationParts = []
     CSG manipulationBall=null
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
		setGearing(gearRatios[0])
		
		ArrayList<CSG> attachmentParts = new ArrayList<CSG>()
		double maxz = 0.001
		
		for(DHParameterKinematics l:getLimbDHChains(base)){
			double thisZ = l.getRobotToFiducialTransform().getZ()
			if(thisZ>maxz)
				maxz=thisZ
		}
		DHParameterKinematics sourceLimb=base.getAppendages() .get(0)
	
		TransformNR step = sourceLimb.calcHome();
		
		Transform tipatHome = TransformFactory.nrToCSG(step)
		
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(0);
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		DHLink dh = dhLinks.get(0);
		HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		double totalFlangLen = (servoMeasurments.flangeLongDimention-servoMeasurments.servoThickDimentionThickness)/2
		double shaftToShortSideFlandgeEdge = servoMeasurments.shaftToShortSideDistance+totalFlangLen
		
		LengthParameter tailLength		= new LengthParameter("Cable Cut Out Length",maxz,[500,0.01])
		tailLength.setMM(maxz)
		//CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		//						.rotz(180+Math.toDegrees(dh.getTheta()))
		//						.movez(-motorBackSetDistance)
		
		double servoNub = servoMeasurments.tipOfShaftToBottomOfFlange - servoMeasurments.bottomOfFlangeToTopOfBody
		//double servoTop = servoReference.getMaxZ()-servoNub
		double topLevel = maxz -(springHeight/2)-linkMaterialThickness +encoderBearingHeight-(washerThickness*2)
		double servoPlane = topLevel - servoMeasurments.bottomOfFlangeToTopOfBody
		double servoEncoderPlane = topLevel - encoderBearingHeight
		double basexLength = gearDistance + servoMeasurments.servoThinDimentionThickness/2
		//double baseyLength = servoMeasurments.flangeLongDimention 
		double servoCentering = servoMeasurments.flangeLongDimention -shaftToShortSideFlandgeEdge
		double minimumWidth = (capPinSpacing-encoderCapRodRadius-cornerRadius)
		if(servoCentering<minimumWidth)
			servoCentering=minimumWidth
		double baseyLength = servoCentering*2
		double keepAwayDistance =10
		CSG gearHole = gearKeepaway
					.movez(topLevel+washerThickness)
		//servoReference=servoReference
		//			.movez(servoPlane)
		//			.movex(-gearDistance)

		CSG baseServo = Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
				.rotz(180)
				.movez(maxz-centerLinkToBearingTop-encoderBearingHeight)		
				.movex(-gearDistance)
				
		CSG encoderBaseKeepaway = (CSG) ScriptingEngine
					 .gitScriptRun(
			            "https://github.com/NotOctogonapus/SeriesElasticActuator.git", // git location of the library
			            "encoderBoard.groovy" , // file to load
			            [topLevel+5]// create a keepaway version
			            )
			            
			            .movez(servoEncoderPlane)
		double encoderKeepawayDistance= 20
		double sidePlateclearenceHeight = 10*24.5
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
						.difference([keepawayBottomY])
		/*
		CSG sidePlate = new Cube( basexLength+(keepAwayDistance*2)+encoderKeepawayDistance,
							1.0,
							topLevel)
							.toCSG()
							.movey((baseyLength+(keepAwayDistance*2))/2)
							.toZMin()
		sidePlate=sidePlate
				.union(sidePlate.movez(sidePlateclearenceHeight))
				.union(sidePlate.movez(-20*25.4))
				.hull()
		CSG strapSlot  = 	new Cube(5,
								(baseyLength+(keepAwayDistance*2))*2,
								25.4
								).toCSG()	
								.toXMax()
								.movex((basexLength+(keepAwayDistance*2)+encoderKeepawayDistance)/2 - 10)
		CSG strapSlots = strapSlot
		for(int i=1;i<13;i++){
			strapSlots=strapSlots.union(strapSlot.movez(-38*i))
		}
		sidePlate=sidePlate.difference(strapSlots)	
		*/							
		CSG screws = screwSet
					//.movez(topLevel)
						
		CSG screwAcross = screwTotal
						.movez(-topLevel)
						.rotx(90)
						.movez(topLevel/2)

		screwAcross=CSG.unionAll([
			//screwAcross,// middle bolt
			screwAcross.movez(topLevel/2-(keepAwayDistance/2+screwHeadKeepaway))// Frontmost bolt
					 .movex(baseShape.getMaxX()-(keepAwayDistance/2+screwHeadKeepaway)-7-printerOffset.getMM()),
			//screwAcross.movex(baseShape.getMinX()+(keepAwayDistance/2+screwHeadKeepaway)), Back most bolt
			screwAcross.movez(topLevel/2-(keepAwayDistance/2+screwHeadKeepaway))
						.movex(screwHeadKeepaway)			
		])
		CSG bottomScrews = screwTotal.rotx(180)
						.union(tmpNut
							.toZMax()
							.movez(-10)
							.union(tmpNut)
							.hull()
							.makeKeepaway(printerOffset.getMM())
							)
		// Originally calculated, fixed to make sure maunfactured parts mesh
		//Bottom Bolt hole pattern x= 97.78700180053711 y = 77.5 inset = 9.75
		//double inset = (keepAwayDistance/2+screwHeadKeepaway)
		//double screwX = baseShape.toXMin().getMaxX()-(inset*2)
		//double screwY = baseShape.toYMin().getMaxY()-(inset*2)
		double inset =  9.75
		double screwX = 97.78700180053711
		double screwY = 77.5	
		
		double distanceFromCenterToBolt = 	baseShape.getMaxX() - inset		
		println "Bottom Bolt hole pattern x total= "+screwX+
		" y total= "+screwY+
		" inset from edge= "+inset+
		" Center to forward Robot Bolts=" +(distanceFromCenterToBolt/2)+
		" center distance to calibration hole ="+tipatHome.getX()
		
		CSG bottomScrewSet =bottomScrews
					.movex(-screwX)
					.movey(screwY/2)
					.union(
						bottomScrews
							.movex(-screwX)
							.movey(-screwY/2)
					)
					.union(
						bottomScrews
							.movey(screwY/2)
					)
					.union(
						bottomScrews
							.movey(-screwY/2)
					)
					.movex(distanceFromCenterToBolt)
					.movez(topLevel)				
		baseShape = baseShape.difference([bottomScrewSet,screwAcross])	
		double boardKeepaway =50
		CSG nucleoBoard = nucleo.get(0).movey(-boardKeepaway)
		double baseBackSet = 	-baseShape.getMaxX()+keepAwayDistance+encoderKeepawayDistance
		double spacing = 75

		double nucleoMountPlacement = spacing+baseBackSet+nucleoBoard.getMaxX()
		CSG boxCut = new Cube((workcellSize/2)+nucleoMountPlacement+boardKeepaway,workcellSize,thickness.getMM()*2).toCSG()
					.toXMax()
					.movex(workcellSize/2)
		manipulationParts.clear()
		def parts =(ArrayList<CSG>)ScriptingEngine
					 .gitScriptRun(
            "https://github.com/WPIRoboticsEngineering/RBELabCustomParts.git", // git location of the library
            "3001TrackingObjects.groovy" , // file to load
            null// no parameters (see next tutorial)
            ) 
          manipulationBall=parts.get(0)
		          		.movez(-ballCenter.getMM())
		            		.roty(90) 
		manipulationParts.addAll(parts.collect{
            	it.movez(-ballCenter.getMM())
            	.roty(90)            	
			.transformed(tipatHome)
            })
		
		//CSG tipHole =new Cylinder(10,10,thickness.getMM()*3,(int)90).toCSG()
		//			.movez(-thickness.getMM()*1.5)
					
		double footingWidth = 330
		double insetForMakerbeam =15
		CSG footing =new Cube(workcellSize,footingWidth,thickness.getMM()).toCSG()
					.toXMin()
					.movex(-centerOfRobotToBackEdgeOfBoard)
		CSG hole =  new Cylinder(1.5, // Radius at the bottom
                      		1.5, // Radius at the top
                      		thickness.getMM()*4, // Height
                      		(int)30 //resolution
                      		).toCSG()//convert to CSG to display 
                      		.movez(thickness.getMM()*-2)
         	def holeset =  hole.movey(  footingWidth/2-insetForMakerbeam)
         				.union(hole.movey(  -footingWidth/2+insetForMakerbeam))  
         	def allSideHoles=holeset
         	for(int i= -workcellSize+insetForMakerbeam;i< workcellSize-insetForMakerbeam;i+=   workcellSize/6){
         		 //allSideHoles= allSideHoles.union(	holeset.movex(i))	
         	}
		double etchWith =0.5
		//etchX = (workcellSize/2)-2
		//CSG etchX =new Cube((workcellSize/2)-2,etchWith,thickness.getMM()).toCSG()
		//			.toXMin()
		//CSG etchY =new Cube(etchWith,footingWidth-2,thickness.getMM()).toCSG()
		def etchParts = []
		double gridDimention =25
		
		CSG etch =new Cube(10,10,thickness.getMM()).toCSG()
					.toXMin()
					.toYMin()

		etch=etch
		int x =0

		
		//def cameraParts = getCameraMount()
		CSG basePlate = footing
						.toZMax()
						.difference(nucleoBoard
									.rotz(90)
							.movey(nucleoBoard.getMaxX()/2)
							.movex(-spacing-nucleoBoard.getMaxX()/2)
							.movex(baseBackSet)
						)
						.difference(	bottomScrewSet.movex(baseBackSet))
						.intersect(boxCut)
						.difference(manipulationParts)
						//.difference(cameraParts)
						.difference(etchParts)
						//.union(allSideHoles)
		
		def basePlateUpper=basePlate.intersect(footing)
		def basePlateLower=basePlate.difference(footing)	/*
		CSG sidePlateA=sidePlate
				.difference(screwAcross)
				.difference(screwAcross.movez(sidePlateclearenceHeight))
				.movex(baseBackSet)
		CSG sidePlateB = sidePlateA
						.movey(-(baseyLength+(keepAwayDistance*2)))
						*/
		baseShape = baseShape				
				.toYMin()
				.movey(-servoCentering-keepAwayDistance)
				.movex(baseBackSet)
				.difference([encoderBaseKeepaway,baseServo,screws,gearHole])

				
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
		
		basePlateUpper.setManufacturing({ toMfg ->
				p= toMfg
						.toXMin()
						.toYMin()
						.toZMin()
				p.addExportFormat("svg")
				//Transform t=new Transform()
				return p
			})
		basePlateLower.setManufacturing({ toMfg ->
				p= toMfg
						.toXMin()
						.toYMin()
						.toZMin()
				p.addExportFormat("svg")
				//Transform t=new Transform()
				return p
			})
		
		/*
		sidePlateA.setManufacturing({ toMfg ->
				return toMfg
						.rotx(-90)
						.toZMin()
						.toXMin()
						.movex(basePlate.getMaxX())
			})
		sidePlateB.setManufacturing({ toMfg ->
			return toMfg
					.rotx(-90)
					.toZMin()
					.toXMin()
					.movex(basePlate.getMaxX())
		})
		*/
		

		
		//if(showRightPrintedParts)attachmentParts.add(sidePlateA)
		//if(showRightPrintedParts)attachmentParts.add(sidePlateB)
		//if(showLeftPrintedParts)attachmentParts.add(baseShapeA)
		//if(showRightPrintedParts)attachmentParts.add(baseShapeB)
		//if(showRightPrintedParts)attachmentParts.add(basePlate)
		//if(showLeftPrintedParts)attachmentParts.add(baseCap)
//		for (int i=0;i<cameraParts.size();i++){
//			CSG p = cameraParts.get(i)
//			p.addExportFormat("svg")
			
//		}
		//cameraParts.forEach{
		//	it.addExportFormat("svg")
		//	add(attachmentParts,it,null,"cameraStand_SVG")
		//}
		basePlateUpper.addExportFormat("svg")
		basePlateLower.addExportFormat("svg")
		add(attachmentParts,baseShapeA,null,"baseLeft")
		add(attachmentParts,baseShapeB,null,"baseRight")
		add(attachmentParts,basePlateUpper,null,"basePlateEtching_SVG")
		add(attachmentParts,basePlateLower,null,"basePlateCut_SVG")
		add(attachmentParts,baseCap,null,"baseCap")
		add(attachmentParts,manipulationParts.get(0),null,"colorObject")
		add(attachmentParts,manipulationParts.get(1),null,"coaster")
		add(attachmentParts,manipulationParts.get(2),null,"calibrationObject")
		return attachmentParts;
	}
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics sourceLimb, int linkIndex) {
		
		
		def parts =(ArrayList<CSG>)ScriptingEngine
					 .gitScriptRun(
            "https://github.com/WPIRoboticsEngineering/RBELabCustomParts.git", // git location of the library
            "3001TrackingObjects.groovy" , // file to load
            null// no parameters (see next tutorial)
            ) 
          manipulationBall=parts.get(0)
		          		.movez(-ballCenter.getMM())
		            		.roty(90) 
		//return new ArrayList<CSG>()
		//Creating the horn
		ArrayList<DHLink> dhLinks=sourceLimb.getChain().getLinks();
		String legStr = sourceLimb.getXml()
		LinkConfiguration conf = sourceLimb.getLinkConfiguration(linkIndex);
		LinkConfiguration nextLink=null;
		if(linkIndex<dhLinks.size()-1){
			nextLink=sourceLimb.getLinkConfiguration(linkIndex+1);
		}
			
		setGearing(gearRatios[linkIndex])
		
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
		CSG springBlockPartRaw=springBlock(drivenLinkThickness)
		CSG springBlockPart =springBlockPartRaw
								.rotz(-Math.toDegrees(dh.getTheta()))
		CSG springBlockPartGear = springBlockPin(gearHeightValue)
								.rotx(180)
								.rotz(-Math.toDegrees(dh.getTheta()))
		// creating the servo
		
	
		
		
		CSG springMoved = moveDHValues(loadCell
            							//.movez(washerThickness)
									.rotz(-Math.toDegrees(dh.getTheta()))
									.movez(springBlockPart.getMinZ())
									//.rotz(linkIndex==0?180:0)
									,dh)
		CSG washerMoved = washerWithKeepaway
					.movez(centerLinkToBearingTop-washerThickness)
		CSG tmpMyGear = gearB
					.rotz(5)
					.union(washer.toZMax())
					.movez(-centerLinkToBearingTop+washerThickness)
		tmpMyGear = 	tmpMyGear	
					.difference(springBlockPartGear
								.intersect(tmpMyGear)
								.hull()
					)
					.union(springBlockPartGear)
		CSG loadCellBolts = moveDHValues(LoadCellScrews
							.rotz(-Math.toDegrees(dh.getTheta()))
								,dh)	
		CSG myArmScrews = moveDHValues(armScrews
						.rotz(-Math.toDegrees(dh.getTheta()))
						,dh)
   						.movex(springBlockPartRaw.getMaxX())
		CSG myGearB = moveDHValues(tmpMyGear
								.difference(loadBearingPin)
								,dh)
					.difference(loadCellBolts)
					.difference(myArmScrews)
					.setColor(javafx.scene.paint.Color.LIGHTGREEN);
		CSG myPin = moveDHValues(loadBearingPin,dh)
		
		CSG myspringBlockPart = moveDHValues(springBlockPart
		
										.difference(loadBearingPin)
										,dh)	
							.difference(loadCellBolts)
							.difference(myGearB)
							.setColor(javafx.scene.paint.Color.BROWN);
							
		CSG handMountPart=null;
		
		if(linkIndex<dhLinks.size()-1){
			//HashMap<String, Object> shaftmap = Vitamins.getConfiguration(nextLink.getShaftType(),nextLink.getShaftSize())
			//HashMap<String, Object> servoMeasurments = Vitamins.getConfiguration(nextLink.getElectroMechanicalType(),nextLink.getElectroMechanicalSize())
			//println conf.getShaftType() +" "+conf.getShaftSize()+" "+shaftmap
			CSG servoReference=   Vitamins.get(nextLink.getElectroMechanicalType(),nextLink.getElectroMechanicalSize())
			.rotz(90)
			
			double servoTop = servoReference.getMaxZ()
			servoReference=servoReference
				.movez(-centerLinkToBearingTop-encoderBearingHeight)		
				.movex(-gearDistance)
				//.rotz(90+Math.toDegrees(dh.getTheta()))
			double gearPlacementVSMotor = -(motorBackSetDistance+washerThickness)

			def myGearA = getGearWithSpline( nextLink );
			if(linkIndex==0){
				CSG baseServo = Vitamins.get(nextLink.getElectroMechanicalType(),nextLink.getElectroMechanicalSize())
							.rotz(180)
							.movez(-centerLinkToBearingTop-encoderBearingHeight)		
							.movex(-gearDistance)
				CSG secondLinkServo =servoReference.clone()
				CSG baseForceSenseEncoder = encoder1
										
				CSG baseEncoder = encoder1
				
				previousEncoder = baseEncoder
				previousServo = baseServo
				
				CSG baseMyGearA = getGearWithSpline( conf )
								.setColor(javafx.scene.paint.Color.BLUE);
				baseMyGearA.setManufacturing({ toMfg ->
					return toMfg
							.toXMin()
							.toZMin()
				})
				add(csg,baseMyGearA,sourceLimb.getRootListener(),"servoGear")
				if(showVitamins)add(csg,baseServo,sourceLimb.getRootListener(),"servo")
				if(showVitamins)add(csg,baseEncoder,sourceLimb.getRootListener(),"encoder")
				if(showVitamins)add(csg,baseForceSenseEncoder,sourceLimb.getRootListener(),"encoder")
			}
			println "Link Hardware: using from index "+
					(linkIndex+1)+
					" "+nextLink.getElectroMechanicalSize() +
					" "+nextLink.getShaftSize()
					
			CSG forceSenseEncoder = encoder1
								
								.rotz(180-Math.toDegrees(dh.getTheta()))
								.rotx(180)
			CSG baseEncoderCap = getEncoderCap()
							.movez(-centerLinkToBearingTop)
							
			CSG thirdPlusLinkServo =servoReference.clone()
			CSG linkEncoder = encoder1
								
								.rotz(-Math.toDegrees(dh.getTheta()))
			ArrayList<CSG> esp = getServoCap(nextLink)
			double linkCconnectorOffset = drivenLinkXFromCenter-(encoderCapRodRadius+bearingDiameter)/2
			def end = [(double)-dh.getR()+linkCconnectorOffset,(double)dh.getD()*0.98,(double)0]
			def controlOne = [(double)-2 ,(double)end.get(1)*0.8,(double)0]
			def controlTwo = [(double)end.get(0),(double)0,(double)end.get(2)*1.1]

			CSG connectorArmCross = new RoundedCube(cornerRadius*2,
											encoderCapRodRadius+bearingDiameter -cornerRadius-5,
											 encoderBearingHeight)
					.cornerRadius(cornerRadius)
					.toCSG()
					
			def ribs = Extrude.moveBezier(	connectorArmCross,
					controlOne, // Control point one
					controlTwo, // Control point two
					end ,// Endpoint
					
					(int)5
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
			CSG bounding = ribs.get(ribs.size()-2).getBoundingBox()
			/*
CSG supportRib = ribs.get(ribs.size()-2)
							//.intersect(bounding.movex(-5))
							//.intersect(bounding.movex(5))
							.movez(encoderBearingHeight -cornerRadius*2)
							//.union(ribs.get(ribs.size()-2))
							.union(ribs.get(ribs.size()-1)
							*/
			CSG upperRib = ribs.get(ribs.size()-2)
						.movez(encoderBearingHeight -cornerRadius*2)
						//.movex(-2)
						.movey(12)
			CSG supportRib = ribs.get(ribs.size()-1)
							.union(upperRib)
							.toZMin()
							.movez(centerLinkToBearingTop-encoderBearingHeight+ cornerRadius*2)
							.union(mountLug)
							.hull()
							.movex(5)// offset to avoid hitting pervious link
							//.movey(-1)// offset to avoid hitting pervious link
							//.movez(-2)// adjust support to met previous gear
			def linkParts = Extrude.bezier(	connectorArmCross,
					controlOne, // Control point one
					controlTwo, // Control point two
					end ,// Endpoint
					linkResolution
					)
			print "\r\nUnioning link..."
			long start = System.currentTimeMillis()
			CSG bracketBezier = CSG.unionAll(linkParts)
						.toZMin()
						.movez(centerLinkToBearingTop )
							.movex(5)// offset to avoid hitting pervious link
							.movey(-2)// offset to avoid hitting pervious link

			CSG sidePlateWithServo =esp.get(0)							
			
			CSG linkSection = bracketBezier
						
						
			linkSection = 	linkSection
							.union(supportRib)
			double xSize= (-linkSection.getMinX()+linkSection.getMaxX())
			double ySize= (-linkSection.getMinY()+linkSection.getMaxY())
			double zSize= (-linkSection.getMinZ()+linkSection.getMaxZ())
			CSG bottomCut = new Cube(xSize*2, ySize*2,zSize).toCSG()
							.toZMax()
							.toXMin()
							.movez(springBlockPart.getMinZ()+cornerRadius)
			bottomCut=moveDHValues(bottomCut
											.rotz(-Math.toDegrees(dh.getTheta()))
											,dh)	
			CSG otherEncoder = linkEncoder.rotx(180)			
			//if(linkIndex==0){
				CSG rightSIdeSupport =bracketBezier
									.toZMin()
									.movez(sidePlateWithServo.getMinZ())	
									.union([supportRib
											.mirrorz()
											.movex(5)
											]
											)
									.difference(sidePlateWithServo.hull())
									.minkowskiDifference(
										springBlockPart.hull(),// the part we want to fit into a cutout
										6.0// the offset distance to fit
										)
									.minkowskiDifference(
										myGearB.hull(),// the part we want to fit into a cutout
										6.0// the offset distance to fit
										)
				sidePlateWithServo =sidePlateWithServo	
					.union(rightSIdeSupport)		
					.difference(linkEncoder)
					//.difference(baseEncoderCap)	
					.difference([springBlockPart,
							tmpMyGear,myGearB])	
					.difference(myArmScrews)
					.difference(springMoved)
					//.difference(bottomCut)
					.difference(linkSection)
			//}
			linkSection = 	linkSection				
				.difference(myspringBlockPart
						.intersect(linkSection)
						.hull()
						.toolOffset(printerOffset.getMM()))
				.difference(baseEncoderCap//.hull()
							.intersect(linkSection)
							.hull()
							)
				.difference(otherEncoder.rotz(180)
							.intersect(linkSection)
							)	
				.difference(	springBlockPart)	
				.difference(myArmScrews)
				.difference(springMoved)
				.difference(bottomCut)
				//.difference(loadCellBolts)
			double took = System.currentTimeMillis()-start
			print "Done, took "+(took/1000.0) +" seconds\r\n"
			baseEncoderCap=baseEncoderCap.union(linkSection)
			baseEncoderCap.setColor(javafx.scene.paint.Color.LIGHTBLUE);
			sidePlateWithServo.setColor(javafx.scene.paint.Color.ORANGE);
			if(esp.size()>1)esp.get(1).setColor(javafx.scene.paint.Color.WHITE);
			previousEncoder = linkEncoder
			previousServo = thirdPlusLinkServo

			double chipToShortside = 8
			double chipToLongSide  = 9.0
			double mountHoleRadius = 2.0/2  
			double chipToShortsideReal = 5.5
			double chipToLongSideReal  = 9.0 
			
			double standoffHeight = 6
			if( standoffBLock==null){
				CSG bolt =new Cylinder(3,3,standoffHeight,(int)20).toCSG() // a one line Cylinder
								           
				CSG boltSet = bolt
						.movex(chipToLongSideReal)
						.movey(chipToShortsideReal)
						
				.union(bolt
						.movex(-chipToLongSideReal)
						.movey(chipToShortsideReal)
						)
				.union(bolt
						.movex(chipToLongSideReal)
						.movey(-chipToShortsideReal)
						)
				.union(bolt
						.movex(-chipToLongSideReal)
						.movey(-chipToShortsideReal)
						) 
				
				print "Creating standoff block..."		    
			     standoffBLock = new Cube((chipToLongSide+mountHoleRadius*1.5)*2.2,(chipToShortside+mountHoleRadius*1.5)*2.2,	2).toCSG()	
			     					.toZMin()
			     					.union(boltSet )
			     					.union(boltSet.rotz(90))
			     					.movez(encoderToEncoderDistance)
			     standoffBLock = standoffBLock
			     					.difference(otherEncoder)
			     					.difference(springBlockPart.hull())
			     					.setColor(javafx.scene.paint.Color.GREY);
			     print "done\n"	
			}
			CSG myStandoff= standoffBLock.clone()
			washerMoved.setManufacturing({ toMfg ->
				return toMfg
						.toXMin()
						.toYMin()
						.toZMin()
			})
		     myStandoff.setManufacturing({ toMfg ->
				return toMfg
						.toXMin()
						.toYMin()
						.toZMin()
			})
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
			sidePlateWithServo.setManufacturing({ toMfg ->
				return toMfg
						.toXMin()
						.toZMin()
			})
			if(esp.size()>1)
			esp.get(1).setManufacturing({ toMfg ->
				return toMfg
						.roty(180)
						.toXMin()
						.toZMin()
			})

			add(csg,washerMoved,dh.getListener(),"washer")
			add(csg,myStandoff,dh.getListener(),"encoderStandoff")
			if(showRightPrintedParts)add(csg,myGearA,dh.getListener(),"servoGear")
			if(showVitamins)add(csg,thirdPlusLinkServo,dh.getListener(),"servo")
			if(showVitamins)add(csg,linkEncoder,dh.getListener(),"encoder")
			//if(showVitamins)add(csg,horn,dh.getListener(),"horn")
			if(showVitamins)add(csg,otherEncoder,dh.getListener(),"otherEncoder")
			if(showRightPrintedParts)add(csg,sidePlateWithServo,dh.getListener(),"sidePlate"+linkIndex)
			if(esp.size()>1)if(showLeftPrintedParts)add(csg,esp.get(1),dh.getListener(),"encoderPlate"+linkIndex)
			if(showLeftPrintedParts)add(csg,baseEncoderCap,dh.getListener(),"baseEncoderCap"+linkIndex)
			
		}else{
			// load the end of limb
			// Target point
			handMountPart = handMount()
			CSG tipCalibrationPart= tipCalibration()
			File gripBaseFile = ScriptingEngine.fileFromGit(
				"https://github.com/NotOctogonapus/SeriesElasticActuator.git",
				"gripper/all.stl");
				/*
			File gripLeftFile = ScriptingEngine.fileFromGit(
				"https://github.com/NotOctogonapus/SeriesElasticActuator.git",
				"gripper/left.stl");
			// Load the .CSG from the disk and cache it in memory
			File gripRightFile = ScriptingEngine.fileFromGit(
				"https://github.com/NotOctogonapus/SeriesElasticActuator.git",
				"gripper/right.stl");
				*/
			// Load the .CSG from the disk and cache it in memory
			double boltPlateThickness = 6
			CSG cup =new Cylinder(ballRadius.getMM()+2,15).toCSG() // a one line Cylinder
			CSG box = cup.getBoundingBox()
					.movez(7)
					.movex(2.5)
			CSG bolt =new Cylinder(4.3/2,70).toCSG() // a one line Cylinder		
			bolt = bolt.union (bolt.movex(-12))
					.movex(-ballRadius.getMM()-8.75)
			
			def mounStrip = new Cube(	40,// X dimention
						13.2,// Y dimention
						boltPlateThickness//  Z dimention
						).toCSG()
						.toXMax()
						.toZMax()
						.movez(cup.getMaxZ())
			cup = cup.intersect(box)
					.union(mounStrip )
					.difference(manipulationBall.makeKeepaway(1))
					.difference(bolt)
			
			//CSG gripBase  = Vitamins.get(gripBaseFile)	
			//				.movex(-56.75-ballRadius.getMM()-8+ballCenter.getMM())
			//				.movez(-2.25)		
			//				.movey(-3.4)	
			//CSG gripLeft  = Vitamins.get(gripLeftFile)
			//CSG gripRight  = Vitamins.get(gripRightFile)
			
			
						//.union([gripLeft,gripRight])
						
			
			double plateThickenss = (-handMountPart.getMinX()+handMountPart.getMaxX())
			double platewidth  = (-handMountPart.getMinY()+handMountPart.getMaxY())
			double plateOffset = Math.abs(handMountPart.getMaxX())

			double springBlockWidth =(-myspringBlockPart.getMinY()+myspringBlockPart.getMaxY())
			double linkLength = dh.getR() -plateOffset-plateThickenss -drivenLinkXFromCenter+8
			CSG connectorArmCross = new RoundedCube(plateThickenss,drivenLinkWidth,drivenLinkThickness)
					.cornerRadius(cornerRadius)
					.toCSG()
					.toXMin()
			CSG screwHead= new Cylinder(boltHeadKeepaway/1.5,boltHeadKeepaway/1.5,drivenLinkThickness,(int)20).toCSG()
							.movez(-drivenLinkThickness/2)
							.movex(-thirdarmBoltBackSetDistance/1.5)
			CSG section = connectorArmCross
					.union(connectorArmCross
							.movex(linkLength )
					)
					//.union(screwHead)
					.hull()
					.toXMax()
					.toZMin()
					.movex(-plateOffset-plateThickenss+cornerRadius*2)
					.movez(-topOfGearToCenter)
					.movez(washerThickness)
			handMountPart=handMountPart
						.union(section)
			try{
				handMountPart=handMountPart
								.difference(myspringBlockPart
										.intersect(handMountPart)
										.hull()
										.toolOffset(printerOffset.getMM()*2))
								.difference(myArmScrews.movex(-45),springMoved.toolOffset(2)	)
			}catch(Exception ex){
				//BowlerStudio.printStackTrace(ex)
			}				
			tipCalibrationPart.setColor(javafx.scene.paint.Color.PINK);
			handMountPart.setColor(javafx.scene.paint.Color.WHITE);
			
			handMountPart.setManufacturing({ toMfg ->
				return toMfg
					.rotx(90)
					.toXMin()
					.toZMin()
			})
			def cupr=cup.mirrorz()
			cup.setManufacturing({ toMfg ->
				return toMfg
					.rotx(180)
					.toZMin()
			})
			cupr.setManufacturing({ toMfg ->
				return toMfg
					.toZMin()
			})
			//if(showRightPrintedParts)add(csg,tipCalibrationPart,dh.getListener(),"calibrationTip")
			if(showLeftPrintedParts)add(csg,handMountPart,dh.getListener(),"lastLink")
			//add(csg,gripBase,dh.getListener(),"gripBase")
			//add(csg,manipulationBall,dh.getListener(),"gripBall")
			add(csg,cup,dh.getListener(),"cup")
			add(csg,cupr,dh.getListener(),"cupr")
			//add(csg,gripLeft,dh.getListener(),"gripLeft")
			//add(csg,gripRight,dh.getListener(),"gripRight")
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
		if(showLeftPrintedParts)add(csg,myspringBlockPart,dh.getListener(),"loadCellBlock")
		if(showVitamins)add(csg,myPin,dh.getListener(),"pin")
		if(showRightPrintedParts)add(csg,myGearB,dh.getListener(),"drivenGear")
		if(showVitamins)add(csg,springMoved,dh.getListener(),"loadCell")
		return csg;
	}
	private CSG tipCalibration(){
		CSG plate = handMount()
		double plateThickenss = (-plate.getMinX()+plate.getMaxX())
		double platewidth  = (-plate.getMinY()+plate.getMaxY())
		plate=plate.movex(plateThickenss)
		double sphereSize = 20
		//centerHoleRad
		CSG centerHole =new Cylinder(1,1,Math.abs(plate.getMaxX())+20,(int)30)
							.toCSG()
							.roty(-90)
							.movex(- Math.abs(plate.getMaxX()))    
		CSG cableTieHole =   new Cylinder(1,sphereSize/2,sphereSize/2,(int)30)
							.toCSG()
							.roty(-90)
							//.movex(- Math.abs(plate.getMaxX())) 
		CSG pyramid = new Cylinder(	platewidth/2, // Radius at the bottom
                      		14, // Radius at the top
                      		Math.abs(plate.getMaxX()), // Height
                      		(int)6 //resolution
                      		).toCSG()//convert to CSG to display 
                      		.roty(-90)
                      		.movex(- Math.abs(plate.getMaxX()))      
          CSG tipSphere = new Sphere(sphereSize/2,(int)40,(int)40).toCSG()            			 
		plate=plate.union([pyramid,tipSphere])
				.difference(centerHole)
				.difference(cableTieHole)
		return plate
	}
	private CSG handMount(){
		
		CSG mountPlate = new RoundedCube(8,drivenLinkWidth,70)
					.cornerRadius(cornerRadius)
					.toCSG()

		HashMap<String, Object>  boltData = Vitamins.getConfiguration( "capScrew","M3")								
		CSG handBolt = Vitamins.get( "capScrew","M3");
		double boltCenterLong = 55.4+boltData.outerDiameter
		double boltShortDistance = 17.2+boltData.outerDiameter
		mountPlate=mountPlate
					.toXMin()
					.difference(centerHole)
					.difference(handBolt
								.roty(90)
								.movez(boltCenterLong /2)
								.movey(boltShortDistance/2)
					)
					.difference(handBolt
								.roty(90)
								.movez(-boltCenterLong /2)
								.movey(boltShortDistance/2)
					)
					.difference(handBolt
								.roty(90)
								.movez(-boltCenterLong /2)
								.movey(-boltShortDistance/2)
					)
					.difference(handBolt
								.roty(90)
								.movez(boltCenterLong /2)
								.movey(-boltShortDistance/2)
					)
		// offset the claw mount so the tip is at the kinematic center
		mountPlate=mountPlate.movex(-54.4-ballRadius.getMM()-8)
		return mountPlate
	}
	private CSG springBlockPin(double thickness){
		double magnetPinDiameter = bearingData.innerDiameter/2
		return new Cylinder(magnetPinDiameter,magnetPinDiameter,encoderBearingHeight+6+thickness,(int)30).toCSG()
				.toZMax()
				.movez(encoderToEncoderDistance+6)
				.difference(encoder1.rotx(180))
	}
	private CSG springBlock(double thickness){
		if(springLinkBlockLocal.get(thickness)!=null)
			return springLinkBlockLocal.get(thickness).clone()
		CSG linkBlank = new RoundedCube(drivenLinkX-1,drivenLinkWidth,thickness)
						.cornerRadius(cornerRadius)
						.toCSG()
						.toXMin()
						.toZMax()
						.movez(centerLinkToBearingTop)
						.movex(-(bearingData.innerDiameter/2)-1)
		double loadCellNub = loadCell.getMaxZ()
		if(loadCellNub>thickness)
			loadCellNub=thickness
		CSG connectionLink = new RoundedCube(25,drivenLinkWidth-20,loadCellNub)
							.cornerRadius(cornerRadius)
							.toCSG()	
							.toZMin()
		CSG linkBackBlank = new RoundedCube(25,drivenLinkWidth,loadCellNub)
						.cornerRadius(cornerRadius)
						.toCSG()
						.toZMin()
						.union(connectionLink.movex(-3))
						.movez(linkBlank.getMinZ())
						.movex(loadCellBoltCenter)
		
		
		CSG springCut = loadCell
						.movez(linkBackBlank.getMinZ())
		//for(int i=1;i<springData.numOfCoils;i++){
		//	springCut=springCut.union(springCut.movez(-springData.wireDiameter*i))
		//}
		
		CSG magnetPin = springBlockPin(thickness)
		
		linkBlank =linkBlank
					.union(magnetPin)
					.difference(armScrews
						.movex(linkBlank.getMaxX())
						.movez(washerThickness)
						)
					.union(linkBackBlank)
					.difference([springCut])
					.difference(encoder1.rotx(180))
					.movez(-washerThickness)
		springLinkBlockLocal.put(thickness,linkBlank)
		return linkBlank
	}
	private CSG getEncoderCap(){
		if(encoderCapCache!=null)
			return encoderCapCache
		
		double bearingHolder = bearingDiameter/2 + encoderCapRodRadius/2
		double SidePlateThickness = encoderBearingHeight 
		CSG pin  =new Cylinder(encoderCapRodRadius,encoderCapRodRadius,SidePlateThickness,(int)30).toCSG()
					.movex(-pinOffset)
		double mountPlatePinAngle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
		
		CSG capPinSet=pin
					.rotz(mountPlatePinAngle)
					.union(pin.rotz(-mountPlatePinAngle))
		
		CSG center  =new Cylinder(bearingHolder,bearingHolder,SidePlateThickness,(int)30).toCSG()
		CSG pinColumn =pin .union(pin
									.movez(topPlateOffset+printerOffset.getMM()/2))
								.hull() 
								.movez(-printerOffset.getMM()/2)
		CSG pivot = center.movex(-encoderCapRodRadius*4)
		CSG bottomBlock = capPinSet
						.union(pivot)
						.hull()
						.union(center.union(pivot).hull())
						//.toZMax()
						.movez(topPlateOffset)
						.difference(encoderKeepaway
								.rotx(180)
								.movez(encoderToEncoderDistance-encoderBearingHeight)
								
								)
						.union(pinColumn.rotz(mountPlatePinAngle))
						.union(pinColumn.rotz(-mountPlatePinAngle))
						.difference(screwSet
									)
		encoderCapCache = bottomBlock
		return encoderCapCache
	}	
	
	private def getGearWithSpline(LinkConfiguration nextLink ){
		def type =nextLink.getShaftType()
		println "Gear with shaft "+type
		CSG horn = Vitamins.get(nextLink.getShaftType(),nextLink.getShaftSize())	
					.rotx(180)
					.toZMin()
		double hornOffset = 	horn.getTotalZ()
						
		horn=horn.movex(-gearDistance)

		double gearPlacementVSMotor = -(motorBackSetDistance+washerThickness)
		CSG myGearA = gearA.clone()
					.union(gearStandoff)
					.movez(washerThickness)	
		horn=horn.movez(gearPlacementVSMotor+washerThickness)
		def numHorns =(myGearA.getTotalZ()-horn.getTotalZ())*3
		
		for(double i=0;i<numHorns;i+=horn.getTotalZ()/3){
			horn=horn.union(horn
							.movez(i))
			
			//println "Moving horn  = "+i			
		}
		myGearA=myGearA
				.difference(horn)
		// special recess for measured difference
		//myGearA=myGearA
		//		.difference(horn.movez(-2.0))
		myGearA = myGearA
					//.rotz(-90)
					.movez(-centerLinkToBearingTop)	
					.setColor(javafx.scene.paint.Color.BLUE);
		return myGearA					
		//return [myGearA,horn.movez(-centerLinkToBearingTop)	]
	}
	private def getServoCap(LinkConfiguration conf ){
		if(sidePlateLocal.get(conf.getXml())!=null)
			return sidePlateLocal.get(conf.getXml()).collect{
				it.clone()
			}
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())

		double SidePlateThickness = encoderBearingHeight 
		double totalFlangLen = servoReference.getTotalX()
		double shaftToShortSideFlandgeEdge = servoReference.getMaxX()
		double hornOffset = 	0	
		double servoNub = 0
		// creating the servo
		 servoReference=  servoReference
			.movez(-centerLinkToBearingTop-SidePlateThickness)			
			.movey(-gearDistance)
			.rotz(90)
			//.movez(-motorBackSetDistance/2)
		CSG gearHole = gearKeepaway
					.movez(servoNub-centerLinkToBearingTop)	
		double servoTop = servoReference.getMaxZ()-servoNub
		double bearingHolder = bearingDiameter/2 + encoderCapRodRadius
		
		CSG pin  =new Cylinder(encoderCapRodRadius,encoderCapRodRadius,SidePlateThickness,(int)30).toCSG()
					.movex(-pinOffset)
		double mountPlatePinAngle 	=Math.toDegrees(Math.atan2(capPinSpacing,pinOffset))
		
		CSG capPinSet=pin
					.rotz(mountPlatePinAngle)
					.union(pin.rotz(-mountPlatePinAngle))
		
		CSG center  =new Cylinder(bearingHolder,bearingHolder,SidePlateThickness,(int)30).toCSG()
		
		CSG baseShape = new Cube(servoReference.getTotalX()+encoderCapRodRadius,
							servoReference.getTotalY()+encoderCapRodRadius,
							SidePlateThickness)
						
						.toCSG()
						.toZMin()
						//.toYMin()
						//.movey(-encoderCapRodRadius/2)
						.movex(-gearDistance-encoderCapRodRadius)
		CSG bottomBlock = capPinSet.union([center,baseShape]).hull()
						.toZMax()
						.movez(encoderBearingHeight-encoderToEncoderDistance)
						.difference(encoderKeepaway)
						.difference(screwSet.movez(-encoderToEncoderDistance-encoderBearingHeight))
						.difference([servoReference.movez(-2),gearHole.movez(2)])
						.difference(servoReference)
		double plateThickness = (-bottomBlock.getMinZ()+bottomBlock.getMaxZ())
		CSG boundingBox = new Cube(   (-bottomBlock.getMinX()+bottomBlock.getMaxX()),
								(-bottomBlock.getMinY()+bottomBlock.getMaxY()),
								plateThickness)
								.toCSG()
								.toXMax()
								.movex(bottomBlock.getMaxX())
								.toYMax()
								.movey(bottomBlock.getMaxY())
								.toZMax()
								.movez(bottomBlock.getMaxZ())
								.movez(- bearingData.width)
		CSG lowerbottomBlock=bottomBlock.difference(boundingBox)
		CSG upperbottomBlock = bottomBlock.intersect(boundingBox)						
		sidePlateLocal.put(conf.getXml(),[bottomBlock]) 
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

	private ArrayList<CSG> getCameraMount(){
		//cameraLocationCSG
		
		double cameraMountSize = 85+5+thickness.getMM()
		Log.enableSystemPrint(true)
		println "Camera at "+cameraLocationNR
		double cameraBolt = (workcellSize-cameraMountSize)/2
		/*
		CSG camera = (CSG) ScriptingEngine
					 .gitScriptRun(
			            "https://github.com/NotOctogonapus/SeriesElasticActuator.git", // git location of the library
			            "camera.groovy" , // file to load
			            null// create a keepaway version
			            )
		*/
		CSG cameraBase = new Cube(6.5,52,thickness.getMM()).toCSG()
			.toZMax()   
			.movex(9+35/2)
		CSG camera = new Cylinder(35/2,35/2,	  thickness.getMM()    ,(int)30).toCSG()   
		.toZMax()
		.union(cameraBase)
		
		camera = camera
			            .transformed(cameraLocationCSG)
		CSG camerMount = new Cube(	cameraMountSize+5,
								cameraMountSize+5,
								thickness.getMM()).toCSG()
								.toZMax()
								.transformed(cameraLocationCSG)
								.difference(camera)
		CSG camerMountLug = new Cube(	45+5+thickness.getMM(),
								thickness.getMM(),
								45).toCSG()
								.toYMax()
								.movey(cameraMountSize/2)
								.toZMin()
								.movex(10)
		CSG notch = new Cube(	thickness.getMM()).toCSG()
								.toZMax()
								.toYMax()		
								.movey(cameraMountSize/2)								
													
		CSG topLug=	camerMountLug.transformed(cameraLocationCSG)
		CSG baseLug = camerMountLug
						.rotz(180)
						.toXMax()
						.movex(workcellSize/2)
						.movex(-20)
		CSG midLug = camerMountLug
						.rotz(180)
						.toXMin()
						.movex(camerMount.getMaxX()+20)
						.movez(cameraBolt+thickness.getMM())
		CSG notches =  notch
					.movex(15)
					.union(	notch
							.movex(-15)	)
		CSG bottomNotches = notches
						.rotx(180)
						.movex(cameraBolt)	
						.toZMax()
		CSG topNotches = notches.transformed(cameraLocationCSG)	

		CSG nut = LockNutKeepaway
				.movez(thickness.getMM())
		//boltSizeParam
		CSG boltCutout = new Cube(boltMeasurments.outerDiameter,
							thickness.getMM(),
							45-thickness.getMM()).toCSG()
							.toZMin()
							.union(new Cylinder(boltMeasurments.outerDiameter/2,
											boltMeasurments.outerDiameter/2,
											thickness.getMM(),(int)30).toCSG()
											.toZMax()
											,
											nut
							)
							.movez(-thickness.getMM()/2)
								
		boltCutout=boltCutout.movey(	-(cameraMountSize-thickness.getMM())/2				)
				.union(boltCutout.movey(	(cameraMountSize-thickness.getMM())/2				))
		CSG topBolts = boltCutout.transformed(cameraLocationCSG)
		CSG bottomBolts = boltCutout.movex(cameraBolt)
		CSG bracketA = topLug
					.union(midLug)
					.hull()
					.union(
						baseLug
						.union(midLug)
						.hull()
						,bottomNotches)
					.difference(bottomBolts,topBolts)
					.difference(camerMount.hull())
					.union(topNotches)
		CSG bracketB = bracketA
						.toYMax()
						.movey(cameraMountSize/2)
		camerMount=camerMount.difference(topBolts,bracketA,bracketB)
		double cameraOffset = cameraMountSize+22
		camerMount.setManufacturing({ toMfg ->
				TransformNR step = cameraLocationNR.inverse()
				Transform move = TransformFactory.nrToCSG(step)
				p= toMfg
						.transformed(move)
						.toXMin()
						.toYMin()
						.toZMin()
						.movex(-cameraOffset*1)
				p.addExportFormat("svg")
				return p
		})
		
		bracketA.setManufacturing({ toMfg ->
				p= toMfg
						.rotx(90)
						.toXMin()
						.toYMin()
						.toZMin()
						.movex(-cameraOffset*2)
				p.addExportFormat("svg")
				return p
		})
		bracketB.setManufacturing({ toMfg ->
				p= toMfg
						.rotx(90)
						.toXMin()
						.toYMin()
						.toZMin()
						.movex(-cameraOffset*3-10)
						.movey(-35)
				p.addExportFormat("svg")
				return p
		})
		bottomBolts.setManufacturing({ toMfg ->
				return null
		})
		
		//return [bottomNotches]
		return [camerMount,bracketA,bracketB,bottomBolts].collect{
			it.addExportFormat("svg")
			return it
		}
		
	}

	private add(ArrayList<CSG> csg ,CSG object, Affine dh , String name){
		if(dh!=null)
			object.setManipulator(dh);
		csg.add(object);
		//BowlerStudioController.addCsg(object);
		if(version[0]>0 || version[1]>=11){
			//println "Name API found"
			object.setName(name)
		}
	}
}
def c=new MyCadGenerator(args)
return c//[c.springBlock(c.drivenLinkThickness), c.springBlockPin(c.gearBMeasurments.height).movey(60)]

base=DeviceManager.getSpecificDevice( "HephaestusWorkCell",{
			//If the device does not exist, prompt for the connection
			
			MobileBase m = MobileBaseLoader.fromGit(
				"https://github.com/NotOctogonapus/SeriesElasticActuator.git",
				"seaArm.xml"
				)
			if(m==null)
				throw new RuntimeException("Arm failed to assemble itself")
			println "Connecting new device robot arm "+m
			return m
		})
def arm = base.getAppendages().get(0)
int linkIndex=2
LinkConfiguration conf = arm.getLinkConfiguration(linkIndex);
return c.generateBody(base)
return c.getGearWithSpline(conf)
return c.generateCad(arm,  linkIndex)
return c.getServoCap(conf)

