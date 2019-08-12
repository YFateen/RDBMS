package db;

/**
 * Created by Yusuf on 2/26/2017.
 */
public class ConditionalStatement {
    private String firstColumnName;
    private String secondColumnName;
    private Value secondValue;
    private final Comparator comparator;
    private final String conditionType;

    // Unary conditional statement.
    public ConditionalStatement(String firstColumnName, Comparator comparator, Value secondValue) {
        this.firstColumnName = firstColumnName;
        this.comparator = comparator;
        this.secondValue = secondValue;
        this.conditionType = "unary";
    }

    // Binary conditional statement.
    public ConditionalStatement(String firstColumnName, Comparator comparator, String secondColumnName) {
        this.firstColumnName = firstColumnName;
        this.comparator = comparator;
        this.secondColumnName = secondColumnName;
        this.conditionType = "binary";
    }

    public String getFirstColumnName() {
        return firstColumnName;
    }

    public String getSecondColumnName() {
        return secondColumnName;
    }

    public Value getSecondValue() {
        return secondValue;
    }

    public Comparator getComparator() {
        return comparator;
    }

    public String getConditionType() {
        return conditionType;
    }
}
