/*
Motor 1=Motor("vexMotor-393", 1.637, 10.471, 14.99, 0.0945)
Motor 2=Motor("vexMotor-393", 1.637, 10.471, 14.99, 0.0945)
Motor 3=Motor("stepperMotor-Pololu35x26", 0.098, 139.626, 12.95, 0.12)
Link 1=100.0
Link 2=170.0
Link 3=130.0
Gear ratio 1=0.33206607726344173
Gear ratio 2=0.8486810156880156
Gear ratio 3=0.1529117565209915
Fitness=-42.93
Constraint values=[-2.9684137354115947, -0.2578238182300667, -0.0034609136279283303, -0.9770638950254984, -5.553205581935878, -13.658149223692268, -0.0001, -7.069767991857092]
Feasible=true
*/

return ScriptingEngine.gitScriptRun(
            "https://github.com/NotOctogonapus/SeriesElasticActuator.git", // git location of the library
            "LinkedGearedCadGen.groovy" , // file to load
            // Parameters passed to the funcetion
            [	  [36,// Number of teeth gear a link 0
	            108],// Number of teeth gear b link 0
	            [36,// Number of teeth gear a link 1
	            108],// Number of teeth gear b link 1
	            [36,// Number of teeth gear a link 2
	            108],// Number of teeth gear b link 2
            ]
            )
