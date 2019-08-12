package db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.io.File;
import java.io.PrintWriter;

public class Parse {
    private static Database db;
    private static String output;
    private static Table tableToBeCreated;

    public Parse(String s, Database db) {
        this.db = db;
        eval(s);
    }

    // Various common constructs, simplifies parsing.
    private static final String REST  = "\\s*(.*)\\s*",
                                COMMA = "\\s*,\\s*",
                                AND   = "\\s+and\\s+";

    // Stage 1 syntax, contains the command name.
    private static final Pattern CREATE_CMD = Pattern.compile("create table " + REST),
                                 LOAD_CMD   = Pattern.compile("load " + REST),
                                 STORE_CMD  = Pattern.compile("store " + REST),
                                 DROP_CMD   = Pattern.compile("drop table " + REST),
                                 INSERT_CMD = Pattern.compile("insert into " + REST),
                                 PRINT_CMD  = Pattern.compile("print " + REST),
                                 SELECT_CMD = Pattern.compile("select " + REST);

    // Stage 2 syntax, contains the clauses of commands.
    private static final Pattern CREATE_NEW  = Pattern.compile("(\\S+)\\s+\\((\\S+\\s+\\S+\\s*" +
                                               "(?:,\\s*\\S+\\s+\\S+\\s*)*)\\)"),
                                 SELECT_CLS  = Pattern.compile("([^,]+?(?:,[^,]+?)*)\\s+from\\s+" +
                                               "(\\S+\\s*(?:,\\s*\\S+\\s*)*)(?:\\s+where\\s+" +
                                               "([\\w\\s+\\-*/'<>=!.]+?(?:\\s+and\\s+" +
                                               "[\\w\\s+\\-*/'<>=!.]+?)*))?"),
                                 CREATE_SEL  = Pattern.compile("(\\S+)\\s+as select\\s+" +
                                                   SELECT_CLS.pattern()),
                                 INSERT_CLS  = Pattern.compile("(\\S+)\\s+values\\s+(.+?" +
                                               "\\s*(?:,\\s*.+?\\s*)*)");


    public static void parseCaller(String s) {
        eval(s);
    }


    private static void eval(String query) {
        Matcher m;
        if ((m = CREATE_CMD.matcher(query)).matches()) {
             createTable(m.group(1));
        } else if ((m = LOAD_CMD.matcher(query)).matches()) {
             loadTable(m.group(1));
        } else if ((m = STORE_CMD.matcher(query)).matches()) {
             storeTable(m.group(1));
        } else if ((m = DROP_CMD.matcher(query)).matches()) {
             dropTable(m.group(1));
        } else if ((m = INSERT_CMD.matcher(query)).matches()) {
             insertRow(m.group(1));
        } else if ((m = PRINT_CMD.matcher(query)).matches()) {
             printTable(m.group(1));
        } else if ((m = SELECT_CMD.matcher(query)).matches()) {
             select(m.group(1));
        }
        try {
            if (!((m = CREATE_CMD.matcher(query)).matches()) && !((m = LOAD_CMD.matcher(query)).matches()) &&
                    !((m = STORE_CMD.matcher(query)).matches()) && !((m = DROP_CMD.matcher(query)).matches())
                    && !((m = INSERT_CMD.matcher(query)).matches()) && !((m = PRINT_CMD.matcher(query)).matches())
                    && !((m = SELECT_CMD.matcher(query)).matches())) {
                throw new IOException();
            }
        }
        catch (IOException e) {
            output = "ERROR: Malformed query: " + query;
        }
    }

    private static void createTable(String expr) {
        Matcher m;
        if ((m = CREATE_NEW.matcher(expr)).matches()) {
            createNewTable(m.group(1), m.group(2).split(COMMA));
        } else if ((m = CREATE_SEL.matcher(expr)).matches()) {
            createSelectedTable(m.group(1), m.group(2), m.group(3), m.group(4));
        } else {
            try {
                if (!((m = CREATE_NEW.matcher(expr)).matches()) && !((m = CREATE_SEL.matcher(expr)).matches())) {
                    throw new IOException();
                }
            }
            catch (IOException e) {
                output = "ERROR: Malformed create: " + expr;
            }
        }
    }

