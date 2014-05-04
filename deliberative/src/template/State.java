package template;

import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.lang.Object;import java.lang.Override;import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * State representation
 */
public class State {
    // current city where vehicle is
    City currentCity;

    // accepted but not yet delivered tasks
    TaskSet inProgress;
    // list of tasks waiting to be delivered
    TaskSet toPickUp;
    // actions so far to be in this state
    Plan plan;
    // vehicle
    Vehicle vehicle;
    // the parent state
    State parent = null;


    public State(Vehicle vehicle, TaskSet toPickUp) {
        this.currentCity = vehicle.getCurrentCity();
        this.vehicle = vehicle;
        // create task sets
        this.toPickUp = TaskSet.copyOf(toPickUp);
        this.inProgress = TaskSet.copyOf(toPickUp);
        inProgress.clear();
        plan = new Plan(currentCity, new ArrayList<Action>());
    }

    private State(Vehicle vehicle, TaskSet inProgress, TaskSet toPickUp, Plan plan) {
        this.currentCity = vehicle.getCurrentCity();
        this.inProgress = inProgress;
        this.toPickUp = toPickUp;
        this.plan = plan;
        this.vehicle = vehicle;
    }

    /**
     * Make the copy of this state
     *
     * @return
     */
    private State duplicateThis() {
        Plan newPlan = new Plan(vehicle.homeCity());
        for (Action a : plan) {
            newPlan.append(a);
        }
        State newState = new State(vehicle, TaskSet.copyOf(inProgress), TaskSet.copyOf(toPickUp), newPlan);
        newState.parent = this;
        return newState;
    }

    /**
     * Check if all tasks are delivered
     *
     * @return
     */
    public boolean isFinalState() {
        tryDelivery();
        return toPickUp.isEmpty() && inProgress.isEmpty();
    }

    /**
     * Check if we have a package for the current city and
     * if so deliver it
     */
    public void tryDelivery() {
        for (Task t : inProgress) {
            if (t.deliveryCity == currentCity) {
                plan.appendDelivery(t);
                inProgress.remove(t);
            }
        }
    }

    /**
     * Generate list of next states for this state.
     * States are generated based on points of interest which are
     * cities where we have something to deliver, or where we
     * can pick up task
     * @return
     */
    public List<State> getChildren() {
        List<State> nextStates = new LinkedList<State>();

        // one option is to pickup some task
        List<Task> tmpProgress = new LinkedList<Task>(inProgress);
        for (Task t : toPickUp) {
            boolean addThisState = true;
            State nextState = duplicateThis();
            for (City c : currentCity.pathTo(t.pickupCity)) {
                nextState.plan.appendMove(c);
                if (c != t.pickupCity && !pickupsInCity(c).isEmpty()) {
                    // do not add this as it will be reached from other state
                    addThisState = false;
                    break;
                }
                for (Task del : deliveriesForCity(c)) {
                    nextState.plan.appendDelivery(del);
                    nextState.inProgress.remove(del);
                    tmpProgress.remove(del);
                }
            }
            if (addThisState) {
                nextState.currentCity = t.pickupCity;
                nextState.plan.appendPickup(t);
                nextState.toPickUp.remove(t);
                nextState.inProgress.add(t);
                if (nextState.canPickUpTask(t) && noCycle(nextState) && !nextStates.contains(nextState)) {
                    nextStates.add(nextState);
                }
            }
        }
        // other option is to deliver some task
        for (Task t : tmpProgress) {
            boolean addThisState = true;
            State nextState = duplicateThis();
            for (City c : currentCity.pathTo(t.deliveryCity)) {
                nextState.plan.appendMove(c);
                if (c != t.deliveryCity && (!pickupsInCity(c).isEmpty() ||
                        !deliveriesForCity(c).isEmpty())) {
                    // do not add this as it will be reached from other state
                    addThisState = false;
                    break;
                }
            }
            if (addThisState) {
                nextState.currentCity = t.deliveryCity;
                nextState.plan.appendDelivery(t);
                nextState.inProgress.remove(t);
                if (noCycle(nextState) && !nextStates.contains(nextState)) {
                    nextStates.add(nextState);
                }
            }
        }
        return nextStates;
    }

    List<Task> deliveriesForCity(City c) {
        List<Task> deliveries = new LinkedList<Task>();
        for (Task t : inProgress) {
            if (t.deliveryCity == c) {
                deliveries.add(t);
            }
        }
        return deliveries;
    }

    List<Task> pickupsInCity(City c) {
        List<Task> pickups = new LinkedList<Task>();
        for (Task t : toPickUp) {
            if (t.pickupCity == c) {
                pickups.add(t);
            }
        }
        return pickups;
    }

    public boolean noCycle(State state) {
        State parentState = state.parent;
        while (parentState != null) {
            if (state.equals(parentState))
                return false;
            parentState = parentState.parent;
        }
        return true;
    }

    /**
     * Check if we have enough space to pick this task
     *
     * @param newTask
     * @return
     */
    public boolean canPickUpTask(Task newTask) {
        int currentWeight = 0;
        for (Task t : inProgress) {
            currentWeight += t.weight;
        }
        return currentWeight + newTask.weight <= vehicle.capacity();
    }

    /**
     * Compute the profit if we are in this state
     *
     * @return
     */
    public double getCost() {
        return plan.totalDistance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (!currentCity.equals(state.currentCity)) return false;
        if (toPickUp.size() != state.toPickUp.size() || inProgress.size() != state.inProgress.size()) return false;
        for (Task t : inProgress) {
            if (!state.inProgress.contains(t)) return false;
        }
        for (Task t : toPickUp) {
            if (!state.toPickUp.contains(t)) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = currentCity.hashCode();
        result = 31 * result + inProgress.hashCode();
        result = 31 * result + toPickUp.hashCode();
        return result;
    }

    /* GETTER AND SETTERS */

    public City getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(City currentCity) {
        this.currentCity = currentCity;
    }

    public TaskSet getInProgress() {
        return inProgress;
    }

    public void setInProgress(TaskSet inProgress) {
        this.inProgress = inProgress;
    }

    public TaskSet getToPickUp() {
        return toPickUp;
    }

    public void setToPickUp(TaskSet toPickUp) {
        this.toPickUp = toPickUp;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
