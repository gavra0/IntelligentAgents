package template;

import java.util.ArrayList;
import java.util.List;

import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/**
 * Centralized agent implementation SLS & CSP
 */
public class CentralizedImpl implements CentralizedBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {
		
		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		DomainVal.init(vehicles, tasks);
		
		A solution = SLS.solve();
		
		List<Plan> plans = new ArrayList<Plan>();
		
		for(int i = 0; i < vehicles.size(); i++){
			plans.add(solution.getPlanForVehicle(i));
		}

		return plans;
	}
}
