package commission;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionImpl implements AuctionBehavior {
    final int MAX_TIME = 29000; // max time in ms for bidding

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;

	private double myCurrentCost = 0;
    private double opponentCurrentCost = 0;
	private A myNewSolution;
    private A opponentNewSolution;
	private double newCost;

    SLS opponentSolution;
    SLS mySolution;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		
		// seed generation (based on ourAgent)
		long seed = -9019554669489983951L * agent.vehicles().get(0).homeCity().hashCode() * agent.id();
		this.random = new Random(seed);

        mySolution = new SLS();
        mySolution.domainVal = new DomainVal();
        //opponentSolution = new SLS();
        //opponentSolution.domainVal = new DomainVal();

        // init our domain val
		mySolution.domainVal.initVehiclesAndActionsList(agent.vehicles());
        mySolution.domainVal.clearTasks();

        // do the same for the opponent, suppose the parameters are the same
        //opponentSolution.domainVal.initMyVehiclesAndActionsList(agent.vehicles());
        //opponentSolution.domainVal.clearTasks();
    }

    int i =0;

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
        System.out.println("AGENT " + agent.id() + ": result for round " + i++ + " is " + (winner == agent.id()));
		if (winner == agent.id()) {
			myCurrentCost = myNewSolution.cost();
            // update solution to start from in SLS
            mySolution.startFrom = myNewSolution;

            //opponentSolution.domainVal.removeTask(previous);
            //if (opponentSolution.startFrom != null) opponentSolution.startFrom.removeTask(previous);
		} else {
            //opponentCurrentCost = opponentNewSolution.cost();
            //opponentSolution.startFrom = opponentNewSolution;
			mySolution.domainVal.removeTask(previous);
            if (mySolution.startFrom != null) mySolution.startFrom.removeTask(previous);
		}
	}
	
	@Override
	public Long askPrice(Task task) {
        // add to my solution
		mySolution.domainVal.addTask(task);
        if (mySolution.startFrom != null) mySolution.startFrom.appendTaskToBiggestVehicle(task);
        // same for the opponent
        //opponentSolution.domainVal.addTask(task);
        //if (opponentSolution.startFrom != null) opponentSolution.startFrom.appendTaskToBiggestVehicle(task);
        // solve now both
        myNewSolution = mySolution.solve(MAX_TIME);
        //opponentNewSolution = opponentSolution.solve();


		double myMarginalCost = myNewSolution.cost() - myCurrentCost;
        //double opponentMarginalCost = opponentNewSolution.cost() - opponentCurrentCost;

		double ratio = 1.05;
		double bid = ratio * myMarginalCost;
        System.out.println("AGENT " + agent.id() + ": for round " + i + " cost is "
                + myMarginalCost);
		return (long) Math.ceil(Math.abs(bid));
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

        A finalSolution = null;
        List<Plan> plans = new ArrayList<Plan>();
		if(tasks != null && !tasks.isEmpty()){
			mySolution.domainVal.clearTasks();
			mySolution.domainVal.initTasks(tasks);
            mySolution.startFrom = null;
            finalSolution = mySolution.solve(MAX_TIME);
		}

        if(finalSolution != null){
            for(int i = 0; i < vehicles.size(); i++){
                plans.add(finalSolution.getPlanForVehicle(i));
            }
        } else {
            for(int i = 0; i < vehicles.size(); i++){
                plans.add(Plan.EMPTY);
            }
        }

        return plans;
	}
}
