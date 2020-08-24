package com.github.benmanes.caffeine.cache.simulator.cache_mem_system;

import static java.util.stream.Collectors.toSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.github.benmanes.caffeine.cache.simulator.admission.Admission;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException; //.ConfigException.*;
import com.typesafe.config.ConfigFactory;

public class MyConfig {
  static final String path_to_conf_file  = "\\src\\main\\resources\\";
  static final String path_to_trace_file = "\\src\\main\\resources\\com\\github\\benmanes\\caffeine\\cache\\simulator\\parser\\";
  static final String confFileName = System.getProperty("user.dir") + path_to_conf_file + "application.conf";
  static final String trace_file_name = "WikiBench.csv"; 
  static final String path_to_res_file = "\\results\\";
  static 			 int 		initialIteration; // the current iteration of the simulation. Used for running multiple sequencing simulations, e.g., for different update intervals, or bw budgets.
  static 			 int 		currIteration; // the current iteration of the simulation. Used for running multiple sequencing simulations, e.g., for different update intervals, or bw budgets.
  static 			 String	ScndConfFileName = "application2.conf";	// The 2nd conf' file to run (in addition to application.conf). Used to allow running multiple sims in parallel.   
  static List<String>	traceFileName = null;
  static 			 int		numOfPoliciesRunning;
  
  public static void setTraceFileName (String fileName) {
  	traceFileName = new ArrayList<>();
  	traceFileName.add(fileName);
  }
  
  public static void setTraceFileName (String format, String fileName) {
  	traceFileName = new ArrayList<>();
  	traceFileName.add(format + ":" + GetPathToTraceFile() + fileName);
  }
  
  public static String GetPathToTraceFile () {
  	return System.getProperty("user.dir") + path_to_trace_file;
  }
  
  public static String GetFullPathToConfFile () {
  	return System.getProperty("user.dir") + path_to_conf_file;
  }
  
  
  // Set the 2nd conf' file to run (in addition to application.conf). Used to allow running multiple sims in parallel.
  public static void setScndConfFileName (String str) {
  	ScndConfFileName = str;
  }
  
  // Get the name of the 2nd conf' file to run (in addition to application.conf). Used to allow running multiple sims in parallel.
  public static String getScndConfFileName () {
  	return ScndConfFileName;
  }
  
  // Set the number of the initial iteration. Used to allow multiple parallel simulations, starting from iteration > 0.
  public static void setInitialIteration (int i) {
  	initialIteration = i;
  }
  
  // Get the number of the initial iteration. Used to allow multiple parallel simulations, starting from iteration > 0.
  public static int getInitialIteration () {
  	return initialIteration;
  }

  // Set the number of the iteration counted in the simulator main's file (simulator.java).
  public static void setIteration (int i) {
  	currIteration = i;
  }
  
  // Let SetOfIndicators.java know what is the current iteration number
  public static int getIteration () {
  	return currIteration;
  }

  // Returns the full path of the result file for a given policy name
  // Enables adding to the fileName the update interval method (currently commented-out).
  public static String resFileFullPath () {
  	return System.getProperty("user.dir") + path_to_res_file;
  } 

  // Returns the full path of the result file for a given policy name
  // Enables adding to the fileName the update interval method (currently commented-out).
  public static String getFullPathResFileName (String str) {
  	return System.getProperty("user.dir") + path_to_res_file + str + ".res";
  } 
  
