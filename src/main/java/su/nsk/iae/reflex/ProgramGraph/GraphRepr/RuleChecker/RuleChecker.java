package su.nsk.iae.reflex.ProgramGraph.GraphRepr.RuleChecker;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ChangeType;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.ProcessNode;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.attributes.*;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributeCollector;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.AttributedPath;


import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProgramMetaData;

import java.util.*;

/**
 * The incompatibility rules of section 4. The path holds, in order, the statements it has
 * executed (start, stop, error, restart, reset timer, set state), the process status checks it
 * has passed and the states it has dispatched, together with the fragments enclosing them.
 * Only the statements count as changes: a fragment's attributes also describe branches the path
 * did not take, and statements of the fragment still to come.
 *
 * Every rule comparing two points of the path asks that nothing between them changes the process
 * in a way that could reconcile them (the predicate unchanged): walking back from the current
 * point, it is the set of kinds of change already passed.
 */
public class RuleChecker implements IRuleChecker{
    ProgramMetaData metaData;
    AttributeCollector collector;
    public RuleChecker(ProgramMetaData metaData, AttributeCollector collector){
        this.metaData = metaData;
        this.collector = collector;
    }

    // Rule 4.2: the timer of the process was zeroed this cycle - in its current state (reset),
    // or by a process dispatched before it starting it - so its timeout cannot have elapsed.
    public boolean checkTimeout(AttributedPath path){
        Iterator<IAttributed> iter =path.descendingIterator();
        ProcessNode process = null;
        while (iter.hasNext()){
            IAttributed attr = iter.next();
            if (process == null) {
                if (Objects.requireNonNull(attr.getContextType()) == AttributedNodeType.Process) {
                    process = (ProcessNode) attr.getAttributedNode();
                } else if (attr.isReset()) {
                    return false;
                }
            } else if (attr.getContextType() == AttributedNodeType.Statement
                    && attr.getProcChange().get(process) == ChangeType.Start) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean checkRules(AttributedPath path, IAttributed attr){
        if(attr==null){
            return true;
        }
        switch (attr.getContextType()){
            case State: {
                return checkRulesStateAttributes(path,(StateAttributes) attr);
            }
            case TimeoutBranch:{
                TimeoutBranch branch = (TimeoutBranch)attr;
                if(branch.isVariable() || !branch.isExceed() || !branch.isPositive()){
                    return true;
                }else{
                    return checkTimeout(path);
                }
            }
            case ProcessStatus: return checkProcessStatusAttributes(path,(ProcessStatusAttributes)attr);
            default: return true;
        }
    }

    // Rules 4.3 and 4.4, for each process the check is about.
    private boolean checkProcessStatusAttributes(AttributedPath path,ProcessStatusAttributes attr){
        for (Map.Entry<String, ProcessStatus> fact : attr.getProcStatuses().entrySet()) {
            String process = fact.getKey();
            ProcessStatus status = fact.getValue();
            // The changes after which the current check can hold.
            Set<ChangeType> reconciling = changesAllowing(status);
            EnumSet<ChangeType> passed = EnumSet.noneOf(ChangeType.class);
            Iterator<IAttributed> iter = path.descendingIterator();
            while (iter.hasNext()) {
                IAttributed earlier = iter.next();
                boolean unchanged = Collections.disjoint(passed, reconciling);
                if (earlier.getContextType() == AttributedNodeType.Statement) {
                    ChangeType change = changeOf(earlier, process);
                    if (change == null) continue;
                    // 4.4: the change leaves a status the check excludes.
                    if (unchanged && !allowed(status).contains(statusAfter(change))) return false;
                    passed.add(change);
                } else if (earlier instanceof ProcessStatusAttributes) {
                    ProcessStatus earlierStatus = earlier.getProcStatuses().get(process);
                    // 4.3: two checks no status satisfies together.
                    if (earlierStatus != null && unchanged
                            && Collections.disjoint(allowed(earlierStatus), allowed(status))) return false;
                } else if (earlier instanceof StateAttributes state
                        && state.getRootProcess().getProcessName().equals(process)) {
                    // The process was dispatched earlier this cycle in a state the check excludes.
                    if (unchanged && !allowed(status).contains(statusOf(state.getStateName()))) return false;
                }
            }
        }
        return true;
    }

    private boolean checkRulesStateAttributes(AttributedPath path,StateAttributes attr){
        ProcessNode rootProcess = attr.getRootProcess();
        ProcessAttributes pattr = (ProcessAttributes) collector.getAttributes(rootProcess);
        // 4.1
        if(!pattr.isReachE() && attr.getStateName().equals("error"))return false;
        if(!pattr.isReachS() && !pattr.isStartS() && attr.getStateName().equals("stop")) return false;

        String stateName = attr.getStateName();
        String firstState = metaData.firstState(rootProcess.getProcessName());
        EnumSet<ChangeType> passed = EnumSet.noneOf(ChangeType.class);
        Iterator<IAttributed> iter = path.descendingIterator();
        while(iter.hasNext()){
            IAttributed newAttr = iter.next();
            if(newAttr.getContextType() == AttributedNodeType.Statement){
                ChangeType ty = newAttr.getProcChange().get(rootProcess);
                if(ty == null) continue;
                // 4.5: after the change, with no other kind of change since, the process is in
                // its first state, in stop, or in error.
                switch(ty){
                    case Start -> {
                        if(!stateName.equals(firstState) && Collections.disjoint(passed, EnumSet.of(ChangeType.Stop, ChangeType.Error)))
                            return false;
                    }
                    case Stop -> {
                        if(!stateName.equals("stop") && Collections.disjoint(passed, EnumSet.of(ChangeType.Start, ChangeType.Error)))
                            return false;
                    }
                    case Error -> {
                        if(!stateName.equals("error") && Collections.disjoint(passed, EnumSet.of(ChangeType.Start, ChangeType.Stop)))
                            return false;
                    }
                }
                passed.add(ty);
            } else if(newAttr instanceof ProcessStatusAttributes){
                ProcessStatus status = newAttr.getProcStatuses().get(rootProcess.getProcessName());
                // 4.6: an earlier check excludes the state, and nothing since could have put the
                // process in a state the check excludes.
                if(status != null && !allowed(status).contains(statusOf(stateName))) {
                    Set<ChangeType> excluded = EnumSet.allOf(ChangeType.class);
                    excluded.removeAll(changesAllowing(status));
                    if(Collections.disjoint(passed, excluded)) return false;
                }
            } else if(newAttr instanceof StateAttributes anotherAttr){
                // 4.7: processes of one group, the other one dispatched earlier this cycle.
                ProcessAttributes anotherPAttr = (ProcessAttributes) collector.getAttributes(anotherAttr.getRootProcess());
                if (pattr.getGroup()==anotherPAttr.getGroup()){
                    String anotherStateName = anotherAttr.getStateName();
                    String anotherFirstState = metaData.firstState(anotherAttr.getRootProcess().getProcessName());
                    if(stateName.equals("stop") != anotherStateName.equals("stop"))
                        return false;
                    if(stateName.equals("error") != anotherStateName.equals("error"))
                        return false;
                    if(stateName.equals(firstState) && justStarted(attr) && !anotherStateName.equals(anotherFirstState))
                        return false;
                    if(anotherStateName.equals(anotherFirstState) && justStarted(anotherAttr) && !stateName.equals(firstState))
                        return false;
                }
            }
        }
        return true;
    }

    // For a first state: no set state leads to it, and every path through it leaves it. A process
    // found there was then started since its previous turn.
    private boolean justStarted(StateAttributes first){
        return first.getReachFrom().isEmpty()
                && !first.getReachTo().isEmpty()
                && !first.getReachTo().contains(first.getStateName());
    }

    private static ChangeType changeOf(IAttributed attr, String process){
        for (Map.Entry<ProcessNode, ChangeType> entry : attr.getProcChange().entrySet()) {
            if (entry.getKey().getProcessName().equals(process)) return entry.getValue();
        }
        return null;
    }

    // The statuses (of active, stop and error) a value of a process status check allows.
    private static Set<ProcessStatus> allowed(ProcessStatus status){
        return switch (status) {
            case Active -> EnumSet.of(ProcessStatus.Active);
            case Inactive -> EnumSet.of(ProcessStatus.Stop, ProcessStatus.Error);
            case Stop -> EnumSet.of(ProcessStatus.Stop);
            case NonStop -> EnumSet.of(ProcessStatus.Active, ProcessStatus.Error);
            case Error -> EnumSet.of(ProcessStatus.Error);
            case NonError -> EnumSet.of(ProcessStatus.Active, ProcessStatus.Stop);
        };
    }

    // The changes after which a check of this value can hold.
    private static Set<ChangeType> changesAllowing(ProcessStatus status){
        Set<ChangeType> changes = EnumSet.noneOf(ChangeType.class);
        for (ChangeType change : ChangeType.values()) {
            if (allowed(status).contains(statusAfter(change))) changes.add(change);
        }
        return changes;
    }

    private static ProcessStatus statusAfter(ChangeType change){
        return switch (change) {
            case Start -> ProcessStatus.Active;
            case Stop -> ProcessStatus.Stop;
            case Error -> ProcessStatus.Error;
        };
    }

    private static ProcessStatus statusOf(String stateName){
        return switch (stateName) {
            case "stop" -> ProcessStatus.Stop;
            case "error" -> ProcessStatus.Error;
            default -> ProcessStatus.Active;
        };
    }

}
