import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;

return new ICadGenerator(){
	HashMap<String , HashMap<String,ArrayList<CSG>>> map =  new HashMap<>();
	HashMap<String,ArrayList<CSG>> bodyMap =  new HashMap<>();
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",100,[200,50])
	LengthParameter snoutLen 		= new LengthParameter("Snout Length",63,[200,50])
	LengthParameter jawHeight 		= new LengthParameter("Jaw Height",32,[200,10])
	LengthParameter eyeCenter 		= new LengthParameter("Eye Center Distance",headDiameter.getMM()/2,[headDiameter.getMM(),headDiameter.getMM()/2])
	StringParameter servoSizeParam 			= new StringParameter("hobbyServo Default","towerProMG91",Vitamins.listVitaminSizes("hobbyServo"))
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))

	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	DHParameterKinematics neck=null;
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
		
		String legStr =""
		for(DHParameterKinematics l:getLimbDHChains(base)){
			legStr+=l.getRobotToFiducialTransform(). getXml();
		}
		if(bodyMap.get(legStr)!=null){
			println "Body cached"
			for(CSG csg:bodyMap.get(legStr))
				csg.setManipulator(base.getRootListener());
			return bodyMap.get(legStr)
		}
		println "Generating body"
		ArrayList<CSG> cutouts=new ArrayList<>();
		ArrayList<CSG> attach=new ArrayList<>();
		
		ArrayList<CSG>  bodyParts = new ArrayList<CSG>()
		double bodyHeight = 0;
		ArrayList<CSG> attachmentParts = new ArrayList<CSG>()
		for(DHParameterKinematics l:base.getLegs()){
			TransformNR position = l.getRobotToFiducialTransform();
			Transform csgTrans = TransformFactory.nrToCSG(position)
			for(CSG attachment:	generateCad(l,0)){
				CSG movedCorner = attachment
					.transformed(csgTrans)// this moves the part to its placement where it will be in the final model
				attachmentParts.add(movedCorner)
				if(movedCorner.getMaxZ()>bodyHeight){
					bodyHeight=movedCorner.getMaxZ()
				}
			}
		}
		
		CSG bodyBlob = attachmentParts
						.get(0)
						.union(attachmentParts)
		CSG bodyExtrude = bodyBlob
						.movez(bodyHeight+thickness.getMM())
						.union(bodyBlob)
						.hull()
		//add(bodyParts,bodyExtrude,base.getRootListener())
		CSG bodyCube = new Cube(	(-bodyExtrude.getMinX()+bodyExtrude.getMaxX())*2,// X dimention
								 (-bodyExtrude.getMinY()+bodyExtrude.getMaxY())*2,// Y dimention
								thickness.getMM()//  Z dimention
							).toCSG()// this converts from the geometry to an object we can work with
							.movez(bodyHeight-thickness.getMM())// recess the body plate to overlap with the connection interface from the limbs
		//add(bodyParts,bodyCube,base.getRootListener())					
		
		CSG bodyPlate=bodyCube	
					.intersect(bodyExtrude)
		bodyPlate.setManufactuing(new PrepForManufacturing() {
					public CSG prep(CSG arg0) {
						return arg0.toZMin();
					}
				});
		add(bodyParts,bodyPlate,base.getRootListener())
		
		bodyMap.put(legStr,bodyParts)

		for(CSG vitamin: bodyParts){
			vitamin.setManufactuing({CSG arg0 ->
	
				return new Cube(	0.001,// X dimention
								0.001,// Y dimention
								0.001//  Z dimention
								).toCSG()// this converts from the geometry to an object we can work with
								.toZMin()
			});
		}
		return bodyParts;
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
		double hornOffset = 	shaftmap.get("hornThickness")	
		
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		.transformed(new Transform().rotZ(90))
		
		double servoTop = servoReference.getMaxZ()
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	
		
		servoReference=servoReference
			.movez(-servoTop)

		
		
		if(linkIndex==0){
			add(csg,servoReference.clone(),sourceLimb.getRootListener())
			add(csg,servoReference,dh.getListener())
		}else{
			if(linkIndex<dhLinks.size()-1)
				add(csg,servoReference,dh.getListener())
			else{
				// load the end of limb
			}
			
		}
		
		add(csg,moveDHValues(horn,dh),dh.getListener())

		if(neck ==sourceLimb ){
			
		}
		
		
		return csg;
	}

	private CSG reverseDHValues(CSG incoming,DHLink dh ){
		println "Reversing "+dh
		return incoming
			.rotx(Math.toDegrees(-dh.getAlpha()))
			.movex(dh.getR()/2)		
	}
	
	private CSG moveDHValues(CSG incoming,DHLink dh ){
		return incoming.transformed(new Transform().translateZ(-dh.getD()))
		.transformed(new Transform().rotZ(-Math.toDegrees(dh.getTheta())))
		.transformed(new Transform().rotZ((90+Math.toDegrees(dh.getTheta()))))
		.transformed(new Transform().translateX(-dh.getR()))
		.transformed(new Transform().rotX(Math.toDegrees(dh.getAlpha())));
		
	}


	private add(ArrayList<CSG> csg ,CSG object, Affine dh ){
		object.setManipulator(dh);
		csg.add(object);
		BowlerStudioController.addCsg(object);
	}
};
