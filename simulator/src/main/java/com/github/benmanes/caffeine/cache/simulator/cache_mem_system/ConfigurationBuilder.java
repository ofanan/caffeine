package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

public class ConfigurationBuilder {
	public double designedFpr = -1;
	public int 		indicatorSize = -1;
	public double updateInterval = -1;
	public double cost = -1;
	public double costOverOptCost; 
	public double normalizedBw = -1;
	public double TP, FP, TN, FN; // True Positive, False Positive, Negative, Rate (out of total requests)

  public ConfigurationBuilder() { }

  public Configuration buildConfiguration()
  {
      return new Configuration(designedFpr, updateInterval, cost, costOverOptCost, normalizedBw, indicatorSize, TP, FP, TN, FN);		     																																										
  }

  public ConfigurationBuilder designedFpr (double designedFpr)
  {
      this.designedFpr = designedFpr;
      return this;
  }

  public ConfigurationBuilder indicatorSize (int indicatorSize)
  {
      this.indicatorSize = indicatorSize;
      return this;
  }

  public ConfigurationBuilder updateInterval (double updateInterval)
  {
      this.updateInterval = updateInterval;
      return this;
  }

  public ConfigurationBuilder cost (double cost)
  {
      this.cost = cost;
      return this;
  }

  public ConfigurationBuilder costOverOptCost (double costOverOptCost)
  {
      this.costOverOptCost = costOverOptCost;
      return this;
  }
  

  public ConfigurationBuilder normalizedBw (double normalizedBw)
  {
      this.normalizedBw = normalizedBw;
      return this;
  }

  public ConfigurationBuilder TP (double TP)
  {
      this.TP = TP;
      return this;
  }

  public ConfigurationBuilder FP (double FP)
  {
      this.FP = FP;
      return this;
  }

  public ConfigurationBuilder TN (double TN)
  {
      this.TN = TN;
      return this;
  }

  public ConfigurationBuilder FN (double FN)
  {
      this.FN = FN;
      return this;
  }

}