    private static void createNewTable(String name, String[] cols) {
        try {
            name = name.trim();
            for (int i = 0; i < cols.length; i = i + 1) {
                cols[i] = cols[i].trim().replaceAll(" +", " ");
            }
            db.createTable(name, cols);
            output = "";
        }
        catch (Exception e) {
            output = "ERROR: " + e.getMessage();
        }
    }

    private static void createSelectedTable(String name, String exprs, String tables, String conds) {
        try {
            select(exprs, tables, conds);
            db.createTable(name, tableToBeCreated);
            output = "";
        }
        catch (Exception e) {
            output = "ERROR: " + e.getMessage();
        }
    }

    private static void loadTable(String name) {
        try {
            name = name.trim();
            Load l1 = new Load(name);
            db.loadTable(l1.getTableName(), l1.getLoadedTable());
            output = l1.messageToReturn();
        }
        catch (Exception e) {
            output = "ERROR: Malformed load command";
        }
    }

    private static void storeTable(String name) {
        try {
            name = name.trim();
            PrintWriter writer = new PrintWriter(name + ".tbl", "UTF-8");
            Table toStore = db.getTable(name);
            String separate = "";

            int numberOfColumns = toStore.getNumColumns();
            String[] columnContents = new String[numberOfColumns];
            for (int i = 0; i < toStore.getNumColumns(); i++) {
                columnContents[i] = toStore.getColumn(i).getColumnName();
                if (i != 0) {
                    columnContents[i] = "," + columnContents[i];
                }
            }
            String columnText = String.join(separate, columnContents);
            writer.println(columnText);
            for (int j =0; j <toStore.getNumRows(); j++) {
                writer.println(toStore.getRow(j).printValues());
            }
            writer.close();
            output = "";
        }
        catch (Exception e) {
            output = "ERROR: Unable to store the following table: " + name + ".tbl";
        }
    }

    private static void dropTable(String name) {
        try {
            name = name.trim();
            db.dropTable(name);
            output = "";
        }
        catch (Exception e) {
            output = "ERROR: " + e.getMessage();
        }
    }

    private static void insertRow(String expr) {
        Matcher m = INSERT_CLS.matcher(expr);
        try {
            if (!m.matches()) {
                throw new IOException();
            }
        }
        catch (IOException e) {
            output = "ERROR: Malformed insert: " + expr;
            return;
        }

        insertRow(m.group(1), m.group(2));
    }

    private static void insertRow(String tableName, String values) {
        String[] valuesArray = values.split(",");
        try {
            Value[] valuesToBe = getFormattedValues(valuesArray, tableName);
            try {
                db.getTable(tableName).insertInto(valuesToBe);
                output = "";
            } catch (Exception e) {
                output = "ERROR: " + e.getMessage();
            }
        }
        catch (IllegalArgumentException e) {
            output = "ERROR: " + e.getMessage();
        }
        catch (Exception e) {
            output = "ERROR: Row does not match table (mismatching type(s))";
        }
    }

    private static Value[] getFormattedValues(String[] values, String tableName) throws Exception {
        Value[] valuesToBe = new Value[values.length];
        Table table = db.getTable(tableName);
        for (int i = 0; i < values.length; i = i + 1) {
            String value = values[i];
            value = value.trim();
            if (value.contains("'")) {
                Value newValue = new Value(value);
                valuesToBe[i] = newValue;
            } else if (value.contains(".")) {
                Float tempFloat = new Float(value);
                Value newValue = new Value(tempFloat);
                valuesToBe[i] = newValue;
            } else if (value.equals("NOVALUE")) {
                Value NOVALUE = new Value("NOVALUE");
                NOVALUE.setType(table.getColumnNames()[i].split(" ")[1]);
                valuesToBe[i] = NOVALUE;
            } else {
                Integer tempInt = new Integer(value);
                Value newValue = new Value(tempInt);
                valuesToBe[i] = newValue;
            }
        }
        return valuesToBe;
    }

