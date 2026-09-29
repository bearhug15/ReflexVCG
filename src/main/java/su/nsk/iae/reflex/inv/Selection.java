package su.nsk.iae.reflex.inv;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Which kinds of derived invariant generation uses: the {@code -x} key.
 *
 * <p>The high priority is always in, so every condition knows which states each process
 * can be in; {@code mid} and {@code low} add the rest priority by priority, and a kind can
 * be named on its own. Only {@link #none()} leaves everything out, which is how the output
 * from before extra invariants is reproduced.
 */
public final class Selection {

    private final Set<ExtraInvariant.Kind> kinds;

    private Selection(Set<ExtraInvariant.Kind> kinds) {
        this.kinds = Collections.unmodifiableSet(kinds.isEmpty()
                ? EnumSet.noneOf(ExtraInvariant.Kind.class) : EnumSet.copyOf(kinds));
    }

    public static Selection none() {
        return new Selection(EnumSet.noneOf(ExtraInvariant.Kind.class));
    }

    /** The default: the invariants every condition gets. */
    public static Selection high() {
        return upTo(ExtraInvariant.Priority.HIGH);
    }

    public static Selection mid() {
        return upTo(ExtraInvariant.Priority.MID);
    }

    /** Everything the program structure yields. */
    public static Selection low() {
        return upTo(ExtraInvariant.Priority.LOW);
    }

    /** The high priority and the kinds named. */
    public static Selection of(ExtraInvariant.Kind... kinds) {
        Set<ExtraInvariant.Kind> selected = EnumSet.copyOf(high().kinds);
        Collections.addAll(selected, kinds);
        return new Selection(selected);
    }

    private static Selection upTo(ExtraInvariant.Priority priority) {
        Set<ExtraInvariant.Kind> selected = EnumSet.noneOf(ExtraInvariant.Kind.class);
        for (ExtraInvariant.Kind kind : ExtraInvariant.Kind.values()) {
            if (kind.isDerived() && kind != ExtraInvariant.Kind.CUSTOM
                    && kind.priority().compareTo(priority) <= 0) {
                selected.add(kind);
            }
        }
        return new Selection(selected);
    }

    /**
     * Reads the key: a comma-separated list of {@code none}, {@code high}, {@code mid},
     * {@code low} (or {@code all}) and kind names such as {@code timer_bounds}. The high
     * priority is in unless the key is {@code none}.
     *
     * @throws IllegalArgumentException for a word that is none of those
     */
    public static Selection parse(String key) {
        String trimmed = key == null ? "" : key.trim();
        if (trimmed.isEmpty()) {
            return high();
        }
        if (trimmed.equalsIgnoreCase("none")) {
            return none();
        }
        Set<ExtraInvariant.Kind> selected = EnumSet.copyOf(high().kinds);
        for (String word : trimmed.split(",")) {
            String name = word.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            switch (name) {
                case "HIGH" -> selected.addAll(high().kinds);
                case "MID" -> selected.addAll(mid().kinds);
                case "LOW", "ALL" -> selected.addAll(low().kinds);
                default -> {
                    ExtraInvariant.Kind kind;
                    try {
                        kind = ExtraInvariant.Kind.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        kind = null;
                    }
                    if (kind == null || !kind.isDerived() || kind == ExtraInvariant.Kind.CUSTOM) {
                        throw new IllegalArgumentException("Not a priority or a kind of extra invariant: "
                                + word.trim() + " (expected none, high, mid, low or one of "
                                + low().kinds.stream().map(k -> k.name().toLowerCase(Locale.ROOT))
                                .collect(Collectors.joining(", ")) + ")");
                    }
                    selected.add(kind);
                }
            }
        }
        return new Selection(selected);
    }

    public boolean includes(ExtraInvariant.Kind kind) {
        return kinds.contains(kind);
    }

    public Set<ExtraInvariant.Kind> kinds() {
        return kinds;
    }

    public boolean isEmpty() {
        return kinds.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Selection selection && selection.kinds.equals(kinds);
    }

    @Override
    public int hashCode() {
        return kinds.hashCode();
    }

    @Override
    public String toString() {
        return kinds.isEmpty() ? "none" : kinds.stream().map(k -> k.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(","));
    }
}
