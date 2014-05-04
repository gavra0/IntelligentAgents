package template;

import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AgentCalculator {
    private static TaskDistribution taskDistribution;
    private static int costPerKm;

    public static Map<TupleSA, Double> rewards;
    public static Map<TupleSAS, Double> transitionProbabilities;

    public static void init(TaskDistribution td, int cpkm, Topology topology) {
        taskDistribution = td;
        costPerKm = cpkm;
        rewards = initRewards(topology);
        transitionProbabilities = initTransitionProbabilities(topology);
    }

    /**
     * If in state delivery to destination is accepted
     *
     * @param state
     * @param action
     * @return
     */
    public static double calculateProfit(AgentState state, AgentAction action) {
        // there is no task
        if (action.getActionType() == AgentAction.ActionType.MOVE) {
            // this will just cost some money
            return (double) (-1) * state.getCurrent().distanceTo(action.getDestination()) * costPerKm;
        }
        // if there is a task from current to destination
        return (double) taskDistribution.reward(state.getCurrent(), state.getDestination()) -
                (state.getCurrent().distanceTo(state.getDestination()) * costPerKm);
    }

    /**
     * Calculate R(s, a) values
     *
     * @param topology
     * @return
     */
    private static Map<TupleSA, Double> initRewards(Topology topology) {
        Map<TupleSA, Double> rewardsMap = new LinkedHashMap<TupleSA, Double>();

        List<City> cities = topology.cities();
        List<AgentState> states = allStates(cities);
        List<AgentAction> actions= allActions(cities);

        // iterate through all cities
        for (AgentState state: states){
            for(AgentAction action: actions){
                if (checkIfPossible(state, action)){
                    double rewardValue = calculateProfit(state, action);

                    TupleSA stateAction = new TupleSA(state, action);

                    rewardsMap.put(stateAction, rewardValue);
                }
            }
        }

        return rewardsMap;
    }

    /**
     * Calculate T(s, a, s') values
     *
     * @param topology
     * @return
     */
    private static Map<TupleSAS, Double> initTransitionProbabilities(Topology topology) {
        Map<TupleSAS, Double> transProbabilities = new LinkedHashMap<TupleSAS, Double>();

        List<City> cities = topology.cities();
        List<AgentAction> actions = allActions(cities);
        List<AgentState> states = allStates(cities);

        for (AgentState startState : states) {
            for (AgentAction action : actions) {
                if (checkIfPossible(startState, action)) {
                    for (AgentState endState : states) {
                        Double probValue = 0.0;
                        if (action.getActionType() == AgentAction.ActionType.PICKUP && startState.getDestination() == endState.getCurrent()) {
                            if (endState.isFree()) {
                                probValue = 1 - sumAllProbabilities(endState.getCurrent(), cities);
                            } else {
                                probValue = taskDistribution.probability(endState.getCurrent(), endState.getDestination());
                            }
                        } else if (action.getActionType() == AgentAction.ActionType.MOVE && action.getDestination() == endState.getCurrent()) {
                            if (endState.isFree()) {
                                probValue = 1 - sumAllProbabilities(endState.getCurrent(), cities);
                            } else {
                                probValue = taskDistribution.probability(endState.getCurrent(), endState.getDestination());
                            }
                        }
                        TupleSAS newSAS = new TupleSAS(startState, action, endState);
                        transProbabilities.put(newSAS, probValue);
                    }
                }
            }
        }

        return transProbabilities;
    }

    /**
     * Total probability that there is a task in city
     *
     * @param startCity
     * @param otherCities
     * @return
     */
    private static double sumAllProbabilities(City startCity, List<City> otherCities) {
        double sumProb = 0;
        for (City city : otherCities) {
            if (city != startCity) {
                sumProb += taskDistribution.probability(startCity, city);
            }
        }
        return sumProb;
    }

    public static List<AgentAction> allActions(List<City> cities) {
        List<AgentAction> actions = new LinkedList<AgentAction>();
        actions.add(new AgentAction(null, AgentAction.ActionType.PICKUP));
        for (City a : cities) {
            actions.add(new AgentAction(a, AgentAction.ActionType.MOVE));
        }
        return actions;
    }

    public static List<AgentState> allStates(List<City> cities) {
        List<AgentState> states = new LinkedList<AgentState>();
        for (City i : cities) {
            states.add(new AgentState(i, null));
            for (City j : cities) {
                if (i != j) {
                    states.add(new AgentState(i, j));
                }
            }
        }
        return states;
    }

    /**
     * It is not allowed to have move to the non neighbouring city. It is not allowed to have
     * destination city null and have pickup action
     *
     * @param state
     * @param action
     * @return
     */
    public static boolean checkIfPossible(AgentState state, AgentAction action) {
        return !(
                (state.isFree() && action.getActionType() == AgentAction.ActionType.PICKUP)
                        ||
                        (action.getActionType() == AgentAction.ActionType.MOVE && !state.getCurrent().hasNeighbor(action.getDestination()))
        );
    }
}