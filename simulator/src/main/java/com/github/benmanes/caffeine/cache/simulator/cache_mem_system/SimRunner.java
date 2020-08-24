package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

import com.github.benmanes.caffeine.cache.simulator.Simulator;

public class SimRunner {

  public static void main(String[] args) {
  	// For running a single trace, call MyConfig.setTraceFileName ({trace-format:full-path-to-trace). 
  	// This allows running the program multiple times in parallel, each time with a different trace.
  	// For running concatenation of several traces, do NOT call MyConfig.setTraceFileName from here, 
  	// and instead, write the requested traces names within application.conf file.
  	String runP8 			= "arc:C:\\Users\\ofanan\\Documents\\traces\\arc\\P8.lis";
  	String runP6 			= "arc:C:\\Users\\ofanan\\Documents\\traces\\arc\\P6.lis";
  	String runP3 			= "arc:C:\\Users\\ofanan\\Documents\\traces\\arc\\P3.lis";
  	String runF2 			= "umass-storage:C:\\Users\\ofanan\\Documents\\traces\\umass\\storage\\F2.spc.bz2";
  	String runF1 			= "umass-storage:C:\\Users\\ofanan\\Documents\\traces\\umass\\storage\\F1.spc.bz2";
  	String runScarab 	= "scarab:C:\\Users\\ofanan\\Documents\\traces\\scarab\\scarab.recs.trace.20160808T073231Z.xz";
  	String runWiki2 	= "wikipedia:C:\\Users\\ofanan\\Google Drive\\Comnet\\BF_n_Hash\\Python_Infocom19\\wiki2.1191403252.gz";
  	String runWiki1 	= "wikipedia:C:\\Users\\ofanan\\Google Drive\\Comnet\\BF_n_Hash\\Python_Infocom19\\wiki.1190448987.gz";
  	 	
 		MyConfig.setTraceFileName (runF1);
  
  	javax.swing.SwingUtilities.invokeLater(new Runnable() {
      	public void run() {
          	String[] args = {
          									"0", // number of first iteration. Used for multiple parallel simulations
          									 "1",  // number of iterations to run
          									 "rgrg" // name of 2nd conf' file, to be used for parallel runs (currently unused).
          									};
          	Simulator.main(args);
          
          }
      });
  }
}

	