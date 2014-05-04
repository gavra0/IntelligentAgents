package commission;

import java.util.*;

import logist.task.Task;
import commission.Action.Type;
import logist.Measures;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.topology.Topology.City;

public class A {
    Map<Vehicle, List<Action>> vehicleActions;
    DomainVal domainVal;
	
	// Empty default constructor
	public A(DomainVal domainVal){
        this.domainVal = domainVal;
        vehicleActions = new HashMap<Vehicle, List<Action>>(domainVal.vehicles.size());
        for (Vehicle v:domainVal.vehicles){
            List<Action> actionList = new ArrayList<Action>();
            vehicleActions.put(v, actionList);
        }
	}
	
	// Copy constructor
	public A(A a){
        // use the sam domain
        this.domainVal = a.domainVal;

        vehicleActions = new HashMap<Vehicle, List<Action>>(domainVal.vehicles.size());
        for (Vehicle v:domainVal.vehicles){
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

    public boolean appendTaskToBiggestVehicle(Task t){
        Vehicle biggestVehicle = domainVal.vehicles.get(0);
        for (Vehicle v:domainVal.vehicles){
            if (biggestVehicle.capacity() < v.capacity()){
                biggestVehicle = v;
            }
        }
        if (biggestVehicle.capacity() < t.weight){
            return false;
        }
        else{
            appendActionForVehicle(new Action(Type.PICKUP, t), biggestVehicle);
            appendActionForVehicle(new Action(Type.DELIVERY, t), biggestVehicle);
            return false;
        }
    }

    public void removeTask(Task t){
        for (Vehicle v: vehicleActions.keySet()){
            removeActionForVehicle(new Action(Type.PICKUP, t), v);
            removeActionForVehicle(new Action(Type.DELIVERY, t), v);
        }
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
		double totalCost = 0;
		for(Vehicle v : vehicleActions.keySet()){
            City c = v.homeCity();
            long vehicleDistanceSum = 0;
			for (Action action : vehicleActions.get(v)){
				vehicleDistanceSum += c.distanceUnitsTo(action.getCity());
                c = action.getCity();
            }
			double vehicleCost = Measures.unitsToKM(vehicleDistanceSum * v.costPerKm());
			totalCost += vehicleCost;
		}
		
		return totalCost;
	}
	
	public Plan getPlanForVehicle(int index){
		Vehicle v = domainVal.vehicles.get(index);
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
