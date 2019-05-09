@GrabResolver(name='sonatype', root='https://oss.sonatype.org/content/repositories/releases/')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava', version='0.10.1')
@Grab(group='com.neuronrobotics', module='SimplePacketComsJava-HID', version='0.10.0')
@Grab(group='org.hid4java', module='hid4java', version='0.5.0')

import org.hid4java.*
import org.hid4java.event.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

public class HIDSimpleComsDevice extends NonBowlerDevice{
	HashMap<Integer,ArrayList<Closure>> events = new HashMap<>()
	HidServices hidServices = null;
	int vid =0 ;
	int pid =0;
	HidDevice hidDevice=null;
	public PacketProcessor processor= new PacketProcessor();
	float [] downstream = new float[15]
	float [] upstream = new float[15]
	boolean HIDconnected = false;
	int idOfCommand=37;
	public HIDSimpleComsDevice(int vidIn, int pidIn){
		// constructor
		vid=vidIn
		pid=pidIn
		setScriptingName("hidbowler")
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
				for(int i=0;i<10;i++){
					Thread.sleep(1)
					if(hidDevice!=null){
						//println "Writing packet"
						try{
							byte[] message = processor.command(idOfCommand,downstream)
							//println "Writing: "+ message
							int val = hidDevice.write(message, message.length, (byte) 0);
							if(val>0){
								int read = hidDevice.read(message, 1000);
								if(read>0){
									//println "Parsing packet"
									//println "read: "+ message
									upstream=processor.parse(message)
								}else{
									println "Read failed"	
								}
								
							}
						}catch (Throwable t){
							t.printStackTrace(System.out)
							disconnect()
						}
					}else{
						//println "Simulation"
						for(int j=0;j<downstream.length&&j<upstream.length;j++){
							upstream[j]=downstream[j];
						}
						
					}
				}
				//println "updaing "+upstream+" downstream "+downstream
				try{
					if(events.get(idOfCommand)!=null){
						for(Closure e:events.get(idOfCommand)){
								e.call()
							
						}
					}
				}catch (Throwable t){
							t.printStackTrace(System.err)
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
		downstream[(index*3)+0] = position
		downstream[(index*3)+1] = velocity
		downstream[(index*3)+2] = force
		//println "Setting Downstream "+downstream
	}
	float [] getValues(int index){
		float [] back = new float [3];
	
		back[0]=upstream[(index*3)+0]
		back[1]=upstream[(index*3)+1]
		back[2]=upstream[(index*3)+2]
		
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
		device.setValues(index,(float)getTargetValue(),(float)0,(float)0)
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

ThreadUtil.wait(100)
while(MobileBaseCadManager.get( base).getProcesIndictor().getProgress()<1){
	ThreadUtil.wait(1000)
	println "Waiting for cad to get to 1:"+MobileBaseCadManager.get(base).getProcesIndictor().getProgress()
	
}

// command index 

long sum
int min =100000;
int max =0;
float sinWaveInc =2000
float seconds = 0.01;
float range = -256
for( int i=1;i<sinWaveInc;i++){
	for(int j=0;j<3;j++){
		dev.setValues(	(int)j,
					(float)(Math.sin(((float)i)/sinWaveInc*Math.PI*2)* range) -range,
					(float)((Math.cos(((float)i)/sinWaveInc*Math.PI*2)* range)/seconds),
					(float)0)
		float []returnValues = 	dev.getValues(j)
		System.out.println("Return data "+ j+" "+ returnValues );								
	}
	Thread.sleep((long)(1000.0*seconds))
}

return null