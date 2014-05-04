package template;

import java.util.ArrayList;
import java.util.List;

import template.Action.Type;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class DomainVal {
	public static List<Vehicle> vehicles;
	public static List<Action> actions;
	
	public static void init(List<Vehicle> allVehicles, TaskSet tasks) {
		DomainVal.vehicles = allVehicles;
		
		actions = new ArrayList<Action>(2*tasks.size());
		for(Task t : tasks){
			actions.add(new Action(Type.PICKUP, t));
			actions.add(new Action(Type.DELIVERY, t));
		}
	}
}
