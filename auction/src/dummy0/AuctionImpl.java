package dummy0;

//the list of imports

import logist.agent.Agent;
import logist.behavior.AuctionBehavior;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 */
@SuppressWarnings("unused")
public class AuctionImpl implements AuctionBehavior {
    static int MAX_TIME = 28000; // max time in ms for bidding

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private Random random;

    private double myCurrentCost = 0;
    private A myNewSolution;
    private A[] opponentNewSolution = {null, null, null};
    private double newCost;

    SLS[] opponentSolution = {new SLS(), new SLS(), new SLS()};
    SLS mySolution;

    List<Long> opponentBids;
    List<Double> estimationRatio;

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
        // init our domain val
        mySolution.domainVal.initMyVehiclesAndActionsList(agent.vehicles());
        mySolution.domainVal.clearTasks();

        opponentBids = new ArrayList<Long>();
        estimationRatio = new ArrayList<Double>();


        // do the same for the opponent, suppose the parameters are the same
        for (int i = 0; i < 3; i++) {
            opponentSolution[i] = new SLS();
            opponentSolution[i].domainVal = new DomainVal();
            opponentSolution[i].domainVal.initShuffleMyVehiclesAndActionsList(agent.vehicles());
            opponentSolution[i].domainVal.clearTasks();
        }
    }

    void addOpponentBid(long bid) {
        opponentBids.add(bid);
    }

    long totalAcceptedOpponentBids() {
        long sum = 0;
        for (Long bid : opponentBids) {
            sum += bid;
        }
        return sum;
    }

    int roundNum = 0;
    int tasksWon = 0;
    double myTotalAcceptedBids = 0;

    @Override
    public void auctionResult(Task previous, int winner, Long[] bids) {

    }

    @Override
    public Long askPrice(Task task) {
        return 0L;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

        A finalSolution = null;
        List<Plan> plans = new ArrayList<Plan>();
        if (tasks != null && !tasks.isEmpty()) {
            mySolution.domainVal.clearTasks();
            mySolution.domainVal.initTasks(tasks);
            mySolution.startFrom = null;
            finalSolution = mySolution.solve(MAX_TIME);
        }

        if (finalSolution != null) {
            for (int i = 0; i < vehicles.size(); i++) {
                plans.add(finalSolution.getPlanForMyVehicle(i));
            }
        } else {
            for (int i = 0; i < vehicles.size(); i++) {
                plans.add(Plan.EMPTY);
            }
        }

        return plans;
    }
}
