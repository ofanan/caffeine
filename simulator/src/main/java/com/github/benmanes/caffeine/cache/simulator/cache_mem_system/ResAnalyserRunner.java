package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

// Runs the "ResAnalyser", which is a post-processor of result files.
public class ResAnalyserRunner {

	public static void main(String[] args) {
		ResAnalyser resAnalyser = new ResAnalyser();

		// resAnalyser.barAlgVsStatic("wiki");
		 resAnalyser.runAll ();
	}

}
