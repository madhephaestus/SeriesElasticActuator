//@Grab(group='org.hid4java', module='hid4java', version='0.5.0')

import org.hid4java.*
import org.hid4java.event.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import Jama.Matrix;
class PacketProcessor{
	ByteOrder be =ByteOrder.LITTLE_ENDIAN; 
	int packetSize = 64
	int numFloats =(packetSize/4)-1

	int getId(byte [] bytes){
		return ByteBuffer.wrap(message).order(be).getInt(0);
	}
	float[] parse(byte [] bytes){
		float[] returnValues = new float[ numFloats];
		
		//println "Parsing packet"
		for(int i=0;i<numFloats;i++){
			int baseIndex = (4*i)+4;
			returnValues[i] = ByteBuffer.wrap(bytes).order(be).getFloat(baseIndex);
		}
			
		return returnValues
	}
	byte[] command(int idOfCommand, float []values){
		byte[] message = new byte[packetSize];
		ByteBuffer.wrap(message).order(be).putInt(0,idOfCommand).array();
		for(int i=0;i<numFloats && i< values.length;i++){
			int baseIndex = (4*i)+4;
			ByteBuffer.wrap(message).order(be).putFloat(baseIndex,values[i]).array();
		}
		return message
	}
	
}

public class PacketType{
	int idOfCommand=0;
	float [] downstream = new float[15]
	float [] upstream = new float[15]
	boolean done=false;
	boolean started = false;
	public PacketType(int id){
		idOfCommand=id;
	}
	
}

public class HIDSimpleComsDevice extends NonBowlerDevice{
	HashMap<Integer,ArrayList<Closure>> events = new HashMap<>()
	HidServices hidServices = null;
	int vid =0 ;
	int pid =0;
	HidDevice hidDevice=null;
	public PacketProcessor processor= new PacketProcessor();
	boolean HIDconnected = false;
	PacketType pollingPacket = new PacketType(37);
	PacketType pidPacket = new PacketType(65);
	PacketType PDVelPacket = new PacketType(48);
	PacketType SetVelocity = new PacketType(42);
	
	ArrayList<PacketType> processQueue = [] as ArrayList<PacketType>
	
	public HIDSimpleComsDevice(int vidIn, int pidIn){
		// constructor
		vid=vidIn
		pid=pidIn
		setScriptingName("hidbowler")
	}

	public void pushPacket(def packet){
		packet.done=false;
		packet.started = false;
		processQueue.add(packet)
		while(packet.done==false){
			Thread.sleep(1)
		}
	}
	void removeEvent(Integer id, Closure event){
		if(events.get(id)==null){
			events.put(id,[])
		}
		events.get(id).remove(event)
	}
	void addEvent(Integer id, Closure event){
		if(events.get(id)==null){
			events.put(id,[])
		}
		events.get(id).add(event)
	}
	@Override
	public  void disconnectDeviceImp(){		
		HIDconnected=false;
		println "HID device Termination signel shutdown"
	}
	private void process(def packet){
		packet.started=true
		try{
			if(hidDevice!=null){
				//println "Writing packet"
				try{
					byte[] message = processor.command(packet.idOfCommand,packet.downstream)
					//println "Writing: "+ message
					int val = hidDevice.write(message, message.length, (byte) 0);
					if(val>0){
						int read = hidDevice.read(message, 1000);
						if(read>0){
							//println "Parsing packet"
							//println "read: "+ message
							def up=processor.parse(message)
							for(int i=0;i<packet.upstream.length;i++){
								packet.upstream[i]=up[i];
							}
						}else{
							println "Read failed"	
						}
						
					}
				}catch (Throwable t){
					t.printStackTrace(System.out)
					disconnect()
				}
			}else{
				println "Simulation"
				for(int j=0;j<packet.downstream.length&&j<packet.upstream.length;j++){
					packet.upstream[j]=packet.downstream[j];
			}
				
			}
			//println "updaing "+upstream+" downstream "+downstream
		
			if(events.get(packet.idOfCommand)!=null){
				for(Closure e:events.get(packet.idOfCommand)){
					if(e!=null){
						try{
							e.call()
						}catch (Throwable t){
							t.printStackTrace(System.out)							
						}
					}
				}
			}
		}catch (Throwable t){
					t.printStackTrace(System.out)
		}
		packet.done=true
	}
	
