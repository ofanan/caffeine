package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

public class accsStrategies {

	// Returns the service cost of a perfect indicator, given the hit ratio and the miss penalty
	static public double piCost (double Phit, double missp) {
		return 1 + (missp-1)*(1 - Phit);
	}
	
	// Returns the prob' of a positive indication (usually denoted by q).
	static double probOfPositiveIndication (double Phit, double fpr, double fnr) {
		return Phit * (1 - fnr) + (1 - Phit) * fpr; 
	}
	
	static double rho0 (double Phit, double fpr, double fnr) {
		return (1 - fpr) * (1 - Phit) / (1 - probOfPositiveIndication (Phit, fpr, fnr));
	}

	static double rho1 (double Phit, double fpr, double fnr) {
		return fpr * (1 - Phit) / probOfPositiveIndication (Phit, fpr, fnr);
	}

	static boolean shouldAccsCache (double Phit, double fpr, double fnr, double missp, boolean indication) {
		
		if (indication) { // positive ind'
		  return (1 + missp*rho1(Phit, fpr, fnr) < missp)? true : false; 	
		}
		else { // negative ind'
			return (1 + missp*rho0(Phit, fpr, fnr) < missp)? true : false; 	
		}
	}
	
	// Returns the expected cost of a perfect indicator
	static double piExpectedCost (double Phit, double missp) {
		return 1 + (missp - 1)*(1 - Phit);
	}
	
	// Returns the expected cost of a naive alg', equipped with an approx (imperfect) ind'
	static double naiveAiExpectedCost (double Phit, double fpr, double fnr, double missp) {
		double q = probOfPositiveIndication (Phit, fpr, fnr);
		return q * (1 + missp * rho1 (Phit, fpr, fnr)) + (1-q) * missp;
	}

	// Returns the expected cost of the ECM (Expected Cost Minimization) alg', equipped with an approx (imperfect) ind'
	static double ecmAiExpectedCost (double Phit, double fpr, double fnr, double missp) {
		
		double q = probOfPositiveIndication (Phit, fpr, fnr);
		double cost = q * (shouldAccsCache (Phit, fpr, fnr, missp, true)? (1 + missp * rho1 (Phit, fpr, fnr)) : missp);

		cost += (1 - q) * (shouldAccsCache (Phit, fpr, fnr, missp, false)? (1 + missp * rho0 (Phit, fpr, fnr)) : missp);
		return cost;
	}
	
	static private double maxFnrForGivenCost (double Phit, double fpr, double missp, double cost) {
		return (cost - missp + (missp-1)*Phit + (Phit-1)*fpr) / ((missp-1)*Phit);
	}
	
	public void main () {
		double missp = 10;
		double Phit = 0.5;
		double cost = 6; // The cost of PI with missp=10, Phit=0.5 is 5.5
		double fpr = 0.01;
		System.out.printf("max allowed fnr = %.5f", maxFnrForGivenCost (Phit, fpr, missp, cost));
	}
	
	
}
