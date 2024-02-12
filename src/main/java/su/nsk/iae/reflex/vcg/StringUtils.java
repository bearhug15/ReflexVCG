package su.nsk.iae.reflex.vcg;

import su.nsk.iae.reflex.expression.types.ExprType;

import java.math.BigDecimal;

public class StringUtils {

    public static String constructGetter(ExprType type, String state,String variable){
        return "("+type.takeGetter()+" "+state+" ''"+variable+"'')";
    }
    public static String constructSetter(ExprType type, String state,String variable,String value){
        return "("+type.takeSetter()+" "+state+" ''"+variable+"'' "+value+")";
    }

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
        int days=0;
        int idx =2;
        int nextIdx= value.indexOf('D');
        if (nextIdx ==-1){
            nextIdx = value.indexOf('d');
            if (nextIdx!=-1){
                days = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }
        }else{
            days = Integer.valueOf(value.substring(idx,nextIdx));
            idx = nextIdx+1;
        }


        int hours =0;
        nextIdx= value.indexOf('H');
        if (nextIdx ==-1){
            nextIdx = value.indexOf('h');
            if (nextIdx!=-1){
                hours = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }
        }else{
            hours = Integer.valueOf(value.substring(idx,nextIdx));
            idx = nextIdx+1;
        }

        int minute =0;
        nextIdx= value.indexOf('M');
        if (nextIdx ==-1){
            nextIdx = value.indexOf('m');
            if (nextIdx!=-1 && value.charAt(nextIdx+1)!='s'){
                minute = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }
        }else{
            if (value.charAt(nextIdx+1)!='S'){
                minute = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }

        }

        int second =0;
        nextIdx= value.indexOf('S');
        if (nextIdx ==-1){
            nextIdx = value.indexOf('s');
            if (nextIdx!=-1 && value.charAt(nextIdx-1)!='m'){
                second = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }
        }else{
            if (value.charAt(nextIdx-1)!='M') {
                second = Integer.valueOf(value.substring(idx, nextIdx));
                idx = nextIdx+1;
            }
        }

        int milisecond =0;
        nextIdx= value.indexOf("MS");
        if (nextIdx ==-1){
            nextIdx = value.indexOf("ms");
            if (nextIdx!=-1){
                milisecond = Integer.valueOf(value.substring(idx,nextIdx));
                idx = nextIdx+1;
            }
        }else{
            milisecond = Integer.valueOf(value.substring(idx,nextIdx));
            idx = nextIdx+1;
        }

        int res = days*24*60*60*1000
                + hours*60*60*1000
                + minute*60*1000
                +second*1000
                +milisecond;

        return String.valueOf(res);
    }
}
