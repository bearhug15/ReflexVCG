package su.nsk.iae.reflex.staticAnalysis;

import su.nsk.iae.reflex.staticAnalysis.attributes.IAttributed;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class AttributedPath {
    ArrayDeque<IAttributed> path = new ArrayDeque<>();

    public void add(IAttributed obj){
        path.addLast(obj);
    }

    public void trimBy(IAttributed obj){
        if (obj!=null){
            List<IAttributed> buff= new ArrayList<>(this.path);
            buff = buff.subList(0,buff.indexOf(obj)+1);
            this.path = new ArrayDeque<>(buff);
        }else{
            this.path.clear();
        }
    }

    public ArrayDeque<IAttributed> getPath(){
        return path;
    }

    public boolean isEmpty() {
        return path.isEmpty();
    }

    public IAttributed peekLast() {
        return path.peekLast();
    }


}
