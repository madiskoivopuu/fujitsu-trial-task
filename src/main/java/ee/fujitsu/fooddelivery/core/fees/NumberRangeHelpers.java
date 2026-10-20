package ee.fujitsu.fooddelivery.core.fees;

/**
 * Provides numeric range checks for extra fee rules.
 * Ranges include their lower bound and exclude their upper bound.
 */
public class NumberRangeHelpers {
    /**
     * Checks whether two number ranges overlap.
     * Ranges that only touch at an endpoint do not overlap, e.g.
     * {@code [0, 10)} and {@code [10, 20)} do not overlap.
     * Each range must have a start that is less than its end. This method
     * does not validate that requirement, it must be fulfilled beforehand.
     *
     * @param firstRangeStart inclusive lower bound of the first range
     * @param firstRangeEndExclusive exclusive upper bound of the first range
     * @param secondRangeStart inclusive lower bound of the second range
     * @param secondRangeEndExclusive exclusive upper bound of the second range
     * @return {@code true} if the supplied valid ranges share at least one value;
     *         {@code false} otherwise
     */
    public static boolean rangesOverlap(double firstRangeStart, double firstRangeEndExclusive,
                                        double secondRangeStart, double secondRangeEndExclusive) {
        return firstRangeStart < secondRangeEndExclusive && secondRangeStart < firstRangeEndExclusive;
    }

    /**
     * Checks whether a number is at least the lower bound and below the upper
     * bound of a range. For example, {@code [0, 10)} contains {@code 0}, but
     * does not contain {@code 10}.
     *
     * @param rangeStart inclusive lower bound of the range
     * @param rangeEndExclusive exclusive upper bound of the range
     * @param number value to check
     * @return {@code true} if {@code rangeStart <= number < rangeEndExclusive};
     *         {@code false} otherwise
     */
    public static boolean numberWithinRange(double rangeStart, double rangeEndExclusive, double number) {
        return rangeStart <= number && number < rangeEndExclusive;
    }
}
