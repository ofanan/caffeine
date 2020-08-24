package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

import com.github.benmanes.caffeine.cache.simulator.admission.Admission;
import com.github.benmanes.caffeine.cache.simulator.policy.AccessEvent;
import com.github.benmanes.caffeine.cache.simulator.policy.linked.LinkedPolicy;
import com.typesafe.config.Config;


// A generic envelope, which can be used (with some little changes) by several policies
// MyGenericPolicy adds to the Policy it extends the following capabilities:
// - Tracking changes in the cache, 
//- Updating indicators.
//- Using the indicator's indications as an "access strategy" to the cache / directly to the "mem".
// - Collecting system-level stats (e.g., counts of true / false positives and true / false negatives)
public class MyLinkedPolicy extends LinkedPolicy {

  private SetOfCBFs<Long> indicators;

  // C'tor
  public MyLinkedPolicy (Admission admission, EvictionPolicy policy, Config config) {
    super (admission, policy, config);
    indicators = new SetOfCBFs<Long> (policyStats.name());
  }

  // Intercept requests for keys and insertions to the cache
  //@Override
  public void record(AccessEvent event) {
    long key = event.key();
    indicators.handleRequest (key, super.isInCache(key)); 
    super.record(event); 
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
