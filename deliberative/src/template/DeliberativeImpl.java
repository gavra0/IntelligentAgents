package template;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.lang.AssertionError;import java.lang.Override;import java.lang.String;import java.lang.System;import java.util.*;

/**
 * Deliberative agent implementation
 */
public class DeliberativeImpl implements DeliberativeBehavior {
    enum Algorithm {BFS, ASTAR}

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;

    /* the planning class */
    Algorithm algorithm;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
    }

    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        Plan plan;
        State root = new State(vehicle, tasks);
        // if our plan got interrupted by some other agent, add the tasks to the state
        if (vehicle.getCurrentTasks() != null){
            root.setInProgress(vehicle.getCurrentTasks());
        }

        // Compute the plan with the selected algorithm.
        switch (algorithm) {
            case ASTAR:
                System.out.println("Algorithm = A*");
                long start1 = System.currentTimeMillis();
                State AStarEndState = AStar.search(root);
                long end1 = System.currentTimeMillis();
                System.out.println("Calculation time: " + (end1 - start1) + "ms" + " cost is " + AStarEndState.getCost());
                plan = AStarEndState.plan;
                break;
            case BFS:
                System.out.println("Algorithm = BFS");
                long start = System.currentTimeMillis();
                State BFSEndState = BFS.search(root);
                long end = System.currentTimeMillis();
                System.out.println("Calculation time: " + (end - start) + "ms");
                plan = BFSEndState.plan;
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        return plan;
    }

    @Override
    public void planCancelled(TaskSet carriedTasks) {

        if (!carriedTasks.isEmpty()) {
            // This cannot happen for this simple agent, but typically
            // you will need to consider the carriedTasks when the next
            // plan is computed.
        }
    }
}
