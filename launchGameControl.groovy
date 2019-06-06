//Your code here
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

def base =ScriptingEngine.gitScriptRun(	"https://github.com/madhephaestus/SeriesElasticActuator.git", 
								"LaunchHardware.groovy", 
									null);
DHParameterKinematics limb = base.getAllDHChains().get(0)

def g  = DeviceManager.getSpecificDevice( "gamepad",{
	for(def control: ControllerEnvironment.getDefaultEnvironment().getControllers()){
		println control.getName()
		if(control.getName().equals("Wireless Controller")){
			def game = new BowlerJInputDevice(control); // This is the DyIO to talk to.
			game.connect();
			return game
		}
		
	}
	throw new RuntimeException("No controller found")
})

TransformNR current = limb.getCurrentPoseTarget();
float xvelocity = 0.0;
float yvelocity = 0.0;
float zvelocity = 0.0;
float gain = 0.2;

IJInputEventListener listener = new IJInputEventListener() {
	@Override public void onEvent(Component comp, Event event1,float value, String eventString) {
		

		try{
			float vel = (Math.pow((double)2.0,value*gain))-1;
			if (Math.abs(vel)<0.001) vel=0.0;
			System.out.println("v is value= "+value);
			if(comp.getName().equals("X Axis")){
				System.out.println(comp.getName()+" is value= "+vel);
				yvelocity = vel;
				//pan.getChannel().setCachedValue(val);
			} 
			if(comp.getName().equals("Y Axis")){
				System.out.println(comp.getName()+" is value= "+vel);
				xvelocity = vel;
				//tilt.getChannel().setCachedValue(val);
			}
			if(comp.getName().equals("Z Rotation")){
				System.out.println(comp.getName()+" is value= "+vel);
				zvelocity = vel;
				//tilt.getChannel().setCachedValue(val);
			}
		}catch(Exception e){
			e.printStackTrace(System.out)
		}
		
		//System.out.println(comp.getName()+" is value= "+value);
	}
}
g.clearListeners()
// gamepad is a BowlerJInputDevice
g.addListeners(listener);
// wait while the application is not stopped
while(!Thread.interrupted()){
	current.translateX(xvelocity);
	current.translateY(yvelocity);
	current.translateZ(zvelocity);	
	/*if (current.getX()>100) current.setX(100);
	if (current.getY()>100) current.setY(100);
	if (current.getZ()>100) current.setZ(100);
	if (current.getX()<-100) current.setX(-100);
	if (current.getY()<-100) current.setY(-100);
	if (current.getZ()<-100) current.setZ(-100);*/
	try {
	//limb.setDesiredTaskSpaceTransform(current,  0.001);
	
	} catch(Exception e){
		
	}
	ThreadUtil.wait(5);
	//dyio.flush(0)
}
//remove listener and exit
g.removeListeners(listener);