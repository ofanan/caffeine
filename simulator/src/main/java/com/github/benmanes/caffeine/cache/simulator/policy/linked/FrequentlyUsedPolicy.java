/*
 * Copyright 2015 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache.simulator.policy.linked;

import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.benmanes.caffeine.cache.simulator.BasicSettings;
import com.github.benmanes.caffeine.cache.simulator.admission.Admission;
import com.github.benmanes.caffeine.cache.simulator.admission.Admittor;
import com.github.benmanes.caffeine.cache.simulator.cache_mem_system.SetOfCBFs;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy;
import com.github.benmanes.caffeine.cache.simulator.policy.Policy.KeyOnlyPolicy;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.google.common.base.MoreObjects;
import com.typesafe.config.Config;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * Least/Most Frequency Used in O(1) time as described in <a href="http://dhruvbird.com/lfu.pdf"> An
 * O(1) algorithm for implementing the LFU cache eviction scheme</a>.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public class FrequentlyUsedPolicy implements KeyOnlyPolicy { //$$ Removed "final", for letting other classes extend it
  private SetOfCBFs<Long> indicators; //$$ Added
	protected PolicyStats policyStats; //$$ Changed from "final" to "protected", for letting sub-class MyLinkedPolicy to access it
  final Long2ObjectMap<Node> data; // The cache
  final EvictionPolicy policy;
  final FrequencyNode freq0;
  final Admittor admittor;
  final int maximumSize;

  public FrequentlyUsedPolicy(Admission admission, EvictionPolicy policy, Config config) {
    this.policyStats = new PolicyStats(admission.format("linked." + policy.label()));
    this.admittor = admission.from(config, policyStats);
    BasicSettings settings = new BasicSettings(config);
    this.data = new Long2ObjectOpenHashMap<>();
    this.maximumSize = settings.maximumSize();
    this.policy = requireNonNull(policy);
    this.freq0 = new FrequencyNode();
    indicators = new SetOfCBFs<Long> (policyStats.name()); //$$
  }

  /** Returns all variations of this policy based on the configuration parameters. */
  public static Set<Policy> policies(Config config, EvictionPolicy policy) {
    BasicSettings settings = new BasicSettings(config);
    return settings.admission().stream().map(admission ->
      new FrequentlyUsedPolicy(admission, policy, config) 
    ).collect(toSet());
  }

  @Override
  public PolicyStats stats() {
    return policyStats;
  }

  //$$
  public boolean isInCache (long key) {
    return (data.get(key) == null)? false:true;   
  }
  
  // $$ Some policies use private methods and / or private class Node. 
  // Hence, it's hard to intercept / override these methods by another class.
  // To solve it, I added "trap" calls to indicateVictim / indicateInsertion.  
  
  //$$ Inform inheriting classes about a victimized key.
  // The function is empty - to be overriden by inheriting classes.
  public void indicateVictim (long key) {
  	indicators.removeFromAllIndicators (key);
  }

  //$$ Inform inheriting classes about a victimized key.
  // The function is empty - to be overriden by inheriting classes.
  public void indicateInsertion (long key) {
  	indicators.insertToAllIndicators (key);
  }

  
  @Override
  public void record(long key) {
    indicators.handleRequest (key, isInCache(key)); //$$ 
    policyStats.recordOperation();
    Node node = data.get(key);
    admittor.record(key);
    if (node == null) {
      onMiss(key);
    } else {
      onHit(node);
    }
  }

  /** Moves the entry to the next higher frequency list, creating it if necessary. */
  private void onHit(Node node) {
    policyStats.recordHit();

    int newCount = node.freq.count + 1;
    FrequencyNode freqN = (node.freq.next.count == newCount)
        ? node.freq.next
        : new FrequencyNode(newCount, node.freq);
    node.remove();
    if (node.freq.isEmpty()) {
      node.freq.remove();
    }
    node.freq = freqN;
    node.append();
  }

  /** Adds the entry, creating an initial frequency list of 1 if necessary, and evicts if needed. */
  private void onMiss(long key) {
    FrequencyNode freq1 = (freq0.next.count == 1)
        ? freq0.next
        : new FrequencyNode(1, freq0);
    Node node = new Node(key, freq1);
    policyStats.recordMiss();
    data.put(key, node);
    node.append();
    evict(node);
  }

  /** Evicts while the map exceeds the maximum capacity. */
  private void evict(Node candidate) {
    if (data.size() > maximumSize) { // Cache is full
      Node victim = nextVictim(candidate);
      boolean admit = admittor.admit(candidate.key, victim.key);
      if (admit) {
      	indicateInsertion (candidate.key); //$$ Let sub-classes know that the candidate is inserted
      	indicateVictim (victim.key); //$$ Let sub-classes know that this key is victimized
        evictEntry(victim);
      } else {
        evictEntry(candidate); // The candidate isn't admitted to the $
      }
      policyStats.recordEviction();
    }
    else { //$$ The cache isn't full, so the candidate is inserted --> inform the indicator 
    	indicateInsertion (candidate.key); //$$ 
    }
  }

  @Override
  public void finished () {
  	indicators.finished(policyStats);
  }

  /**
   * Returns the next victim, excluding the newly added candidate. This exclusion is required so
   * that a candidate has a fair chance to be used, rather than always rejected due to existing
   * entries having a high frequency from the distant past.
   */
  Node nextVictim(Node candidate) {
    if (policy == EvictionPolicy.MFU) {
      // highest, never the candidate
      return freq0.prev.nextNode.next;
    }

    // find the lowest that is not the candidate
    Node victim = freq0.next.nextNode.next;
    if (victim == candidate) {
      victim = (victim.next == victim.prev)
          ? victim.freq.next.nextNode.next
          : victim.next;
    }
    return victim;
  }

  private void evictEntry(Node node) {
    data.remove(node.key);
    node.remove();
    if (node.freq.isEmpty()) {
      node.freq.remove();
    }
  }

  public enum EvictionPolicy {
    LFU, MFU;

    public String label() {
      return StringUtils.capitalize(name().toLowerCase(US));
    }
  }

  /** A frequency count and associated chain of cache entries. */
  static final class FrequencyNode {
    final int count;
    final Node nextNode;

    FrequencyNode prev;
    FrequencyNode next;

    public FrequencyNode() {
      nextNode = new Node(this);
      this.prev = this;
      this.next = this;
      this.count = 0;
    }

    public FrequencyNode(int count, FrequencyNode prev) {
      nextNode = new Node(this);
      this.prev = prev;
      this.next = prev.next;
      prev.next = this;
      next.prev = this;
      this.count = count;
    }

    public boolean isEmpty() {
      return (nextNode == nextNode.next);
    }

    /** Removes the node from the list. */
    public void remove() {
      prev.next = next;
      next.prev = prev;
      next = prev = null;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("count", count)
          .toString();
    }
  }

  /** A cache entry on the frequency node's chain. */
  static final class Node {
    final long key;

    FrequencyNode freq;
    Node prev;
    Node next;

    public Node(FrequencyNode freq) {
      this.key = Long.MIN_VALUE;
      this.freq = freq;
      this.prev = this;
      this.next = this;
    }

    public Node(long key, FrequencyNode freq) {
      this.next = null;
      this.prev = null;
      this.freq = freq;
      this.key = key;
    }

    /** Appends the node to the tail of the list. */
    public void append() {
      prev = freq.nextNode.prev;
      next = freq.nextNode;
      prev.next = this;
      next.prev = this;
    }

    /** Removes the node from the list. */
    public void remove() {
      prev.next = next;
      next.prev = prev;
      next = prev = null;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("key", key)
          .add("freq", freq)
          .toString();
    }
  }
}