package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.checkerframework.checker.units.qual.K;
import java.util.HashMap;

//Tasks performed by ResAnalyzer. See further details in the documentation of 
// simulator/src/main/resources/application.conf parameter "run-mode".
enum Tasks {  
//	costHeatmap, 								// cost heatmap as func of d.fpr, update interval. 
	//budgetPareto, 							// find best conf' in budget bin
	//fprOverFnrHeatmap, 					// fpr / fnr heatmap as func of d.fpr, update interval.
	//BestConfPerBudget,					// best conf' within a budget each bin
	//fprOverFnrClosestInBudget,	// for each budget bin, the single conf' closest to the requested fpr/fnr golden ratio (usually this ratio is missp).
	//costByFprFnrHeatmap, 
	//bestWorstConfInBin
	//costByIndSize,
	// softBudgetBenchmark,
	// hardBudgetBenchmark,
	// costOfWorstStatic, 
	//	 FppPerUpdateInterval, 
	// 	 FnpPerUpdateInterval, 
		 FnrPerStaleness,
     FprPerStaleness,
	//  allBestConfPerBudget,
	//  barAlgVsStatic,
	// barAlgVsStaticForVariousBudgets
};
 
//enum Yaxis {updateInterval, cost};

public class ResAnalyser {

	int outputType;
	// Indices of the fields within the output of a static configuration
	static final short 	idxOfDesignedFprField    	= 0;
	static final short 	idxOfIndSizeField    			= 0;
	static final short 	idxOfUpdateIntervalField 	= 1;
	static final short 	idxOfTpField 							= 2;
	static final short 	idxOfFpField 							= 3;
	static final short 	idxOfTnField 							= 4;
	static final short 	idxOfFnField 							= 5;
	static final short 	idxOfMeanBwField   				= 6;
	static final short 	idxOfMaxBwField   				= 7;
	
	// Indices of the fields within the standard naming convention
	static final short 	idxOfTrace								= 0;
	static final short 	idxOfRunMode							= 1;
	static final short 	idxOfCacheSize						= 2;
	static final short 	idxOfMissPenalty					= 3;
	static final short 	idxOfBudget								= 4;
	static final short 	idxOfPolicy								= 5;
	static final short	idxOfHitRatio							= 3;

	//boolean parameters and variables, indicating whether to use soft / hard budget, and whether the func FpFnPerUpdateInterval() whould print the FP, or the FN.
	final  int		     	printFpp 									= 1; // print the False Positive prob', as a func of the uInterval
	final  int		     	printFnp 									= 2; // print the False Negative prob', as a func of the uInterval
	final  int		     	printStaleFpr							= -1; // print the False Negative Rate, as a func of the staleness (# req. since last update)
	final  int		     	printStaleFnr							= -2; // print the False Negative Rate, as a func of the staleness (# req. since last update)
	final  boolean		 	softBudgetMod							= true;													
	final  boolean		 	hardBudgetMod							= false;													
	protected String		trace;
	protected String		runMode;
	protected String		cacheSizeToken;
	protected String		policy;
	protected String 		budgetToken;
	protected int				cacheSize;
	protected boolean		budgetMod;
	protected double		budget;	
	
	
	private int 				numOfBins;
	private int 				numOfBwBins;
	private boolean			discoveredAllBins = false;
	private double 			deltaBudget; //Gap between budgets of subsequent bins 
	private double 			maxBinnedBw;
	private double [] 	budgetBin; //budgetBin[i]  will hold the max bw budget for configurations in this bins
	private double 			TP, FP, TN, FN; // True Positive, False Positive, True Negative, False Negative - all out of request. Namely, TP + FP + TN + FN == 1. 
	public  double 			missp; // Miss penalty
	private double 			meanBw, maxBw, cost;
	private double 			hitRatio; 
	private int 				bwBin;
	private double 			designedFpr, indicatorSize, updateInterval;
	private int 				numOfIndicators;
	private double 			deltaFpr; 
  Set     <Double> 		binSet = new HashSet <Double> ();	// Set of identifier for the bin - e.g., indicatorSize, d.fpr, updateInterval
  List    <Double>		binList; // List of the bins
  protected double[] 	budgets = {20};
	int									numOfBudgets; 
	
	HashMap<String, Double> points; 				 // Points of final results (costs), to be printed as bar. 	
	Configuration[] BestConfigurationInBin;  // BestConfigurationInBin[i] will hold the Conf' (d.fpr, update interval, budget, cost) with the minimal cost in budgetBin[i]
	Configuration[] WorstConfigurationInBin; // WorstConfigurationInBin[i] will hold the Conf' (d.fpr, update interval, budget, cost) with the minimal cost in budgetBin[i]
  Vector<Vector <Configuration>> confsInBin 	 = new Vector<Vector<Configuration>>(); // confsInBin [i] will hold all the conf's whose belonging to bin [i]
  Vector<Vector <Configuration>> confsInFprBin = new Vector<Vector<Configuration>>(); 

  private static final String[] addPlotStr	 = {String.format ("%s\n", "\\addplot[color=black, 	mark=triangle, 	width = \\plotwidth] coordinates {"),
                                        	    	String.format ("%s\n", "\\addplot[color=red, 	 	mark=o, 				width = \\plotwidth] coordinates {"),
                                        	    	String.format ("%s\n", "\\addplot[color=blue,  	mark=x, 				width = \\plotwidth] coordinates {"),
  																							String.format ("%s\n", "\\addplot[color=purple, mark=+, 				width = \\plotwidth] coordinates {")};

