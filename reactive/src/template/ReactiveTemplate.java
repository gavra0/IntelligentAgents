package template;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReactiveTemplate implements ReactiveBehavior {
    private Map<AgentState, AgentAction> VTable;
    private Map<TupleSA, Double> QTable;

    private void reinforceLearning(List<City> cities, double discountFactor) {
        VTable = new LinkedHashMap<AgentState, AgentAction>();
        QTable = new LinkedHashMap<TupleSA, Double>();
        List<AgentAction> actions = AgentCalculator.allActions(cities);
        List<AgentState> states = AgentCalculator.allStates(cities);

        double delta = 1;
        discountFactor = .85;
        int k = 0;
        while (delta > 0.000000001) {
            k++;
            for (AgentState startState : states) {
                double currentQValue;
                for (AgentAction action : actions) {
                    if (AgentCalculator.checkIfPossible(startState, action)) {
                        // get R(S,a)
                        TupleSA stateAction = new TupleSA(startState, action);
                        currentQValue = AgentCalculator.rewards.get(stateAction);

                        // sum(disc * T(S, a, S') * Max(S')
                        for (AgentState endState : states) {
                            TupleSAS probTKey = new TupleSAS(startState, action, endState);

                            Double probValue = AgentCalculator.transitionProbabilities.get(probTKey);

                            if (probValue != null) {
                                AgentAction newStateBestAction = VTable.get(endState);
                                if (newStateBestAction != null) {
                                    Double newStateQValue = QTable.get(new TupleSA(endState, newStateBestAction));
                                    currentQValue += discountFactor * probValue * newStateQValue;
                                }
                            }
                        }

                        // check if new max
                        AgentAction currentBestAction = VTable.get(startState);
                        TupleSA currentBestSA = new TupleSA(startState, currentBestAction);
                        if (null == QTable.get(currentBestSA)) {
                            QTable.put(new TupleSA(startState, action), currentQValue);
                            VTable.put(startState, action);
                        } else {
                            Double prevVal = QTable.get(currentBestSA);
                            if (prevVal < currentQValue) {
                                if (currentQValue != 0) {
                                    delta = (currentQValue - prevVal) / prevVal;
                                }

                                QTable.remove(currentBestSA);
                                VTable.remove(startState);

                                QTable.put(new TupleSA(startState, action), currentQValue);
                                VTable.put(startState, action);
                            }
                        }
                    }
                }
            }
        }

        for (AgentState sa : states) {
            for (AgentAction action : actions) {
                TupleSA stateAction = new TupleSA(sa, action);
                if (QTable.get(stateAction) == null) continue;
//                System.out.println(sa + ": " + QTable.get(stateAction));
                System.out.println(sa + ": " + AgentCalculator.rewards.get(stateAction));
            }
        }
        System.out.println("In " + k + " iterations.");
    }

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        AgentCalculator.init(td, agent.readProperty("cost-per-km", Integer.class, 5), topology);
        reinforceLearning(topology.cities(), agent.readProperty("discount-factor", Double.class,
                0.85));
    }

    private Action bestAction(City current, Task task) {
        AgentState currState = new AgentState(null, null);
        currState.setCurrent(current);
        if (null != task) {
            currState.setDestination(task.deliveryCity);
        }

        AgentAction agentAction = VTable.get(currState);
        if (agentAction.getActionType() == AgentAction.ActionType.MOVE) {
            return new Move(agentAction.getDestination());
        } else {
            return new Pickup(task);
        }
    }

    @Override
    public Action act(Vehicle vehicle, Task availableTask) {
        Action action = bestAction(vehicle.getCurrentCity(), availableTask);

        System.out.println(action.toString());

        return action;
    }
}