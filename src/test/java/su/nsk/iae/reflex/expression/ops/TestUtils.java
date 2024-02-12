package su.nsk.iae.reflex.expression.ops;

import su.nsk.iae.reflex.expression.types.*;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static List<ExprType> reflexTypes;
    public static List<ExprType> allTypes;

    static {
        reflexTypes = new ArrayList<>();
        reflexTypes.add(new BoolType());
        reflexTypes.add(new DoubleType());
        reflexTypes.add(new FloatType());
        reflexTypes.add(new Int8Type());
        reflexTypes.add(new Int16Type());
        reflexTypes.add(new Int32Type());
        reflexTypes.add(new Int64Type());
        reflexTypes.add(new UInt8Type());
        reflexTypes.add(new UInt16Type());
        reflexTypes.add(new UInt32Type());
        reflexTypes.add(new UInt64Type());
        reflexTypes.add(new VoidType());
        reflexTypes.add(new TimeType());

        allTypes = new ArrayList<>();
        allTypes.add(new BoolType());
        allTypes.add(new DoubleType());
        allTypes.add(new FloatType());
        allTypes.add(new Int8Type());
        allTypes.add(new Int16Type());
        allTypes.add(new Int32Type());
        allTypes.add(new Int64Type());
        allTypes.add(new UInt8Type());
        allTypes.add(new UInt16Type());
        allTypes.add(new UInt32Type());
        allTypes.add(new UInt64Type());
        allTypes.add(new VoidType());
        allTypes.add(new TimeType());
        allTypes.add(new RealType());
        allTypes.add(new NatType());
        allTypes.add(new IntType());
        allTypes.add(new StateType(""));

    }
}
