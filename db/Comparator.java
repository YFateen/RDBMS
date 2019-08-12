package db;

/**
 * Created by Yusuf on 3/4/2017.
 */
public class Comparator {
    private final String comparator;
    public Comparator(String comparator) throws Exception {
        if (!comparator.equals("==") && !comparator.equals("!=") && !comparator.equals("<") &&
                !comparator.equals(">") && !comparator.equals("<=") && !comparator.equals(">=")) {
            throw new IllegalArgumentException("Comparison not supported. " +
                    "Can only use ==, !=, <, >, <= and >=.");
        }
        this.comparator = comparator;
    }

    public String getComparator() {
        return comparator;
    }

    // String vs. String
    public boolean compare(String first, String second) {
        if (comparator.equals("==")) {
            return first.equals(second);
        } else if (comparator.equals("!=")) {
            return !first.equals(second);
        } else if (comparator.equals("<")) {
            return first.compareTo(second) < 0;
        } else if (comparator.equals(">")) {
            return first.compareTo(second) > 0;
        } else if (comparator.equals("<=")) {
            return first.compareTo(second) <= 0;
        } else {
            return first.compareTo(second) >= 0;
        }
    }

    // int vs. int
    public boolean compare(int first, int second) {
        if (comparator.equals("==")) {
            return first == second;
        } else if (comparator.equals("!=")) {
            return first != second;
        } else if (comparator.equals("<")) {
            return first < second;
        } else if (comparator.equals(">")) {
            return first > second;
        } else if (comparator.equals("<=")) {
            return first <= second;
        } else {
            return first >= second;
        }
    }

    // float vs. float
    public boolean compare(float first, float second) {
        if (comparator.equals("==")) {
            return first == second;
        } else if (comparator.equals("!=")) {
            return first != second;
        } else if (comparator.equals("<")) {
            return first < second;
        } else if (comparator.equals(">")) {
            return first > second;
        } else if (comparator.equals("<=")) {
            return first <= second;
        } else {
            return first >= second;
        }
    }

    // int vs. float
    public boolean compare(int first, float second) {
        if (comparator.equals("==")) {
            return first == second;
        } else if (comparator.equals("!=")) {
            return first != second;
        } else if (comparator.equals("<")) {
            return first < second;
        } else if (comparator.equals(">")) {
            return first > second;
        } else if (comparator.equals("<=")) {
            return first <= second;
        } else {
            return first >= second;
        }
    }

    // float vs. int
    public boolean compare(float first, int second) {
        if (comparator.equals("==")) {
            return first == second;
        } else if (comparator.equals("!=")) {
            return first != second;
        } else if (comparator.equals("<")) {
            return first < second;
        } else if (comparator.equals(">")) {
            return first > second;
        } else if (comparator.equals("<=")) {
            return first <= second;
        } else {
            return first >= second;
        }
    }
}
