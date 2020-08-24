package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

import com.github.benmanes.caffeine.cache.simulator.policy.irr.LirsPolicy;
import com.typesafe.config.Config;

// A generic envelope, which can be used (with some little changes) by several policies
// MyGenericPolicy adds to the Policy it extends the following capabilities:
// - Tracking changes in the cache, 
//- Updating indicators.
//- Using the indicator's indications as an "access strategy" to the cache / directly to the "mem".
// - Collecting system-level stats (e.g., counts of true / false positives and true / false negatives)
public class MyLirsPolicy extends LirsPolicy {

  // C'tor
  public MyLirsPolicy (Config config) {
    super (config);
    indicators = new SetOfCBFs<Long> (policyStats.name());
  }

  // Intercept that a key was victimized from the cache
  @Override
  public void indicateVictim (long key) {
  	indicators.remove (key);
  }

  // Intercept that a key was victimized from the cache
  @Override
  public void indicateInsertion (long key) {
  	indicators.insert (key);  
  }

  @Override
  public void finished () {
  	indicators.finished(policyStats);
  }
}