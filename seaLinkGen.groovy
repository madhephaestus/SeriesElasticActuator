return ScriptingEngine.gitScriptRun(
            "https://github.com/madhephaestus/SeriesElasticActuator.git", // git location of the library
            "LinkedGearedCadGen.groovy" , // file to load
            // Parameters passed to the funcetion
            [	  [36,// Number of teeth gear a link 0
	            84],// Number of teeth gear b link 0
	            [36,// Number of teeth gear a link 1
	            84],// Number of teeth gear b link 1
	            [36,// Number of teeth gear a link 2
	            84],// Number of teeth gear b link 2
            ]
            )


