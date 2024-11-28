package su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes;

public enum ChangeType {
    Start,Stop,Error;

    @Override
    public String toString(){
         switch (this){
            case Start -> {
                return "Start";
            }
            case Stop -> {
                return "Stop";
            }
            case Error -> {
                return "Error";
            }
        }
        return "";
    }
}
