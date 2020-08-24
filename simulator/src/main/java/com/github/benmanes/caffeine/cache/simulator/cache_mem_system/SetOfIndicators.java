package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;

public class SetOfIndicators<K> {
  protected int 	 			cacheSize; //Cache size is used for estimating the # of items to be concurrently stored in the CBF
  protected short 	 		numOfIndicators, numOfSuggestedIndicators; // Number of simulated indicators (in case of a static benchmark), or of possible indicators vals (in some cases of a reconf' alg')
  protected double			minIndicatorSize, maxIndicatorSize;
  protected double			minUpdateInterval;
  protected double 			missp;
  protected double[] 		fprVals; // Will hold the d.fpr values of the indicators
  protected String			policyName;
  protected int					runMode; 			// Mode of the run - see list of possible values (static, various versions of alg' etc.)  
	public 		final int 	IDJmm	= 32; // Our CAB alg' (aka "IDJmm" for historical reasons)   
	public 		final int 	staticConfWithForcedTokens	= 99; // Run a static conf', but enforce token regulation    
  protected int  				verbose; //verbose = -1: print nothing. 0: print Alg's final cost. 1: print detailed output file. 2: additionally, print tikz-formatted output files
  protected boolean 		checkMyPolicyPatch = false; // Check whether the "MyPolicy" indicateInsertion, indicateVictim indeed well dispatch every insertion, eviction.
  protected boolean 		checkParadox = false; // Check and stdout a message when the Bloom Paradox happens
  protected final long 	maxNumOfReq = Long.MAX_VALUE; // When reaching this # of requests, the simulation is stopped.
  protected String			ScndConfFileName;
  protected	boolean 		enforceTokensRegulation = false; 
  protected boolean 		oversubscribedToken = false; 
  
  int 									currIteration, initialIteration; // for multi-iterations runs of the simulation
	protected double 			updateIntervalRatioOfCacheSize;
  protected int 				numOfEventsBetweenUpdates;
  protected long 				reqCnt;
  protected long[]  		indication, invIndication; //indication[i], invIndication[i] will hold the indication of indicator i and its negation, resp.
  protected long[]  		tpCnt;
  protected long[]  		fpCnt;
  protected long[]  		fnCnt;
  protected long[]  		tnCnt;
  protected double 		 	hdrSize; // size of header, added to every update
  protected double [] 	totalBw; // totalBw[i] will store the # of bits used by indicator i's updates
  protected double []		bwInCurReconfInterval, maxBwInReconfInterval;
	protected String 			algMode; 

  // Used for re-conf' alg', that is, alg' that (dynamically) uses the best conf'
  protected final short algIndicatorIdx = 0; // The index of alg's data within data structures, e.g.: stale_indicator[], updated_indicator[], indicatorSize[]
  protected final short tpIdx 					= 0;
  protected final short fpIdx 					= 1;
  protected final short tnIdx 					= 2;
  protected final short fnIdx 					= 3;
  protected final short bwIdx 					= 4;
  
  protected double [] 		algFullSimCnt; // Cntrs of alg's FP, FN etc. (see indices above) along a full sim'
  protected double [] 		algSinceLastReConfCnt; // Cntrs of alg's FP, FN etc. These counters are reset every reconf'
  protected List<Double>	budgetsList; // List of the budgets to run, as indicated by the conf' file. 
  protected double []			missPenalties = {3}; // When running multiple iterations of alg', each run will take one value from the array. Static conf's are ignorant to the miss penalties.
  protected double 				bwBudget; // Budget of BW used (# of bits sent per req. , on avg)
  protected double				token; //number of bits allowed to use for updates at each time segment (T)
  protected boolean 			runReconfAlg; // When true, will run a re-conf' alg'
  protected int		 				reconfInterval; // Will run the re-conf' alg' once in reconfInterval requests 
  protected Configuration curConf, newConf; // Current and previous conf' (indicator size + update interval) used by the re-conf' alg'
  protected double 				deltaUpdateInterval; // multiplicative difference between succeessive update intervals   
  protected double 				deltaIndicatorSize; // mult' gap between sequencing indicator sizes
  protected String 				staticConfOutputFileName, algOutputFileName, algOutputFileName_FP_over_FN, algOutputFileName_u_interval, algOutputFileName_ind_size, algOutputFileName_Bw, algOutputFileName_hitRatio, algOutputFileName_util, algOutputFileName_details;
  protected HashMap<K, Object> 			cache;
  protected boolean				sendOnlyFullIndicators = false; // When true, must always send a full indicator
  protected boolean 			sendFullIndicator; // Becomes true when it's necessary to send a full ind', e.g. at Full Indicator mode, or after each scaling of the ind'
  protected int    []			indSize; // indSize[i] will hold the Size of the indicator i
  protected double []			indSizeDouble; // A double representation of the indicators' sizes, which is more convenient for some calculations.
  protected double []			indSizelgIndSize; // indSizelgIndSize[i] will hold indSizeDouble[i] * log_2 (indSizeDouble[i])
  protected boolean 			runSingleIteration = true;
  
  // Temporal checks, for internal usage 
  protected int 					deltaUpdatesCnt, fullUpdatesCnt;