  // C'tor
	public ResAnalyser () {

		// Read some parameters from the conf' file ("application.conf")
		outputType 			= MyConfig.GetIntParameterFromConfFile 		("run-mode"); //the concrete tasks to run,  
    numOfIndicators = MyConfig.GetIntParameterFromConfFile		("num-of-indicators");
		numOfBwBins 		= MyConfig.GetIntParameterFromConfFile 		("num-of-bw-bins");
		deltaBudget			= MyConfig.GetDoubleParameterFromConfFile ("delta-budget"); // Difference between sequencing budgets
		missp						= MyConfig.GetDoubleParameterFromConfFile ("missp");
    deltaFpr 				= MyConfig.GetDoubleParameterFromConfFile	("delta-designed-indicator-fpr"); //delta between the values of inherent fpr of indicators
		
  	maxBinnedBw 							= deltaBudget * (double) numOfBwBins;
		// Init arrays which will hold the best, worst configurations per bw budget 
		budgetBin 							= new double 				[numOfBwBins];
		BestConfigurationInBin 	= new Configuration [numOfBwBins];
		WorstConfigurationInBin = new Configuration [numOfBwBins];
    binList 								= new ArrayList<> 						 ();
    numOfBudgets 						= budgets.length;
		Arrays.parallelSetAll (this.BestConfigurationInBin,  i -> new ConfigurationBuilder ().cost(Double.MAX_VALUE).buildConfiguration());
		Arrays.parallelSetAll (this.WorstConfigurationInBin, i -> new ConfigurationBuilder ().cost(0)		 						.buildConfiguration());
		Arrays.parallelSetAll (budgetBin, i -> i * deltaBudget);
    for (int i = 0; i < numOfIndicators; i++) {
    	confsInFprBin.addElement(new Vector<Configuration>()); 
  	}
    points = new HashMap<String, Double> ();
	}
	
	
	// Given the FP and TN out of total requests, calculates the false positive ratio
	// Note: FP == FPP = false positive Probability, that is, the # of FP over the total # of req.
	// Note: TN == TNP = true negative Probability, that is, the # of TN over the total # of req.
	private double fpr () {
		return FP / (FP + TN);
	}
	
	// Given the FP and TN out of total requests (FPP, TNP), calculates the false positive ratio
	private double fnr () {
		return FN / (TP + FN);
	}
	
		// calculate the ratio fpr/fnr. Note that this is different from the ratio FP / FN:
	// fpr = fp rate.
	// FP == FPP = false positive Probability, that is, the # of FP over the total # of req.
	private double fprOverFnr () {
		double fnr = fnr ();
		return (fnr == 0)? Double.MAX_VALUE : fpr () / fnr; 
	}
	
	// The hit rate obtained by a Perfect indicator.
  // This hit rate is calculated as the number of requests found in the cache over the total number of requests.
  // Hence, it is the TP rate + FN rate. 
  public double PiHitRate() {
    return TP + FN;
  }

  // Returns the expected service cost of a NI (No Indicator) configuration
  public double niServiceCost (double missp) {
    return Math.min (1 + (1 - PiHitRate ()) * missp, missp); 		 
  }

  // Returns the expected service cost of a NI (No Indicator) configuration
  public static double niServiceCost (double TP, double FN, double missp) {
    return  1 + (1 - (TP + FN)) * missp; 		 
  }

  // Returns the expected service cost of a NI (No Indicator) configuration
  public double cacheFirstServiceCost (double hitRatio, double missp) {
    return 1 + (1 - hitRatio) * missp; 		 
  }

  // Returns the expected service cost of a perfect indicator
  public double piServiceCost (double missp) {
  	return 1 + (1 - PiHitRate()) * (missp-1); 		 
  }

  // Returns the expected service cost of a perfect indicator
  public static double piServiceCost (double TP, double FN, double missp) { 
  	return 1 + (1 - (TP + FN)) * (missp-1); 		 
  }

  // Returns the expected service cost of an approximate indicator
  public double aiServiceCost (double missp) {
    return TP + FP + (1 - TP) * missp;
  }

  // Returns the expected service cost of an approximate indicator
  public static double aiServiceCost (double TP, double FP, double missp) {
    return TP + FP + (1 - TP) * missp;
  }

  // Returns the expected service cost of an approximate indicator, normalized w.r.t. the service cost of No Indicator
  public double normalizedPiServiceCost (double missp) {
  	return piServiceCost (missp) / niServiceCost (missp);
  }

  // Returns the expected service cost of an approximate indicator, normalized w.r.t. the service cost of No Indicator
  public double normalizedAiServiceCost (double missp) {
  	return aiServiceCost (missp) / niServiceCost (missp);
  }
  
  // Returns the expected service cost of an approximate indicator, normalized w.r.t. the service cost of No Indicator
  public static double normalizedAiServiceCost (double TP, double FP, double FN, double missp) {
  	return aiServiceCost (TP, FP, missp) / niServiceCost (TP, FN, missp);
  }

  // Returns the expected service cost of an approximate indicator, normalized w.r.t. the service cost of a Perfect Indicator
  public static double AiOverPiServiceCost (double TP, double FP, double FN, double missp) {
  	return aiServiceCost (TP, FP, missp) / piServiceCost (TP, FN, missp);
  }

  // Find the conf' with min, and max, within a given bw budget for the updates.
	public void calcMinMaxInBudgetBins () {
		// System.out.printf("cost = %.3f, BestConfigurationInBin[bwBin].cost = %.3f\n", cost, BestConfigurationInBin[bwBin].cost);
		if (cost < BestConfigurationInBin[bwBin].cost) { // Found a better conf' for this bwBin 
			BestConfigurationInBin[bwBin]    = new Configuration (designedFpr, updateInterval, cost, meanBw);
		} 
  	if (cost > this.WorstConfigurationInBin[bwBin].cost) { // Found a worse conf' for this bwBin 
  			WorstConfigurationInBin[bwBin] = new Configuration (designedFpr, updateInterval, cost, meanBw);
		}
	}

	// Insert a conf' to the list of conf' with this designed fpr.
	private void insertConfToFprBin () {
		int fprBin = (int) (designedFpr / deltaFpr);
		confsInFprBin.elementAt(fprBin).addElement(new Configuration (designedFpr, updateInterval, cost, meanBw));
	}
	
	// Insert a conf' to the list of conf' with this designed binId
	private void insertConfToBin (double curBinId) {
		int bin; // bin of the new conf' 
		
		if (discoveredAllBins) {
			bin = this.binList.indexOf (curBinId); 
		}
		else { // haven't discovered all bins yet
			if (binList.contains(curBinId)) { //If we've already seen this bin...
				discoveredAllBins = true;	// then we already know all the bins
				numOfBins 				= binList.size();     
				bin 							= binList.indexOf (curBinId); 
			}
			else {															// realized a new bin --> generate a new vector of conf's
				binList.add (curBinId);
				bin = numOfBins++;								// We assume here that the same bin is repeated only after all bins are discovered
	    	confsInBin.addElement(new Vector<Configuration>()); 
			}
		}
		confsInBin.elementAt(bin).addElement(
				new ConfigurationBuilder()
  			.indicatorSize 			((int) this.indicatorSize)
				.updateInterval 		(updateInterval)
				.cost 							(cost)
				.TP 								(TP)
				.FP 								(FP)
				.TN 								(TN)
				.FN 								(FN)
				.normalizedBw				(meanBw)				
				.buildConfiguration	());		
	}
	
