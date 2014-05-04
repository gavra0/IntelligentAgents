package commission;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import commission.Action.Type;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class DomainVal {
	public List<Vehicle> vehicles;
	public List<Action> actions;
	
	public void initVehiclesAndActionsList(List<Vehicle> allVehicles) {
		this.vehicles = allVehicles;
		this.actions = new ArrayList<Action>(60);
	}
	
	public void addTask(Task t){
		actions.add(new Action(Type.PICKUP, t));
		actions.add(new Action(Type.DELIVERY, t));
	}
	
	public void removeTask(Task t){
        for (Iterator<Action> it = actions.iterator(); it.hasNext(); ){
            if (it.next().task.id == t.id){
                it.remove();
            }
        }
	}
	
	public void clearTasks(){
		actions.clear();
	}
	
	public void initTasks(TaskSet ts){
		for(Task t : ts){
			actions.add(new Action(Type.PICKUP, t));
			actions.add(new Action(Type.DELIVERY, t));
		}
	}
}
