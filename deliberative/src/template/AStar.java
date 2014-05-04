package template;

import logist.task.Task;
import logist.topology.Topology;

import java.util.*;

/**
 * A* search algorithm implementation with two separate heuristics
 */
public class AStar {

    /**
     * A star search algorithm implementation
     */
    public static State search(State root) {
        List<State> queue = new ArrayList<State>();
        Map<Integer, State> costMap = new HashMap<Integer, State>();
        queue.add(root);
        while (!queue.isEmpty()) {
            State first = queue.remove(0);
            if (first.isFinalState()) return first;
            int hash = first.hashCode();
            if (!costMap.containsKey(hash) || first.getCost() < costMap.get(hash).getCost()) {
                costMap.put(hash, first);
                List<State> successors = first.getChildren();
                queue = mergeByHeuristic(queue, successors);
            }
        }
        return null; // failure
    }

    static List<State> mergeByHeuristic(List<State> states1, List<State> states2) {
        states1.addAll(states2);
        Collections.sort(states1, new Comparator<State>() {
            public int compare(State s1, State s2) {
                double v1 = s1.getCost() + heuristic(s1);
                double v2 = s2.getCost() + heuristic(s2);
                if (v1 > v2) return 1;
                else if (v1 < v2) return -1;
                else return 0;
            }
        });
        return states1;
    }

    static Map<State, Double> heuristicTable = new HashMap<State, Double>();

    static double heuristic(State s){
        if (heuristicTable.containsKey(s)){
            return heuristicTable.get(s);
        }
        Double val = hMST(s);
        heuristicTable.put(s, val);
        return val;
    }

    /**
     * Calculate MST-similar cost of traveling between points of
     * interest
     * @param s current state
     * @return the cost visiting all cities
     */
    static double hMST(State s) {double ret = 0;
        Topology.City currentCity = s.currentCity;
        Set<Topology.City> MST = new HashSet<Topology.City>();
        MST.add(currentCity);

        Set<Topology.City> cities = new HashSet<Topology.City>();
        for (Task t : s.inProgress) {
            cities.add(t.deliveryCity);
        }
        for (Task t : s.toPickUp) {
            cities.add(t.pickupCity);
            cities.add(t.deliveryCity);
        }
        cities.remove(currentCity);

        while (!cities.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            Topology.City closestOutside = null;
            for (Topology.City m : MST) {
                for (Topology.City c : cities) {
                    double distance = m.distanceTo(c);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestOutside = c;
                    }
                }
            }
            cities.remove(closestOutside);
            MST.add(closestOutside);
            ret += minDistance;
        }
        return ret;
    }

    /**
     * Calculate the biggest distance from current city and
     * cities that present points of interest.
     * @param s current state
     * @return max distance to point of interest
     */
    static double hFurthestTask(State s){
        Topology.City currentCity = s.currentCity;

        double maxDistance = Double.MIN_VALUE;
        for(Task t : s.toPickUp){
            double currentDistance = currentCity.distanceTo(t.deliveryCity);
            if(currentDistance>maxDistance) maxDistance = currentDistance;
            currentDistance = currentCity.distanceTo(t.pickupCity);
            if(currentDistance>maxDistance) maxDistance = currentDistance;
        }
        for(Task t : s.inProgress){
            double currentDistance = currentCity.distanceTo(t.deliveryCity);
            if(currentDistance>maxDistance) maxDistance = currentDistance;
        }
        return maxDistance;
    }
}
