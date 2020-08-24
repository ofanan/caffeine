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
package com.github.benmanes.caffeine.cache.simulator.policy;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;

/**
 * Statistics gathered by a policy execution.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class PolicyStats {
  private final Stopwatch stopwatch;

  private String name;
  private long hitCount;
  private long missCount;
  private long hitsWeight;
  private long missesWeight;
  private double hitPenalty;
  private double missPenalty;
  private long evictionCount;
  private long admittedCount;
  private long rejectedCount;
  private long operationCount;
  
  //$$ Added fields (cntrs)
  private long tpCnt;
  private long fpCnt;
  private long fnCnt;
  private long tnCnt;
  private long stalenessFpCnt;

  public PolicyStats(String name) {
    this.name = requireNonNull(name);
    this.stopwatch = Stopwatch.createUnstarted();
  }

  public Stopwatch stopwatch() {
    return stopwatch;
  }

  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = requireNonNull(name);
  }

  public void recordOperation() {
    operationCount++;
  }

  public long operationCount() {
    return operationCount;
  }

  public void addOperations(long operations) {
    operationCount += operations;
  }

  public void recordHit() {
    hitCount++;
  }

  public long hitCount() {
    return hitCount;
  }

  public void addHits(long hits) {
    hitCount += hits;
  }

  public void recordWeightedHit(int weight) {
    hitsWeight += weight;
    recordHit();
  }  
  
  public long hitsWeight() {
    return hitsWeight;
  }
  
  public void recordHitPenalty(double penalty) {
    hitPenalty += penalty;
  }

  public double hitPenalty() {
    return hitPenalty;
  }

  public void recordMiss() {
    missCount++;
  }

  public long missCount() {
    return missCount;
  }

  public void addMisses(long misses) {
    missCount += misses;
  }

  public void recordWeightedMiss(int weight) {
    missesWeight += weight;
    recordMiss();
  }  
  
  public long missesWeight() {
    return missesWeight;
  }
  
  public void recordMissPenalty(double penalty) {
    missPenalty += penalty;
  }

  public double missPenalty() {
    return missPenalty;
  }

  public long evictionCount() {
    return evictionCount;
  }

  public void recordEviction() {
    evictionCount++;
  }

  public void addEvictions(long evictions) {
    evictionCount += evictions;
  }

  public long requestCount() {
    return hitCount + missCount;
  }

  public long requestsWeight() {
    return hitsWeight + missesWeight;
  }

  public long admissionCount() {
    return admittedCount;
  }

  public void recordAdmission() {
    admittedCount++;
  }

  public long rejectionCount() {
    return rejectedCount;
  }

  public void recordRejection() {
    rejectedCount++;
  }

  public double totalPenalty() {
    return hitPenalty + missPenalty;
  }

  public double hitRate() {
    long requestCount = requestCount();
    return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
  }

  public double weightedHitRate() {
    long requestsWeight = requestsWeight();
    return (requestsWeight == 0) ? 1.0 : (double) hitsWeight / requestsWeight;
  }

  public double missRate() {
    long requestCount = requestCount();
    return (requestCount == 0) ? 0.0 : (double) missCount / requestCount;
  }

  public double weightedMissRate() {
    long requestsWeight = requestsWeight();
    return (requestsWeight == 0) ? 1.0 : (double) missesWeight / requestsWeight;
  }

  public double admissionRate() {
    long candidateCount = admittedCount + rejectedCount;
    return (candidateCount == 0) ? 1.0 : (double) admittedCount / candidateCount;
  }

  public double complexity() {
    long requestCount = requestCount();
    return (requestCount == 0) ? 0.0 : (double) operationCount / requestCount;
  }

  public double avergePenalty() {
    long requestCount = requestCount();
    return (requestCount == 0) ? 0.0 : totalPenalty() / requestCount;
  }

  public double avergeHitPenalty() {
    return (hitCount == 0) ? 0.0 : hitPenalty / hitCount;
  }

  public double averageMissPenalty() {
    return (missCount == 0) ? 0.0 : missPenalty / missCount;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).addValue(name).toString();
  }

  //$$ Added methods from here until the file's end
  
  // Record a True Positive event
  public void recordTp() {
    tpCnt++;
  }

  public long tpCnt() {
    return tpCnt;
  }
  
  public void recordFp() {
    fpCnt++;
  }

  public long fpCnt() {
    return fpCnt;
  }
  
  public void recordStalenessFp() {
    stalenessFpCnt++;
  }

  public long stalenessFpCnt() {
    return stalenessFpCnt;
  }
  
  public void recordFn() {
    fnCnt++;
  }

  public long fnCnt() {
    return fnCnt;
  }

  public void recordTn() {
    tnCnt++;
  }

  public long tnCnt() {
    return tnCnt;
  }

  public double fpRate() {
    long missCount = missCount();
    return (missCount == 0) ? 0.0 : (double) fpCnt / missCount;
  }

  // returns the ratio of FP which happened due to staleness  
  public double stalenessFpRate () {
    return (missCount == 0) ? 0.0 : (double) stalenessFpCnt / missCount;
  }

  public double fnRate() {
    return (hitCount == 0) ? 1.0 : (double) fnCnt / hitCount;
  }

  // Returns the ratio: true positive events over the total # of requests
  public double tpFromReqRate() {
    return (double) tpCnt / requestCount();
  }

  // Returns the ratio: falst positive events over the total # of requests
  public double fpFromReqRate() {
    return (double) fpCnt / requestCount();
  }

  // Returns the ratio: true negative events over the total # of requests
  public double tnFromReqRate() {
    return (double) tnCnt / requestCount();
  }

  // Returns the ratio: false negative events over the total # of requests
  public double fnFromReqRate() {
    return (double) fnCnt / requestCount();
  }

  // Returns the expected service cost of a NI (No Indicator) configuration
  public double NiServiceCost (double missp) {
    return 1 + (1 - hitRate()) * missp;
  }
  
  // Returns the expected service cost of a perfect indicator, normalized w.r.t. the service cost of No Indicator
  public double NormalizedPiServiceCost (double missp) {
    double piServiceCost = 1 + (1 - hitRate()) * (missp-1);
    return piServiceCost / NiServiceCost (missp);
  }

  // Returns the expected service cost of an approximate indicator, normalized w.r.t. the service cost of No Indicator
  public double NormalizedAiServiceCost (double missp) {
    double aiServiceCost = tpFromReqRate() + fpFromReqRate() + (1 - tpFromReqRate()) * missp;
    return aiServiceCost / NiServiceCost (missp);
  }
}


