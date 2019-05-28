@GrabResolver(name='sonatype', root='https://oss.sonatype.org/content/repositories/releases/')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava', version='0.10.1')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava-HID', version='0.10.0')
@Grab(group='org.hid4java', module='hid4java', version='0.5.0')

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

	def hidEventEngine;
	def physicsSource ;
	int count = 0;
	Runnable run={
	
	
			count ++
			if(count >10){
				count =0
				// Setup of variables done, next perfoem one compute cycle
				
				//get the current FK pose to update the data used by the jacobian computation
				TransformNR pose = physicsSource.getCurrentTaskSpaceTransform()
				// Convert the tip transform to Matrix form for math
				Matrix matrixForm= pose.getMatrixTransform()
				// get the position of all the joints in engineering units
				double[] jointSpaceVector = physicsSource.getCurrentJointSpaceVector()
				// compute the Jacobian using Jama matrix library
				Matrix jacobian = physicsSource.getJacobian();
				Matrix[] massMatrix =  new Matrix[jointSpaceVector.length]
				Matrix[] incrementalJacobian =  new Matrix[jointSpaceVector.length]
				double [] masses = new double [jointSpaceVector.length]
				//TODO LoadMasses and mass Matrix here
				
				for (int i=0;i<jointSpaceVector.length;i++){
					incrementalJacobian[i] = physicsSource.getJacobian(jointSpaceVector,i);
					
					//println "Increment "+i+" "+  TransformNR.getMatrixString(incrementalJacobian[i])
				}
				//println "Total "+  TransformNR.getMatrixString(jacobian)
								
			}
		}
	public PhysicicsDevice(def c,def  d){
		hidEventEngine=c;
		physicsSource=d;
		hidEventEngine.arm.addEvent(37,run)
		
	}
	@Override
	public  void disconnectDeviceImp(){		
		println "Physics Termination signel shutdown"
		hidEventEngine.arm.removeEvent(37,run)
	}
	
	@Override
	public  boolean connectDeviceImp(){
		println "Physics Startup signel "
		return true
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

//return base

def physics =DeviceManager.getSpecificDevice( "HephaestusPhysics",{
	PhysicicsDevice pd = new PhysicicsDevice(dev,base.getAllDHChains().get(0))
	
	return pd
})