  // Write a given string to a given output fileName. If file exists - overwrite
  public static void overwriteStringToFile (String fileName, String str) {
    try {
      BufferedWriter writer = Files.newBufferedWriter (Paths.get(fileName), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
      writer.write (str);
      writer.close ();
    }
    catch (IOException e) {
  		System.out.println ("***Error: couldn't write to file " + fileName + " due to the following exception: " + e.getMessage());
  		System.exit(0);
    }
	}

  // Write a given string to a given output fileName. If file exists - append
  public static void writeStringToFile (String fileName, String str) {
    try {
      BufferedWriter writer = Files.newBufferedWriter (Paths.get(fileName), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      writer.write (str);
      writer.close ();
    }
    catch (IOException e) {
  		System.out.println ("***Error: couldn't write to file " + fileName + " due to the following exception: " + e.getMessage());
  		System.exit(0);
    }
	}

  // Returns a File (java's file descriptor) for a given fileName.
  public static File getFile (String fileName) {
    File file = new File (fileName); //("C:\\Users\\ofanan\\gamad.txt"); //
    if (!file.isFile()) {
      System.out.println ("****** Error ****: Cannot find file " + fileName);
      System.exit(0);
    }
    return file;
  }
  
  // Returns a Config instance for a given fileName
  private static Config myGetConfig (String fileName) {
    return ConfigFactory.parseFile (getFile (fileName));
  }
  
  // Returns an int, which is the value of a key (given as a string) written in a given fileName
  private static int getIntParameterFromFile (String str, String fileName) {
    int res = -1;
    try {
      res = myGetConfig(fileName).getInt(str);
    }
    catch (ConfigException.Missing | ConfigException.WrongType e) { //
      System.out.println("Missing int parameter " + str + " in file " + fileName);
      System.exit (0);
    }
    return res;
  }
  
  // Returns an int, which is the value of a key (given as a string) written in a given fileName
  private static double getDoubleParameterFromFile (String str, String fileName) {
    Double res = -1.0;
    try {
      res = myGetConfig(fileName).getDouble(str);
    }
    catch (ConfigException.Missing | ConfigException.WrongType e) { //
      System.out.println("Missing double parameter " + str + " in file " + fileName);
      System.exit (0);
    }
    return res;
  }
  
  // Returns an int, which is the value of a key (given as a string) written in a given fileName
  private static String getStringParameterFromFile (String str, String fileName) {
    String res = null;
    try {
      res = myGetConfig(fileName).getString(str);
    }
    catch (ConfigException.Missing | ConfigException.WrongType e) { //
      System.out.println("Missing string parameter " + str + " in file " + fileName);
      System.exit (0);
    }
    return res;
  }
  
/*  
  // Returns the desired String parameter written in the configuration file
  public static String GetStringParameterFromConfFile (String str) {
    String res = null;
    try {
      res = myGetConfig(confFileName).getString(str);
    }
    catch (ConfigException.Missing | ConfigException.WrongType e) { //
      System.out.println("****** Error ****: Missing String parameter " + str + " in configuration file");
      System.exit (0);
    }
    return res;
  }
  
  // Returns the desired double parameter written in the configuration file
  public static double GetDoubleParameterFromConfFile (String str) {
    double res = -1;
    try {
      res = myGetConfig(confFileName).getDouble(str);
    }
    catch (ConfigException.Missing | ConfigException.WrongType e) { //
      System.out.println("Missing double parameter " + str + " in configuration file");
      System.exit (0);
    }
    return res;
  }
*/
  
  // Returns the desired int parameter written in the default configuration file (application.conf)
  public static int GetIntParameterFromConfFile (String str) {
  	return getIntParameterFromFile (str, confFileName);
  }

  // Returns the desired int parameter written in the default configuration file (application.conf)
  public static double GetDoubleParameterFromConfFile (String str) {
  	return getDoubleParameterFromFile (str, confFileName);
  }

  // Returns the desired int parameter written in the default configuration file (application.conf)
  public static String GetStringParameterFromConfFile (String str) {
  	return getStringParameterFromFile (str, confFileName);
  }
  
  // Returns a list of Double parameters from from a conf' file
  public static List<Double> getDoubleListParameterFromFile (String str, String fileName) {
  	List<Double> res = null;

      try {
        res = myGetConfig(fileName).getDoubleList(str);
      }
      catch (ConfigException.Missing | ConfigException.WrongType e) { //
        System.out.println("Missing string[] parameter " + str + " in file " + fileName);
        System.exit (0);
      }
    return res;
  }
  

  // Returns a list of String parameters from from a conf' file
  public static List<String> getStringListParameterFromFile (String str, String fileName) {
  	List<String> res = null;

      try {
        res = myGetConfig(fileName).getStringList(str);
      }
      catch (ConfigException.Missing | ConfigException.WrongType e) { //
        System.out.println("Missing string[] parameter " + str + " in file " + fileName);
        System.exit (0);
      }
    return res;
  }
  
  public static void setNumOfPoliciesRunning () {
  	numOfPoliciesRunning = getStringListParameterFromFile ("caffeine.simulator.policies", confFileName).size();
  }
  
  public static int decNumOfPoliciesRunning () {
  	return (--numOfPoliciesRunning);
  }
  
  // Returns the names of the trace files running in the trace.
  public static List<String>  getTraceFileName() {
  	return (traceFileName == null)? 
  			getStringListParameterFromFile ("caffeine.simulator.files.paths", confFileName) : traceFileName;
  }

  // Returns A list of the budgets to be run.
  public static List<Double>  getBudgets() {
  	return getDoubleListParameterFromFile ("budgets", confFileName);
  }

  // Returns the name of the trace file running in the trace.
  public static String getTraceName() {
  	 String res = "";
  	 String fileName, fullPathfileName;
  	 String[] fullPathfileNameTokens;
  	 for (String fullTraceName : getTraceFileName()) {
  		 fullPathfileName 				= fullTraceName.split(":")[2];
  		 fullPathfileNameTokens 	= fullPathfileName.split("\\\\"); 
  		 fileName									= fullPathfileNameTokens[fullPathfileNameTokens.length-1]; 
  		 res 										  = res + fileName.split("\\.")[0] + ".";
  	 }
  	return res;
  }
  
  // Returns a File (java's File Descriptor) for the trace file to be run.
  // This function is usually not used, because I use Caffeine's native traces.
  public static File GetTraceFile() {
    String trace_file_full_path = System.getProperty("user.dir") + "\\traces\\" + trace_file_name;
    File trace_file = new File (trace_file_full_path);   
    if (!trace_file.isFile()) {
      System.out.println ("Trace file " + trace_file_full_path + " does not exist");
      System.exit(0);
    }
    return trace_file;
  }
  
  // Returns a Scanner, which is Java's class object for reading from a file.
  public static Scanner GetTraceScanner() {
    Scanner scanner = null; 
    try {
      scanner = new Scanner(GetTraceFile());
    }
    catch (FileNotFoundException e) {
      System.out.println("Couldn't open Scanner for reading file " + e.getMessage());
      System.exit (0);      
    }
    return scanner;
  }
  
  public static void printAndExit (String str) {
  	System.out.println (str);
  	System.exit (0);
  }
  
  public static Set<Admission> admission() {
    return 
    		myGetConfig(confFileName).getStringList("admission").stream()
        .map(String::toUpperCase)
        .map(Admission::valueOf)
        .collect(toSet());
  }

}
