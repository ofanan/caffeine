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
package com.github.benmanes.caffeine.cache.simulator;

import static com.github.benmanes.caffeine.cache.simulator.Simulator.Message.ERROR;
import static com.github.benmanes.caffeine.cache.simulator.Simulator.Message.FINISH;
import static com.github.benmanes.caffeine.cache.simulator.Simulator.Message.START;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.benmanes.caffeine.cache.simulator.cache_mem_system.MyConfig;
import com.github.benmanes.caffeine.cache.simulator.cache_mem_system.ResAnalyser;
import com.github.benmanes.caffeine.cache.simulator.parser.TraceFormat;
import com.github.benmanes.caffeine.cache.simulator.parser.TraceReader;
import com.github.benmanes.caffeine.cache.simulator.policy.AccessEvent;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyActor;
import com.github.benmanes.caffeine.cache.simulator.policy.PolicyStats;
import com.github.benmanes.caffeine.cache.simulator.policy.Registry;
import com.github.benmanes.caffeine.cache.simulator.report.Reporter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;
import com.typesafe.config.Config;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;


/**
 * A simulator that broadcasts the recorded cache events to each policy and generates an aggregated
 * report. See <tt>reference.conf</tt> for details on the configuration.
 * <p>
 * The simulator reports the hit rate of each of the policy being evaluated. A miss may occur
 * due to,
 * <ul>
 *   <li>Conflict: multiple entries are mapped to the same location
 *   <li>Compulsory: the first reference misses and the entry must be loaded
 *   <li>Capacity: the cache is not large enough to contain the needed entries
 *   <li>Coherence: an invalidation is issued by another process in the system
 * </ul>
 * <p>
 * It is recommended that multiple access traces are used during evaluation to see how the policies
 * handle different workload patterns. When choosing a policy some metrics that are not reported
 * may be relevant, such as the cost of maintaining the policy's internal structures.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
public final class Simulator extends AbstractActor {
  public enum Message { START, FINISH, ERROR }

  private final TraceReader traceReader;
  private final BasicSettings settings;
  private final Stopwatch stopwatch;
  private final Reporter reporter;
  private final Router router;
  private final int batchSize;
  private int remaining; // Will hold the # of currently running policies
  
  private static int currIteration;   //$$ 
	private static int finalIteration; //$$ 
	private final  static int maxNumOfIterations = 500;

  public Simulator() {
    Config config = context().system().settings().config().getConfig("caffeine.simulator");
    settings = new BasicSettings(config);
    traceReader = makeTraceReader();

    List<Routee> routes = makeRoutes(); // Assign to routes the list of policies to run 
    router = new Router(new BroadcastRoutingLogic(), routes);
    remaining = routes.size(); // # of policies to run

    batchSize = settings.batchSize();
    stopwatch = Stopwatch.createStarted();
    reporter = settings.report().format().create(config);
  }

  @Override
  public void preStart() {
    self().tell(START, self());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .matchEquals(START, msg -> broadcast())
        .matchEquals(ERROR, msg -> context().stop(self()))
        .match(PolicyStats.class, this::reportStats)
        .build();
  }

  /** Broadcast the trace events to all of the policy actors. */
  private void broadcast() {
    if (remaining == 0) {
      context().system().log().error("No active policies in the current configuration");
      context().stop(self());
      return;
    }

    try (Stream<AccessEvent> events = traceReader.events()) {
      Iterators.partition(events.iterator(), batchSize)
          .forEachRemaining(batch -> router.route(batch, self()));
      router.route(FINISH, self());
    } catch (Exception e) {
      context().system().log().error(e, "");
      context().stop(self());
    }
  }

  /** Returns a trace reader for the access events. */
  private TraceReader makeTraceReader() {
    if (settings.isSynthetic()) {
      return Synthetic.generate(settings);
    }
    List<String> filePaths = settings.traceFiles().paths();
    TraceFormat format = settings.traceFiles().format();
    return format.readFiles(filePaths);
  }

  /** Returns the actors to broadcast trace events to. */
  private List<Routee> makeRoutes() {
    return Registry.policies(settings, traceReader.characteristics()).stream().map(policy -> {
      ActorRef actorRef = context().actorOf(Props.create(PolicyActor.class, policy));
      context().watch(actorRef);
      return new ActorRefRoutee(actorRef);  
    }).collect(toList());
  }

  /** This func is called when a simulation is finished. */
  private void reportStats(PolicyStats stats) throws IOException {
    reporter.add(stats); // Add the stats of the policy which finished to the reporter's data base.
    if (--remaining == 0) { // All policies finished sim
      context().stop(self());
      if (++currIteration < finalIteration ) {//$$ Running static conf': run more iteration, if needed
      	MyConfig.setIteration(currIteration);
      	akka.Main.main(new String[] { Simulator.class.getName() } ); //$$
      }
    }
  }

  // The main function is usually called by SimRunner.java
  // If called with 0 paramters ("args" is null), all parameters are read from the configuration file, "application.conf".
  // Else, some parameters are defined by args. This allows running multiple prallel sims, while using parameters that change from run to run.
  public static void main(String[] args) {
  	int runMode = MyConfig.GetIntParameterFromConfFile("run-mode");
  	if (runMode <= 0) {
  	   ResAnalyser resAnalyser = new ResAnalyser(); 
  	   resAnalyser.runAll ();
   	   return;
  	}
  	if (args.length > 0) { // Allow starting from iteration > 0, or with multiple conf' files. Used for multiple parallel simulations.
  		currIteration = (runMode > 0)? Integer.parseInt(args[0]) : 0; //Running alg' always begins at iteration 0. 
   	  finalIteration = currIteration + Integer.parseInt(args[1]);  
  		if (currIteration < 0 || finalIteration < 0 || currIteration > maxNumOfIterations || finalIteration > maxNumOfIterations) {
  			System.out.printf("Wrong iteration parameters in call to simulator.main: currIteration = %d, num of iterations = %d\n", args[0], args[1]);
  			System.exit(0);
  		}
  		MyConfig.setInitialIteration	(currIteration);
  		MyConfig.setIteration					(currIteration);
    	MyConfig.setPolicies 					(args[2]);
    	System.out.printf("Running %d iterations, runMode=%d\n", finalIteration - currIteration, runMode);
  	}
  	else {
  		finalIteration = maxNumOfIterations;
  	}
  	currIteration 	= MyConfig.getIteration();
   	akka.Main.main(new String[] { Simulator.class.getName() } );
    }
}