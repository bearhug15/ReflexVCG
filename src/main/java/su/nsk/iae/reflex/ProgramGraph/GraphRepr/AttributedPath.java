package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.IAttributed;

import java.util.ArrayDeque;
import java.util.Iterator;

public class AttributedPath extends ArrayDeque<IAttributed> {
    ArrayDeque<IAttributed> path = new ArrayDeque<>();


    public void trimBy(IAttributed obj){
        if (obj!=null){
            Iterator<IAttributed> iter = this.descendingIterator();
            while(iter.hasNext() && iter.next().equals(obj)){
                this.removeLast();
            }
            /*List<IAttributed> buff= new ArrayList<>(this);
            buff = buff.subList(0,buff.indexOf(obj)+1);
            this.
            this.path = new ArrayDeque<>(buff);*/
        }else{
            this.path.clear();
        }
    }

    /*public ArrayDeque<IAttributed> getPath(){
        return path;
    }*/

    /*public boolean isEmpty() {
        return path.isEmpty();
    }

    public IAttributed peekLast() {
        if (path.isEmpty()){return null;}
        return path.peekLast();
    }

    public IAttributed removeLast(){
        if (path.isEmpty()){return null;}
        return path.removeLast();
    }*/

}
