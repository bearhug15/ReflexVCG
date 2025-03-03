package su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes;

public class PotChange {
    boolean start=false;
    boolean stop=false;
    boolean error=false;

    public PotChange(boolean start,boolean stop,boolean error){
        this.start = start;
        this.stop = stop;
        this.error = error;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public PotChange add(PotChange other){
        start = start || other.start;
        stop = stop || other.stop;
        error = error || other.error;
        return this;
    }
}