  // C'tor
  public SetOfIndicators (String policyName) {

  	MyConfig.setNumOfPoliciesRunning();
  	ScndConfFileName = MyConfig.GetFullPathToConfFile() + MyConfig.GetStringParameterFromConfFile("scnd-conf-file-name");
  	
  	this.policyName = policyName;
    // Retrieve parameters' values from the conf' file (application.conf)

    cacheSize 						= MyConfig.GetIntParameterFromConfFile 		("cache-size"); 
    double deltaFpr 			= MyConfig.GetDoubleParameterFromConfFile	("delta-designed-indicator-fpr"); //delta between the values of inherent fpr of indicators
    hdrSize								= MyConfig.GetDoubleParameterFromConfFile	("hdr-size");
  	minUpdateInterval 		= MyConfig.GetDoubleParameterFromConfFile	("minimal-update-interval");
  	verbose								= MyConfig.GetIntParameterFromConfFile 		("verbose"); 
    missp									= MyConfig.GetDoubleParameterFromConfFile	("missp");
    runMode				 				= MyConfig.GetIntParameterFromConfFile		("run-mode");
    deltaIndicatorSize 		= MyConfig.GetDoubleParameterFromConfFile	("delta-indicator-size");
    deltaUpdateInterval		= MyConfig.GetDoubleParameterFromConfFile	("delta-update-interval");
  	minIndicatorSize			= MyConfig.GetDoubleParameterFromConfFile	("minimal-bpe") * (double) (cacheSize);
  	maxIndicatorSize			= MyConfig.GetDoubleParameterFromConfFile	("maximal-bpe") * (double) (cacheSize);
    currIteration 				= MyConfig.getIteration										();
    initialIteration 			= MyConfig.getInitialIteration						();
    budgetsList						= MyConfig.getBudgets();
    
  	if (hdrSize > 0) {
    	MyConfig.printAndExit("You stated a positive header size. Sorry, but a positive header size is currently unsupported\n");
    }
    runReconfAlg 							= (runMode > 1)? true : false; // Check in conf' file whether to run a reconf' alg'  
   
    if (runReconfAlg) {
    	cache = new HashMap<K, Object> ();
    	numOfIndicators 		= 1;
      
    	// The lines below enable running multiple sequencing sims of alg', with different budgets, miss-penalties and regulations (soft / hard token) 
    	if (currIteration < budgetsList.size() * missPenalties.length) {
      	enforceTokensRegulation = true;
        missp										= missPenalties [currIteration / budgetsList.size()];
      }
      else {
      	enforceTokensRegulation = false;
        missp										= missPenalties [(currIteration - budgetsList.size() * missPenalties.length) / budgetsList.size()];
      }     
      
      bwBudget									= budgetsList.get (currIteration % budgetsList.size());    
      setReconfInterval ();
      
      token											= bwBudget * this.reconfInterval; // num of bits allowed to send during a reconf' interval
      
    	algFullSimCnt 						= new double [5];
    	algSinceLastReConfCnt 		= new double [5];
    	selectInitialConf ();
    	genIndicator (curConf.indicatorSize);
    	numOfEventsBetweenUpdates = (int) curConf.updateInterval;
    	switch (runMode) {
    	
    		case IDJmm:
    			algMode = "IDJmm";
    			break;

    		case staticConfWithForcedTokens:
    			algMode = "SFT";
        	enforceTokensRegulation = true;
    			break;
    			
    		default:
    			MyConfig.printAndExit(String.format("Sorry, you chose run-mode %d, that is currently unsupported\n", runMode));
    	}
    	
    	algOutputFileName						  = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".alg";
    	algOutputFileName_FP_over_FN  = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".FP_over_FN.alg";
    	algOutputFileName_u_interval  = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".u_intrvl.alg";
    	algOutputFileName_ind_size	  = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".ind_size.alg";
    	algOutputFileName_Bw				  = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".bw.alg";
    	algOutputFileName_details		  = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".details.alg";
    	algOutputFileName_util			  = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".util.alg";
    	algOutputFileName_hitRatio    = MyConfig.resFileFullPath() + "\\details\\" + algSettingsString() + ".hitRatio.alg";
     	if (verbose >=0) {
    		//printAlgOutputHeader 		();
    	}  
    }
    else { // Running static configurations

    	bwBudget = budgetsList.get(0);
      setReconfInterval ();
  		staticConfOutputFileName = 
  				MyConfig.resFileFullPath () + MyConfig.getTraceName() +   
					String.format ("C%dK.HB%.0f.", 		cacheSize / 1000, this.bwBudget) + policyName + ".res";
//  				MyConfig.resFileFullPath () + MyConfig.getTraceName() +  ( (initialIteration == 0)? 
//  						String.format ("C%dK.", 		cacheSize / 1000) + policyName + ".res" :				
//  						String.format ("C%dK.%s.i%d.", cacheSize / 1000, policyName, initialIteration) + ".res");

      // Retrieve additional parameters' values from the conf' file (application.conf)
      double minFpr 							= MyConfig.GetDoubleParameterFromConfFile	("min-designed-indicator-fpr"); //Min value of inherent fpr;
      Integer numOfIndicatorsInt	= MyConfig.GetIntParameterFromConfFile		("num-of-indicators");
      numOfIndicators 						= numOfIndicatorsInt.shortValue						();
      
      fprVals = new double [numOfIndicators];
      if (minFpr > 0) { // Determine indicators' sizes by the desired fpr
      	Arrays.parallelSetAll (fprVals, i -> minFpr + deltaFpr * i);
      }
    }
  
    indication 	 						= new long 		[numOfIndicators];
    invIndication 					= new long 		[numOfIndicators];
    tpCnt 									= new long 		[numOfIndicators];
    fpCnt 									= new long 		[numOfIndicators];
    tnCnt 									= new long 		[numOfIndicators];
    fnCnt 									= new long 		[numOfIndicators];
    totalBw									= new double 	[numOfIndicators];
    bwInCurReconfInterval 	= new double	[numOfIndicators];
    maxBwInReconfInterval 	= new double	[numOfIndicators];
    calcPossibleIndSizes ();    
  }
  
