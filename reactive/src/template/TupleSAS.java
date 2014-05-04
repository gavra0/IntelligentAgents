package template;

public class TupleSAS {
	public final AgentState state;
	public final AgentAction action;
	public final AgentState newState;
	public TupleSAS(AgentState state, AgentAction action, AgentState newState){
		this.state = state;
		this.action = action;
		this.newState = newState;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TupleSAS sas = (TupleSAS) o;

        if (!state.equals(sas.state)) return false;
        if (!action.equals(sas.action)) return false;
        if (!newState.equals(sas.newState)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = state != null ? state.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (newState != null ? newState.hashCode() : 0);
        return result;
    }
}
