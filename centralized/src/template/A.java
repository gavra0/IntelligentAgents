package template;

import java.util.*;

import template.Action.Type;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.topology.Topology.City;

public class A {
    Map<Vehicle, List<Action>> vehicleActions;
	
	// Empty default constructor
	public A(){
        vehicleActions = new HashMap<Vehicle, List<Action>>(DomainVal.vehicles.size());
        for (Vehicle v:DomainVal.vehicles){
            List<Action> actionList = new ArrayList<Action>();
            vehicleActions.put(v, actionList);
        }
	}
	
	// Copy constructor
	public A(A a){
        vehicleActions = new HashMap<Vehicle, List<Action>>(DomainVal.vehicles.size());
        for (Vehicle v:DomainVal.vehicles){
            List<Action> actionList = new ArrayList<Action>();
            actionList.addAll(a.vehicleActions.get(v));
            vehicleActions.put(v, actionList);
        }
	}

    public void appendActionForVehicle(Action a, Vehicle v){
        List<Action> actionList = vehicleActions.get(v);
        actionList.add(a);
        vehicleActions.put(v, actionList);
    }
    public void replaceActionForVehicle(Action a, Vehicle v, int index){
        List<Action> actionList = vehicleActions.get(v);
        actionList.set(index, a);
        vehicleActions.put(v, actionList);
    }

    public void removeActionForVehicle(Action a, Vehicle v){
        List<Action> actionList = vehicleActions.get(v);
        actionList.remove(a);
        vehicleActions.put(v, actionList);
    }
	
	public double cost(){
		double cost = 0;
		for(Vehicle v : vehicleActions.keySet()){
            City c = v.homeCity();
			for (Action action : vehicleActions.get(v)){
                cost += c.distanceTo(action.getCity()) * v.costPerKm();
                c = action.getCity();
            }
		}
		return cost;
	}
	
	public Plan getPlanForVehicle(int index){
		Vehicle v = DomainVal.vehicles.get(index);
		City currentCity = v.homeCity();
		Plan plan = new Plan(currentCity);
		
		for(Action a: vehicleActions.get(v)){
			City destination = a.getCity();
			
			for(City c : currentCity.pathTo(destination)){
				plan.appendMove(c);
			}
			
			if(a.type == Type.PICKUP) plan.appendPickup(a.task);
			else plan.appendDelivery(a.task);
			
			currentCity = destination;
		}
		
		return plan;
	}
}
