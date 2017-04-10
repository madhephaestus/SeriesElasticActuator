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
	String type= "compressionSpring"
	if(args==null)
		args=["Compression-dummy"]
	StringParameter size = new StringParameter(	type+" Default",
										args.get(0),
										Vitamins.listVitaminSizes(type))
	//println "Database loaded "+database
	HashMap<String,Object> config = Vitamins.getConfiguration( type,size.getStrValue())
	double height = config.relaxedLength
	double coreDelta= config.od
	
	CSG core  =new Cylinder(coreDelta/2,
						coreDelta/2,
						height,(int)30).toCSG() // a one line Cylinder
				.difference(new Cylinder(config.id/2,config.id/2,height,(int)30).toCSG())
	
	return core
		.setParameter(size)
		.setRegenerate({getNut()})
}
return getNut()
