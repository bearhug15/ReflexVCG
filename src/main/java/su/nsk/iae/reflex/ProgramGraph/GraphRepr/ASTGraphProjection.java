package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.antlr.v4.runtime.ParserRuleContext;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.GraphNodes.IReflexNode;

import java.math.BigDecimal;

import java.util.HashMap;

public class ASTGraphProjection {
    HashMap<ParserRuleContext, IReflexNode> projection=new HashMap<>();

    public void put(ParserRuleContext context,IReflexNode node){
        projection.put(context,node);
    }

    public IReflexNode get(ParserRuleContext context){
        IReflexNode node = projection.get(context);
        if(node==null){
            throw new RuntimeException("Projection tried to get node for unknown context:\n"+context.getText());
        }else{
            return node;
        }
    }

    /*public static class ValueParser {

        public static String parseInteger(String value){
            value=value.replace("L","");
            value=value.replace("l","");
            value=value.replace("U","");
            value=value.replace("u","");
            int num = value.indexOf("+")+1;
            if(num==0){
                num = value.indexOf("-")+1;
            }
            String res;
            if (value.contains("x") || value.contains("X")){
                res = parseHex(value);
            } else if (value.charAt(num)=='0'){
                res = parseOctal(value);
            }else{
                res = parseInt(value);
            }
            return res;
        }

        public static String parseHex(String value) {
            //return Integer.valueOf(value.substring(2),16).toString();
            return Integer.decode(value).toString();
        }
        public static String parseOctal(String value) {
            return Integer.valueOf(value,8).toString();
        }
        public static String parseInt(String value) {
            return value;
        }

        public static String parseFloat(String value){
            value=value.replace("L","");
            value=value.replace("l","");
            value=value.replace("U","");
            value=value.replace("u","");
            return (BigDecimal.valueOf(Double.parseDouble(value))).toString();
        }
        public static String parseBool(String value){
            return value.substring(0, 1).toUpperCase() + value.substring(1);
        }
        public static String parseTime(String value){
            value = value.toUpperCase();
            int days=0;
            int idx =2;
            int nextIdx= value.indexOf('D');
            if (nextIdx !=-1){
                days = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }

            int hours =0;
            nextIdx= value.indexOf('H');
            if (nextIdx !=-1){
                hours = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }

            int minute =0;
            nextIdx= value.indexOf('M');
            if (nextIdx !=-1){
                if (nextIdx+1== value.length() ||  value.charAt(nextIdx+1)!='S'){
                    minute = Integer.valueOf(value.substring(idx,nextIdx));
                    idx = nextIdx+1;
                }
            }

            int second =0;
            nextIdx= value.indexOf('S');
            if (nextIdx !=-1){
                if (value.charAt(nextIdx-1)!='M'){
                    second = Integer.valueOf(value.substring(idx,nextIdx));
                    idx = nextIdx+1;
                }
            }

            int milisecond =0;
            nextIdx= value.indexOf("MS");
            if (nextIdx !=-1){
                milisecond = Integer.valueOf(value.substring(idx,nextIdx));
            }

            int res = days*24*60*60*1000
                    + hours*60*60*1000
                    + minute*60*1000
                    +second*1000
                    +milisecond;

            return String.valueOf(res);
        }
    }*/
}
