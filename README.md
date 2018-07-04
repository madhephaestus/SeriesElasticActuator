# SeriesElasticActuator
A cad generator for series elastic actuator modules

# Prerequisites

PC with Ubuntu 16.04 64 bit, 16gb of ram and a discrete graphics card with OpenGL 3.2+ support. 
# Start Simulation
To simulate and control the arm, after programming and calibrating the arm (see notes in FW comments, calibrating the zeros of the arm is lab one) run BowlerStudio
From here: 
http://commonwealthrobotics.com/#downloads
or directly from the PPA:
	sudo add-apt-repository ppa:webupd8team/java
	sudo add-apt-repository ppa:mad-hephaestus/commonwealthrobotics -y
	sudo apt-get update
	sudo apt-get remove arduino bowlerstudio 
	sudo apt-get install bowlerstudio oracle-java8-set-default oracle-java8-installer arduino-ide
To open the arm, run BowlerStudio and From the Add 

Device Menu-> Creatures -> Hephaestus Arm

This will open a simulation of the arm. 
# Build an arm work cell
The BOM and printable files are here: https://github.com/madhephaestus/SeriesElasticActuator/releases/tag/0.1.6
The 3d printed parts should be printed at 60% infill. The SVG should be cut from 6.1mm wood (roughly !/4 inch). Assembly instruction document is here:
https://github.com/madhephaestus/SeriesElasticActuator/blob/master/assembly/RBE3001_Robot_Assembling_Guide.pdf
Its missing the intermediate step assemblies, with the load cell, make sure you have a dowel pin in the middle of the link, and rout the load cell wires up through the washer along the shaft. 
## Off The Shelf ( OTS ) electronics Alternate build
For a full OTS build, you can replace the Axis Encoder Board (custom board) with One of these:
https://www.digikey.com/product-detail/en/ams/AS5055A-QF_EK_AB/AS5055A-DK-AB-ND/4764625
and one of these: 
https://www.sparkfun.com/products/13879

You will need to modify the FW to use the I2c load cell. If you choose not to use the load cell, you will still need the device installed for the mechanical system to work. 

The Motherboard can replaced with hand wiring a breadboard using the wiring information in this file:
https://github.com/WPIRoboticsEngineering/RBE3001_nucleo_firmware/blob/master/src/main.h

# Firmware
The firmware for the nucleo can be found here:
https://github.com/WPIRoboticsEngineering/RBE3001_nucleo_firmware

Follow the instructions in the readme to setup and install the firmware. 

The arm needs to be calibrated out of the box, the orientation of the magnet is unknown at the time of assembly, so you need to change one line in the header file of the firmware. That value is derived by placing the arm in the home position, indicated by the pose the simulation starts up in. These values should be read as zero, but will have some random value based on how the magnet is glued in. 
# Connect Arm to Kinematics and Simulation
To launch the hardware interface, in BowlerStudio in the My Devices tab, click Disconnect all. Then open 

File Menu->Load File->gitcache/github.com/madhephaestus/SeriesElasticActuator/FullHardwareLaunch.groovy		


You will see a green Run button for the file, click that with the nucleo connected to the computer. It will load the model of the arm again, except this time the position of the arm will come from the hardware. To set set points in the tab called HephaestusWorkCell, expand the Arms menu and click on Hephaestus Arm, expand Current Position and use the slider or type values to move the arm.
