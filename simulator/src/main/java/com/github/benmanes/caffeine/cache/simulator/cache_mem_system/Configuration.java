package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

public class Configuration {
	public double designedFpr;
	public int 		indicatorSize;
	public double updateInterval;  
	public double cost;
	public double costOverOptCost; 
	public double normalizedBw;
	public double TP, FP, TN, FN; // True Positive, False Positive, Negative, Rate (out of total requests)

	// C'tor
	public Configuration (double designedFpr, double updateInterval, double cost, double normalizedBw) {
		this.designedFpr = designedFpr;
		this.updateInterval = updateInterval;
		this.cost = cost;
		this.normalizedBw = normalizedBw;
	}

	public Configuration (double designedFpr, double updateInterval, double cost, double costOverOptCost, double normalizedBw, 
												int indicatorSize, 	double TP, double FP, double TN, double FN) {
		this.designedFpr 			= designedFpr;
		this.updateInterval 	= updateInterval;
		this.cost 						= cost;
		this.costOverOptCost 	= costOverOptCost;
		this.normalizedBw 		= normalizedBw;
		this.indicatorSize 		= indicatorSize;
		this.TP 							= TP;
		this.FP 							= FP;
		this.TN 							= TN;
		this.FN 							= FN;
	}

	public Configuration copy () {
		return new ConfigurationBuilder () 
				.designedFpr 		(designedFpr) 
				.updateInterval (updateInterval)
				.normalizedBw		(normalizedBw)
				.indicatorSize	(indicatorSize)
				.TP(TP) .FP(FP)
				.TN(TN) .FN(FN)
				.buildConfiguration();
	}
	
	public void resetStat () {
		TP = 0; FP = 0;
		TN = 0; FN = 0;
		cost = 0;
		costOverOptCost = 0;
		normalizedBw = 0;
	}
	
	public boolean equals (Configuration other) {
		return (this.updateInterval == other.updateInterval && 
						this.designedFpr 		== other.designedFpr 		&& 
						this.indicatorSize 	== other.indicatorSize)?
						true : false;			
	}
	
	// updates the costs - both the absolute cost, and the cost normalized w.r.t. perfect ind' 
	public void updateCosts (double missp) {
    this.cost						 = ResAnalyser.aiServiceCost 			 (TP, FP, missp);
    this.costOverOptCost = ResAnalyser.AiOverPiServiceCost (FP, FP, FN, missp);
	}
	
	// Returns true if the current conf' causes the Bloom Paradox
	public boolean paradox (double missp) {
		return ( (ResAnalyser.normalizedAiServiceCost(this.TP, this.FP, this.FN, missp) > 1) ||
							ResAnalyser.aiServiceCost(this.TP, this.FP, missp) > missp)	? true : false;
	}
	
}
