//Your code here
import com.neuronrobotics.sdk.addons.gamepad.IJInputEventListener;
import com.neuronrobotics.sdk.addons.gamepad.BowlerJInputDevice;
import net.java.games.input.Component;
import net.java.games.input.Event;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

def limb =ScriptingEngine.gitScriptRun(	"https://github.com/madhephaestus/SeriesElasticActuator.git", 
								"LaunchHardware.groovy", 
									null);
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

IJInputEventListener listener = new IJInputEventListener() {
	@Override public void onEvent(Component comp, Event event1,float value, String eventString) {
		/*
		int val =(int)(127*value+128)
		try{
			if(comp.getName().equals("x")){
				//System.out.println(comp.getName()+" is value= "+value);
				pan.getChannel().setCachedValue(val);
			}
			if(comp.getName().equals("y")){
				//System.out.println(comp.getName()+" is value= "+value);
				tilt.getChannel().setCachedValue(val);
			}
		}catch(Exception e){
			e.printStackTrace(System.out)
		}
		*/
		System.out.println(comp.getName()+" is value= "+value);
	}
}
g.clearListeners()
// gamepad is a BowlerJInputDevice
g.addListeners(listener);
// wait while the application is not stopped
while(!Thread.interrupted()){
	ThreadUtil.wait(20)
	//dyio.flush(0)
}
//remove listener and exit
g.removeListeners(listener);