import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;

import javafx.scene.paint.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.mihosoft.vrl.v3d.parametrics.*;
CSG getNut(){
	LengthParameter printerOffset 		= new LengthParameter("printerOffset",0.5,[1.2,0])
	String type= "torsionSpring"
	if(args==null)
		args=["Torsion-9271K133"]
	StringParameter size = new StringParameter(	type+" Default",
										args.get(0),
										Vitamins.listVitaminSizes(type))
	//println "Database loaded "+database
	HashMap<String,Object> config = Vitamins.getConfiguration( type,size.getStrValue())
	double height = config.wireDiameter * (config.numOfCoils+1)
	double coreDelta= config.od+(config.od - config.id - config.wireDiameter) +printerOffset.getMM()
	
	CSG core  =new Cylinder(coreDelta/2,
						coreDelta/2,
						height,(int)30).toCSG() // a one line Cylinder
				.difference(new Cylinder(config.id/2,config.id/2,height,(int)30).toCSG())
	CSG leg = new Cube(	config.legLength+(printerOffset.getMM()*2),
					config.wireDiameter+printerOffset.getMM(),
					config.wireDiameter+printerOffset.getMM()).toCSG()
				.toZMin()
				.toXMin()
				.toYMin()
				.movey(-config.od/2- printerOffset.getMM()/2)
	

	core = core.union(leg)
			 .union(leg
			 		.toZMax()
			 		.movey(config.od-config.wireDiameter)
			 		.rotz(-config.deflectionAngle)
			 		.movez(height)
			 		)
	
	return core
		.setParameter(size)
		.setRegenerate({getNut()})
}
return getNut()
