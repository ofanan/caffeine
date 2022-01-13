[![Build Status](https://travis-ci.org/ben-manes/caffeine.svg)](https://travis-ci.org/ben-manes/caffeine)
[![Coverage Status](https://img.shields.io/coveralls/ben-manes/caffeine.svg)](https://coveralls.io/r/ben-manes/caffeine?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ben-manes.caffeine/caffeine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ben-manes.caffeine/caffeine)
[![JavaDoc](http://www.javadoc.io/badge/com.github.ben-manes.caffeine/caffeine.svg)](http://www.javadoc.io/doc/com.github.ben-manes.caffeine/caffeine)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Stack Overflow](http://img.shields.io/:stack%20overflow-caffeine-brightgreen.svg)](http://stackoverflow.com/questions/tagged/caffeine)
<a href="https://github.com/ben-manes/caffeine/wiki">
<img align="right" height="90px" src="https://raw.githubusercontent.com/ben-manes/caffeine/master/wiki/logo.png">
</a>

Caffeine is a [high performance](https://github.com/ben-manes/caffeine/wiki/Benchmarks), [near optimal](https://github.com/ben-manes/caffeine/wiki/Efficiency) Java caching library. Caffeine's simulator allows simulating multiple cache policies, sizes and workloads. For more details, see caffeine's [user's guide](https://github.com/ben-manes/caffeine/wiki) and browse the [API docs](https://www.javadoc.io/doc/com.github.ben-manes.caffeine/caffeine/latest/com.github.benmanes.caffeine/module-summary.html) for
the latest release.

This fork adds to Caffeine's simulator an indicator. An indicator is a compact, lightweight database that allows the user predict whether a requested datum is found in the cache. If the answer is negative, the user can access directly a remote server instead of querying the cache for that item, thus saving the overhead of unnecessary cache accesses. The most prevalent indicator is the Bloom filter.

This fork further implements and tests the algorithm CAB, which dynamically scales the indicator, and the frequency of sending it to the user. This fork also implements and tests an optimal static 
advertisement strategy. For further details, please refer to the paper

Cohen, G. Einziger, G. Scalosub. [Self-adjusting Advertisement of Cache Indicators with Bandwidth Constraints](https://www.researchgate.net/profile/Itamar-Cohen-2/publication/346733118_Self-adjusting_Advertisement_of_Cache_Indicators_with_Bandwidth_Constraints/links/606825bd92851c91b19c20b5/Self-adjusting-Advertisement-of-Cache-Indicators-with-Bandwidth-Constraints.pdf), Infocom 2021, pp. 1-10.

The documentation bellow details how to run a simulation, and the directories and files. Further documentation is found within the code files. 

### Running a simulation

To run a simulation, run the file 

\caffeine\simulator\src\main\java\com\github\benmanes\caffeine\cache\simulator\cache_mem_system\SimRunner.java 

as a Java application (in Eclipse: ctrl+F11, or select the file in the file’s browser within the project’s view, and then press ctrl-x, and then J).

The simulation's settings are determined by the configuration file:

caffeine\simulator\src\main\resources\application.conf

This file determines settings such as the cache policy, which trace to run, etc.
To allow running multiple simulations in parallel, some of the parameters (trace name, policy, iteration #) can be set also from SimRunner.java.

Results ('.res') files are written to: 
##### caffeine\simulator\results

#### Post-simulation parsing of results

The file 

\caffeine\simulator\src\main\java\com\github\benmanes\caffeine\cache\simulator\cache_mem_system\ResAnalyserRunner.java 

parses the '.res' result files produced by the simulator, and performs tasks such as generating the points for tikz plots.

To select which task to run (e.g., which tikz plot input to generate), see the documentation within 

caffeine\simulator\src\main\java\com\github\benmanes\caffeine\cache\simulato/cache_mem_system/ResAnalyser.java 

### Source code details #

Edits to the standard Caffeine Github source files are usually indicated by $$ in the comments in the files. All relevant source files are in

caffeine\simulator\src\main\java\com\github\benmanes\caffeine\cache\simulator

The paths described below are as relative to that path.

##### simulator.java
Runs a simulation.
The simulation runs by akka actors, which are async threads. 

To allow multiple sequential runs (e.g., with various update intervals), use the variables currIteration and numOfIterations.
The number of iterations is set by SimRunner.java.

The concrete tasks to run (e.g., run simulation, post-sim analysis of results for plotting a pareto / heatmap) are also defined in application.conf.
In general, the simulator either generates a simulation (by registry.java, setOfIndicators.java etc., see below), or a post-sim analysis (by resAnalyzer.java, see below).

##### policy\registry.java
In this file each cache policy registers itself and define the function which dispatches its running (usually XXXPolicy.policies).

The main here w.r.t. the standard Caffeine are:
- Addion of “import” to “my” cache policies.
- Adding the functions “registerMyXXX” to the functions “registerXXX”. These functions handle the policies beginning with “my_”.
The dispatch function of “my” policies is MyXXXPolicy.policies

##### policy\PolicyStats.java
Used to collect stats during the simulation, and analyze it (e.g., calculate the hit ratio) in the end. The class’ fields include the policy name and multiple counters which the policy ask its policyStats to increase during the simulation.

The code includes some counters and functions not found in the standard Caffeine. However, they’re currently unused, as the simulation code collects statistics, analyze and prints independently, without using this stats.

##### policy\AccessEvent.java
Class of the event ( “request”). Includes the requested key, its weight, the concrete miss penalty of this concrete key etc.

##### report\ directory
This directory includes several classes which calculate / print the post-sim reports in various formats. Currently I don’t use them, as my code collects code, analyze and prints independently.

##### Policy\…\XXXPolicy.java
(e.g., policy\linked\XXXPolicy.java)
Using Caffeine’s simulator policies for our needs entitles the following changes:
- Added import of …/CacheMemSystem.My….Policy;
- Added function isInCache(), for checking whether an item is in the cache, even without requesting the item from the cache. This is required mainly for stat (knowing whether as a request resulted in true / false positive / negative.
- Added functions indicateInsertion, indicateVictim – intercepts insertion / victimization of a key from the $, for letting setOfIndicators add / remove the items from the indicators accordingly.
- Some changes of fields from “private” or “final”, for letting other classes using and extending these fields.
- Change of the method policies, so that now it calls MyXXXPolicy rather than XXXPolicy.

##### cache_mem_system\ directory
This directory contains the code files, which are new (that is, not slight modifications of standard Caffeine). 
The code files in this directory are detailed below.

##### cache_mem_system\MyConfig.java
This class is responsible to reading configuration details from the file application.conf (see above), and writing results to stdout / to files it generates in simulator\results.
The class also helps the main, found in simulator.java, and the akka actors (the policies which run along the simulation) set / get the current iteration #.

##### cache_mem_system\MyXXXPolicy.java
(e.g., MyLinkedPolicy.java).
A class which extends Caffeine’s original XXX policy, by adding to it indicators and more statistics. 

In particular, upon intercepting an insertion / eviction of a key to / from the cache, the class updates the indicators. 
When finished, the class calls the indicators finished() function to analyze and print the stat.

Upon a record (a data request), the class updates the indicators, for deciding whether to access the cache / the memory and update the counters respectively, and then call the method record of the super-class XXXPolicy.

##### cache_mem_system\SetOfIndicators.java
The heart of this project. Includes 2 arrays of indicators, each of them of its own size, as required by the desired inherent (“designed”) false positive rate, indicated in application.conf (see above).

Keys are inserted into the updated indicators. Queries are from stale indicators. Once in a while a “SendUpdate()” happens: all the updated indicators are copied to the respective stale indicators.

Note that “TP, FP” etc. calculated here are different from false positive ratio, false negative ratio. Here we use mainly the false positive / negative probability, that is, the # of TP, FP etc., over the total # of req.

Using the generic class <K> for the keys allows storing different types of Keys (Caffeine’s simulator type for the keys is long).

The individual insertion / removal / query operations are performed by the inheriting classes (e.g., SetOfCBFs), thus allowing multiple implementations of the indicators. 

While during running a trace, or just before exiting, SetOfIndicators collects may print various reports to output files. The definition of which reports to print is done by the parameter “verbose” in application.conf (see above).

##### cache_mem_system\SetOfCBFs.java
Inherits from cache_mem_system\SetOfIndicators.java, using CBFs as indicators.

##### cache_mem_system\resAnalyser.java
-	Performs post-run calculations, such as absolute / normalized the costs of Approximate Ind, Perfect Ind’ and No Ind’. 
-	Formats the data in a convenient way to tikz.

Note that the cost may be calculated post-run iff the accs strategy is independent upon the missp, fpr, fnr etc. If the access strat’ is dependent upon missp, fpr , fnr, one should consider that already during run time.

### Indicators used in this proejct
Caffeine has several built-in indicators which are used for items’ popularity evaluation. They are found in: 

com.github.benmanes.caffeine.cache.simulator.membership.

However, none of them support removals. The indicators by com.google.common.hash.BloomFilter, or com.clearspring.analytics.stream.membership also don't support removals.

Hence, this project uses the [Orestes](https://github.com/Baqend/Orestes-Bloomfilter) Counting Bloom Filters.





















