package template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Breadth first search implementation
 */
public class BFS {
    /**
     * Returns first solution. Can change to best one.
     */
    public static State search(State root) {
        List<State> queue = new LinkedList<State>();
        Map<State, Double> costMap = new HashMap<State, Double>();
        queue.add(root);
        costMap.put(root, root.getCost());
        while (!queue.isEmpty()) {
            State first = queue.remove(0);
            if (first.isFinalState()) {
                return first;
            }
            Double prevCost = costMap.get(first);
            if (prevCost == null || (prevCost != null && first.getCost() <= prevCost)) {
                costMap.put(first, first.getCost());
                List<State> children = first.getChildren();
                queue.addAll(children);
            }
        }
        return null; // error
    }
}