	// If the output file we are requested to gen already exists - print a proper output msg, and finish.
	private void checkFileAlreadyExists (String fileName) {
		File file = new File (fileName);  	
  	if (file.exists()) { // Output already exists
  		System.out.println ("output file " + fileName + " already exists. Please delete it.");
  		System.exit (0);
  	}	
	}
	
	// Parse a line read from a standard sim run output file to its fields, representing, e.g., the update intervae, TP, TN etc.
	// Returns true if the line had data to parse; otherwise (e.g., this line is a comment / empty line), returns false.
	private boolean parseLine (String line) {
	
  	String[] lineAsArray = line.split("\\s+"); 

  	if (lineAsArray.length<2 || lineAsArray[0].startsWith("//")) {
    	return false;
    }

    updateInterval 	= Double.parseDouble (lineAsArray [idxOfUpdateIntervalField]);
    designedFpr 		= Double.parseDouble (lineAsArray [idxOfDesignedFprField]);
    indicatorSize		= Double.parseDouble (lineAsArray [idxOfIndSizeField]);
    meanBw 					= Double.parseDouble (lineAsArray [idxOfMeanBwField]);
    maxBw 					= Double.parseDouble (lineAsArray [idxOfMaxBwField]);
    TP 							= Double.parseDouble (lineAsArray [idxOfTpField]);
    FP 							= Double.parseDouble (lineAsArray [idxOfFpField]);
    TN 							= Double.parseDouble (lineAsArray [idxOfTnField]);
    FN 							= Double.parseDouble (lineAsArray [idxOfFnField]);
    cost 						= aiServiceCost (missp);
    return true;
    
	}

	// To be called once an exception happens. Prints an error msg, and exits.
	private void exceptionCatcher (Exception e, String line) {
		System.out.println ("Error while parsing file. exception msg is: " + e.getMessage());
		if (line != null) {
			System.out.println ("Last parsed line is " + line); 
		}
		System.exit(0);

	}
	
	// Plot for each budget the cheapest cost one could obtain using this budget
  public void PlotMinCostPerBudget () {
    System.out.println ("Min:");
    for (int i = 0; i < numOfBwBins; i++) {
    	if (BestConfigurationInBin[i].normalizedBw == -1) {// Didn't find any conf' in this bwBin
    		continue;
    	}
    	// The commented-out option below prints the exact bw
      // System.out.printf ("(%.0f, %.4f)", BestConfigurationInBin[i].normalizedBw, BestConfigurationInBin[i].cost);
      System.out.printf ("(%.0f, %.4f)", budgetBin[i], BestConfigurationInBin[i].cost);
    }
    System.out.println ("");
  }
		
	public void PlotMaxCostPerBudget () {
    System.out.println ("Max:");
    for (int i = 0; i < numOfBwBins; i++) {
    	if (WorstConfigurationInBin[i].normalizedBw == -1) {// Didn't find any conf' in this bwBin
    		continue;
    	}
    	// The commented-out option below prints the exact bw
      // System.out.printf ("(%.0f, %.4f)", WorstConfigurationInBin[i].normalizedBw, WorstConfigurationInBin[i].cost);
      System.out.printf ("(%.0f, %.4f)", budgetBin[i], WorstConfigurationInBin[i].cost);
    }
    System.out.println ("");
  }
	

	private void printPoint (String outputFileName, double x, double y) {
    MyConfig.writeStringToFile (outputFileName, String.format("(%.5f, %.5f)", x, y));	
	}
	

