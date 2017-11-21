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
import javafx.scene.transform.Affine;

if(args == null){
	def base =DeviceManager.getSpecificDevice( "HephaestusWorkCell",{
		//If the device does not exist, prompt for the connection
		
		MobileBase m = BowlerStudio.loadMobileBaseFromGit(
			"https://github.com/osh1996/SeriesElasticActuator.git",
			"seaArm.xml"
			)
		if(m==null)
			throw new RuntimeException("Arm failed to assemble itself")
		println "Connecting new device robot arm "+m
		return m
	})
	DHParameterKinematics arm = base.getAllDHChains().get(0)
	ArrayList<DHLink> dhLinks=arm.getChain().getLinks();
	DHLink dh = dhLinks.get(0);
	args=[dh,(int)2] // args = [dh_parameters, joint #]  !!!Pass extra parameters into here if I need them from external script
}

 CSG reverseDHValues(CSG incoming,DHLink dh ){
	println "Reversing "+dh
	TransformNR step = new TransformNR(dh.DhStep(0))
	Transform move = TransformFactory.nrToCSG(step)
	return incoming.transformed(move)
}
 CSG moveDHValues(CSG incoming,DHLink dh ){
	TransformNR step = new TransformNR(dh.DhStep(0)).inverse()
	Transform move = TransformFactory.nrToCSG(step)
	return incoming.transformed(move)
	
}

CSG DummyStandInForLink = new Cube(20).toCSG()
//here i need to define the shape(s) that will result in a new link for the arm

return [moveDHValues(DummyStandInForLink,args[0])]