  protected void setReconfInterval () {
    reconfInterval = (int) (MyConfig.GetIntParameterFromConfFile ("reconf-interval") *Math.max (Math.ceil(maxIndicatorSize / bwBudget), cacheSize)); 
  }
  
	protected String algSettingsString () {
		//String fullIndicatorOrDeltaMode = (this.sendOnlyFullIndicators)? 	"F" 	: "D";  //Full-indicators or Deltas
		return String.format("%s%s.C%dK.M%.0f.%s%.0f.%s", 
				MyConfig.getTraceName(), 
				algMode,
				cacheSize / 1000, 
				missp, 
				(this.enforceTokensRegulation)? "HB" 	: "SB", 
				bwBudget, 
				policyName
		);
	}
	

  // Set the update interval according to the conf' file (application.conf).
  // To be called only when no re-conf' alg' is run 
  protected void setUpdateInterval () {
  	if (currIteration == 0) {
  		numOfEventsBetweenUpdates = (int) minUpdateInterval;
  	}
  	else {
  	numOfEventsBetweenUpdates =
  			(int) (minUpdateInterval * Math.pow (1 + MyConfig.GetDoubleParameterFromConfFile	("delta-update-interval"), (double) (currIteration)));
  	}

  	// For linear scaling of the update interval, comment the line above, and uncomment the lines below
  	// updateIntervalRatioOfCacheSize = currIteration * MyConfig.GetDoubleParameterFromConfFile ("delta-update-interval-ratio-of-cache-size");
  	// numOfEventsBetweenUpdates = (int) (cacheSize * updateIntervalRatioOfCacheSize);   	
  	// numOfEventsBetweenUpdates = (numOfEventsBetweenUpdates > 0)? numOfEventsBetweenUpdates : 1;
    //    if (numOfEventsBetweenUpdates <=0) {
    //    	System.out.printf ("Error: wrong configuration values. num of req between updates = %d", numOfEventsBetweenUpdates);
    //    	System.exit (1);
    //    }
  }

  
  
  // Abstract methods for inserting / removing an item from a single indicator.
  // To be implemented by concrete indicators (e.g., CBF).
  protected void removeFromSinglendicator (int i, K key) {}
  protected void insertToSingleIndicator  (int i, K key) {}
  
  public void remove (K key) {
  	if (this.runReconfAlg) {
  		cache.remove (key);
  		this.removeFromSinglendicator(this.algIndicatorIdx, key);
  	}
  	else {
  		removeFromAllIndicators (key);
  	}
  }

  public void removeFromAllIndicators (K key) {
  	for (short i = 0; i < this.numOfIndicators; i++) {
  		removeFromSinglendicator (i, key);
  	}    
  }
  

  // Queries the last known indicator to the user, in case of enforcing token regulation and oversubscription
  // Returns 1 if the given key is found in this indicator, 0 else. 
  // To be overriden by child classes
  protected int queryLastKnownIndicator (K key, int i) { return -1;}
  
  // Queries a single indicator for a given key.
  // Returns 1 if the given key is found in the indicator, 0 else. 
  // To be overriden by child classes
  protected int querySingleIndicator (int i, K key) {return -1;   }

  // Query all the stale indicators, and set indications[i] to 1 if the given key is found in CBF i, 0 else.
  // Set for each i invIndication[i] to the complement of indication[i]
  protected void queryAll (K key) {
		Arrays.parallelSetAll (indication, 	  i -> querySingleIndicator (i, key)); 		
		Arrays.parallelSetAll (invIndication, i -> 1 - indication[i]);
  }

  // Queries a single CBF for a given key.
  // Sets indications[i] to 1 if the given key is found in CBF i, 0 else.
  // If the key wasn't already in the CBF - insert it
  // To be overriden by child classes
  protected void queryAndInsertSingleIndicator (int i, K key) {}
  
  // 1. Queries all the stale CBFs, and set indications[i] to 1 if the given key is found in CBF i, 0 else.
  // 2. If the given key wasn't already in a given indicator - insert it
  protected void queryAndInsertAll (K key) {
	  for (short i = 0; i < numOfIndicators; i++) {
	  	queryAndInsertSingleIndicator (i, key); 
	  }
  }
  
  // Insert a given key to the indicator(s)
  public void insert (K key) {
  	if (this.runReconfAlg) {
  		cache.put(key, null);
  		this.insertToSingleIndicator (this.algIndicatorIdx, key);
  	}
  	else {
  		insertToAllIndicators (key);
  	}
  }
  
  // Insert a given key to the all indicators
  public void insertToAllIndicators (K key) {
	  for (short i = 0; i < numOfIndicators; i++) {
	  	insertToSingleIndicator (i, key); 
	  }
  }
  
  // Returns the size of indicator i (in CBF the size means: number of counters)    
  public int indicatorSize (int i) {return -1;}
  
  // Returns the number of counters in each CBF, as int[]  
  public int[] numOfCntrs () {return null;} 

  // Returns the number of counters in each CBF, as double[]  
  public double[] numOfCntrsDouble () {return null;} 

  // Returns the number of bits per elements in the indicator, that is, indicatorSize / cacheSize   
  public double[] bitPerElement () {return null;  }
  