	// Generates the data for a tikz' heatmap: plots for all the points the X value (d.fpr), Y val (update interval), and "meta", z axis (cost).
	public void costHeatmap (File inputFile, String outputFileName) {
  	Set<Double> fprVals				= new HashSet<Double> ();
  	Set<Double> intervalVals	= new HashSet<Double> ();

  	checkFileAlreadyExists (outputFileName);
  	MyConfig.writeStringToFile (outputFileName, "x   y   C\n");
  	
		double minCost 					 = missp; // The min and max values of the Z axis are required by some tikz's heatmap imp'.
		double maxCost 					 = 0; 

    String line = null;
    
    // Read the given input file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }
	      
	      	double cost = aiServiceCost(missp);
	      	MyConfig.writeStringToFile (outputFileName, String.format("	%.3f	%.1f	%.4f\n", designedFpr, updateInterval, cost));
	      	minCost = Math.min (cost, minCost);
	      	maxCost = Math.max (cost, maxCost);
	      	fprVals.		 add (designedFpr);
	      	intervalVals.add (updateInterval);	      	
	      	continue;
	      
	    } // end while
		} // end try

		catch (Exception e) {
			exceptionCatcher (e, line);
		}

	}


	// Plot a Pareto graph, showing, for each given budget, the best conf' in each d.fpr value. 
	public void plotBudgetPareto (File inputFile, String outputFileName) {
		
//    String line = null;
//		
//    // Read the given input file line by line
//    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
//	    
//	    // Read the file, line by line
//	    while ((line = br.readLine()) != null) {
//
//	    	// parse the line, skipping empty / comments lines.
//	    	if (!parseLine (line)) {
//	      	continue;
//	      }
//	    	
//	      insertConfToFprBin ();
//		
//	    } // end while
//		} // end try
//		
//		catch (Exception e) {
//			exceptionCatcher (e, line);
//		}
//		
//		// finished reading from file --> analyze the data
//		for (int budget: budgets) {
//			
//			switch ((int) budget) {
//			
//				case 10:
//			    MyConfig.writeStringToFile (outputFileName, addPlotStr[0]);
//					break;
//			
//				case 20:
//					MyConfig.writeStringToFile (outputFileName, addPlotStr[1]);
//					break;
//
//				case 30:
//					MyConfig.writeStringToFile (outputFileName, addPlotStr[2]);
//					break;
//					
//			}
//			
//  		Configuration BestConfInFprBin; // Will hold the best conf' found for this d.fpr val AND budget constraints.
//  		for (int fprBin = 0; fprBin < numOfIndicators; fprBin++) { // loop over all possible d.fpr vals
//  		
//  			BestConfInFprBin = new ConfigurationBuilder().cost(missp).buildConfiguration(); // Reset the conf' 			
//  			
//  			// Find the best conf' within the budget limitation for this d.fpr value
//  			//Vector<Configuration> confsWithThisFpr = confsInFprBin.elementAt(fprBin); // confsWithThisFpr will hold all the confs' with this d.fpr value
//    		for (Configuration conf : confsInFprBin.elementAt(fprBin)) { // For every conf' with this d.fpr val...
//    			
//    		// If this conf' complies the budget constraint AND cheaper than BestConfInFprBin conf', make it the new BestConfInFprBin 
//    			if (conf.normalizedBw <= budget && conf.cost < BestConfInFprBin.cost) { 
//    				BestConfInFprBin = conf.copy();
//    			}
//    		}
//    		
//    		if (BestConfInFprBin.cost < missp) { // Have we found a non-empty conf' within this budget, for this d.fpr?
//    			// Yep --> Print this conf'
//    			
//    			printPoint (outputFileName, BestConfInFprBin.designedFpr, BestConfInFprBin.updateInterval);    				
//    			
//    		}
//    				
//    		}
//  		MyConfig.writeStringToFile (outputFileName, String.format ("\n};  \\addlegendentry {$B$ = %.0f}\n\n", budget));
//  		}
//  		
		}
		
	
	// Prints a heatmap showing the value of fpr / fnr.
	public void fprOverFnrHeatmap (File inputFile, String outputFileName) {
		double maxFprOverFnr = 10;
  	Set<Double> fprVals				= new HashSet<Double> ();
  	Set<Double> intervalVals	= new HashSet<Double> ();
		
  	MyConfig.writeStringToFile (outputFileName, "x   y   C\n");
  	
    String line = null;
    // Read the given input file line by line
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }

      	fprVals.		 add (designedFpr);
      	intervalVals.add (updateInterval);

      	double fprOverFnr = fprOverFnr ();
      	fprOverFnr = Math.min (fprOverFnr, maxFprOverFnr); // Trunc to at most maxFprOverFnr
      	MyConfig.writeStringToFile (outputFileName, String.format("	%.3f	%.1f	%.4f\n", designedFpr, updateInterval, fprOverFnr) );
  	
	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}
		//System.out.printf ("mesh/cols = %d,\nmesh/rows = %d,\n", fprVals.size(), intervalVals.size());

	}
  	
	// Plot the costs as a func' of the obtained fpr, fnr values for each conf'.
	public void costByFprFnrHeatmap (File inputFile, String outputFileName) {
  	Set<Double> fprVals				= new HashSet<Double> ();
  	Set<Double> intervalVals	= new HashSet<Double> ();
		
  	checkFileAlreadyExists (outputFileName);
  	MyConfig.writeStringToFile (outputFileName, "x   y   C\n");  			
    

  	// Read the given input file line by line
    String line = null;
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }
	      
	      	MyConfig.writeStringToFile (outputFileName, String.format("	%.4f	%.4f	%.4f\n", fpr(), fnr(), cost) );
	      	fprVals.		 add (designedFpr);
	      	intervalVals.add (updateInterval);
	      
	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}
		// System.out.printf ("mesh/cols = %d,\nmesh/rows = %d,\n", fprVals.size(), intervalVals.size());

	}
	
	public void BestWorstConfInBin (File inputFile, String outputFileName) {
  	
    String line = null;
    
    // Read the given input file line by line
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }

      	bwBin = (meanBw > maxBinnedBw)? numOfBwBins-1 : (int) (meanBw / deltaBudget); // Budget bin to which this budget belongs 
      	calcMinMaxInBudgetBins ();
	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}

    PlotMaxCostPerBudget ();
  	PlotMinCostPerBudget ();

	}
	
	// Plot for each budget the single conf' (d.fpr, and upadte interval) obtaining the cheapest cost within this budget.
	// By (un)commenting the print line, one can print either the conf' itself, and / or its meanBw and cost.
	public void BestConfPerBudget (File inputFile, String outputFileName) {
  	
  	// Read the given input file line by line
    String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }

      	bwBin = (meanBw > maxBinnedBw)? numOfBwBins-1 : (int) (meanBw / deltaBudget); // Budget bin to which this budget belongs 
      	calcMinMaxInBudgetBins ();
	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}

		double minCost = missp;
		System.out.printf("\n%s:\n", outputFileName);
    for (int bwBin = 1; bwBin < numOfBwBins; bwBin++) {
    	if (BestConfigurationInBin[bwBin].normalizedBw == -1) {// Didn't find any conf' in this bwBin
    		continue;
    	}
    	
    	// Uncomment the if clause to get the best conf' in AT MOST the budget, not only in this concrete budget bin.
    	printPoint (outputFileName, bwBin * deltaBudget, BestConfigurationInBin[bwBin].cost);
    	printPoint (outputFileName, BestConfigurationInBin[bwBin].designedFpr, updateInterval);
    }
    MyConfig.writeStringToFile (outputFileName, "\n};\\addlegendentry {Opt Conf' in budget bin}\n\n");

    	
    for (int bwBin = 1; bwBin < numOfBwBins; bwBin++) {
    	if (BestConfigurationInBin[bwBin].normalizedBw == -1) {// Didn't find any conf' in this bwBin
    		continue;
    	}
    	if (BestConfigurationInBin[bwBin].cost < minCost) { //Found a better conf'
    	printPoint (outputFileName, bwBin * deltaBudget, BestConfigurationInBin[bwBin].cost);
        minCost = BestConfigurationInBin[bwBin].cost;
      }
    }
    MyConfig.writeStringToFile (outputFileName, "\n};\\addlegendentry {Opt Opt Conf' WITHIN budget}\n\n");

	}
	
	// Plot for each budget the single conf' (d.fpr, and upadte interval) obtaining the cheapest cost within this budget.
	// By (un)commenting the print line, one can print either the conf' itself, and / or its bw and cost.
	public void plotFprOverFnrClosestInBudget (File inputFile, String outputFileName) {
		double goldenRatio = missp;
  	double[] fprOverFnrClosestToRatio = new double [numOfBwBins]; // fprOverFnrClosestToRatio[i] will hold the fpr/fnr closest-to-ratio found among all the conf's with the same fpr. 
		Arrays.parallelSetAll(fprOverFnrClosestToRatio, i -> Double.MAX_VALUE);

		Configuration[] closestConfInbwBin = new Configuration[numOfBwBins];
		
		// Read the given input file line by line
    String line = null;
    Double fprOverFnr; // Will hold each time the fprOverFnr of the current record
    int bwBin;
    
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }
	    	
	  		bwBin = (meanBw > maxBinnedBw)? numOfBwBins-1 : (int) (meanBw / deltaBudget); // Budget bin to which this budget belongs 
	  		fprOverFnr = fprOverFnr();
	  		if ( Math.abs(fprOverFnr - goldenRatio) < fprOverFnrClosestToRatio [bwBin]) {
	  			closestConfInbwBin 			 [bwBin] = new Configuration (designedFpr, updateInterval, cost, meanBw);
	  			fprOverFnrClosestToRatio [bwBin] = fprOverFnr; 
	  		}

	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}

		double closestTogoldenRatio = Double.MAX_VALUE;
    for (bwBin = 1; bwBin < numOfBwBins; bwBin++) {
    	
    	if (fprOverFnrClosestToRatio[bwBin] == Double.MAX_VALUE) { // Didn't find any relevant conf' in this budget bin
    		continue;
    	}

    	// Uncomment the if clause to get the best conf' in AT MOST the budget, not only in this concrete budget bin.
    	//    	if (fprOverFnrClosestToRatio[bwBin] < closestTogoldenRatio) { //Found a conf' closer to the golden ratio

    	printPoint (outputFileName, bwBin * deltaBudget, closestConfInbwBin [bwBin].cost);
    	// printPoint (outputFileName, closestConfInbwBin [bwBin].designedFpr, closestConfInbwBin [bwBin].updateInterval);
    		closestTogoldenRatio = fprOverFnrClosestToRatio[bwBin];
    		//  		}
		
    }
    MyConfig.writeStringToFile (outputFileName, "\n};\\addlegendentry {Closest to ratio in budget bin}\n\n");

	} // end method
	

	// Plot: X-axis: indicator size. Y-axis: cost.  - for several distinct budgets 
	public void PlotCostPerIndSize (File inputFile, String outputFileName) {
		
    String line = null;
		
    // Read the given input file line by line
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }
	    	
	      insertConfToBin (indicatorSize);
		
	    } // end while
		} // end try
		
		catch (Exception e) {
			exceptionCatcher (e, line);
		}
    
		// finished reading from file --> analyze the data
		double minCostInBin; // Will hold the cheapest cost of any conf' found in this bin AND budget constraints
		double indicatorSize;
		
		double cacheSize = (double) MyConfig.GetIntParameterFromConfFile ("caffeine.simulator.maximum-size"); 
		
		for (short i = 0; i < numOfBudgets; i++) {
			
			MyConfig.writeStringToFile (outputFileName, addPlotStr[i]);
			
			double budget = budgets[i];
			
  		for (int bin = 0; bin < numOfBins; bin++) { // loop over all possible d.fpr vals 
  			
  			minCostInBin 	= missp; // Init the cost to highest possible val
  			indicatorSize = 	 -1;
  			
  			// Find the best conf' within the budget limitation for this d.fpr value
    		for (Configuration conf : confsInBin.elementAt(bin)) { // For every conf' with this d.fpr val...
    			   			
    		// If this conf' complies the budget constraint AND cheaper than BestConfInFprBin conf', record its cost 
    			if (conf.normalizedBw <= budget && conf.cost < minCostInBin) { 
    				minCostInBin = conf.cost;
    				indicatorSize = (double) conf.indicatorSize;
    			}
    		}
    		
    		if (minCostInBin < missp) { // Have we found a non-empty conf' within this budget, for this binId?
    			// Yep --> Print this conf'
  				printPoint (outputFileName, indicatorSize / cacheSize, minCostInBin);
  				
    		}
    				
    		}
  		MyConfig.writeStringToFile (outputFileName, String.format ("\n};  \\addlegendentry {$B$ = %.0f}\n\n", budget));
  		}
  		
		}
	
	// Prints the cost, indSize, uInterval, FP and FN of the cheapest-cost static conf'
	public void BestConfPerBudget (File inputFile, String settings, boolean mod) {
    
		int numOfBudgets = this.budgets.length;
		BestConfigurationInBin 	= new Configuration [numOfBudgets];
		Arrays.parallelSetAll (BestConfigurationInBin,  i -> new ConfigurationBuilder ().cost(Double.MAX_VALUE).buildConfiguration());

    // Read the given input file line by line
    String line = null;
    int i;
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }

	    	for (i=0; i < numOfBudgets; i++) { // for each budget bin
	    		double usedBw = (mod == this.softBudgetMod)? meanBw : maxBw;  
	    		if (usedBw < this.budgets[i] && cost < BestConfigurationInBin[i].cost) { // found a better conf' within this budget
	    			BestConfigurationInBin[i] = new ConfigurationBuilder () .cost(cost) .TP(TP) .FP(FP) .TN(TN) .FN (FN) 
	    																															.indicatorSize ((int)indicatorSize) .updateInterval(updateInterval) 
	    																															.buildConfiguration();  
	    		}
	    	}
	    	
		
	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}

  	String[] settingsAsArray = settings.split("\\."); 
		String policy = settingsAsArray[2];
		String outputFileName = MyConfig.resFileFullPath() + settingsAsArray[idxOfTrace] + ".res";
  	String outputStr = String.format ("%s.static.%s.M%.0f.%s", 
  																		 settingsAsArray[idxOfTrace], settingsAsArray[1], this.missp, (mod == this.softBudgetMod)? "SB" : "HB");
  			
    double minUpdateInterval = MyConfig.GetDoubleParameterFromConfFile	("minimal-update-interval");
  	for (i=0; i < numOfBudgets; i++) { // for each budget bin
  		MyConfig.writeStringToFile (outputFileName, outputStr + String.format(
  						"%.0f.%s | cost = %.4f | ind Size = %d, uInterval = %.0f, FP = %.4f, FN = %.4f slack = %.2f\n", 
  						budgets[i], policy, Math.min (BestConfigurationInBin[i].cost, this.missp), 
  						BestConfigurationInBin[i].indicatorSize, BestConfigurationInBin[i].updateInterval, BestConfigurationInBin[i].FP, BestConfigurationInBin[i].FN,
  				budgets[i] / (BestConfigurationInBin[i].indicatorSize / BestConfigurationInBin[i].updateInterval)));  				
//  		MyConfig.writeStringToFile (outputFileName, " | "
//  						+ " %.0f cost = %.4f\n", 
//					outputStr, budgets[i], Math.min (BestConfigurationInBin[i].cost, this.missp)));
  		//double FpOverFn = (BestConfigurationInBin[i].FN == 0)? -1 : BestConfigurationInBin[i].FP / BestConfigurationInBin[i].FN;
  	}
