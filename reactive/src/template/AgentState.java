package template;

import logist.topology.Topology.City;


public class AgentState {
    // if there is a task in the current city for the destination city
    private City destination;
    // current city
    private City current;

    public AgentState( City current, City destination) {
        this.destination = destination;
        this.current = current;
    }

    public boolean isFree(){
        return destination == null;
    }


    /** GETTERS SETTERS **/
    public City getDestination() {
        return destination;
    }

    public void setDestination(City destination) {
        this.destination = destination;
    }

    public City getCurrent() {
        return current;
    }

    public void setCurrent(City current) {
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentState state = (AgentState) o;

        if (!current.equals(state.current)) return false;
        if (destination != null ? !destination.equals(state.destination) : state.destination != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = destination != null ? destination.hashCode() : 0;
        result = 31 * result + current.hashCode();
        return result;
    }

    public String toString() {
        return "(" + current + " - " + destination + ")";
    }

}
