package template;

import logist.topology.Topology.City;

/**
 * If action type is move, destination specifies where to move
 */
public class AgentAction {
    private City destination;
    private ActionType actionType;

    public enum ActionType{
        PICKUP,
        MOVE
    }

    public AgentAction(City destination, ActionType actionType) {
        this.destination = destination;
        this.actionType = actionType;
    }

    public City getDestination() {
        return destination;
    }

    public void setDestination(City destination) {
        this.destination = destination;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentAction that = (AgentAction) o;

        if (actionType != that.actionType) return false;
        if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = destination != null ? destination.hashCode() : 0;
        result = 31 * result + (actionType != null ? actionType.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "[" + actionType + ": " + destination + "]";
    }
}