//  	for (i=0; i < numOfBudgets; i++) { // for each budget bin
//  	//if (BestConfigurationInBin[i].updateInterval > minUpdateInterval) {
//  			MyConfig.writeStringToFile(outputFileName, String.format ("// %s B = %.0f | ind Size = %d, uInterval = %.0f, FP = %.4f, FN = %.4f slack = %.2f\n", outputStr,  budgets[i],
//  				BestConfigurationInBin[i].indicatorSize, BestConfigurationInBin[i].updateInterval, BestConfigurationInBin[i].FP, BestConfigurationInBin[i].FN,
//  				budgets[i] / (BestConfigurationInBin[i].indicatorSize / BestConfigurationInBin[i].updateInterval)
//  				));  				
//  		//}
//  	}
//		MyConfig.writeStringToFile(outputFileName, "\n"); 
	}

	protected String extractTraceFromFileName (String fileName) {
		String traceName = null;
		return traceName;
	}
	
	protected String extractCacheSizeTokenFromFileName (String cacheSizeToken) {
		String res = null;
		return res;
	}
	
	protected String extractPolicyFromFileName (String fileName) {
		String res = null;
		return res;
	}
	
	protected int extractBudgetFromBudgetToken (String budgetToken) {
		return Integer.parseInt (budgetToken.split("B")[1]);
	}
	
	public void PrintCostOfWorstStatic (File inputFile, String outputStr) {
    
		int numOfBudgets = this.budgets.length;
		WorstConfigurationInBin 	= new Configuration [numOfBudgets];
		Arrays.parallelSetAll (WorstConfigurationInBin,  i -> new ConfigurationBuilder ().cost(-1.0).buildConfiguration());

    // Read the given input file line by line
    String line = null;
    int i;
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }

	    	for (i=0; i < numOfBudgets; i++) { // for each budget bin
	    		if (meanBw > this.budgets[i]/2 && meanBw < this.budgets[i] && cost > WorstConfigurationInBin[i].cost) { // found a better conf' within this budget
	    			WorstConfigurationInBin[i] = new ConfigurationBuilder () .cost(cost) .TP(TP) .FP(FP) .TN(TN) .FN (FN) 
	    																															.indicatorSize ((int)indicatorSize) .updateInterval(updateInterval) 
	    																															.buildConfiguration();  
	    		}
	    	}
	    	
		
	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}

    
  	for (i=0; i < numOfBudgets; i++) { // for each budget bin
  		System.out.printf ("%s, B = %.0f cost = %.4f\n", 
  												outputStr, budgets[i], WorstConfigurationInBin[i].cost);
  	}
    

	}
	
	public void PlotFpFnPerUpdateInterval  (File inputFile, String outputFileName, int mode) {

		int[] bitsPerElement = {2, 4, 8, 16};
    double cacheSize = 8192;

    int i;

    // Read the given input file line by line
    String line = null;
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
	    
	    // Read the file, line by line
	    while ((line = br.readLine()) != null) {

	    	// parse the line, skipping empty / comments lines.
	    	if (!parseLine (line)) {
	      	continue;
	      }

	    	for (int bpe : bitsPerElement) {
	    		if (this.indicatorSize / cacheSize == bpe) {
		    		this.insertConfToBin (bpe);
	    		}    		
	    	}
	    	
		
	    } // end while
		} // end try
		catch (Exception e) {
			exceptionCatcher (e, line);
		}
    double prevConfFpp, prevConfFnp;
    double hitRatio 					= 0;
    double missRatio 					= 0;
    boolean hitRatioIsKnown 	= false;
    boolean missRatioIsKnown 	= false;
  	for (i = 0; i < confsInBin.size(); i++) {
			MyConfig.writeStringToFile (outputFileName, addPlotStr[i]);
			prevConfFnp = 0;
			prevConfFpp = 0;
			for (Configuration conf : confsInBin.elementAt(i)) {
				
				switch (mode) {
				
  				case printFpp:
  					printPoint (outputFileName, conf.updateInterval, conf.FP);
  					break;
  						
  				case printFnp:
  					printPoint (outputFileName, conf.updateInterval, conf.FN);					
  					break;

  				case printStaleFnr:
  					if (!hitRatioIsKnown) {
  						hitRatio = conf.TP + conf.FN;
  						hitRatioIsKnown = true;
  					}
  					if (conf.updateInterval > 1) {
  						printPoint (outputFileName, conf.updateInterval, 2 * (conf.FN - prevConfFnp / 2) / hitRatio);
  					}
						prevConfFnp = conf.FN;
  					break;
  					
  				case printStaleFpr:
   					if (!missRatioIsKnown) {
  						missRatio = conf.FP + conf.TN;
  						missRatioIsKnown = true;
  					}
  					if (conf.updateInterval > 1) {
  						printPoint (outputFileName, conf.updateInterval, 2 * (conf.FP - prevConfFpp / 2) /missRatio);
  					}
						prevConfFpp = conf.FP;
  					break;

  				default:
  					break;
					
				}
				
			}
			MyConfig.writeStringToFile (outputFileName, String.format("\n};\\addlegendentry {%d-BPE}\n\n", bitsPerElement[i]));
    
  	}
	}
	

	protected boolean parseLineInResResFile (String line) {

		String[] lineAsArray = line.split("\\|"); 

  	if (lineAsArray.length<2 || lineAsArray[0].startsWith("//")) {
    	return false;
    }
  	
  	String[] settingsAsArray = lineAsArray[0].split("\\.");

		trace   				= settingsAsArray[idxOfTrace];
		cacheSizeToken	= settingsAsArray[this.idxOfCacheSize];
		budgetToken 		= settingsAsArray[idxOfBudget];
		runMode 				= settingsAsArray[idxOfRunMode];
		cost						= Double.parseDouble (lineAsArray[1].split("=")[1]);
		policy  				= settingsAsArray[idxOfPolicy].split(" ")[0];
		hitRatio				= (runMode.equals("IDJmm"))? Double.parseDouble (lineAsArray[idxOfHitRatio].split("=")[1]) : -1.0; 
//		cacheFirstCost  = (runMode.equals("static"))? -1.0 : this.cacheFirstServiceCost (TP, FN, missp)
//		cacheSize 	= Integer.parseInt 		(lineAsArray[this.idxOfCacheSize].split("C")[1].split("K")[0]); 
//		budget			= Double.parseDouble 	(lineAsArray[this.idxOfBudget].split("B")[1]);
//		budgetMod		= (lineAsArray[this.idxOfBudget].split("B")[0].equals("H"))? this.hardBudgetMod : this.softBudgetMod;
		return true;
	}


	protected void printPointForBarChart (String outputFileName, String runMode, String cacheSizeToken, String budgetToken, String policy) {
		MyConfig.writeStringToFile (outputFileName, 
				String.format("%.5f  ", points.get (calcKey (runMode, cacheSizeToken, budgetToken, policy))));		
	}

	protected String calcKey (String runMode, String cacheSizeToken, String budgetToken, String policy) {
		return runMode + "." + cacheSizeToken + "." + budgetToken + "." + policy;
	}
	
	
	protected void insertResDataToHashMap (String requestedTrace, String requestedBudgetToken) {
    String inputFileName  			= MyConfig.getFullPathResFileName(requestedTrace); 
		double missp								= 3;
		
  	// Read the given input file line by line
    String line = null;
  	try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
      // Read the file, line by line
      while ((line = br.readLine()) != null) {
  
      	// parse the line, skipping empty / comments lines.
      	if (!parseLineInResResFile (line)) {
        	continue;
        }
      	
      	if (this.budgetToken.equals (requestedBudgetToken) && this.trace.equals(requestedTrace)) {
      		points.put(calcKey (runMode, cacheSizeToken, requestedBudgetToken, policy), cost);
      		if (hitRatio != -1) {
        		points.put(calcKey ("cacheFirst", cacheSizeToken, requestedBudgetToken, policy), this.cacheFirstServiceCost(hitRatio, missp));
      		}
      	}

      } // end while
  	} // end try
  	catch (Exception e) {
  		exceptionCatcher (e, line);
  	}
		
	}//wiki.static.C4K.M3.HB20.Frd 

	
	protected void barAlgVsStaticForVariousBudgets (String requestedTrace) {
		
    String outputFileName 			= MyConfig.resFileFullPath() + requestedTrace + ".budget.dat"; 
		points.clear(); // Clear the hash of all configuration 

		String[] requestedBudgetTokens = {"HB10", "HB20", "HB40", "HB80"};
  	String Alg = "IDJmm";
  	String[] policies = {"Lru", "Lfu", "Frd", "Hyperbolic"};
  	String cacheSizeToken = "C16K";

  	MyConfig.writeStringToFile(outputFileName, "budget  LRU-A   LRU-SC  LRU-CF  LFU-A   LFU-SC  LFU-CF  FRD-A   FRD-SC  FRD-CF  Hyp-A   Hyp-SC  Hyp-CF\n");

  	String requestedBudgetToken;
  	for (int i = 0; i < requestedBudgetTokens.length; i++) {
  		requestedBudgetToken = requestedBudgetTokens[i];
  		
			insertResDataToHashMap (requestedTrace, requestedBudgetToken);
			MyConfig.writeStringToFile (outputFileName, String.format ("B%d   ",  i+1));

    	for (String policy : policies) {
    			printPointForBarChart (outputFileName, Alg, 				cacheSizeToken, requestedBudgetToken, policy); //print alg's cost for this cache size
    			printPointForBarChart (outputFileName, "static", 		cacheSizeToken, requestedBudgetToken, policy); //print static's cost for this cache size
    			printPointForBarChart (outputFileName, "cacheFirst", cacheSizeToken,requestedBudgetToken, policy); //print static's cost for this cache size
    		}
    		MyConfig.writeStringToFile(outputFileName, "\n");
    	}

  }

	protected void barAlgVsStatic (String requestedTrace) {
		
    String outputFileName 			= MyConfig.resFileFullPath() + requestedTrace + ".dat"; 
		points.clear(); // Clear the hash of all configuration 
		String requestedBudgetToken = "HB20";
		insertResDataToHashMap (requestedTrace, requestedBudgetToken);
		  	
  	String Alg = "IDJmm";
  	String[] policies = {"Lru", "Lfu", "Frd", "Hyperbolic"};
  	String[] cacheSizeTokens = {"C4K", "C16K", "C64K"};
  	
  	MyConfig.writeStringToFile(outputFileName, "policy  4KAlg   4KBM    4KCF    16KAlg  16KBM   16KCF   64KAlg  64KBM   64KCF\n");
  	for (String policy : policies) {
  		if (policy.equals("Lru")) 
  			MyConfig.writeStringToFile(outputFileName, "LRU     ");
  		else if (policy.equals("Lfu")) { 
  			MyConfig.writeStringToFile(outputFileName, "W-tLFU  ");
  		}
  		else if (policy.equals("Frd")) { 
  			MyConfig.writeStringToFile(outputFileName, "FRD     ");
  		}
  		else
  			MyConfig.writeStringToFile(outputFileName, "Hyper   ");
 
  		for (String cacheSizeToken : cacheSizeTokens) {
  			printPointForBarChart(outputFileName, Alg, 					cacheSizeToken, requestedBudgetToken, policy); //print alg's cost for this cache size
  			printPointForBarChart(outputFileName, "static", 		cacheSizeToken, requestedBudgetToken, policy); //print static's cost for this cache size
  			printPointForBarChart(outputFileName, "cacheFirst", cacheSizeToken, requestedBudgetToken, policy); //print static's cost for this cache size
  		}
  		MyConfig.writeStringToFile(outputFileName, "\n");
  	}

  }


	public void proveNoOneConfFitsAll ( ) {

		String outputFileName = "ditinctConfs";
		String resFileFullPath 	= MyConfig.resFileFullPath();
		String[] settings		= {"P8.C16K.Lru"}; //"F1.C16K.Lru", "F2.C16K.Lru", "scarab.C16K.Lru", "wiki.C16K.Lru", "P3.C16K.Lru"}; //"P6.C16K.Lru"
		//String[] settings		= {"P8.C64K.Lru", "F1.C64K.Lru", "F2.C64K.Lru", "scarab.C64K.Lru", "wiki.C16K.Lru"}; 
		int [] C0 = {40000,   1761};
		int [] C1 = {40000,   2025};
		int [] C2 = {77948, 4073};
		int [] C3 = {183798,     9423};
		int [] C4 = {240000, 12462};
		int[][] Confs = {C0, C1, C2, C3, C4};
		//int[] conf = {-1, -1};
		double minCostForThisSetting = Double.MAX_VALUE;
		int idxOfCheapestConf = -1;
		int i;
		double costOfCurConfInCurSetting = -1;
		for (String setting : settings) {
			for (i = 0; i < Confs.length; i++) {
				costOfCurConfInCurSetting = printCostAndBwOfConcreteStaticConf (setting, Confs[i][0], Confs[i][1], outputFileName);
				if (costOfCurConfInCurSetting < minCostForThisSetting) {
					minCostForThisSetting = costOfCurConfInCurSetting;
					idxOfCheapestConf =  i;
				}
			} 
			MyConfig.writeStringToFile (resFileFullPath + outputFileName 	+ ".res", String.format("%d\n", idxOfCheapestConf));
		}

	}
	
	
	public double printCostAndBwOfConcreteStaticConf (String inputFileName, int indSize, int uInterval, String outputFileName) {
		String resFileFullPath 	= MyConfig.resFileFullPath();
		File 	 inputFile 				= MyConfig.getFile (resFileFullPath + inputFileName 	+ ".res");
		
  	// Read the given input file line by line
    String line = null;
  	try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
      // Read the file, line by line
      while ((line = br.readLine()) != null) {
  
      	// parse the line, skipping empty / comments lines.
      	if (!this.parseLine (line)) {
        	continue;
        }
      	
      	if (this.updateInterval == uInterval && this.indicatorSize == indSize) { //Bingo
      		MyConfig.writeStringToFile (resFileFullPath + outputFileName 	+ ".res", 
          		String.format("%s | I=$d, u = %d| cost = %.4f | maxBw = %.2f\n", 
          				inputFileName, indSize, uInterval, cost, maxBw));
//      		String.format("using static conf' uInterval = %d, indSize = %d in file %s | cost = %.4f, maxBw = %.2f, meanBw = %.2f\n",
//      				uInterval, indSize, inputFileName, this.cost, this.maxBw, this.meanBw));
      	}

      } // end while
  	} // end try
  	catch (Exception e) {
  		exceptionCatcher (e, line);
  	}
  	return cost;
		
	}

	// Run a single result analysis task, on a single given settings.
	// The settings include system parameters, e.g., cache size, cache policy.
	public void runSingleTask (String settings, Tasks task) {

		String resFileFullPath 	= MyConfig.resFileFullPath();
		File 	 inputFile 				= MyConfig.getFile (resFileFullPath + settings + ".res");
		String taskAsString 		= task.toString();
		String outputFileName 	= String.format (resFileFullPath + settings + "BM.M%.0f." + taskAsString + ".dat", missp);  
		
		switch (taskAsString) {
		
			case "costHeatmap":
				this.costHeatmap (inputFile, outputFileName);
				break;
				
			case "bestWorstConfInBin":
				this.BestWorstConfInBin (inputFile, outputFileName);
				break;
			
			case "fprOverFnrHeatmap":
				this.fprOverFnrHeatmap (inputFile, outputFileName);
				break;
				
			case "costByFprFnrHeatmap":
				this.costByFprFnrHeatmap (inputFile, outputFileName);
				break;
			
			case "BestConfPerBudget":
				this.BestConfPerBudget(inputFile, outputFileName);
				break;

			case "fprOverFnrClosestInBudget":
				this.plotFprOverFnrClosestInBudget (inputFile, outputFileName);
				break;

			case "budgetPareto":
				this.plotBudgetPareto (inputFile, outputFileName);
				break;
				
			case "costByIndSize":
				this.PlotCostPerIndSize (inputFile, outputFileName);
				break;

			case "softBudgetBenchmark":
				// outputFileName 	= String.format (resFileFullPath + settings + ".bncmrk");  
				// this.BestConfPerBudget (inputFile, String.format (settings + ".M%.0f.SB", missp), this.softBudgetMod, outputFileName);
				BestConfPerBudget (inputFile, settings, this.softBudgetMod);
				break;
				
			case "hardBudgetBenchmark":
				// outputFileName 	= String.format (resFileFullPath + settings + ".bncmrk");  
				// this.BestConfPerBudget (inputFile, String.format (settings + ".M%.0f.HB", missp), this.hardBudgetMod, outputFileName);
				BestConfPerBudget (inputFile, settings, this.hardBudgetMod);
				break;
				
			case "costOfWorstStatic":
				this.PrintCostOfWorstStatic (inputFile, String.format (settings + " M = %.0f", missp));
				break;
			
			case "FppPerUpdateInterval":

				this.PlotFpFnPerUpdateInterval (inputFile, outputFileName, this.printFpp);
				break;
				
			case "FnpPerUpdateInterval":

				this.PlotFpFnPerUpdateInterval (inputFile, outputFileName, this.printFnp);
				break;
				
			case "FprPerStaleness":

				this.PlotFpFnPerUpdateInterval (inputFile, outputFileName, this.printStaleFpr);
				break;
				
			case "FnrPerStaleness":

				this.PlotFpFnPerUpdateInterval (inputFile, outputFileName, this.printStaleFnr);
				break;
				
			case "allBestConfPerBudget":
				String requestedTrace 			= settings;
		    File folder 								= new File (MyConfig.resFileFullPath ());

		    String 		fileName;
		    String[] 	fileNameAsArray; 
		    for (final File fileEntry : folder.listFiles()) {
		      if (!fileEntry.isDirectory()) { 
		      	fileName = fileEntry.getName();
		      	fileNameAsArray = fileName.split("\\.");
		      	if (fileNameAsArray[0].equals(requestedTrace) && fileNameAsArray.length == 4 && !fileNameAsArray[1].equals("res") && !fileNameAsArray[1].equals("dat")) {
		      		BestConfPerBudget (MyConfig.getFile (resFileFullPath + fileName), fileName.split("\\.res")[0], this.hardBudgetMod);
		      	}
		      }
		    }
  			break;
				
  			
			case "barAlgVsStatic":
  			barAlgVsStatic (settings);
  			break;
  			
			case "barAlgVsStaticForVariousBudgets":
				barAlgVsStaticForVariousBudgets(settings);
				break;

			default:
				System.out.println ("Unknown task " + taskAsString);
				System.exit (0);
		}

		// Clear some variables, as a preparation for the next run
		for (int bin = 0; bin < numOfBins; bin++) { // loop over all possible d.fpr vals 
			confsInBin.elementAt(bin).clear();
		}
		confsInBin.clear();
		binList		.clear();
		numOfBins 				= 0;
		discoveredAllBins = false;
	} // end func'

	
	public void runAll () {

		double[] allMissPenalties = {3};
		String[] allSettings = {"F1.C8K.Lru"}; //wiki", "wiki2", "P3", "P6", "P8", "scarab", "F1", "F2"};  
		Tasks [] tasks = Tasks.values();
		
		for (String settings : allSettings) {
			for (double curMissp : allMissPenalties) {
				this.missp = curMissp;
				for (Tasks taskToRun : tasks) {
					runSingleTask (settings, taskToRun);
				}
			}
		}	
		
	}
	
	
}// end class