  protected void genIndicator (int size) {}

  protected void initReconfAlg() {
  		
  }

  
  // Sets initial conf' within the budget constraints. Called only when a re-conf' alg' is run.
  protected void selectInitialConf () {
  	
  	// Calculate feasible update interval and indicator size
    
  	// For: randomly choose an reasonable initial indicator size 
  	//  	Random rnd 						= new java.util.Random();
  	//  	double rndPower 			= (double) (rnd.nextInt(10) + 1);
  	//  	double indicatorSize 	= Math.pow (1 + deltaIndicatorSize, rndPower) * minIndicatorSize;

    int indicatorSize 			= (int) (this.minIndicatorSize);
    double updateInterval 	= indicatorSize / bwBudget;   
    
    // Insert here specific values for Mode99: staticConfWithForcedTokens
    indicatorSize						= (runMode == staticConfWithForcedTokens)? 283449  : indicatorSize; 
    updateInterval 					= (runMode == staticConfWithForcedTokens)? 14331   : updateInterval; 
    		
  	curConf = new ConfigurationBuilder()
  			.indicatorSize 			(indicatorSize)
				.updateInterval 		(updateInterval)
				.cost 							(0)
				.costOverOptCost 		(Double.MAX_VALUE)
				.buildConfiguration	();
  	 	
  }

  
  // Scale (either -up or -down) the indicator's size by a mult' factor of (1 + deltaIndicatorSize)
  // Calculates the expected BW of the new conf', after the scaling 
  protected void scaleIndicator (int newSize) {}
  
  protected void printCurConf () {
  		// MyConfig.writeStringToFile (algOutputFileName, 						String.format ("%d            %d          FP = %.3f, FN = %.3f, %.2f       %.1f\n", curConf.indicatorSize, (int) curConf.updateInterval, curConf.FP,  curConf.FN, ResAnalyser.normalizedAiServiceCost(curConf.TP,  curConf.FP,  curConf.FN,  missp), curConf.normalizedBw));
  		// MyConfig.writeStringToFile (algOutputFileName, 						String.format ("%.2f  %.2f\n", ResAnalyser.normalizedAiServiceCost(curConf.TP,  curConf.FP,  curConf.FN,  missp), ResAnalyser.AiOverPiServiceCost(curConf.TP, curConf.FP, curConf.FN, missp)));
 			// MyConfig.writeStringToFile (algOutputFileName_FP_over_FN, String.format ("(%d, %.5f)", reqCnt, curConf.FP / curConf.FN));
    	MyConfig.writeStringToFile (algOutputFileName_u_interval, String.format ("(%d, %d)", 	 reqCnt, numOfEventsBetweenUpdates));
    	MyConfig.writeStringToFile (algOutputFileName_ind_size, 	String.format ("(%d, %d)", 	 reqCnt, indicatorSize (algIndicatorIdx)));
    	//MyConfig.writeStringToFile (algOutputFileName_Bw, 				String.format ("(%d, %.3f)", reqCnt, curConf.normalizedBw / bwBudget));
  }
  
  protected void printAlgOutputHeader () {

  	File algOutputFile  = new File (algOutputFileName);

  	if (algOutputFile.exists()){ // Output file doesn't exist yet --> should write the header phrase, detailing the configuration
  		algOutputFile.deleteOnExit();
  	}
		MyConfig.overwriteStringToFile (algOutputFileName, String.format ("//Results for alg %s \n", this.algSettingsString()));
		MyConfig.writeStringToFile 		 (algOutputFileName, String.format ("//*****************************************************************\n"));
		MyConfig.writeStringToFile 		 (algOutputFileName, String.format ("//Ai cost / Ni cost    Ai cost / Pi cost\n"));	
  }
  
  protected int fitIndSizeToRange (double size) {
  	return (int) Math.max (minIndicatorSize, Math.min (size, maxIndicatorSize));
  }

  protected void calcNewIndicatorSizeInFullIndModIDJmm () {
  	newConf.indicatorSize 	= //(int) this.maxIndicatorSize; 
  			  (curConf.FN == 0) ?
  			  (int) this.maxIndicatorSize :
  			  fitIndSizeToRange (Math.floor ( (double) curConf.indicatorSize * Math.sqrt ( curConf.FP / (curConf.FN * (missp-1))) 
  	));
  }
 
  protected void calcNewIndicatorSizeInFullIndMod () {
  	newConf.indicatorSize 	= //(int) this.maxIndicatorSize; 
  			  (curConf.FN == 0) ?
  			  (int) this.maxIndicatorSize :
  			  fitIndSizeToRange (Math.floor ( (double) curConf.indicatorSize * Math.sqrt ( curConf.FP / (curConf.FN * missp)) 
  	));
  }
 
