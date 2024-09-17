package su.nsk.iae.reflex.vcg;

import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

class ProgramMetaDataTest {
    /*ProgramMetaData metaData;

    @BeforeEach
    void prepare(){
        metaData = new ProgramMetaData();
        metaData.clockValue = "10";
        Map<String, Pair<String, String>> inits = metaData.initializer;
        inits.put("proc1",new Pair<>("#","(setVarBool # ''proc1'' ''var1'' True)"));

        List<Pair<String, List<String>>> processes = metaData.processes;
        processes.add(new Pair<>("proc1",List.of("state1","state2")));
    }

    @Test
    void nextState() {
        assertEquals("state2",metaData.nextState("proc1","state1"));
        assertThrows(RuntimeException.class,()->metaData.nextState("proc1","state2"));
        assertThrows(RuntimeException.class,()->metaData.nextState("proc2","state1"));
    }

    @Test
    void addProcess() {
        ProgramMetaData newMetaData = new ProgramMetaData();
        newMetaData.clockValue = "10";
        Map<String, Pair<String, String>> inits = newMetaData.initializer;
        inits.put("proc1",new Pair<>("#","(setVarBool # ''proc1'' ''var1'' True)"));

        List<Pair<String, List<String>>> processes = newMetaData.processes;
        processes.add(new Pair<>("proc1",List.of("state1","state2")));
        processes.add(new Pair<>("proc2",List.of("state1","state2")));

        metaData.addProcess("proc2",List.of("state1","state2"));
        assertEquals(newMetaData,metaData);
    }

    @Test
    void addInitializer() {
        ProgramMetaData newMetaData = new ProgramMetaData();
        newMetaData.clockValue = "10";
        Map<String, Pair<String, String>> inits = newMetaData.initializer;
        inits.put("proc1",new Pair<>("#","(setVarBool # ''proc1'' ''var1'' True)"));
        inits.put("proc2",new Pair<>("#","(setVarNat # ''proc2'' ''var1'' True)"));

        List<Pair<String, List<String>>> processes = newMetaData.processes;
        processes.add(new Pair<>("proc1",List.of("state1","state2")));

        metaData.addInitializer("#","proc2","(setVarNat # ''proc2'' ''var1'' True)");

        assertEquals(newMetaData,metaData);

    }

    @Test
    void initializeProcess() {
        assertEquals("(setVarBool s0 ''proc1'' ''var1'' True)",metaData.initializeProcess("s0","proc1"));
        assertThrows(RuntimeException.class,()->metaData.initializeProcess("#","proc2"));
    }

    @Test
    void startState() {
        assertEquals("state1",metaData.startState("proc1"));
    }

    @Test
    void startProcess() {
        assertEquals("proc1",metaData.startProcess());
    }

    @Test
    void processNames() {
        assertEquals(List.of("proc1"),metaData.processNames());
    }

    @Test
    void stateByIdx() {
        assertEquals("state2",metaData.stateByIdx("proc1",1));
        assertThrows(RuntimeException.class,()->metaData.stateByIdx("proc2",1));
        assertThrows(RuntimeException.class,()->metaData.stateByIdx("proc1",2));
    }*/
}