package su.nsk.iae.reflex.vcg;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.formulas.ImplicationFormula;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

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
        String clock = metaData.clockValue;
        String dirName = destination.getFileName().toString();
        String fileName;
        if (sourceName==null) {
            fileName = dirName.split("\\.")[0];
        }else{
            fileName = sourceName.split("\\.")[0];
        }
        fileName = fileName+"Theory";
        StringBuilder builder = new StringBuilder();
        builder.append("theory ").append(fileName).append("\n");
        builder.append("imports ReflexPatterns\n");
        builder.append("begin\n\n");
        builder.append("fun ltime:: \"state \\<Rightarrow> process \\<Rightarrow> nat\" where \n" +
                "\"ltime emptyState _ = 0\" \n" +
                "| \"ltime (toEnv s) p = (ltime s p) + "+clock+"\" \n" +
                "| \"ltime (setVarBool s _ _) p = ltime s p\" \n" +
                "| \"ltime (setVarInt s _ _) p = ltime s p\"\n" +
                "| \"ltime (setVarNat s _ _) p = ltime s p\"\n" +
                "| \"ltime (setVarReal s _ _) p = ltime s p\"\n" +
                "| \"ltime (setPstate s p1 _) p =\n" +
                "  (if p=p1 then 0 else ltime s p)\" \n" +
                "| \"ltime (reset s p1) p =\n" +
                "  (if p=p1 then 0 else ltime s p)\"\n\n" +
                "lemma ltime_mod:\n"+
                "assumes \"ltime s0 p < a*"+clock+"\"\n"+
                "shows \"ltime s0 p \\<le> (a*"+clock+"-"+clock+")\"\n"+
                "proof -\n"+
                "have\"(ltime s0 p) mod "+clock+" = 0\" by (induction s0) (auto)\n"+
                "thus ?thesis using assms by (induction a) (auto)\n"+
                "qed\n");

        builder.append("lemma ltime_mult:\n" +
                "\"ltime s p mod "+clock+" = 0\"\n" +
                "  by (induction s) (auto)\n" +
                "\n" +
                "lemma ltime_div_less:\n" +
                "assumes \"(ltime s0 p div "+clock+")≤ a\"\n" +
                "shows \"(ltime s0 p -"+clock+") div "+clock+" < a ∨ ltime s0 p = 0\"\n" +
                "proof -\n" +
                "have\"(ltime s0 p) mod "+clock+" = 0\" by (induction s0) (auto)\n" +
                "thus ?thesis using assms by (induction a) (auto)\n" +
                "qed\n" +
                "\n" +
                "lemma ltime_le_toEnvNum: \n" +
                "\"ltime s p div "+clock+" ≤ toEnvNum emptyState s\"\n" +
                "  apply(induction s)\n" +
                "         apply(auto)\n" +
                "  done\n");
        builder.append("lemma toEnvNum_getPstate:\n" +
                "\"toEnvNum s s' < ltime s' p div "+clock+" \\<Longrightarrow> getPstate s p = getPstate s' p\"\n" +
                "  apply (induction s' arbitrary:s)\n" +
                "  apply auto\n" +
                "  apply (metis Suc_eq_plus1 getPstate.simps(2) not_less_eq)\n" +
                "  using getPstate.simps apply presburger+\n" +
                "done\n" +
                "\n" +
                "lemma inter_toEnvNum_getPstate:\n" +
                "\"toEnvNum s s' < ltime s' p div "+clock+" ∧ substate s s'' ∧ substate s'' s'\\<Longrightarrow> toEnvNum s'' s' < ltime s' p div "+clock+"\"\n" +
                "  using toEnvNum3 by fastforce");
        builder.append("\nend\n");
        fileName = fileName+".thy";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(builder.toString());
            writer.close();
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }


    public void printBaseVCInDir(){
        if (!isGlobalTheory){
            createGlobalTheory();
            copyReflexTheory();
            isGlobalTheory = true;
        }
        String init = creator.createEmptyStateStatement();

        List<String> processNames = metaData.processNames();
        //formula.addConjunct(new StateFormula(stateName(),(new StateType(stateName())).defaultValue()));
        ArrayList<String> statements = new ArrayList<>();
        int stateNum =0;
        for(String processName: processNames){
            String initialized = metaData.initializeProcess(creator.createPlaceHolder(),processName);
            if (!initialized.equals(creator.createPlaceHolder())) {
                statements.add(creator.createExpressionStatement(stateNum,initialized));
                stateNum++;
            }
        }
        String startProcess = metaData.startProcess();
        statements.add(creator.createSetStateStatement(stateNum,startProcess,metaData.startState(startProcess)));
        stateNum++;
        statements.add(creator.createEnvStatement(stateNum));
        stateNum++;
        statements.add(creator.createFinalStatement(stateNum));
        String impl = creator.createImplInvariantStatement(creator.createFinalStatementName());
        String lemma = creator.createLemma(init,statements,impl);
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

        StringBuilder builder = new StringBuilder();

        builder.append("theory ").append(fileName).append("\n");
        builder.append("imports ");
        builder.append(baseTheory);
        builder.append("\nbegin\n\n");
        builder.append(lemma);
        builder.append("\n");
        builder.append("\nend\n");

        fileName = fileName+".thy";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(builder.toString());
            writer.close();
            lemmasPrinted++;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void printVCInDir(ArrayList<String> statements, int lastState){
        if (!isGlobalTheory){
            createGlobalTheory();
            copyReflexTheory();
            isGlobalTheory = true;
        }
        String init = creator.createInitInvariantStatement();
        statements.add(creator.createEnvStatement(lastState));
        lastState++;
        statements.add(creator.createFinalStatement(lastState));
        String impl = creator.createImplInvariantStatement(creator.createFinalStatementName());
        String lemma = creator.createLemma(init,statements,impl);

        String dirName = destination.getFileName().toString();
        String fileName;
        if (sourceName==null) {
            fileName = dirName.split("\\.")[0];
        }else{
            fileName = sourceName.split("\\.")[0];
        }
        String baseTheory = fileName+"Theory";
        fileName = fileName+"_VC"+lemmasPrinted;

        StringBuilder builder = new StringBuilder();

        builder.append("theory ").append(fileName).append("\n");
        builder.append("imports ");
        builder.append(baseTheory);
        builder.append("\nbegin\n\n");
        builder.append(lemma);
        builder.append("\n");
        builder.append("\nend\n");

        fileName = fileName+".thy";
        try {
            FileWriter writer = new FileWriter(new File(destination.toString() + "/" + fileName), StandardCharsets.UTF_8);
            writer.write(builder.toString());
            writer.close();
            lemmasPrinted++;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public int getLemmasPrinted(){
        return lemmasPrinted;
    }

}
