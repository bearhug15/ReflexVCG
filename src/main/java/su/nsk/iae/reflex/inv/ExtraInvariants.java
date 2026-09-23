package su.nsk.iae.reflex.inv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The extra invariants of a program, each with a set of tags.
 *
 * <p>Tags can be attached when an invariant is added or at any point after, and
 * {@link #find} returns the invariants carrying <em>every</em> tag asked for - so a
 * condition that passes through {@code Controller} in {@code filling} asks for
 * {@code {state=Controller.filling}}, and one that wants only the cheap lemmas adds
 * {@code group=ADVANCED}.
 *
 * <p>Everything comes back in the order the invariants were added, whatever the query:
 * generation has to be deterministic, and the order they are written in is this one.
 */
public final class ExtraInvariants implements Iterable<ExtraInvariant> {

    /** Keyed by name, which is what makes a name unique. */
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    /** Every invariant carrying a tag, for searching without a scan. */
    private final Map<Tag, Set<ExtraInvariant>> byTag = new LinkedHashMap<>();

    private record Entry(ExtraInvariant invariant, Set<Tag> tags) {
    }

    /**
     * Adds an invariant with its tags. Its kind and group are tagged automatically, so
     * those can always be searched for.
     *
     * @throws IllegalArgumentException if an invariant of the same name is already held
     */
    public ExtraInvariant add(ExtraInvariant invariant, Tag... tags) {
        return add(invariant, Arrays.asList(tags));
    }

    public ExtraInvariant add(ExtraInvariant invariant, Collection<Tag> tags) {
        if (entries.containsKey(invariant.name())) {
            throw new IllegalArgumentException("An extra invariant named " + invariant.name()
                    + " is already present");
        }
        entries.put(invariant.name(), new Entry(invariant, new LinkedHashSet<>()));
        attach(invariant, Tag.kind(invariant.kind()), Tag.group(invariant.group()));
        attach(invariant, tags);
        return invariant;
    }

    /** Attaches more tags to an invariant already held. */
    public void attach(ExtraInvariant invariant, Tag... tags) {
        attach(invariant, Arrays.asList(tags));
    }

    public void attach(ExtraInvariant invariant, Collection<Tag> tags) {
        Entry entry = entryOf(invariant);
        for (Tag tag : tags) {
            if (entry.tags().add(tag)) {
                byTag.computeIfAbsent(tag, t -> new LinkedHashSet<>()).add(entry.invariant());
            }
        }
    }

    /** Removes tags from an invariant. A tag it does not carry is ignored. */
    public void detach(ExtraInvariant invariant, Tag... tags) {
        Entry entry = entryOf(invariant);
        for (Tag tag : tags) {
            if (entry.tags().remove(tag)) {
                Set<ExtraInvariant> tagged = byTag.get(tag);
                tagged.remove(entry.invariant());
                if (tagged.isEmpty()) {
                    byTag.remove(tag);
                }
            }
        }
    }

    /** Removes an invariant and all its tags. Returns whether it was held. */
    public boolean remove(ExtraInvariant invariant) {
        Entry entry = entries.get(invariant.name());
        if (entry == null || !entry.invariant().equals(invariant)) {
            return false;
        }
        detach(invariant, entry.tags().toArray(new Tag[0]));
        entries.remove(invariant.name());
        return true;
    }

    /** The tags an invariant carries, in the order they were attached. */
    public Set<Tag> tagsOf(ExtraInvariant invariant) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(entryOf(invariant).tags()));
    }

    /** The invariant of that name, or null. */
    public ExtraInvariant get(String name) {
        Entry entry = entries.get(name);
        return entry == null ? null : entry.invariant();
    }

    /**
     * The invariants carrying every one of {@code tags}. No tags at all matches every
     * invariant.
     */
    public List<ExtraInvariant> find(Tag... tags) {
        return find(Arrays.asList(tags));
    }

    public List<ExtraInvariant> find(Collection<Tag> tags) {
        if (tags.isEmpty()) {
            return all();
        }
        // Start from the rarest tag, then check the rest against each candidate's own set.
        Set<ExtraInvariant> smallest = null;
        for (Tag tag : tags) {
            Set<ExtraInvariant> tagged = byTag.getOrDefault(tag, Set.of());
            if (smallest == null || tagged.size() < smallest.size()) {
                smallest = tagged;
            }
        }
        List<ExtraInvariant> found = new ArrayList<>();
        for (Entry entry : entries.values()) {
            if (smallest.contains(entry.invariant()) && entry.tags().containsAll(tags)) {
                found.add(entry.invariant());
            }
        }
        return found;
    }

    /** The invariants carrying at least one of {@code tags}. */
    public List<ExtraInvariant> findAny(Collection<Tag> tags) {
        List<ExtraInvariant> found = new ArrayList<>();
        for (Entry entry : entries.values()) {
            for (Tag tag : tags) {
                if (entry.tags().contains(tag)) {
                    found.add(entry.invariant());
                    break;
                }
            }
        }
        return found;
    }

    /** Every invariant, in the order added. */
    public List<ExtraInvariant> all() {
        List<ExtraInvariant> all = new ArrayList<>();
        entries.values().forEach(entry -> all.add(entry.invariant()));
        return all;
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public Iterator<ExtraInvariant> iterator() {
        return all().iterator();
    }

    private Entry entryOf(ExtraInvariant invariant) {
        Entry entry = entries.get(invariant.name());
        if (entry == null || !entry.invariant().equals(invariant)) {
            throw new IllegalArgumentException("Not in this container: " + invariant.name());
        }
        return entry;
    }
}
