package db;

/**
 * Created by Yusuf on 2/26/2017.
 */
public class ColumnExpression {
    private String singleColumnName;
    private String firstColumnName;
    private Operator operator;
    private String secondColumnName;
    private Value secondValue;
    private String alias;
    private final String expressionType;

    // Column expression is a single operand
    public ColumnExpression(String operand) {
        if (operand.equals("*")) {
            expressionType = "*";
        } else {
            singleColumnName = operand;
            expressionType = "<column>";
        }
    }

    // Column expression is <column0> <arithmetic operator> <column1> as <alias>
    public ColumnExpression(String column0, Operator operator, String column1, String alias) {
        firstColumnName = column0;
        this.operator = operator;
        secondColumnName = column1;
        expressionType = "<column0> <arithmetic operator> <column1> as <alias>";
        this.alias = alias;
    }

    // Column expression is <column0> <arithmetic operator> <value> as <alias>
    public ColumnExpression(String column0, Operator operator, Value value, String alias) {
        firstColumnName = column0;
        this.operator = operator;
        secondValue = value;
        expressionType = "<column0> <arithmetic operator> <value> as <alias>";
        this.alias = alias;
    }

    public String getExpressionType() {
        return expressionType;
    }

    public String getSingleColumnName() {
        return singleColumnName;
    }

    public String getFirstColumnName() {
        return firstColumnName;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getSecondColumnName() {
        return secondColumnName;
    }

    public Value getSecondValue() {
        return secondValue;
    }

    public String getAlias() {
        return alias;
    }
}