	@Override
	public  boolean connectDeviceImp(){
		if(hidServices==null)
			hidServices = HidManager.getHidServices();
		// Provide a list of attached devices
		hidDevice=null
		for (HidDevice h : hidServices.getAttachedHidDevices()) {
		  if(h.isVidPidSerial(vid, pid, null)){
		  	  hidDevice=h
			 
			  hidDevice.open();
			  System.out.println("Found! "+hidDevice);
			 
		  }
		}
		HIDconnected=true;
		Thread.start{
			println "Starting HID Thread"
			while(HIDconnected){
				//println "loop"
				try{
					Thread.sleep(1)
					if(pollingPacket!=null){
						pollingPacket.done=false;
						pollingPacket.started = false;
						process(pollingPacket)
					}
					while(processQueue.size()>0){
						try{
							def temPack =processQueue.remove(0)
							if(temPack!=null){
								println "Processing "+temPack
								process(temPack)
							}
						}catch(Exception e){
							e.printStackTrace()
						}
						
					}
				}catch(Exception e){
					e.printStackTrace()
				}

				
			}
			if(hidDevice !=null){
				hidDevice.close();
			}
			if(hidServices!=null){
				// Clean shutdown
				hidServices.shutdown();
			}
			println "HID device clean shutdown"
		 }
		//throw new RuntimeException("No HID device found")
	}
	void setValues(int index,float position, float velocity, float force){
		pollingPacket.downstream[(index*3)+0] = position
		pollingPacket.downstream[(index*3)+1] = velocity
		pollingPacket.downstream[(index*3)+2] = force
		//println "Setting Downstream "+downstream
	}
	void setPIDGains(int index,float kp, float ki, float kd){
		
		pidPacket.downstream[(index*3)+0] = kp
		pidPacket.downstream[(index*3)+1] = ki
		pidPacket.downstream[(index*3)+2] = kd
		//println "Setting Downstream "+downstream
	}
	void pushPIDGains(){
		pushPacket(pidPacket)
	}
	void setPDVelGains(int index,float kp, float kd){
		
		PDVelPacket.downstream[(index*2)+0] = kp
		PDVelPacket.downstream[(index*2)+1] = kd
		//println "Setting Downstream "+downstream
	}
	void pushPDVelGains(){
		pushPacket(PDVelPacket)
	}
	void setVelocity(int index,float TPS){
		SetVelocity.downstream[index] = TPS
		//println "Setting Downstream "+downstream
	}
	void pushVelocity(){
		pushPacket(SetVelocity)
	}
	float [] getValues(int index){
		float [] back = new float [3];
	
		back[0]=pollingPacket.upstream[(index*3)+0]
		back[1]=pollingPacket.upstream[(index*3)+1]
		back[2]=pollingPacket.upstream[(index*3)+2]
		
		return back
	}
	@Override
	public  ArrayList<String>  getNamespacesImp(){
		// no namespaces on dummy
		return null;
	}
	
	
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
		c.addEvent(37,{
			int val= getCurrentPosition();
			if(lastPushedVal!=val){
				//println "Fire Link Listner "+index+" value "+getCurrentPosition()
				fireLinkListener(val);
			}
			lastPushedVal=val
		})
		
	}

	/* (non-Javadoc)
	 * @see com.neuronrobotics.sdk.addons.kinematics.AbstractLink#cacheTargetValueDevice()
	 */
	@Override
	public void cacheTargetValueDevice() {
		device.setValues(index,(float)getTargetValue(),(float)velocityTerm ,(float)gravityCompTerm)
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
		return device.getValues(index)[0];
	}

}

