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
def dev = DeviceManager.getSpecificDevice( "hidbowler",{return null})	

dev.setPIDGains(0,0.0005, 0, 0)
dev.setPIDGains(1,0.001, 0, 0)
dev.setPIDGains(2,0.001, 0, 0)
dev.pushPIDGains()
								
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
float gain = -1.0;

IJInputEventListener listener = new IJInputEventListener() {
	@Override public void onEvent(Component comp, Event event1,float value, String eventString) {
		

		try{
			//float vel = (Math.pow((double)2.0,value*gain))-1;
			float vel = value*gain;
			if (Math.abs(vel)<0.001) vel=0.0;
			//System.out.println("v is value= "+value);
			if(comp.getName().equals("X Axis")){
				yvelocity=0
				xvelocity=0
				zvelocity=0
				//System.out.println(comp.getName()+" is value= "+vel);
				yvelocity = vel;
				
			} 
			if(comp.getName().equals("Y Axis")){
				yvelocity=0
				xvelocity=0
				zvelocity=0
				//System.out.println(comp.getName()+" is value= "+vel);
				xvelocity = vel;
				
			}
			if(comp.getName().equals("Z Rotation")){
				yvelocity=0
				xvelocity=0
				zvelocity=0
				//System.out.println(comp.getName()+" is value= "+vel);
				zvelocity = vel;
				
			}
			println current
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
		try {
			current.translateX(xvelocity);
			current.translateY(yvelocity);
			current.translateZ(zvelocity);	
			if (current.getX()>250) current.setX(250);
			if (current.getY()>100) current.setY(100);
			if (current.getZ()>320) current.setZ(320);
			if (current.getX()<95) current.setX(95);
			if (current.getY()<-100) current.setY(-100);
			if (current.getZ()<20) current.setZ(20);
			
		
			limb.setDesiredTaskSpaceTransform(current,  0.001);
		} catch(Exception e){
			BowlerStudio.printStackTrace(e)
		}
	ThreadUtil.wait(20);
	//dyio.flush(0)
}
//remove listener and exit
g.removeListeners(listener);