    private static void printTable(String name) {
        try {
            name = name.trim();
            output = db.getTable(name).print();
        } catch (Exception e) {
            output = "ERROR: " + e.getMessage();
        }
    }

    private static void select(String expr) {
        Matcher m = SELECT_CLS.matcher(expr);

        try {
            if ((!m.matches())) {
                throw new IOException();
            }
        }
        catch (IOException e) {
            output = "ERROR: Malformed select: " + expr;
        }

        try {
            select(m.group(1), m.group(2), m.group(3));
        }
        catch (Exception e) {
            output = "ERROR: Malformed select: " + expr;
        }
    }

    private static void select(String exprs, String tables, String conds) throws Exception {
        exprs = exprs.replaceAll(" +", " ");
        tables = tables.replaceAll(" +", "");
        if (conds == null) {
            selectNoConditions(exprs, tables);
        } else {
            conds = conds.replaceAll(" +", " ");
            selectWithConditions(exprs, tables, conds);
        }
    }

    private static void selectNoConditions(String exprs, String tables) throws Exception {
        String[] expressions = exprs.split(",");
        ArrayList<ColumnExpression> columnExpressions = new ArrayList<>();
        for (String expression : expressions) {
            if (expression.contains("+") || expression.contains("-") ||
                    (expression.contains("*") && expression.length() > 1) || expression.contains("/")) {
                int operatorIndex;
                String operatorString;
                if (expression.contains("+")) {
                    operatorIndex = expression.indexOf("+");
                    operatorString = "+";
                } else if (expression.contains("-")) {
                    operatorIndex = expression.indexOf("-");
                    operatorString = "-";
                } else if (expression.contains("*")) {
                    operatorIndex = expression.indexOf("*");
                    operatorString = "*";
                } else {
                    operatorIndex = expression.indexOf("/");
                    operatorString = "/";
                }
                String[] columnExpression = new String[4];
                columnExpression[0] = expression.substring(0, operatorIndex).trim();
                columnExpression[1] = operatorString;
                String rest = expression.substring(operatorIndex + 1).trim();
                columnExpression[2] = rest.split(" ")[0];
                columnExpression[3] = rest.split(" ")[2];
                String column0 = columnExpression[0];
                Operator operator = new Operator(columnExpression[1]);
                String operand1 = columnExpression[2];
                Value operand1Literal;
                String operand1Type;
                String alias = columnExpression[3];

                if (operand1.contains("'")) {
                    operand1Literal = new Value(operand1);
                    operand1Type = "value";
                } else if (operand1.contains(".")) {
                    operand1Literal = new Value(new Float(operand1));
                    operand1Type = "value";
                } else if (Character.isDigit(operand1.charAt(0))) {
                    operand1Literal = new Value(new Integer(operand1));
                    operand1Type = "value";
                } else {
                    operand1Literal = new Value(operand1);
                    operand1Type = "column name";
                }
                if (operand1Type.equals("value")) {
                    columnExpressions.add(new ColumnExpression(column0, operator, operand1Literal, alias));
                } else {
                    columnExpressions.add(new ColumnExpression(column0, operator, operand1, alias));
                }
            } else {
                columnExpressions.add(new ColumnExpression(expression.trim()));
            }
        }
        ColumnExpression[] arrayColumnExpressions = new ColumnExpression[columnExpressions.size()];
        for (int i = 0; i < columnExpressions.size(); i++) {
            arrayColumnExpressions[i] = columnExpressions.get(i);
        }
        String[] tableNames = tables.split(",");
        for (int i = 0; i < tableNames.length; i = i + 1) {
            tableNames[i] = tableNames[i].trim();
        }
        ArrayList<Table> selectedTables = new ArrayList<>();
        for (String tableName : tableNames) {
            try {
                selectedTables.add(db.getTable(tableName));
            } catch (Exception e) {
                output = "ERROR: " + e.getMessage();
                return;
            }
        }
        Table[] arrayTables = new Table[selectedTables.size()];
        for (int i = 0; i < selectedTables.size(); i++) {
            arrayTables[i] = selectedTables.get(i);
        }
        try {
            Select newSelect = new Select(arrayColumnExpressions, arrayTables);
            Table createdTable = newSelect.getTable();
            tableToBeCreated = createdTable;
            output = createdTable.print();
        }
        catch (Exception e) {
            output = "ERROR: " + e.getMessage();
        }
    }

