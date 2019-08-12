package db;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Yusuf on 2/26/2017.
 */
public class Select {
    private final Table finalTable;
    private ColumnExpression[] columnExpressions;
    private Table[] tables;
    private ConditionalStatement[] conditionalStatements;
    private final String selectType;

    // select <column expressions> from <tables>
    public Select(ColumnExpression[] columnExpressions, Table[] tables) throws Exception {
        this.selectType = "No Conditions";
        this.columnExpressions = columnExpressions;
        this.tables = tables;
        this.finalTable = makeTable();
    }

    // select <column expressions> from <tables> where <conditions>
    public Select(ColumnExpression[] columnExpressions, Table[] tables, ConditionalStatement[] conditionalStatements)
            throws Exception {
        this.selectType = "With Conditions";
        this.columnExpressions = columnExpressions;
        this.tables = tables;
        this.conditionalStatements = conditionalStatements;
        this.finalTable = makeTable();
    }

    public Table makeTable() throws Exception {
        if (selectType.equals("No Conditions")) {
            return makeTableNoConditions();
        } else {
            return makeTableWithConditions();
        }
    }

    public Table makeTableNoConditions() throws Exception {
        if (columnExpressions.length == 1) {
            return makeTableNCSingle();
        } else {
            return makeTableNC();
        }
    }

