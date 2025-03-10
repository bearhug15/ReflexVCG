package su.nsk.iae.reflex.vcg;

import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProgramMetaData;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class VCPrinter {
    String sourceName;

    Path destination=null;

    HashSet<String> VC;
    int lemmasPrinted;
    ProgramMetaData metaData;
    IStatementCreator creator;

    boolean isGlobalTheory;
    public VCPrinter(Path destination, Path source, ProgramMetaData programMetaData, IStatementCreator creator){
        isGlobalTheory = false;
        lemmasPrinted=0;
        this.sourceName=source.getFileName().toString();
        this.creator = creator;
        VC = new HashSet<>();
        File file = destination.toFile();
        if (file.isDirectory()){
            this.destination=destination;
        } else{
            throw new RuntimeException("Destination not directory");
        }
        this.metaData = programMetaData;
        //copyReflexTheory();
    }

    public void copyReflexTheory(){
        try {
            InputStream input1 = getClass().getClassLoader().getResourceAsStream("ReflexTheory/Reflex.thy");
            Files.copy(input1,Paths.get(destination.toString()+"/Reflex.thy"),REPLACE_EXISTING );
            InputStream input2 = getClass().getClassLoader().getResourceAsStream("ReflexTheory/ReflexPatterns.thy");
            Files.copy(input2,Paths.get(destination.toString()+"/ReflexPatterns.thy"),REPLACE_EXISTING );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void createGlobalTheory(){
        String clock = metaData.getClockValue();
        String dirName = destination.getFileName().toString();
        String fileName;
        if (sourceName==null) {
            fileName = dirName.split("\\.")[0];
        }else{
            fileName = sourceName.split("\\.")[0];
        }
        fileName = fileName+"Theory";

        String inner = "fun ltime:: \"state \\<Rightarrow> process \\<Rightarrow> nat\" where \n" +
                "\"ltime emptyState _ = 0\" \n" +
                "| \"ltime (toEnv s) p = (ltime s p) + " + clock + "\" \n" +
                "| \"ltime (setVarBool s _ _) p = ltime s p\" \n" +
                "| \"ltime (setVarInt s _ _) p = ltime s p\"\n" +
                "| \"ltime (setVarNat s _ _) p = ltime s p\"\n" +
                "| \"ltime (setVarReal s _ _) p = ltime s p\"\n" +
                "| \"ltime (setPstate s p1 _) p =\n" +
                "  (if p=p1 then 0 else ltime s p)\" \n" +
                "| \"ltime (reset s p1) p =\n" +
                "  (if p=p1 then 0 else ltime s p)\"\n\n" +

                "lemma ltime_mod:\n" +
                "assumes \"ltime s0 p < a*" + clock + "\"\n" +
                "shows \"ltime s0 p \\<le> (a*" + clock + "-" + clock + ")\"\n" +
                "proof -\n" +
                "have\"(ltime s0 p) mod " + clock + " = 0\" by (induction s0) (auto)\n" +
                "thus ?thesis using assms by (induction a) (auto)\n" +
                "qed\n" +

                "lemma ltime_mult:\n" +
                "\"ltime s p mod " + clock + " = 0\"\n" +
                "  by (induction s) (auto)\n" +
                "\n" +
                "lemma ltime_div_less:\n" +
                "assumes \"(ltime s0 p div " + clock + ")≤ a\"\n" +
                "shows \"(ltime s0 p -" + clock + ") div " + clock + " < a ∨ ltime s0 p = 0\"\n" +
                "proof -\n" +
                "have\"(ltime s0 p) mod " + clock + " = 0\" by (induction s0) (auto)\n" +
                "thus ?thesis using assms by (induction a) (auto)\n" +
                "qed\n" +
                "\n" +

                "lemma ltime_le_toEnvNum: \n" +
                "\"ltime s p div " + clock + " ≤ toEnvNum emptyState s\"\n" +
                "  apply(induction s)\n" +
                "         apply(auto)\n" +
                "  done\n" +
                "lemma toEnvNum_getPstate:\n" +
                "\"toEnvNum s s' < ltime s' p div " + clock + " \\<Longrightarrow> getPstate s p = getPstate s' p\"\n" +
                "  apply (induction s' arbitrary:s)\n" +
                "  apply auto\n" +
                "  apply (metis Suc_eq_plus1 getPstate.simps(2) not_less_eq)\n" +
                "  using getPstate.simps apply presburger+\n" +
                "done\n" +
                "\n" +
                "lemma inter_toEnvNum_getPstate:\n" +
                "\"toEnvNum s s' < ltime s' p div " + clock + " ∧ substate s s'' ∧ substate s'' s'\\<Longrightarrow> toEnvNum s'' s' < ltime s' p div " + clock + "\"\n" +
                "  using toEnvNum3 by fastforce";


        String theory = creator.Theory(fileName,List.of("ReflexPatterns"), inner);

        fileName = fileName+".thy";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(theory);
            writer.close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public void createRequirementsTheory(){
        String dirName = destination.getFileName().toString();
        String fileName = "Requirements";

        String inner = creator.Definition("inv",List.of("s"),creator.True());

        String theory = creator.Theory(fileName,List.of("ReflexPatterns"), inner);
        fileName = fileName+".thy";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(theory);
            writer.close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public void printBaseVCInDir(){
        if (!isGlobalTheory){
            createGlobalTheory();
            createRequirementsTheory();
            copyReflexTheory();
            isGlobalTheory = true;
        }
        String init = creator.EmptyStateStatement();

        List<String> processNames = metaData.processNames();
        //formula.addConjunct(new StateFormula(stateName(),(new StateType(stateName())).defaultValue()));
        ArrayList<String> statements = new ArrayList<>();
        int stateNum =0;
        for(String processName: processNames){
            String initialized = metaData.initializeProcess(creator.PlaceHolder(),processName);
            if (!initialized.equals(creator.PlaceHolder())) {
                statements.add(creator.ExpressionStatement(stateNum,initialized));
                stateNum++;
            }
        }
        String startProcess = metaData.startProcess();
        statements.add(creator.SetStateStatement(stateNum,startProcess,metaData.startState(startProcess)));
        stateNum++;
        statements.add(creator.EnvStatement(stateNum));
        stateNum++;
        statements.add(creator.FinalStatement(stateNum));
        String impl = creator.ImplInvariantStatement(creator.FinalStatementName());
        String lemma = creator.Lemma(init,statements,impl);
        //String setP = setPstate(stateName(),startProcess,metaData.startState(startProcess));
        //stateCount++;
        //formula.addConjunct(new StateFormula(stateName(),setP));
        String dirName = destination.getFileName().toString();
        String fileName;
        if (sourceName==null) {
            fileName = dirName.split("\\.")[0];
        }else{
            fileName = sourceName.split("\\.")[0];
        }
        String baseTheory = fileName+"Theory";
        fileName = fileName+"_VC"+lemmasPrinted;

        String theory = creator.Theory(fileName, List.of(baseTheory,"Requirements"),lemma);

        fileName = fileName+".thy";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(theory);
            writer.close();
            lemmasPrinted++;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void printVCInDir(ArrayList<String> statements, int lastState){
        if (!isGlobalTheory){
            createGlobalTheory();
            createRequirementsTheory();
            copyReflexTheory();
            isGlobalTheory = true;
        }
        String init = creator.InitInvariantStatement();
        statements.add(creator.EnvStatement(lastState));
        lastState++;
        statements.add(creator.FinalStatement(lastState));
        String impl = creator.ImplInvariantStatement(creator.FinalStatementName());
        String lemma = creator.Lemma(init,statements,impl);

        String dirName = destination.getFileName().toString();
        String fileName;
        if (sourceName==null) {
            fileName = dirName.split("\\.")[0];
        }else{
            fileName = sourceName.split("\\.")[0];
        }
        String baseTheory = fileName+"Theory";
        fileName = fileName+"_VC"+lemmasPrinted;

        String theory = creator.Theory(fileName, List.of(baseTheory,"Requirements"),lemma);

        fileName = fileName+".thy";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(theory.toString());
            writer.close();
            lemmasPrinted++;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public void printGraph(String graph){
        String dirName = destination.getFileName().toString();
        String fileName;
        if (sourceName==null) {
            fileName = dirName.split("\\.")[0];
        }else{
            fileName = sourceName.split("\\.")[0];
        }
        fileName = fileName + "_" + "program_graph.gv";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(graph);
            writer.close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public int getLemmasPrinted(){
        return lemmasPrinted;
    }

}
