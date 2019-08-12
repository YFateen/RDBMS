package db;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Parts of join method may be shared between both ICTs (code for joining two tables).
 * (Allowed per http://datastructur.es/sp17/materials/lab/lab5/lab5.html#between-now-and-lab-6)
 */
public class Table {
    private final ArrayList<Column> columns;
    private String[] columnNames;
    private final int numColumns;
    private final ArrayList<Row> rows;
    private boolean emptyRows;

    public Table(String[] names) throws Exception {
        for (String name : names) {
            if (Character.isDigit(name.charAt(0))) {
                throw new IllegalArgumentException("Table names must start with a letter");
            }
        }
        columnNames = names;
        numColumns = names.length;
        columns = new ArrayList<>();
        rows = new ArrayList<>();
        for (int i = 0; i < numColumns; i += 1) {
            columns.add(new Column(names[i]));
        }
        emptyRows = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Table table = (Table) o;

        if (numColumns != table.numColumns) return false;
        if (columns != null ? !columns.equals(table.columns) : table.columns != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(columnNames, table.columnNames)) return false;
        return rows != null ? rows.equals(table.rows) : table.rows == null;
    }

    public void addRow(Value[] input) {
        for (int i = 0; i < numColumns; i += 1) {
            columns.get(i).addItem(input[i]);
        }
        rows.add(new Row(columnNames, input));
        emptyRows = false;
    }

    public boolean hasEmptyRows() {
        return emptyRows;
    }

    // May not be useful
    public void addRow(Row input) throws Exception {
        for (int i = 0; i < numColumns; i += 1) {
            columns.get(i).addItem(input.get(columnNames[i]));
        }
        rows.add(input);
        emptyRows = false;
    }

    public int getNumRows() {
        return rows.size();
    }

    public Row getRow(int index) {
        return rows.get(index);
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public ArrayList<Column> getColumns() { return columns; }

    public int getNumColumns() {
        return numColumns;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    // Find mutual column names between t1 and t2.
    private static ArrayList<String> getMutualColumnNames(Table t1, Table t2) {
        ArrayList<String> mutualColumns = new ArrayList<>(t1.getNumColumns());

        for (int i = 0; i < t1.numColumns; i += 1) {
            String name1 = t1.columnNames[i];
            for (int j = 0; j < t2.numColumns; j += 1) {
                String name2 = t2.columnNames[j];
                if (!mutualColumns.contains(name1) && name1.equals(name2)) {
                    mutualColumns.add(name1);
                }
            }
        }
        return mutualColumns;
    }

    // Find mutual column names between t1 and t2.
    private String[] getUniqueColumnNames(ArrayList mutualColumns) {
        String[] uniqueColumns = new String[this.getNumColumns()];
        int length = 0;
        for (String name : this.getColumnNames()) {
            if (!mutualColumns.contains(name)) {
                uniqueColumns[length] = name;
                length += 1;
            }
        }
        String[] newUniqueColumns = new String[length];
        System.arraycopy(uniqueColumns, 0, newUniqueColumns, 0, newUniqueColumns.length);
        return newUniqueColumns;
    }

    // Return a table t3 that is the join of two tables t1 and t2.
    private static Table joinTwo(Table t1, Table t2) throws Exception {

        ArrayList<String> mutualColumns = getMutualColumnNames(t1, t2);

        if (mutualColumns.size() == 0) {
            return joinCartesian(t1, t2);
        }

        String[] mutualAsArray = new String[mutualColumns.size()];
        for (int i = 0; i < mutualAsArray.length; i = i + 1) {
            mutualAsArray[i] = mutualColumns.get(i);
        }
        String[] t1UniqueColumns = t1.getUniqueColumnNames(mutualColumns);
        String[] t2UniqueColumns = t2.getUniqueColumnNames(mutualColumns);

        // Create combined list of all new column names.
        String[] newNames = new String[mutualAsArray.length + t1UniqueColumns.length + t2UniqueColumns.length];
        System.arraycopy(mutualAsArray,0, newNames, 0, mutualAsArray.length);
        System.arraycopy(t1UniqueColumns, 0, newNames, mutualAsArray.length, t1UniqueColumns.length);
        System.arraycopy(t2UniqueColumns, 0, newNames, mutualAsArray.length + t1UniqueColumns.length,
                t2UniqueColumns.length);

        Table t3 = new Table(newNames);

        // Iterate through rows to check if they should be joined.
        for (int i = 0; i < t1.getNumRows(); i += 1) {
            Row row1 = t1.getRow(i);
            for (int j = 0; j < t2.getNumRows(); j += 1) {
                Row row2 = t2.getRow(j);
                boolean join = true;
                for (String name : mutualColumns) {
                    if (!row1.get(name).equals(row2.get(name))) {
                        join = false;
                        break;
                    }
                }
                // If the rows should be joined, sorts items from the two rows to create a new row in proper order.
                ArrayList<Value> newItems = new ArrayList<>();
                if (join) {
                    for (String name : newNames) {
                        if (row1.hasColumn(name)) {
                            newItems.add(row1.get(name));
                        } else {
                            newItems.add(row2.get(name));
                        }
                    }
                    // Converts ArrayList to Value[] array
                    Value[] newItemsArray = new Value[newItems.size()];
                    for (int v = 0; v < newItems.size(); v = v + 1) {
                        newItemsArray[v] = newItems.get(v);
                    }
                    t3.addRow(newItemsArray);
                }
            }
        }
        return t3;
    }

    // Join between two tables with no mutual columns.
    private static Table joinCartesian(Table t1, Table t2) throws Exception {
        // Create combined list of all new column names.
        String[] t1ColumnNames = t1.getColumnNames();
        String[] t2ColumnNames = t2.getColumnNames();
        String[] newNames = new String[t1ColumnNames.length + t2ColumnNames.length];
        System.arraycopy(t1ColumnNames, 0, newNames, 0, t1ColumnNames.length);
        System.arraycopy(t2ColumnNames, 0, newNames, t1ColumnNames.length, t2ColumnNames.length);

        Table t3 = new Table(newNames);

        for (int i = 0; i < t1.getNumRows(); i += 1) {
            Row row1 = t1.getRow(i);
            for (int j = 0; j < t2.getNumRows(); j += 1) {
                Row row2 = t2.getRow(j);

                // Sorts items from the two rows to create a new row in proper order.
                ArrayList<Value> newItems = new ArrayList<>();
                for (String name : newNames) {
                    if (row1.hasColumn(name)) {
                        newItems.add(row1.get(name));
                    } else {
                        newItems.add(row2.get(name));
                    }
                }
                // Converts ArrayList to Value[] array
                Value[] newItemsArray = new Value[newItems.size()];
                for (int v = 0; v < newItems.size(); v = v + 1) {
                    newItemsArray[v] = newItems.get(v);
                }
                t3.addRow(newItemsArray);
            }
        }
        return t3;
    }

    // Join together all the tables in an array.
    public static Table join(Table[] tables) throws Exception {
        Table mainTable = tables[0];
        for (int i = 1; i < tables.length; i = i + 1) {
            Table joinedTable = joinTwo(mainTable, tables[i]);
            mainTable = joinedTable;
        }
        return mainTable;
    }

    // StringBuilder from: https://piazza.com/class/ir6ikxxrjtm3j5?cid=2336
    public String print() {
        StringBuilder printed = new StringBuilder();
        if (!hasEmptyRows()) {
            printed.append(rows.get(0).printColNames());
            for (Row row : rows) {
                printed.append("\n").append(row.printValues());
            }
        } else {
            for (int i = 0; i < columnNames.length; i = i + 1) {
                printed.append(columnNames[i]);
                if (i + 1 != columnNames.length) {
                    printed.append(",");
                }
            }
        }
        return printed.toString();
    }

    public void insertInto(Value[] input) throws Exception {
        if (input.length != numColumns) {
            throw new IllegalArgumentException("Row does not match table (different size)");
        }
        for (int i = 0; i < input.length; i = i + 1) {
            Value ithInput = input[i];
            Column ithColumn = columns.get(i);
            if (!ithInput.getType().equals(ithColumn.getType())) {
                throw new IllegalArgumentException("Row does not match table (mismatching type(s))");
            }
        }
        addRow(input);
    }

    // Creates a table with the columns in the tables in order.
    public static Table selectJoin(Column[] columns) throws Exception {
        String[] columnNames = new String[columns.length];
        for (int i = 0; i < columns.length; i = i + 1) {
            columnNames[i] = columns[i].getColumnName();
        }
        Table newTable = new Table(columnNames);
        int numRows = columns[0].getSize();
        for (int i = 0; i < numRows; i = i + 1) {
            Value[] rowToBeAdded = new Value[newTable.getNumColumns()];
            for (int j = 0; j < rowToBeAdded.length; j = j + 1) {
                rowToBeAdded[j] = columns[j].get(i);
            }
            newTable.addRow(rowToBeAdded);
        }
        return newTable;
    }

    public static Table conditionJoin(Table[] tables) throws Exception {
        Table mainTable = tables[0];
        for (int i = 1; i < tables.length; i = i + 1) {
            Table joinedTable = conditionJoinTwo(mainTable, tables[i]);
            mainTable = joinedTable;
        }
        return mainTable;
    }

    // Return a table that includes every row in t2 that is also in t1.
    public static Table conditionJoinTwo(Table t1, Table t2) throws Exception {
        ArrayList<Row> t1Rows = t1.getRows();
        ArrayList<Row> t2Rows = t2.getRows();
        ArrayList<Row> newRows = new ArrayList<>();
        for (Row row2 : t2Rows) {
            for (Row row1 : t1Rows) {
                if (row2.equals(row1)) {
                    newRows.add(row2);
                    break;
                }
            }
        }
        Table newTable = new Table(t2.getColumnNames());
        for (Row row : newRows) {
            newTable.addRow(row);
        }
        return newTable;
    }

    public static void main(String[] args) throws Exception {
//        Table T1 = new Table(new String[]{"x int", "y int"});
//        T1.addRow(new Value[]{new Value(2), new Value(5)});
//        T1.addRow(new Value[]{new Value(8), new Value(3)});
//        T1.addRow(new Value[]{new Value(13), new Value(7)});
//        System.out.println(T1.print());
//
//        Table T2 = new Table(new String[]{"x int", "z int"});
//        T2.addRow(new Value[]{new Value(2), new Value(4)});
//        T2.addRow(new Value[]{new Value(8), new Value(9)});
//        T2.addRow(new Value[]{new Value(10), new Value(1)});
//        System.out.println(T2.print());
//
//        Table T3Expected = new Table(new String[]{"x int", "y int", "z int"});
//        T3Expected.addRow(new Value[]{new Value(2), new Value(5), new Value(4)});
//        T3Expected.addRow(new Value[]{new Value(8), new Value(3), new Value(9)});
//        Table T3 = joinTwo(T1, T2);
//        System.out.println(T3.print());
//
//        assertEquals(T3Expected, T3);
//
//        Table T4 = new Table(new String[]{"a int", "b int"});
//        T4.addRow(new Value[]{new Value(7), new Value(0)});
//        T4.addRow(new Value[]{new Value(2), new Value(8)});
//
//        Table T5Expected = new Table(new String[]{"x int", "y int", "z int", "a int", "b int"});
//        T5Expected.addRow(new Value[]{new Value(2), new Value(5), new Value(4),
//                new Value(7), new Value(0)});
//        T5Expected.addRow(new Value[]{new Value(2), new Value(5), new Value(4),
//                new Value(2), new Value(8)});
//        T5Expected.addRow(new Value[]{new Value(8), new Value(3), new Value(9),
//                new Value(7), new Value(0)});
//        T5Expected.addRow(new Value[]{new Value(8), new Value(3), new Value(9),
//                new Value(2), new Value(8)});
//        T5Expected.print();
//        Table T5 = joinTwo(T3, T4);
//        System.out.println(T5.print());
//
//        assertEquals(T5Expected, T5);
//
//        Table T6 = join(new Table[]{T1, T2, T4});
//        System.out.println(T6.print());
//        assertEquals(T5, T6);
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

        Table T3 = join(new Table[]{T1, T2});
        System.out.println(T3.print());
    }
}
