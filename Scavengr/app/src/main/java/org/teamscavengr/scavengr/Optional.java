package org.teamscavengr.scavengr;

/**
 * Shim for Java 8's optional
 */
public class Optional<T> {

    private final T object;
    private final boolean present;

    private Optional(T object, boolean present) {
        this.object = object;
        this.present = present;
    }

    /**
     * @param thing A thing.
     * @return An optional containing that thing.
     */
    public static <T> Optional<T> of(T thing) {
        return new Optional<>(thing, true);
    }

    /**
     * @param thing A thing.
     * @return An optional containing that thing, or empty if it is null.
     */
    public static <T> Optional<T> ofNullable(T thing) {
        return new Optional<>(thing, thing != null);
    }

    public static <T> Optional<T> empty() {
        return new Optional<>(null, false);
    }

    /**
     * @return True if this has a thing in it.
     */
    public boolean isPresent() {
        return present;
    }

    /**
     * @return The thing stored in this optional.
     * @throws java.lang.IllegalStateException if this optional is empty
     */
    public T get() {
        if(!present) throw new IllegalStateException("optional is empty");
        return object;
    }

}