  protected void calcPossibleIndSizes () {
    Integer numOfSuggestedIndicatorsInt	= MyConfig.GetIntParameterFromConfFile ("num-of-indicators"); //if running static configurations, numOfSuggestedIndicators==numOfIndicators. However, if running reconf' alg', we have numOfIndicators==1.
    numOfSuggestedIndicators = numOfSuggestedIndicatorsInt.shortValue();
    indSize											= new int			[numOfSuggestedIndicators];
    indSizeDouble								= new double	[numOfSuggestedIndicators];
    indSizelgIndSize						= new double	[numOfSuggestedIndicators];
  	double minBitsPerElement		= MyConfig.GetDoubleParameterFromConfFile	("minimal-bpe"); // Minimal feasible num of bits in the indicator per a cached element 
  	if (minBitsPerElement < 2) {
  		System.out.printf ("Configuration error: minimal bits per element chosen is %.2f. Please choose a value of at least 2\n", minBitsPerElement);
  	}
  	Arrays.parallelSetAll (indSizeDouble, 		i -> cacheSize * minBitsPerElement * Math.pow(1 + deltaIndicatorSize, i));  
  	Arrays.parallelSetAll (indSizeDouble, 		i -> Math.min (indSizeDouble [i], maxIndicatorSize));
  	Arrays.parallelSetAll (indSizelgIndSize, 	i -> indSizeDouble[i] * Math.log(indSizeDouble[i]) / Math.log(2));
  	Arrays.parallelSetAll (indSize, 					i -> (int) indSizeDouble[i]);
  }

  protected void correctIndSizeInDeltasMode () {
  	double deisredRatio = bwBudget / curConf.normalizedBw;
  	double curIndSize						  	= (double) curConf.indicatorSize;
  	double curIndSizelgCurIndSize 	= curIndSize * Math.log (curIndSize) / Math.log(2); 
  	double [] diffFromdesiredRatio 	= new double	[numOfSuggestedIndicators];
  	Arrays.parallelSetAll (diffFromdesiredRatio, i ->  Math.abs(indSizelgIndSize[i] / curIndSizelgCurIndSize - deisredRatio));
  	
  	short  idxOfMin = 0;
  	double minDiff = diffFromdesiredRatio[0];
  	for (short i=1; i < numOfSuggestedIndicators; i++) {
  		if (diffFromdesiredRatio[i] < minDiff) {
  			minDiff = diffFromdesiredRatio[i];
  			idxOfMin = i;
  		}
  	}
		newConf.indicatorSize = indSize[idxOfMin];
  }

  protected void fitUpdateIntervalToBudgetInFullIndMod () {
  	newConf.updateInterval 	= newConf.indicatorSize / this.bwBudget;  			 	
  }
  
  protected void selectConfIdjmm () {
  	if (InDeltaMode()) {
  		if (curConf.indicatorSize == minIndicatorSize && curConf.normalizedBw > bwBudget) {
  			fitUpdateIntervalToBudgetInFullIndMod ();
        if (verbose == 3 ) {
        	MyConfig.writeStringToFile (algOutputFileName_details, "Jumping out of delta mode\n");
        }

  		}
  		else {
  			correctIndSizeInDeltasMode ();
  			newConf.updateInterval 	= this.minUpdateInterval;
        if (verbose == 3 ) {
        	MyConfig.writeStringToFile (algOutputFileName_details, "Lambert\n");
        }
  		}
  	}
  	else {
 			calcNewIndicatorSizeInFullIndModIDJmm ();
			fitUpdateIntervalToBudgetInFullIndMod ();
      if (verbose == 3 ) {
      	MyConfig.writeStringToFile (algOutputFileName_details, String.format ("Full indicator mode. full updates cnt = %d, delta cnt = %d\n" , fullUpdatesCnt, deltaUpdatesCnt));
      }
  	}
  }
    
  protected boolean InDeltaMode () {
  	return (this.fullUpdatesCnt <= 1);
  }
  
  
  protected void updateConfIntervalCntrs () {
  	algSinceLastReConfCnt [bwIdx] = this.bwInCurReconfInterval [algIndicatorIdx];
    Arrays.parallelSetAll (algFullSimCnt, i -> algFullSimCnt[i]	+ algSinceLastReConfCnt[i]); // Add the cntrs since last re-conf' to the full-sim cntr


    if (verbose >= 1 ) {
    	
    	MyConfig.writeStringToFile 	(algOutputFileName_details, String.format ("reqCnt = %d, FP cnt = %.0f, FN cnt = %.0f, FP/FN = %.2f, indSize = %d, uInterval = %d, ", 
    			this.reqCnt,
    			this.algSinceLastReConfCnt[this.fpIdx], 
    			this.algSinceLastReConfCnt[this.fnIdx],
    			this.algSinceLastReConfCnt[this.fpIdx] / this.algSinceLastReConfCnt[this.fnIdx], 
    			curConf.indicatorSize,
    			(int) curConf.updateInterval
    			));
    }
    Arrays.parallelSetAll 			(algSinceLastReConfCnt, i -> algSinceLastReConfCnt[i] / reconfInterval); // normalize results to prob' values (between 0 and 1)
    curConf.TP 					 	= algSinceLastReConfCnt [tpIdx]; 
    curConf.FP 					 	= algSinceLastReConfCnt [fpIdx]; 
    curConf.TN 					 	= algSinceLastReConfCnt [tnIdx]; 
    curConf.FN 					 	= algSinceLastReConfCnt [fnIdx]; 
    curConf.normalizedBw	= algSinceLastReConfCnt [bwIdx];

    Arrays.parallelSetAll (algSinceLastReConfCnt, i -> 0);
   	
  }
  
