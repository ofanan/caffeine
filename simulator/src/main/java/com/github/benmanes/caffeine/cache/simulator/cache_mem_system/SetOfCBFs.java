package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;

import orestes.bloomfilter.CountingBloomFilter;
import orestes.bloomfilter.FilterBuilder;

public class SetOfCBFs<K> extends SetOfIndicators<K> {
  protected CountingBloomFilter<K>[] updated_indicator; 
  protected CountingBloomFilter<K>[] stale_indicator;
  protected CountingBloomFilter<K>[] lastKnownIndicator; // When using hard-budget, this is the last indicator known to the user.
  protected int	[] 	 	numOfCntrs; // numOfCntrs[i] will hold the # of cntrs in indicator i. 
  protected int [] 		lgNumOfCntrs; // lgNumOfCntrs[i] will hold the ceiling of log_2(# of cntrs in indicator[i]). this will be used for calculating the update size
  protected int				algLgNumOfCntrs;    
  protected static double ln2 = Math.log(2); 

  protected double []	designedFpr; // designedFpr[i] will hold the d.fpr of indicator i.  
  
  // C'tor
  public SetOfCBFs (String policyName) { 

  	super (policyName); 
    
    if (!runReconfAlg) { // Running static configurations 
      @SuppressWarnings("unchecked")
      CountingBloomFilter<K>[] stale_tmp_ar 		= (CountingBloomFilter<K>[])   Array.newInstance(CountingBloomFilter.class, numOfIndicators);
      @SuppressWarnings("unchecked")
      CountingBloomFilter<K>[] updated_tmp_ar 	= (CountingBloomFilter<K>[]) Array.newInstance(CountingBloomFilter.class, numOfIndicators);
      @SuppressWarnings("unchecked")
      CountingBloomFilter<K>[] last_known_tmp_ar = (CountingBloomFilter<K>[]) Array.newInstance(CountingBloomFilter.class, numOfIndicators);

      
    	numOfCntrs 		= new int [numOfIndicators];
    	lgNumOfCntrs 	= new int [numOfIndicators] ;

      if (fprVals[0] > 0) { // Should determine indicators' sizes by the desired fpr
        for (short i = 0; i < numOfIndicators; i++) { 
        	stale_tmp_ar 	 		[i]	= new FilterBuilder (cacheSize, fprVals[i]) .buildCountingBloomFilter();
        	updated_tmp_ar 		[i]	= new FilterBuilder (cacheSize, fprVals[i]) .buildCountingBloomFilter();
        	last_known_tmp_ar [i]	= new FilterBuilder (cacheSize, fprVals[i]) .buildCountingBloomFilter();
        }
      	Arrays.parallelSetAll (numOfCntrs, i -> stale_tmp_ar[i].getSize()); // The CBF will calculate the size according to the given d.fpr 
      }
      else { // Should determine indicators' num of hashes by desired indicators' sizes
      	Arrays.parallelSetAll (numOfCntrs, 			i -> (int) indSize[i]);  
      	int		 [] numOfHashFuncs	= new int 	 [numOfIndicators];  
      	Arrays.parallelSetAll (numOfHashFuncs, 	i -> this.optimalNumOfHashFuncs (numOfCntrs[i]));

   			for (short i = 0; i < numOfIndicators; i++) { 
        	stale_tmp_ar 	 		[i] 	= new FilterBuilder () .size (numOfCntrs[i]) .hashes(numOfHashFuncs[i]) .buildCountingBloomFilter();
        	updated_tmp_ar 		[i] 	= new FilterBuilder () .size (numOfCntrs[i]) .hashes(numOfHashFuncs[i]) .buildCountingBloomFilter();
        	last_known_tmp_ar [i] 	= new FilterBuilder () .size (numOfCntrs[i]) .hashes(numOfHashFuncs[i]) .buildCountingBloomFilter();
        }	
        if (verbose==2) {
        	MyConfig.writeStringToFile(this.staticConfOutputFileName, String.format("num of hashes = %d\n", numOfHashFuncs[0]));
        }
      }
           
      stale_indicator 	 = stale_tmp_ar;
      updated_indicator  = updated_tmp_ar;
      lastKnownIndicator = last_known_tmp_ar;
      calcLgNumOfCntrs ();
    	super.setUpdateInterval (); // Set the interval between transmitted updates (fresh indicators)
    }

  }