    public Table makeTableNCSingle() throws Exception {
        ColumnExpression singleColumnExpression = columnExpressions[0];
        if (singleColumnExpression.getExpressionType().equals("*")) {
            Table newTable = Table.join(tables);
            return newTable;
        } else if (singleColumnExpression.getExpressionType().equals("<column>")) {
            String columnNameNoType = singleColumnExpression.getSingleColumnName();
            Table tableToSelectFrom = Table.join(tables);
            ArrayList<Column> tableToSelectFromColumns = tableToSelectFrom.getColumns();
            String columnName = "";
            for (int i = 0; i < tableToSelectFromColumns.size(); i = i + 1) {
                Column column = tableToSelectFromColumns.get(i);
                String thisColumnName = column.getName();
                if (thisColumnName.equals(columnNameNoType)) {
                    columnName = column.getColumnName();
                    break;
                }
            }
            if (columnName.equals("")) {
                throw new IllegalArgumentException("No such column: " + columnNameNoType +
                        " in the table being selected from.");
            }
            Table newTable = new Table(new String[]{columnName});
            for (int i = 0; i < tableToSelectFrom.getNumRows(); i = i + 1) {
                newTable.addRow(new Value[]{tableToSelectFrom.getRow(i).get(columnName)});
            }
            return newTable;
        } else if (singleColumnExpression.getExpressionType().equals
                ("<column0> <arithmetic operator> <column1> as <alias>")) {
            String column0NameNoType = singleColumnExpression.getFirstColumnName();
            Operator operator = singleColumnExpression.getOperator();
            String column1NameNoType = singleColumnExpression.getSecondColumnName();
            String alias = singleColumnExpression.getAlias();
            Select[] selects = new Select[2];
            selects[0] =  new Select(new ColumnExpression[]{new ColumnExpression(column0NameNoType)}, tables);
            selects[1] =  new Select(new ColumnExpression[]{new ColumnExpression(column1NameNoType)}, tables);
            Column column0 = selects[0].getTable().getColumn(0);
            Column column1 = selects[1].getTable().getColumn(0);
            Value[] valuesToAdd = new Value[column0.getSize()];
            for (int i = 0; i < column0.getSize(); i = i + 1) {
                Value currentValue0 = column0.get(i);
                Value currentValue1 = column1.get(i);
                Value newValue;
                try {
                    if (currentValue0.getStrRepr().equals("NaN") || currentValue1.getStrRepr().equals("NaN")) {
                        newValue = new Value("NaN");
                        if (currentValue0.getType().equals("int") && currentValue1.getType().equals("int")) {
                            newValue.setType("int");
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("float")) {
                            newValue.setType("float");
                        } else if (currentValue0.getType().equals("int") && currentValue1.getType().equals("float")) {
                            newValue.setType("float");
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("int")) {
                            newValue.setType("float");
                        } else if (currentValue0.getType().equals("string") && currentValue1.getType().equals("string")) {
                            newValue.setType("string");
                        } else {
                            throw new IllegalArgumentException("Incompatible types: " + currentValue0.getType()
                                    + " and " + currentValue1.getType());
                        }
                    } else if (currentValue0.getStrRepr().equals("NOVALUE") && currentValue1.getStrRepr().equals("NOVALUE")) {
                        newValue = new Value("NOVALUE");
                        if (currentValue0.getType().equals("int") && currentValue1.getType().equals("int")) {
                            newValue.setType("int");
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("float")) {
                            newValue.setType("float");
                        } else if (currentValue0.getType().equals("int") && currentValue1.getType().equals("float")) {
                            newValue.setType("float");
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("int")) {
                            newValue.setType("float");
                        } else if (currentValue0.getType().equals("string") && currentValue1.getType().equals("string")) {
                            newValue.setType("string");
                        } else {
                            throw new IllegalArgumentException("Incompatible types: " + currentValue0.getType()
                                    + " and " + currentValue1.getType());
                        }
                    } else if (currentValue0.getStrRepr().equals("NOVALUE")) {
                        if (currentValue0.getType().equals("int") && currentValue1.getType().equals("int")) {
                            int second = new Integer(currentValue1.getStrRepr());
                            newValue = new Value(operator.apply(0, second));
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("float")) {
                            float second = new Float(currentValue1.getStrRepr());
                            newValue = new Value(operator.apply(new Float("0.000"), second));
                        } else if (currentValue0.getType().equals("int") && currentValue1.getType().equals("float")) {
                            float second = new Float(currentValue1.getStrRepr());
                            newValue = new Value(operator.apply(0, second));
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("int")) {
                            int second = new Integer(currentValue1.getStrRepr());
                            newValue = new Value(operator.apply(new Float("0.000"), second));
                        } else if (currentValue0.getType().equals("string") && currentValue1.getType().equals("string")) {
                            String second = currentValue1.getStrRepr();
                            newValue = new Value(operator.apply("''", second));
                        } else {
                            throw new IllegalArgumentException("Incompatible types: " + currentValue0.getType()
                                    + " and " + currentValue1.getType());
                        }
                    } else if (currentValue1.getStrRepr().equals("NOVALUE")) {
                        if (currentValue0.getType().equals("int") && currentValue1.getType().equals("int")) {
                            int first = new Integer(currentValue0.getStrRepr());
                            newValue = new Value(operator.apply(first, 0));
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("float")) {
                            float first = new Float(currentValue0.getStrRepr());
                            newValue = new Value(operator.apply(first, new Float("0.000")));
                        } else if (currentValue0.getType().equals("int") && currentValue1.getType().equals("float")) {
                            int first = new Integer(currentValue0.getStrRepr());
                            newValue = new Value(operator.apply(first, new Float("0.000")));
                        } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("int")) {
                            float first = new Float(currentValue0.getStrRepr());
                            newValue = new Value(operator.apply(first, 0));
                        } else if (currentValue0.getType().equals("string") && currentValue1.getType().equals("string")) {
                            String first = currentValue0.getStrRepr();
                            newValue = new Value(operator.apply(first, "''"));
                        } else {
                            throw new IllegalArgumentException("Incompatible types: " + currentValue0.getType()
                                    + " and " + currentValue1.getType());
                        }
                    } else if (currentValue0.getType().equals("int") && currentValue1.getType().equals("int")) {
                        int first = new Integer(currentValue0.getStrRepr());
                        int second = new Integer(currentValue1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("float")) {
                        float first = new Float(currentValue0.getStrRepr());
                        float second = new Float(currentValue1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (currentValue0.getType().equals("int") && currentValue1.getType().equals("float")) {
                        int first = new Integer(currentValue0.getStrRepr());
                        float second = new Float(currentValue1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (currentValue0.getType().equals("float") && currentValue1.getType().equals("int")) {
                        float first = new Float(currentValue0.getStrRepr());
                        int second = new Integer(currentValue1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (currentValue0.getType().equals("string") && currentValue1.getType().equals("string")) {
                        String first = currentValue0.getStrRepr();
                        String second = currentValue1.getStrRepr();
                        newValue = new Value(operator.apply(first, second));
                    } else {
                        throw new IllegalArgumentException("Incompatible types: " + currentValue0.getType()
                                + " and " + currentValue1.getType());
                    }
                }
                catch (ArithmeticException e) {
                    newValue = new Value("NaN");
                }
                if (newValue.getStrRepr().equals("Infinity")) {
                    newValue = new Value("NaN");
                    newValue.setType("float");
                }
                valuesToAdd[i] = newValue;
            }
            String aliasType = valuesToAdd[0].getType();
            String newColumnName = alias + " " + aliasType;
            Table newTable = new Table(new String[]{newColumnName});
            for (Value value : valuesToAdd) {
                newTable.addRow(new Value[]{value});
            }
            return newTable;
        } else if (singleColumnExpression.getExpressionType().equals
                ("<column0> <arithmetic operator> <value> as <alias>")) {
            String column0NameNoType = singleColumnExpression.getFirstColumnName();
            Operator operator = singleColumnExpression.getOperator();
            Value value1 = singleColumnExpression.getSecondValue();
            String alias = singleColumnExpression.getAlias();
            Select column0Select = new Select(new ColumnExpression[]{new ColumnExpression(column0NameNoType)}, tables);
            Column column0 = column0Select.getTable().getColumn(0);
            Value[] valuesToAdd = new Value[column0.getSize()];
            for (int i = 0; i < column0.getSize(); i = i + 1) {
                Value value0 = column0.get(i);
                Value newValue;
                try {
                    if (value0.getStrRepr().equals("NaN")) {
                        newValue = new Value("NaN");
                        if (value0.getType().equals("int") && value1.getType().equals("int")) {
                            newValue.setType("int");
                        } else if (value0.getType().equals("float") && value1.getType().equals("float")) {
                            newValue.setType("float");
                        } else if (value0.getType().equals("int") && value1.getType().equals("float")) {
                            newValue.setType("float");
                        } else if (value0.getType().equals("float") && value1.getType().equals("int")) {
                            newValue.setType("float");
                        } else if (value0.getType().equals("string") && value1.getType().equals("string")) {
                            newValue.setType("string");
                        } else {
                            throw new IllegalArgumentException("Incompatible types: " + value0.getType()
                                    + " and " + value1.getType());
                        }
                    } else if (value0.getStrRepr().equals("NOVALUE")) {
                        if (value0.getType().equals("int") && value1.getType().equals("int")) {
                            int second = new Integer(value1.getStrRepr());
                            newValue = new Value(operator.apply(0, second));
                        } else if (value0.getType().equals("float") && value1.getType().equals("float")) {
                            float second = new Float(value1.getStrRepr());
                            newValue = new Value(operator.apply(new Float("0.000"), second));
                        } else if (value0.getType().equals("int") && value1.getType().equals("float")) {
                            float second = new Float(value1.getStrRepr());
                            newValue = new Value(operator.apply(0, second));
                        } else if (value0.getType().equals("float") && value1.getType().equals("int")) {
                            int second = new Integer(value1.getStrRepr());
                            newValue = new Value(operator.apply(new Float("0.000"), second));
                        } else if (value0.getType().equals("string") && value1.getType().equals("string")) {
                            String second = value1.getStrRepr();
                            newValue = new Value(operator.apply("''", second));
                        } else {
                            throw new IllegalArgumentException("Incompatible types: " + value0.getType()
                                    + " and " + value1.getType());
                        }
                    } else if (value0.getType().equals("int") && value1.getType().equals("int")) {
                        int first = new Integer(value0.getStrRepr());
                        int second = new Integer(value1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (value0.getType().equals("float") && value1.getType().equals("float")) {
                        float first = new Float(value0.getStrRepr());
                        float second = new Float(value1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (value0.getType().equals("int") && value1.getType().equals("float")) {
                        int first = new Integer(value0.getStrRepr());
                        float second = new Float(value1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (value0.getType().equals("float") && value1.getType().equals("int")) {
                        float first = new Float(value0.getStrRepr());
                        int second = new Integer(value1.getStrRepr());
                        newValue = new Value(operator.apply(first, second));
                    } else if (value0.getType().equals("string") && value1.getType().equals("string")) {
                        String first = value0.getStrRepr();
                        String second = value1.getStrRepr();
                        newValue = new Value(operator.apply(first, second));
                    } else {
                        throw new IllegalArgumentException("Incompatible types: " + value0.getType()
                                + " and " + value1.getType());
                    }
                }
                catch (ArithmeticException e) {
                    newValue = new Value("NaN");
                    newValue.setType(value0.getType());
                }
                if (newValue.getStrRepr().equals("Infinity")) {
                    newValue = new Value("NaN");
                    newValue.setType("float");
                }
                valuesToAdd[i] = newValue;
            }
            String aliasType = valuesToAdd[0].getType();
            String newColumnName = alias + " " + aliasType;
            Table newTable = new Table(new String[]{newColumnName});
            for (Value value : valuesToAdd) {
                newTable.addRow(new Value[]{value});
            }
            return newTable;
        } else {
            throw new IllegalArgumentException("Malformed select statement");
        }
    }

    public Table makeTableNC() throws Exception {
        Select[] selects = new Select[columnExpressions.length];
        Column[] columnsToBeJoined = new Column[selects.length];
        for (int i = 0; i < selects.length; i = i + 1) {
            selects[i] = new Select(new ColumnExpression[]{columnExpressions[i]}, tables);
        }
        for (int i = 0; i < selects.length; i = i + 1) {
            columnsToBeJoined[i] = selects[i].getTable().getColumn(0);
        }
        Table newTable = Table.selectJoin(columnsToBeJoined);
        return newTable;
    }

    public Table makeTableWithConditions() throws Exception {
        Table tableNoConditions;
        if (columnExpressions.length == 1) {
            tableNoConditions = makeTableNCSingle();
        } else {
            tableNoConditions = makeTableNC();
        }
        ArrayList<Column> tableNoConditionsColumns = tableNoConditions.getColumns();
        ArrayList<Table> tablesToBeJoined = new ArrayList<>();
        for (ConditionalStatement conditionalStatement : conditionalStatements) {
            if (conditionalStatement.getConditionType().equals("unary")) {
                String column0NameNoType = conditionalStatement.getFirstColumnName();
                Comparator comparator = conditionalStatement.getComparator();
                Value value1 = conditionalStatement.getSecondValue();
                String columnName = "";
                for (int i = 0; i < tableNoConditionsColumns.size(); i = i + 1) {
                    Column column = tableNoConditionsColumns.get(i);
                    String thisColumnName = column.getName();
                    if (thisColumnName.equals(column0NameNoType)) {
                        columnName = column.getColumnName();
                        break;
                    }
                }
                if (columnName.equals("")) {
                    throw new IllegalArgumentException("No such column: " + column0NameNoType +
                            " in the table being selected from.");
                }
                Table newTable = new Table(tableNoConditions.getColumnNames());
                for (int i = 0; i < tableNoConditions.getNumRows(); i = i + 1) {
                    Value value0 = tableNoConditions.getRow(i).get(columnName);
                    boolean addThisRow;
                    if (value0.getStrRepr().equals("NOVALUE")) {
                        addThisRow = false;
                    } else if (value0.getStrRepr().equals("NaN") && value1.getStrRepr().equals("NaN")) {
                        if (comparator.getComparator().equals("==")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("!=")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("<")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals(">")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("<=")) {
                            addThisRow = true;
                        } else {
                            addThisRow = true;
                        }
                    } else if (value0.getStrRepr().equals("NaN")) {
                        if (comparator.getComparator().equals("==")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("!=")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("<")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals(">")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("<=")) {
                            addThisRow = false;
                        } else {
                            addThisRow = true;
                        }
                    } else if (value1.getStrRepr().equals("NaN")) {
                        if (comparator.getComparator().equals("==")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("!=")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("<")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals(">")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("<=")) {
                            addThisRow = true;
                        } else {
                            addThisRow = false;
                        }
                    } else if (value0.getType().equals("string") && value1.getType().equals("string")) {
                        String first = value0.getStrRepr();
                        String second = value1.getStrRepr();
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("int") && value1.getType().equals("int")) {
                        int first = new Integer(value0.getStrRepr());
                        int second = new Integer(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("float") && value1.getType().equals("float")) {
                        float first = new Float(value0.getStrRepr());
                        float second = new Float(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("int") && value1.getType().equals("float")) {
                        int first = new Integer(value0.getStrRepr());
                        float second = new Float(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("float") && value1.getType().equals("int")) {
                        float first = new Float(value0.getStrRepr());
                        int second = new Integer(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else {
                        throw new IllegalArgumentException("Incompatible types: " + value0.getType()
                                + " and " + value1.getType());
                    }
                    if (addThisRow) {
                        newTable.addRow(tableNoConditions.getRow(i));
                    }
                }
                tablesToBeJoined.add(newTable);
            } else {
                String column0NameNoType = conditionalStatement.getFirstColumnName();
                String column1NameNoType = conditionalStatement.getSecondColumnName();
                Comparator comparator = conditionalStatement.getComparator();
                String columnName0 = "";
                String columnName1 = "";
                for (int i = 0; i < tableNoConditionsColumns.size(); i = i + 1) {
                    Column column = tableNoConditionsColumns.get(i);
                    String thisColumnName = column.getName();
                    if (thisColumnName.equals(column0NameNoType)) {
                        columnName0 = column.getColumnName();
                        break;
                    }
                }
                if (columnName0.equals("")) {
                    throw new IllegalArgumentException("No such column: " + column0NameNoType +
                            " in the table being selected from.");
                }
                for (int i = 0; i < tableNoConditionsColumns.size(); i = i + 1) {
                    Column column = tableNoConditionsColumns.get(i);
                    String thisColumnName = column.getName();
                    if (thisColumnName.equals(column1NameNoType)) {
                        columnName1 = column.getColumnName();
                        break;
                    }
                }
                if (columnName1.equals("")) {
                    throw new IllegalArgumentException("No such column: " + column1NameNoType +
                            " in the table being selected from.");
                }
                Table newTable = new Table(tableNoConditions.getColumnNames());
                for (int i = 0; i < tableNoConditions.getNumRows(); i = i + 1) {
                    Value value0 = tableNoConditions.getRow(i).get(columnName0);
                    Value value1 = tableNoConditions.getRow(i).get(columnName1);
                    boolean addThisRow;
                    if (value0.getStrRepr().equals("NOVALUE") || value1.getStrRepr().equals("NOVALUE")) {
                        addThisRow = false;
                    } else if (value0.getStrRepr().equals("NaN") && value1.getStrRepr().equals("NaN")) {
                        if (comparator.getComparator().equals("==")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("!=")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("<")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals(">")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("<=")) {
                            addThisRow = true;
                        } else {
                            addThisRow = true;
                        }
                    } else if (value0.getStrRepr().equals("NaN")) {
                        if (comparator.getComparator().equals("==")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("!=")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("<")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals(">")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("<=")) {
                            addThisRow = false;
                        } else {
                            addThisRow = true;
                        }
                    } else if (value1.getStrRepr().equals("NaN")) {
                        if (comparator.getComparator().equals("==")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("!=")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals("<")) {
                            addThisRow = true;
                        } else if (comparator.getComparator().equals(">")) {
                            addThisRow = false;
                        } else if (comparator.getComparator().equals("<=")) {
                            addThisRow = true;
                        } else {
                            addThisRow = false;
                        }
                    } else if (value0.getType().equals("string") && value1.getType().equals("string")) {
                        String first = value0.getStrRepr();
                        String second = value1.getStrRepr();
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("int") && value1.getType().equals("int")) {
                        int first = new Integer(value0.getStrRepr());
                        int second = new Integer(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("float") && value1.getType().equals("float")) {
                        float first = new Float(value0.getStrRepr());
                        float second = new Float(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("int") && value1.getType().equals("float")) {
                        int first = new Integer(value0.getStrRepr());
                        float second = new Float(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else if (value0.getType().equals("float") && value1.getType().equals("int")) {
                        float first = new Float(value0.getStrRepr());
                        int second = new Integer(value1.getStrRepr());
                        addThisRow = comparator.compare(first, second);
                    } else {
                        throw new IllegalArgumentException("Incompatible types: " + value0.getType()
                                + " and " + value1.getType());
                    }
                    if (addThisRow) {
                        newTable.addRow(tableNoConditions.getRow(i));
                    }
                }
                tablesToBeJoined.add(newTable);
            }
        }
        Table[] tablesToBeJoinedArray = new Table[tablesToBeJoined.size()];
        for (int i = 0; i < tablesToBeJoinedArray.length; i = i + 1) {
            tablesToBeJoinedArray[i] = tablesToBeJoined.get(i);
        }

        String[][] tablesToBeJoinedColumnNames = new String[tablesToBeJoinedArray.length][];
        for (int i = 0; i < tablesToBeJoinedColumnNames.length; i = i + 1) {
            tablesToBeJoinedColumnNames[i] = tablesToBeJoinedArray[i].getColumnNames();
        }

        String[] columnNames = tablesToBeJoinedColumnNames[0];
        for (String[] columnNamesChecked : tablesToBeJoinedColumnNames) {
            if (!Arrays.equals(columnNames, columnNamesChecked)) {
                return Table.join(tablesToBeJoinedArray);
            }
        }
        return Table.conditionJoin(tablesToBeJoinedArray);
    }

    public Table getTable() {
        return finalTable;
    }

    public String printTable() {
        return finalTable.print();
    }

    public String getSelectType() {
        return selectType;
    }

    public static void main(String[] args) throws Exception {
        Table T1 = new Table(new String[]{"x int", "y int"});
        T1.addRow(new Value[]{new Value(2), new Value(5)});
        T1.addRow(new Value[]{new Value(8), new Value(3)});
        T1.addRow(new Value[]{new Value(13), new Value(7)});
        System.out.println(T1.print());

        Table T2 = new Table(new String[]{"x int", "z int"});
        T2.addRow(new Value[]{new Value(2), new Value(4)});
        T2.addRow(new Value[]{new Value(8), new Value(9)});
        T2.addRow(new Value[]{new Value(10), new Value(1)});
        System.out.println(T2.print());

        ColumnExpression star = new ColumnExpression("*");
        ColumnExpression[] testExpressions = new ColumnExpression[]{star};
        Table[] testTables = new Table[]{T1, T2};

        Select testSelect = new Select(testExpressions, testTables);
        Table T3 = Table.join(testTables);

        System.out.println(T3.print());
        System.out.println(testSelect.printTable());

        assertEquals(T3, testSelect.getTable());
    }
}