public class PhysicicsDevice extends NonBowlerDevice{
	HIDSimpleComsDevice hidEventEngine;
	DHParameterKinematics physicsSource ;
	int count = 0;
	Closure event = {
	
	
			count ++
			if(count >100){
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
				Matrix jacobian =  chain.getJacobian(jointSpaceVector);
				// convert to the 3x6 marray of doubles for display
				double [][] data = jacobian.getArray();
	
			     ArrayList<TransformNR> intChainLocal = chain.intChain
				ArrayList<TransformNR> stateChainLocal = chain.chain
			     double PreviousOmega = 0
			     double [] corilousTerm =[0,0,0]
				for (int i=0;i<jointSpaceVector.length;i++){
					TransformNR previousTransform = i==0?new TransformNR():intChainLocal.get(i-1)
					
					TransformNR previousTransformTranspose = new TransformNR(previousTransform.getMatrixTransform().transpose() );
					
					RotationNR  perviousRot = previousTransform.getRotation()
					double [][] rotationMatrix = perviousRot.getRotationMatrix() 
					
					TransformNR rotationBetweenZeroAndI = stateChainLocal.get(i)
					TransformNR rotationBetweenZeroAndITranspose = new TransformNR(0,0,0,new TransformNR(rotationBetweenZeroAndI .getMatrixTransform().transpose()).getRotation() )
					
					
					double [][] zVector = [rotationMatrix[2]]				
					Matrix zMatrix = new Matrix(zVector)
					Matrix bRotationToAllignFrames = rotationBetweenZeroAndITranspose.getMatrixTransform().times(zMatrix)
					
					double [][] perviousTerm = [[0,0,0]]
					if(i>0){
						perviousTerm[i-1]=PreviousOmega
					}
					Matrix angularVelocityOfLink = rotationBetweenZeroAndITranspose.times(new Matrix(perviousTerm))
												.add(bRotationToAllignFrames)
					
					corilousTerm[i]=angularVelocityOfLink.get(0,i)
					PreviousOmega= angularVelocityOfLink.get(0,i)
				}
				println corilousTerm
			}
		}
	public PhysicicsDevice(HIDSimpleComsDevice c,DHParameterKinematics  d){
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
	
	HIDSimpleComsDevice d = new HIDSimpleComsDevice(0x3742,0x7)
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
/*
def physics =DeviceManager.getSpecificDevice( "HephaestusPhysics",{
	PhysicicsDevice pd = new PhysicicsDevice(dev,base. getAllDHChains().get(0))
	
	return pd
})
*/

/*
ThreadUtil.wait(100)
while(MobileBaseCadManager.get( base).getProcesIndictor().getProgress()<1){
	ThreadUtil.wait(1000)
	println "Waiting for cad to get to 1:"+MobileBaseCadManager.get(base).getProcesIndictor().getProgress()
	
}*/
// command index 

long sum
int min =100000;
int max =0;
float sinWaveInc =10000
float seconds = 0.005;
float range = -256
dev.setPIDGains(0,(float)0.004,(float)0.002,(float)0.0015)
dev.setPIDGains(1,(float)0.004,(float)0.002,(float)0.0015)
dev.setPIDGains(2,(float)0.006,(float)0.002,(float)0.0015)
dev.pushPIDGains()
dev.setPDVelGains(0,(float)0.1,0)
dev.setPDVelGains(1,(float)0.0001,0)
dev.setPDVelGains(2,(float)0.1,0)
dev.pushPDVelGains()
/*
for( int i=1;i<sinWaveInc;i++){
	for(int j=0;j<3;j++){
		dev.setValues(	(int)j,
					(float)(Math.sin(((float)i)/sinWaveInc*Math.PI*2)* range) -range*1.5,
					(float)((Math.cos(((float)i)/sinWaveInc*Math.PI*2)* range)/seconds),
					(float)0)
		float []returnValues = 	dev.getValues(j)
		//System.out.println("Return data "+ j+" "+ returnValues );								
	}
	Thread.sleep((long)(1000.0*seconds))
}
*/

return null
dev.setVelocity(0,0)
dev.setVelocity(1,(float)(-300+10.0*Math. random()))
dev.setVelocity(2,0)
dev.pushVelocity()
println "Running"
Thread.sleep((long)(1500.0))
println "Stopping"
for(int j=0;j<3;j++){
	dev.setValues(	(int)j,
				(float) 10.0*Math. random(),
				(float)0,
				(float)0)
	float []returnValues = 	dev.getValues(j)
	//System.out.println("Return data "+ j+" "+ returnValues );								
}
println "Done"
return null