    private static void selectWithConditions(String exprs, String tables, String conds) throws Exception {
        String[] expressions = exprs.split(",");
        ArrayList<ColumnExpression> columnExpressions = new ArrayList<>();
        for (String expression : expressions) {
            if (expression.contains("+") || expression.contains("-") ||
                    (expression.contains("*") && expression.length() > 1) || expression.contains("/")) {
                int operatorIndex;
                String operatorString;
                if (expression.contains("+")) {
                    operatorIndex = expression.indexOf("+");
                    operatorString = "+";
                } else if (expression.contains("-")) {
                    operatorIndex = expression.indexOf("-");
                    operatorString = "-";
                } else if (expression.contains("*")) {
                    operatorIndex = expression.indexOf("*");
                    operatorString = "*";
                } else {
                    operatorIndex = expression.indexOf("/");
                    operatorString = "/";
                }
                String[] columnExpression = new String[4];
                columnExpression[0] = expression.substring(0, operatorIndex).trim();
                columnExpression[1] = operatorString;
                String rest = expression.substring(operatorIndex + 1).trim();
                columnExpression[2] = rest.split(" ")[0];
                columnExpression[3] = rest.split(" ")[2];
                String column0 = columnExpression[0];
                Operator operator = new Operator(columnExpression[1]);
                String operand1 = columnExpression[2];
                Value operand1Literal;
                String operand1Type;
                String alias = columnExpression[3];
                if (operand1.contains("'")) {
                    operand1Literal = new Value(operand1);
                    operand1Type = "value";
                } else if (operand1.contains(".")) {
                    operand1Literal = new Value(new Float(operand1));
                    operand1Type = "value";
                } else if (Character.isDigit(operand1.charAt(0))) {
                    operand1Literal = new Value(new Integer(operand1));
                    operand1Type = "value";
                } else {
                    operand1Literal = new Value(operand1);
                    operand1Type = "column name";
                }
                if (operand1Type.equals("value")) {
                    columnExpressions.add(new ColumnExpression(column0, operator, operand1Literal, alias));
                } else {
                    columnExpressions.add(new ColumnExpression(column0, operator, operand1, alias));
                }
            } else {
                columnExpressions.add(new ColumnExpression(expression.trim()));
            }
        }
        ColumnExpression[] arrayColumnExpressions = new ColumnExpression[columnExpressions.size()];
        for (int i = 0; i < columnExpressions.size(); i++) {
            arrayColumnExpressions[i] = columnExpressions.get(i);
        }
        String[] tableNames = tables.split(",");
        for (int i = 0; i < tableNames.length; i = i + 1) {
            tableNames[i] = tableNames[i].trim();
        }
        ArrayList<Table> selectedTables = new ArrayList<>();
        for (String tableName : tableNames) {
            try {
                selectedTables.add(db.getTable(tableName));
            } catch (Exception e) {
                output = "ERROR: " + e.getMessage();
                return;
            }
        }
        Table[] arrayTables = new Table[selectedTables.size()];
        for (int i = 0; i < selectedTables.size(); i++) {
            arrayTables[i] = selectedTables.get(i);
        }

        String[] conditionalStatementsStrings = conds.split(" and ");
        ConditionalStatement[] conditionalStatements = new ConditionalStatement[conditionalStatementsStrings.length];
        for (int i = 0; i < conditionalStatements.length; i = i + 1) {
            String currentConditionalStatement = conditionalStatementsStrings[i];
            String[] conditionalStatementSplit = new String[3];
            if (currentConditionalStatement.contains("==")) {
                int comparatorIndex = currentConditionalStatement.indexOf("==");
                conditionalStatementSplit[0] = currentConditionalStatement.substring(0, comparatorIndex).trim();
                conditionalStatementSplit[1] = "==";
                conditionalStatementSplit[2] = currentConditionalStatement.substring(comparatorIndex + 2).trim();
            } else if (currentConditionalStatement.contains("!=")) {
                int comparatorIndex = currentConditionalStatement.indexOf("!=");
                conditionalStatementSplit[0] = currentConditionalStatement.substring(0, comparatorIndex).trim();
                conditionalStatementSplit[1] = "!=";
                conditionalStatementSplit[2] = currentConditionalStatement.substring(comparatorIndex + 2).trim();
            } else if (currentConditionalStatement.contains("<=")) {
                int comparatorIndex = currentConditionalStatement.indexOf("<=");
                conditionalStatementSplit[0] = currentConditionalStatement.substring(0, comparatorIndex).trim();
                conditionalStatementSplit[1] = "<=";
                conditionalStatementSplit[2] = currentConditionalStatement.substring(comparatorIndex + 2).trim();
            } else if (currentConditionalStatement.contains(">=")) {
                int comparatorIndex = currentConditionalStatement.indexOf(">=");
                conditionalStatementSplit[0] = currentConditionalStatement.substring(0, comparatorIndex).trim();
                conditionalStatementSplit[1] = ">=";
                conditionalStatementSplit[2] = currentConditionalStatement.substring(comparatorIndex + 2).trim();
            } else if (currentConditionalStatement.contains("<")) {
                int comparatorIndex = currentConditionalStatement.indexOf("<");
                conditionalStatementSplit[0] = currentConditionalStatement.substring(0, comparatorIndex).trim();
                conditionalStatementSplit[1] = "<";
                conditionalStatementSplit[2] = currentConditionalStatement.substring(comparatorIndex + 1).trim();
            } else if (currentConditionalStatement.contains(">")) {
                int comparatorIndex = currentConditionalStatement.indexOf(">");
                conditionalStatementSplit[0] = currentConditionalStatement.substring(0, comparatorIndex).trim();
                conditionalStatementSplit[1] = ">";
                conditionalStatementSplit[2] = currentConditionalStatement.substring(comparatorIndex + 1).trim();
            }
            String column0 = conditionalStatementSplit[0];
            Comparator comparator = new Comparator(conditionalStatementSplit[1]);
            String operand1 = conditionalStatementSplit[2];
            Value operand1Literal;
            String operand1Type;
            if (operand1.contains("'")) {
                operand1Literal = new Value(operand1);
                operand1Type = "value";
            } else if (operand1.contains(".")) {
                operand1Literal = new Value(new Float(operand1));
                operand1Type = "value";
            } else if (Character.isDigit(operand1.charAt(0))) {
                operand1Literal = new Value(new Integer(operand1));
                operand1Type = "value";
            } else {
                operand1Literal = new Value(operand1);
                operand1Type = "column name";
            }
            if (operand1Type.equals("value")) {
                conditionalStatements[i] = new ConditionalStatement(column0, comparator, operand1Literal);
            } else {
                conditionalStatements[i] = new ConditionalStatement(column0, comparator, operand1);
            }
        }

        try {
            Select newSelect = new Select(arrayColumnExpressions, arrayTables, conditionalStatements);
            Table createdTable = newSelect.getTable();
            tableToBeCreated = createdTable;
            output = createdTable.print();
        }
        catch (Exception e) {
            output = "ERROR: " + e.getMessage();
        }
    }

    public String getOutput() {
        return output;
    }

}
