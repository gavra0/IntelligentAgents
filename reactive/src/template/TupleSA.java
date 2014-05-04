package template;

public class TupleSA {
	public final AgentState state;
	public final AgentAction action;
	public TupleSA(AgentState state, AgentAction action){
		this.state = state;
		this.action = action;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TupleSA sa = (TupleSA) o;

        if (!state.equals(sa.state)) return false;
        if (!action.equals(sa.action)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = state != null ? state.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}
