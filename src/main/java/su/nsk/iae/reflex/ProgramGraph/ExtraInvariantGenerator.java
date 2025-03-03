package su.nsk.iae.reflex.ProgramGraph;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProgramMetaData;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributeCollector;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.ProcessAttributes;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtraInvariantGenerator {
    ProgramMetaData metaData;
    AttributeCollector collector;
    IStatementCreator creator;
    public ExtraInvariantGenerator(ProgramMetaData metaData, AttributeCollector collector, IStatementCreator creator){
        this.metaData = metaData;
        this.collector = collector;
        this.creator = creator;
    }

    String createProcessInvariant(String processName){
        List<String> states = metaData.getProcessStates(processName);
        ProcessAttributes processAttributes = collector.getProcessAttributeByName(processName);
        if(processAttributes.isReachS() || processAttributes.isStartS())
            states.add("stop");
        if(processAttributes.isReachE())
            states.add("error");

        String stateInSet =
                creator.PartOfSet(
                    creator.PstateGetter("s1",processName),
                    states);

        String condition =
                creator.Implication(
                        creator.Conjunction(List.of(
                                creator.ToEnvP("s1"),
                                creator.Substate("s1","s")
                        )),
                        stateInSet
                );
        String inv = creator.ForAll(List.of("s1"),condition);
        return inv;
    }
    Map.Entry<String,String> createProcessesInvariant(){
        List<String> procInvariants = metaData.processNames().stream().map(this::createProcessInvariant).toList();
        String invariantsConj = creator.Conjunction(procInvariants);
        String name = "ProcessesStates";
        String definition = creator.Definition(name,List.of("s"),invariantsConj);
        return new AbstractMap.SimpleImmutableEntry<>(name,definition);
    }
    ArrayList<String> createPossibleStatesInvariant(){
        return null;
    }

    String createStateInvariant(String stateName, String processName){
        return null;
    }

}