  // Calculates the log (with base 2) of the sizes of indicators 
  private void calcLgNumOfCntrs () {
  	Arrays.parallelSetAll (lgNumOfCntrs, i -> (int) ( Math.ceil( Math.log(numOfCntrs[i]) / ln2)) );  	
  }
  

  // scale staleIndicator[algIndicatorIdx] and updatedIndicator[algIndicatorIdx] to the desired size
  @Override
  protected void scaleIndicator (int size) {
    updated_indicator [algIndicatorIdx]	= new FilterBuilder () .size (size) .hashes(optimalNumOfHashFuncs(size)) .buildCountingBloomFilter();
    updated_indicator [algIndicatorIdx].addAll (cache.keySet());    
  	numOfCntrs				[algIndicatorIdx]	= size;
  	calcLgNumOfCntrs ();  	
  }
  
  // Returns the optimal number of hash functions for a given indicator's size 
  private int optimalNumOfHashFuncs (int size){
  	return (int) (Math.round (((double) size / cacheSize) * ln2) );
  }
  
  
  // Generate a single stale indicator in stale_indicator[algIndicatorIdx], and a single update indicator, in updated_indicator[algIndicatorIdx],
  // both of them at the requested size, and with opt' num' of hash funcs.
  // To be used by the reconf' alg'
  @Override
  protected void genIndicator (int size) {
  	int numOfHashFuncs = optimalNumOfHashFuncs (size);
    @SuppressWarnings("unchecked")
    CountingBloomFilter<K>[] stale_tmp_ar 			= (CountingBloomFilter<K>[]) Array.newInstance(CountingBloomFilter.class, numOfIndicators);
    @SuppressWarnings("unchecked")
    CountingBloomFilter<K>[] updated_tmp_ar 	 	= (CountingBloomFilter<K>[]) Array.newInstance(CountingBloomFilter.class, numOfIndicators);
    @SuppressWarnings("unchecked")
    CountingBloomFilter<K>[] last_known_tmp_ar 	= (CountingBloomFilter<K>[]) Array.newInstance(CountingBloomFilter.class, numOfIndicators);

    stale_tmp_ar				[algIndicatorIdx] = new FilterBuilder () .size (size) .hashes(numOfHashFuncs) .buildCountingBloomFilter();  	
    updated_tmp_ar			[algIndicatorIdx]	= new FilterBuilder () .size (size) .hashes(numOfHashFuncs) .buildCountingBloomFilter();
    last_known_tmp_ar	  [algIndicatorIdx]	= new FilterBuilder () .size (size) .hashes(numOfHashFuncs) .buildCountingBloomFilter();
    
    stale_indicator 		= stale_tmp_ar;
    updated_indicator 	= updated_tmp_ar;
    lastKnownIndicator	= last_known_tmp_ar;
  	numOfCntrs 					= new int [numOfIndicators];
  	lgNumOfCntrs 				= new int [numOfIndicators] ;

  	numOfCntrs		[algIndicatorIdx]	= size;
  	calcLgNumOfCntrs ();
    
  }      
  
  @Override
  // Returns the size of the indicator transmitted to the user. 
  // In case of CBF, the "indicator size" sent is actually the number of counters in the indicator
  public int indicatorSize (int i) {
  	return numOfCntrs[i];
  }

  
  // Returns the number of counters in each CBF, as int[]  
  @Override
  public int[] numOfCntrs () {
  	int[] numOfCntrs = new int[numOfIndicators];
  	Arrays.parallelSetAll (numOfCntrs, i -> updated_indicator[i].getSize());    
  	return numOfCntrs;
  } 

  // Returns the number of counters in each CBF, as double[]  
  @Override
  public double[] numOfCntrsDouble () {
  	int		[] numOfCntrs 			= numOfCntrs ();
  	double[] numOfCntrsDouble = new double[numOfIndicators];
  	Arrays.parallelSetAll (numOfCntrsDouble, i -> (double) numOfCntrs[i]);    
  	return numOfCntrsDouble;
  } 

  // Returns for each CBF the parameter "m", namely, the # of counters over the maximal # of items in the $  
  @Override
  public double[] bitPerElement () {
  	double[] bpe 							= new double[numOfIndicators];
  	double[] numOfCntrsDouble = numOfCntrsDouble ();
   	double cacheSizeDouble 		= (double)cacheSize;
   	Arrays.parallelSetAll (bpe, i -> numOfCntrsDouble[i] / cacheSizeDouble); 	
  	return bpe;
  }
   