  // Check the performance of the current conf' (indicatorSize, updateInterval). If performance aren't "good enough" - 
  // consider switching to a neighbor conf' 
  protected void reConf () {
  	
  updateConfIntervalCntrs ();
   if (verbose >= 1 ) {
    	MyConfig.writeStringToFile (algOutputFileName_details, 		String.format ("util = %.2f, hit ratio = %.2f\n", curConf.normalizedBw / bwBudget, curConf.TP + curConf.FN));
    	if (verbose == 2 || verbose == 3) {
      	MyConfig.writeStringToFile (algOutputFileName_hitRatio, 	String.format ("(%d, %.2f)", 	 reqCnt, curConf.TP + curConf.FN));
      	MyConfig.writeStringToFile (algOutputFileName_util,  String.format ("(%d, %.2f)", 	 reqCnt, curConf.normalizedBw / bwBudget));
    	}
    }

    curConf.updateCosts(missp); // Update both absolute cost and the cost normalized w.r.t. perfect ind'
    
  	if (verbose == 2 || verbose == 3) {
      printCurConf ();
      if (checkParadox && curConf.paradox(missp)) { 
      	System.out.println ("The Paradox happened");
      }     
  	}

  	newConf = curConf.copy();
  	
  	switch (runMode) {
  		 			
  		case IDJmm:
  			this.selectConfIdjmm (); // The little difference between IDJ and IDJmm is within the func' selectConfIdj () itself 
  			break;
  			
  		case staticConfWithForcedTokens: // When Alg is merely checking a concrete static conf' no need to select new conf' 
  			break;
  			
  		default:
  			MyConfig.printAndExit(String.format("Sorry, you chose run-mode %d, that is currently unsupported\n", runMode));
        
  	}
  	if (newConf.indicatorSize != curConf.indicatorSize) {
  		scaleIndicator (newConf.indicatorSize);
  		sendFullIndicator = true; // Next update, will have to send a full indicator
  	}
		curConf 									= newConf.copy();

    numOfEventsBetweenUpdates = (int) (curConf.updateInterval);  		
    curConf.resetStat(); // reset the TP, FP, TN, FN, and costs counts
  	this.deltaUpdatesCnt 	= 0;
  	this.fullUpdatesCnt		= 0;
  	  	
  }

  public void handleRequest (K key, boolean isInCache) {
    reqCnt++;
    if (reqCnt==1) {
    	System.out.printf("%s: runMode = %d. Starting iteration %d with uInterval=%d\n",
    										MyConfig.getTraceName(),
    										this.runMode,
    										currIteration, this.numOfEventsBetweenUpdates);
    }
    if (reqCnt > maxNumOfReq) { 
      MyConfig.printAndExit (String.format("Finished simulating %d requests\n", reqCnt-1));
    }
    
  	if (runReconfAlg) {
  	
  	// Below is a patch for checking whether the "MyPolicy" indicateInsertion, indicateVictim indeed well dispatch every insertion, eviction caused by Caffeine 
    //  		if (checkMyPolicyPatch) { // Check whether the "MyPolicy" indicateInsertion, indicateVictim indeed well dispatch every insertion, eviction.
    //  			boolean isInMyShadowCache = cache.containsKey(key); 
    //  			if (isInCache != isInMyShadowCache) {
    //  				System.out.printf("Bug! InCache = " + isInCache + ", isInMyCache = " + isInMyShadowCache + "\n");
    //  				System.exit(0);
    //  			}
    //  		}
  		
  		int indication = 
  				(oversubscribedToken)? queryLastKnownIndicator(key, algIndicatorIdx) : querySingleIndicator (algIndicatorIdx, key);
  		if (isInCache) { 		
      	algSinceLastReConfCnt [tpIdx] += 		 indication;
      	algSinceLastReConfCnt [fnIdx] += 1 - indication;
      	}
      else {
      	algSinceLastReConfCnt [fpIdx] += 		 indication;
      	algSinceLastReConfCnt [tnIdx] += 1 - indication;
      }
  		if (reqCnt % reconfInterval == 0) {
  	  	maxBwInReconfInterval[algIndicatorIdx] = Math.max (maxBwInReconfInterval[algIndicatorIdx], bwInCurReconfInterval[algIndicatorIdx]);
  	  	totalBw[algIndicatorIdx] += bwInCurReconfInterval[algIndicatorIdx];					
      	reConf ();
  	  	bwInCurReconfInterval[algIndicatorIdx] = 0;
  	  	oversubscribedToken = false; // Start using a new token
  		}
  	}
  	else { // Running static configurations 
   	 	// Query all indicators, and update their stats accordingly 
    	queryAll (key);
    	if (isInCache) { 		
        Arrays.parallelSetAll(tpCnt, i -> tpCnt[i] + indication		[i]); // Increment TP cnt of all indicators with TP indications
        Arrays.parallelSetAll(fnCnt, i -> fnCnt[i] + invIndication[i]); // Increment FN cnt of all indicators with FN indications
    	}
    	else {
        Arrays.parallelSetAll(fpCnt, i -> fpCnt[i] + indication		[i]); // Increment FP Cnt of all indicators with FP indications
        Arrays.parallelSetAll(tnCnt, i -> tnCnt[i] + invIndication[i]); // Increment TN Cnt of all indicators with TN indications
    	}
  		if (reqCnt % reconfInterval == 0) {
  	  	Arrays.parallelSetAll (maxBwInReconfInterval, i -> Math.max (maxBwInReconfInterval[i], bwInCurReconfInterval[i]));
  	  	Arrays.parallelSetAll (totalBw, 							i -> totalBw[i] + bwInCurReconfInterval[i]);					
  	  	Arrays.parallelSetAll (bwInCurReconfInterval, i -> 0);
  		}
  	}
    if (reqCnt % numOfEventsBetweenUpdates == 0) {
      sendUpdate ();
    }
    
  }
  
  
  // Simulate sending an update by copying all the updated indicators to the respective stale indicators
  public void sendUpdate () {  	
  	for (short i = 0; i < numOfIndicators; i++) {
  		updateSingleIndicator (i);
  	}
  }
  
