package su.nsk.iae.reflex.vcg;

import su.nsk.iae.reflex.formulas.ImplicationFormula;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

public class VCPrinter {
    String sourceName;

    Path destination;

    HashSet<String> VC;
    int lemmasPrinted;
    ProgramMetaData programMetaData;

    public VCPrinter(Path destination, String sourceName, ProgramMetaData programMetaData){
        lemmasPrinted=0;
        this.sourceName=sourceName;
        VC = new HashSet<>();
        File file = destination.toFile();
        if (file.isDirectory()){
            this.destination=destination;
        } else{
            throw new RuntimeException("Destination not directory");
        }
        this.programMetaData = programMetaData;
    }

    public void printVC(ImplicationFormula formula){
        printVCInDir(formula);
        lemmasPrinted++;
    }

    public void printVCInDir(ImplicationFormula formula){
        String dirName = destination.getFileName().toString();
        String fileName;
        if (sourceName==null) {
            fileName = dirName.split("\\.")[0];
        }else{
            fileName = sourceName.split("\\.")[0];
        }
        fileName = fileName+"_VC"+lemmasPrinted;
        String lemma = toDetailedLemma(formula);
        StringBuilder builder = new StringBuilder();

        builder.append("theory ").append(fileName).append("\n");
        builder.append("imports Reflex\n");
        builder.append("begin\n\n");
        builder.append("fun ltime:: \"state \\<Rightarrow> process \\<Rightarrow> nat\" where \n" +
                "\"ltime emptyState _ = 0\" \n" +
                "| \"ltime (toEnv s) p = (ltime s p) + "+programMetaData.clockValue+"\" \n" +
                "| \"ltime (setVarBool s _ _) p = ltime s p\" \n" +
                "| \"ltime (setVarInt s _ _) p = ltime s p\"\n" +
                "| \"ltime (setVarNat s _ _) p = ltime s p\"\n" +
                "| \"ltime (setVarReal s _ _) p = ltime s p\"\n" +
                "| \"ltime (setPstate s p1 _) p =\n" +
                "  (if p=p1 then 0 else ltime s p)\" \n" +
                "| \"ltime (reset s p1) p =\n" +
                "  (if p=p1 then 0 else ltime s p)\"");
        builder.append("\n\n");
        builder.append(lemma);
        builder.append("\n");
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

    public String toDetailedLemma(ImplicationFormula formula){
        StringBuilder lemma = new StringBuilder();
        lemma.append("lemma\n ");
        lemma.append("assumes ");
        List<String> assumes = formula.left().trim().toNamedStrings();
        StringJoiner joiner1 = new StringJoiner("\n and ");
        for (String string: assumes)
            joiner1.add(string);
        lemma.append(joiner1);
        lemma.append("\n");
        lemma.append("shows ");

        String shows = "\""+formula.right().toString()+"\"";
        lemma.append(shows);
        /*List<String> shows = formula.right().trim().toNamedStrings();
        StringJoiner joiner2 = new StringJoiner("\n and ");
        for (String string: shows)
            joiner2.add(string);
        lemma.append(joiner2);*/
        return lemma.toString();
    }

}
