package ee.fujitsu.fooddelivery.core.fees;

public class NumberRangeHelpers {
    public static boolean rangesOverlap(double firstRangeStart, double firstRangeEndExclusive,
                                        double secondRangeStart, double secondRangeEndExclusive) {
        return firstRangeStart < secondRangeEndExclusive && secondRangeStart < firstRangeEndExclusive;
    }

    public static boolean numberWithinRange(double rangeStart, double rangeEndExclusive, double number) {
        return rangeStart <= number && number < rangeEndExclusive;
    }
}