  // Copy updated_indicator[i] to stale_indicator[i]
	protected void updateSingleIndicator (int i) {}

	// Statistics functions
	//////////////////////////////////////////////////////////////////////////
	
  // Returns an array. The entry i in the array is the FP rate of indicator i
  public double[] fpRate() {
  	double[] fpRate = new double[numOfIndicators]; 	
    long[] missCnt = new long [numOfIndicators];
    Arrays.parallelSetAll (missCnt, i -> tnCnt[i] + fpCnt[i]);
    Arrays.parallelSetAll (fpRate, i -> (missCnt[i] == 0) ? 0.0 : (double) fpCnt[i] / missCnt[i]); 
  	return fpRate;
  }
  
  // returns the ratio of FP which happened due to staleness  
//  public double stalenessFpRate () {
//    return (missCount == 0) ? 0.0 : (double) stalenessFpCnt / missCount;
//  }

  // Returns an array. The entry i in the array is the FN rate of indicator i
  public double[] fnRate() {
  	
  	double [] fnRate = new double [numOfIndicators]; 	
    long	 [] hitCnt = new long		[numOfIndicators]; 	
    Arrays.parallelSetAll (hitCnt, i -> tpCnt[i] + fnCnt[i]);
    Arrays.parallelSetAll (fnRate, i -> (hitCnt[i] == 0) ? 1.0 : (double) fnCnt[i] / hitCnt[i]);
  	return fnRate;
  }
  
  // Returns the ratio: true positive events over the total # of requests
  public double[] tpFromReqRate() {
  	
  	double[] res = new double[numOfIndicators]; 	
    Arrays.parallelSetAll(res, i -> (double) tpCnt[i] / reqCnt); 		 
    return res;
  }

  // Returns the ratio: falst positive events over the total # of requests
  public double[] fpFromReqRate() {
  	
  	double[] res = new double[numOfIndicators]; 	
    Arrays.parallelSetAll(res, i -> (double) fpCnt[i] / reqCnt); 		 
    return res;
  }

  // Returns the ratio: true negative events over the total # of requests
  public double[] tnFromReqRate() {
  	
  	double[] res = new double[numOfIndicators]; 	
    Arrays.parallelSetAll(res, i -> (double) tnCnt[i] / reqCnt); 		 
    return res;
  }
  
  // Returns the ratio: false negative events over the total # of requests
  public double[] fnFromReqRate() {
  	
  	double[] res = new double[numOfIndicators]; 	
    Arrays.parallelSetAll(res, i -> (double) fnCnt[i] / reqCnt); 		 
    return res;
  }

  // Returns the cost of indicator i caused by missindications (FP or FN).
  public double nonCompulsoryCost (int i) {
  	return ( (double) fpCnt[i] + (double) fnCnt[i] * missp) / (double) reqCnt;
  }

  // Returns the cost of indicator i caused by missindications (FP or FN), normalized w.r.t. the compulsory cost, namely, the cost obtained by a perfect ind' 
  public double normalizedNonCompulsoryCost (int i) {
  	return nonCompulsoryCost(i) / ( 1 + (1 - ((double)tpCnt[i] + (double)fnCnt[i])) * (missp-1) );
  }
  
  // The hit rate obtained by a Perfect indicator.
  // This hit rate is calculated as the number of requests found in the cache over the total number of requests.
  // Hence, it is the TP rate + FN rate. 
  public double[] PiHitRate() {
  	double[] res = new double[numOfIndicators]; 	
    Arrays.parallelSetAll(res, i -> tpFromReqRate ()[i] + fnFromReqRate()[i]);
    return res;
  }

  // Returns the expected service cost of a NI (No Indicator) configuration
  public double[] niServiceCost (double missp) {
  	double[] res = new double[numOfIndicators];
  	double[] PiHitRate = PiHitRate ();
    Arrays.parallelSetAll(res, i -> 1 + (1 - PiHitRate[i]) * missp); 		 
    return res; 
  }
  
  // Returns the expected service cost of a perfect indicator
  public double[] piServiceCost (double missp) {
  	double[] piServiceCost = new double[numOfIndicators];
  	double[] PiHitRate = PiHitRate ();
    Arrays.parallelSetAll (piServiceCost, i -> 1 + (1 - PiHitRate[i]) * (missp-1)); 		 
    return piServiceCost;
  }

  // Returns the expected service cost of an approximate indicator, normalized w.r.t. the service cost of No Indicator
  public double[] normalizedPiServiceCost (double missp) {
  	double[] res 					 = piServiceCost (missp); 	
  	double[] niServiceCost = niServiceCost (missp);
    Arrays.parallelSetAll (res, i -> res[i] / niServiceCost [i]);
    return res;
  }

  // Returns the expected service cost of an approximate indicator
  public double[] aiServiceCost (double missp) {
  	double[] res 						= new double[numOfIndicators]; 	
  	double[] tpFromReqRate 	= tpFromReqRate();
  	double[] fpFromReqRate 	= fpFromReqRate();
    Arrays.parallelSetAll (res, i -> tpFromReqRate[i] + fpFromReqRate[i] + (1 - tpFromReqRate[i]) * missp);
    return res;
  }

  // Returns the expected service cost of an approximate indicator, normalized w.r.t. the service cost of No Indicator
  public double[] normalizedAiServiceCost (double missp) {
  	double[] res 					 = aiServiceCost (missp); 	
  	double[] niServiceCost = niServiceCost (missp);
    Arrays.parallelSetAll (res, i -> res[i] / niServiceCost [i]);
    return res;
  }
  