  // Queries the last known indicator to the user, in case of enforcing token regulation and oversubscription
  @Override
  protected int queryLastKnownIndicator (K key, int i) {
  	return lastKnownIndicator[i].contains (key)? 1 : 0; 
  }
  
  // Query all the stale indicators, and set indications[i] to 1 if the given key is found in CBF i, 0 else.
  // Set for each i invIndication[i] to the complement of indication[i]
  @Override
  protected void queryAll (K key) {
		Arrays.parallelSetAll (indication, 	  i -> stale_indicator[i].contains (key)? 1 : 0); 		
		Arrays.parallelSetAll (invIndication, i -> 1 - indication[i]);
  }

  // Queries a single CBF for a given key.
  // Sets indications[i] to 1 if the given key is found in CBF i, 0 else.
  // Sets invIndications[i] to the complement of indication[i]  
  // If the key wasn't already in the CBF - insert it
  @Override 
  protected void queryAndInsertSingleIndicator (int i, K key) {
  	if (stale_indicator[i].contains (key)) { // Is key found in the (stale) indicator?  
  		indication[i] = 1;
  	}
  	else { // key wasn't found in the (stale) indicator?
  		indication[i] = 0;
  		updated_indicator[i].add (key); // Insert the key to the (updated) indicator
  	}
		invIndication[i] = 1 - indication[i];
  }

  @Override
  protected int querySingleIndicator (int i, K key) {
  	return stale_indicator[i].contains (key)? 1 : 0; 
  }

  // Insert a given key to a single (updated) CBF.
  @Override
  protected void insertToSingleIndicator (int i, K key) {
  	updated_indicator[i].add (key);
  }

  // Removes the given key from updated_indicator[i]
  @Override
  public void removeFromSinglendicator (int i, K key) {
  	updated_indicator[i].remove (key);
  }
 
  // Insert a given key to the all indicators
  @Override
  public void insertToAllIndicators (K key) {
	  for (short i = 0; i < numOfIndicators; i++) {
	  	updated_indicator[i].add (key);
	  } 	
  }

  @Override
  public void removeFromAllIndicators (K key) {
  	for (short i = 0; i < this.numOfIndicators; i++) {
    	updated_indicator[i].remove (key);
  	}    
  }

  // Simulate sending an update by copying all the updated indicators to the respective stale indicators
  @Override
  public void sendUpdate () {

  	if (sendOnlyFullIndicators) { // Have to send only full indicators - no DELTAs allowed
  		Arrays.parallelSetAll (bwInCurReconfInterval, i -> bwInCurReconfInterval[i] + (double) numOfCntrs[i]); // inc the bw by the size of the relevant full ind'
  		Arrays.parallelSetAll (stale_indicator, 			i -> updated_indicator		[i].clone());  								 // copy updated_indicator to stale_indicator
  	}
  	else { // Allowed to use DELTAs
    	for (short i = 0; i < numOfIndicators; i++) {
    		updateSingleIndicator (i);
    	}
  	}
  }

  // Copy updated_indicator[i] to stale_indicator[i]
  // Increment the bw consumption count respectively
  @Override
	protected void updateSingleIndicator (int i) {
  	
  	// First check the size of the DELTA update
    BitSet diffBitSet = updated_indicator[i].getBitSet(); //diffBitSet will hold the location of the bits set in the updated indicators
    diffBitSet.xor     (stale_indicator  [i].getBitSet()); // XOR with the stale idicator's bitset to find differences
    int deltaUpdateSize = diffBitSet.cardinality() * lgNumOfCntrs[i];
    if (sendFullIndicator) {
    	bwInCurReconfInterval[i] 	= (double) (numOfCntrs[i]);
    	sendFullIndicator = false;
    	fullUpdatesCnt++;
    }
    else {
      if (deltaUpdateSize < numOfCntrs[i]) {
      	deltaUpdatesCnt++;
      	bwInCurReconfInterval [i] += (double) deltaUpdateSize;
      }
      else {
      	fullUpdatesCnt++;
      	bwInCurReconfInterval [i] += (double) numOfCntrs[i];
      }
    }
  	if (runReconfAlg && enforceTokensRegulation && oversubscribedToken == false && bwInCurReconfInterval[algIndicatorIdx] >= token) {
      lastKnownIndicator[i] = stale_indicator[i].clone(); //The stale indicator (before the current update) is the last known to the user; the user won't see further updates until a new token is given
      oversubscribedToken   = true; // finished using the current token -> blocked until receiving the next token
  	}    	
	  stale_indicator[i] = updated_indicator[i].clone();  		
	}


}