package su.nsk.iae.reflex.formulas;

import java.util.List;

public interface Formula {
    String toString();
    List<String>toStrings();
    String toNamedString();
    List<String> toNamedStrings();
    Formula trim();
}