	protected void checkResults () {
		if (reqCnt < 12 * cacheSize) {
			System.out.println ("Warning: this trace is too short for this cache");
		}
  	if ( reqCnt < reconfInterval) {
  		System.out.println ("Warning: this trace is too short for this reconf' interval");
  	}
	}

	protected void printOutputFileHeader (String outputFileName) {

		File outputFile = new File (outputFileName);
  	
  	if (!outputFile.exists()){ // Output file doesn't exist yet --> should write the header phrase, detailing the configuration
  		MyConfig.writeStringToFile (outputFileName, String.format ("//Cache size = %d num of requests = %d\n",
					 cacheSize, 		 reqCnt));
  		MyConfig.writeStringToFile (outputFileName, String.format ("//**************************************************************************************************************\n"));
  		MyConfig.writeStringToFile (outputFileName, String.format ("//indSize  u.Interval    TP      FP      TN       FN      meanBW     maxBw\n"));
  	}		
	}
	
	public void printBenchmarkRunReport () {
		
		printOutputFileHeader 			(staticConfOutputFileName);
   	MyConfig.writeStringToFile 	(staticConfOutputFileName, "\n");
   	
   	double[] tpFromReqRate = tpFromReqRate();
		double[] fpFromReqRate = fpFromReqRate();
		double[] tnFromReqRate = tnFromReqRate();
		double[] fnFromReqRate = fnFromReqRate();
		int   [] indicatorSize = new int 	 [numOfIndicators];
		double[] normalizedBw  = new double[numOfIndicators];
		Arrays.parallelSetAll (normalizedBw, i -> totalBw[i] / (double) reqCnt);
		Arrays.parallelSetAll (indicatorSize, i -> indicatorSize(i));
		
		for (short i = 0; i < numOfIndicators; i++) {
			MyConfig.writeStringToFile (staticConfOutputFileName, String.format ("%d     %d         %.5f  %.5f  %.5f  %.5f  %.3f   %.3f\n",
					indicatorSize [i],
					numOfEventsBetweenUpdates, 
					tpFromReqRate[i], fpFromReqRate[i], tnFromReqRate[i], fnFromReqRate[i], normalizedBw[i], maxBwInReconfInterval[i] / this.reconfInterval));
		}
		if (this.maxBwInReconfInterval[numOfIndicators-1] / reconfInterval < budgetsList.get(0)) {
			System.out.printf("maxBw = %.2f. ", maxBwInReconfInterval[numOfIndicators-1] / reconfInterval);
			System.out.println ("Policy " + this.policyName + " simulated enough iterations. All further iterations won't obtain lower cost");
			if (MyConfig.decNumOfPoliciesRunning() == 0) {
				System.out.print ("Stopped simulation because all further iterations won't obtain lower cost\n");
				System.exit(0);
			}
		}

	}
	
	protected void printAlgRunReport () {
		
  	if (verbose == 2 || verbose == 3) {
  		//MyConfig.writeStringToFile (algOutputFileName_FP_over_FN, String.format ("\n"));
     	MyConfig.writeStringToFile (algOutputFileName_u_interval, String.format ("\n"));
     	MyConfig.writeStringToFile (algOutputFileName_ind_size, 	String.format ("\n"));
     	//MyConfig.writeStringToFile (algOutputFileName_Bw, 				String.format ("\n"));
  	}
		double TP = algFullSimCnt[tpIdx] / (double) reqCnt;
		double FP = algFullSimCnt[fpIdx] / (double) reqCnt;
		double FN = algFullSimCnt[fnIdx] / (double) reqCnt;
		
		if (runMode == this.staticConfWithForcedTokens ) {
			System.out.printf("%s | cost = %.4f | indSize = %d, uInterval = %d |  FP = %.4f, FN = %.4f\n", 
												algSettingsString(), ResAnalyser.aiServiceCost (TP, FP, missp), 
												curConf.indicatorSize, this.numOfEventsBetweenUpdates,
												FP, FN);
			return;
		}
		MyConfig.writeStringToFile (MyConfig.resFileFullPath () + MyConfig.getTraceName() + "res", 				
				algSettingsString() +
				String.format (" | cost = %.4f", ResAnalyser.aiServiceCost (TP, FP, missp)) +
				String.format (" | meanBw used = %.2f", this.totalBw[this.algIndicatorIdx] / (double) this.reqCnt) +
				String.format (" | hit ratio = %.4f\n", TP  + FN));
				
	}
	
  public void finished (PolicyStats policyStats) {
  	
  	checkResults (); // Basic sanity check
  	Arrays.parallelSetAll (totalBw, i -> totalBw[i] + bwInCurReconfInterval[i]); // Add the bw of the last (possibly incomplete) reconf' interval					
    System.out.printf ("Simulated %d requests\n", reqCnt);

    if (runReconfAlg) { 
    	updateConfIntervalCntrs (); //Add the data of the last (possibly unfinished) reconf' interval to the total cntrs 
    	if (verbose >= 0) {
    		printAlgRunReport ();
    	}
    	if (this.runSingleIteration || (currIteration >= 2 * budgetsList.size() * missPenalties.length) ) {
  			System.out.println ("Policy " + this.policyName + " of Alg simulated all required settings");
    	}
  		return;
  	}
  	
  	printBenchmarkRunReport ();
  	
  }

}
