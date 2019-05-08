@GrabResolver(name='sonatype', root='https://oss.sonatype.org/content/repositories/releases/')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava', version='0.1.0')

import edu.wpi.SimplePacketComs.device.hephaestus.HephaestusArm;
import Jama.Matrix;

public class HIDSimpleComsDevice extends NonBowlerDevice{
	public HephaestusArm arm;
	public HIDSimpleComsDevice(int vidIn, int pidIn){
		arm = new HephaestusArm(vidIn,pidIn); 
		setScriptingName("hidbowler")
	}
	@Override
	public  void disconnectDeviceImp(){arm.disconnect()}
	@Override
	public  boolean connectDeviceImp(){arm.connect()}
	@Override
	public  ArrayList<String>  getNamespacesImp(){return null}
}
public class HIDRotoryLink extends AbstractRotoryLink{
	HIDSimpleComsDevice device;
	int index =0;
	int lastPushedVal = 0;
	double velocityTerm = 0;
	double gravityCompTerm = 0;
	/**
	 * Instantiates a new HID rotory link.
	 *
	 * @param c the c
	 * @param conf the conf
	 */
	public HIDRotoryLink(HIDSimpleComsDevice c,LinkConfiguration conf) {
		super(conf);
		index = conf.getHardwareIndex()
		device=c
		if(device ==null)
			throw new RuntimeException("Device can not be null")
		c.arm.addEvent(37,{
			int val= getCurrentPosition();
			if(lastPushedVal!=val){
				//println "Fire Link Listner "+index+" value "+getCurrentPosition()
				try{
					fireLinkListener(val);
				}catch(Exception ex){}
			}
			lastPushedVal=val
		})
		
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#cacheTargetValueDevice()
	 */
	@Override
	public void cacheTargetValueDevice() {
		device.arm.setValues(index,(float)getTargetValue(),(float)velocityTerm ,(float)gravityCompTerm)
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#flush(double)
	 */
	@Override
	public void flushDevice(double time) {
		// auto flushing
	}
	
	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#flushAll(double)
	 */
	@Override
	public void flushAllDevice(double time) {
		// auto flushing
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#getCurrentPosition()
	 */
	@Override
	public double getCurrentPosition() {
		return device.arm.getPosition(index);
	}

}

public class PhysicicsDevice extends NonBowlerDevice{
	/**
	 * Cross product.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the double[]
	 */
	private double [] crossProduct(double[] a, double[] b){
		double [] xProd = new double [3];
		
		xProd[0]=a[1]*b[2]-a[2]*b[1];
		xProd[1]=a[2]*b[0]-a[0]*b[2];
		xProd[2]=a[0]*b[1]-a[1]*b[0];
		
		return xProd;
	}
		/**
	 * Gets the Jacobian matrix.
	 *
	 * @param jointSpaceVector the joint space vector
	 * @return a matrix representing the Jacobian for the current configuration
	 */
	public Matrix getJacobian(DHChain chain, double[] jointSpaceVector, int index){
		int size = chain.getLinks().size()
		double [][] data = new double[6][size]; 
		chain.getChain(jointSpaceVector);
		for(int i=0;i<size;i++){
			if(i>index){
				
				continue
			}
			Matrix rotationComponent = new TransformNR().getMatrixTransform();
			for(int j=i;j<size && j<=index;j++) {
				double value=0;
				if(chain.getLinks().get(j).getLinkType()==DhLinkType.ROTORY)
					value=Math.toRadians(jointSpaceVector[j]);
				else
					value=jointSpaceVector[j];
				Matrix step = chain.getLinks().get(j).DhStep(value);
				//Log.info( "Current:\n"+current+"Step:\n"+step);
				//println i+" Link "+j+" index "+index+" step "+TransformNR.getMatrixString(step)
				rotationComponent = rotationComponent.times(step);
			}
			double [] zVect = new double [3];
			double [] zVectEnd = new double [3];
			double [][] rotation=new TransformNR(rotationComponent).getRotationMatrix().getRotationMatrix()
			zVectEnd[0]=rotation[2][2];
			zVectEnd[1]=rotation[2][1];
			zVectEnd[2]=rotation[2][0];
			if(i==0 && index ==0 ){
				zVect[0]=0;
				zVect[1]=0;
				zVect[2]=1;
			}else if(i<=index){
				//println "Link "+index+" "+TransformNR.getMatrixString(new Matrix(rotation))
				//Get the rz vector from matrix
				zVect[0]=zVectEnd[0];
				zVect[1]=zVectEnd[1];
				zVect[2]=zVectEnd[2];
			}else{
				zVect[0]=0;
				zVect[1]=0;
				zVect[2]=0;
			}
			//Assume all rotational joints
			//Set to zero if prismatic
			if(chain.getLinks().get(i).getLinkType()==DhLinkType.ROTORY){
				data[3][i]=zVect[0];
				data[4][i]=zVect[1];
				data[5][i]=zVect[2];
			}else{
				data[3][i]=0;
				data[4][i]=0;
				data[5][i]=0;
			}
			double []rVect = new double [3];
			
			
			Matrix rComponentmx = new TransformNR().getMatrixTransform();
			//if(i>0){
				for(int j=0;j<i ;j++) {
					double value=0;
					if(chain.getLinks().get(j).getLinkType()==DhLinkType.ROTORY)
						value=Math.toRadians(jointSpaceVector[j]);
					else
						value=jointSpaceVector[j];
					Matrix step = chain.getLinks().get(j).DhStep(value);
					//Log.info( "Current:\n"+current+"Step:\n"+step);
					//println i+" Link "+j+" index "+index+" step "+TransformNR.getMatrixString(step)
					rComponentmx = rComponentmx.times(step);
				}
			//}
			
			//Figure out the current 
			Matrix tipOffsetmx = new TransformNR().getMatrixTransform();
			for(int j=0;j<size && j<=index;j++) {
				double value=0;
				if(chain.getLinks().get(j).getLinkType()==DhLinkType.ROTORY)
					value=Math.toRadians(jointSpaceVector[j]);
				else
					value=jointSpaceVector[j];
				Matrix step = chain.getLinks().get(j).DhStep(value);
				//Log.info( "Current:\n"+current+"Step:\n"+step);
				//println i+" Link "+j+" index "+index+" step "+TransformNR.getMatrixString(step)
				tipOffsetmx = tipOffsetmx.times(step);
			}
			
			double []tipOffset = new double [3];
			double []rComponent = new double [3];
			TransformNR tipOffsetnr = new TransformNR(tipOffsetmx)//.times(myInvertedStarting);
			tipOffset[0]=tipOffsetnr.getX();
			tipOffset[1]=tipOffsetnr.getY();
			tipOffset[2]=tipOffsetnr.getZ();
			
			TransformNR rComponentnr = new TransformNR(rComponentmx)//.times(myInvertedStarting);
			rComponent[0]=rComponentnr.getX();
			rComponent[1]=rComponentnr.getY();
			rComponent[2]=rComponentnr.getZ();
			for(int x=0;x<3;x++)
				rVect[x]=(tipOffset[x]-rComponent[x])
				
			/*
			Matrix current = new TransformNR().getMatrixTransform();
			for(int j=index;j>=i;j--) {
				double value=0;
				if(chain.getLinks().get(j).getLinkType()==DhLinkType.ROTORY)
					value=Math.toRadians(jointSpaceVector[j]);
				else
					value=jointSpaceVector[j];
				Matrix step = new TransformNR(chain.getLinks().get(j).DhStep(value)).inverse().getMatrixTransform();
				//Log.info( "Current:\n"+current+"Step:\n"+step);
				//println i+" Link "+j+" index "+index+" step "+TransformNR.getMatrixString(step)
				current = current.times(step);
			}
			TransformNR intermediate = new TransformNR(current)//.times(myInvertedStarting);
			rVect[0]=intermediate.getX();
			rVect[1]=intermediate.getY();
			rVect[2]=intermediate.getZ();	
			*/
			//Cross product of rVect and Z vect
			double []xProd = crossProduct( zVect,rVect);
			println i+" R vector "+rVect //+" \t\t Zvect "+zVect+" \t\tcrossProd "+xProd
			//println TransformNR.getMatrixString(tipOffsetmx)
			
			
			data[0][i]=xProd[0];
			data[1][i]=xProd[1];
			data[2][i]=xProd[2];
			
		}
		println "\n\n"
		return new Matrix(data);
	}
	def hidEventEngine;
	def physicsSource ;
	int count = 0;
	Closure event = {
	
	
			count ++
			if(count >10){
				count =0
						//Get the DHChain object
				DHChain chain = physicsSource.getChain()
				// Setup of variables done, next perfoem one compute cycle
				
				//get the current FK pose to update the data used by the jacobian computation
				TransformNR pose = physicsSource.getCurrentTaskSpaceTransform()
				// Convert the tip transform to Matrix form for math
				Matrix matrixForm= pose.getMatrixTransform()
				// get the position of all the joints in engineering units
				double[] jointSpaceVector = physicsSource.getCurrentJointSpaceVector()
				// compute the Jacobian using Jama matrix library
				Matrix jacobian = getJacobian(chain,jointSpaceVector,jointSpaceVector.length-1);
				Matrix[] massMatrix =  new Matrix[jointSpaceVector.length]
				Matrix[] incrementalJacobian =  new Matrix[jointSpaceVector.length]
				double [] masses = new double [jointSpaceVector.length]
				//TODO LoadMasses and mass Matrix here
				
				for (int i=0;i<jointSpaceVector.length;i++){
					incrementalJacobian[i] = getJacobian(chain,jointSpaceVector,i);
					
					println "Increment "+i+" "+  TransformNR.getMatrixString(incrementalJacobian[i])
				}
				println "Total "+  TransformNR.getMatrixString(jacobian)
								
			}
		}
	public PhysicicsDevice(def c,def  d){
		hidEventEngine=c;
		physicsSource=d;
		hidEventEngine.addEvent(37,event)
		
	}
	@Override
	public  void disconnectDeviceImp(){		
		println "Physics Termination signel shutdown"
		hidEventEngine.removeEvent(37,event)
	}
	
	@Override
	public  boolean connectDeviceImp(){
		println "Physics Startup signel "
	}
	public  ArrayList<String>  getNamespacesImp(){
		// no namespaces on dummy
		return [];
	}
	
}


def dev = DeviceManager.getSpecificDevice( "hidbowler",{
	//If the device does not exist, prompt for the connection
	
	HIDSimpleComsDevice d = new HIDSimpleComsDevice(0x3742,0x8)
	d.connect(); // Connect to it.
	LinkFactory.addLinkProvider("hidsimple",{LinkConfiguration conf->
				println "Loading link "
				return new HIDRotoryLink(d,conf)
		}
	)
	println "Connecting new device: "+d
	return d
})
def base =DeviceManager.getSpecificDevice( "HephaestusArm",{
	//If the device does not exist, prompt for the connection
	
	MobileBase m = BowlerStudio.loadMobileBaseFromGit(
		"https://github.com/madhephaestus/SeriesElasticActuator.git",
		"HIDarm.xml"
		)
	if(m==null)
		throw new RuntimeException("Arm failed to assemble itself")
	println "Connecting new device robot arm "+m
	return m
})

return base

def physics =DeviceManager.getSpecificDevice( "HephaestusPhysics",{
	PhysicicsDevice pd = new PhysicicsDevice(dev,base. getAllDHChains().get(0))
	
	return pd
})
