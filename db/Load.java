package db;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.IllegalFormatException;

/**
 * Created by Yusuf on 2/28/17.
 */
public class Load {
    private String tableName;
    private Table loadedTable;
    private static String toReturn;

    public Load(String name) {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(name + ".tbl"));
            String header = input.readLine();
            if (header == null) {
                throw new IOException();
            }
            String[] columnNames = header.split(",");
            for (int i = 0; i < columnNames.length; i = i + 1) {
                columnNames[i] = columnNames[i].trim().replaceAll(" +", " ");
                if (!columnNames[i].contains(" ")) {
                    throw new IllegalArgumentException("Malformed column declaration: " + columnNames[i]);
                }
            }
            Table t1 = new Table(columnNames);
            String line = input.readLine();
            while (line != null) {
                String[] rowElements = line.split(",");
                Value[] valuesToBe = new Value[rowElements.length];
                for (int i = 0; i < rowElements.length; i++) {
                    String rowItem = rowElements[i];
                    String columnType = columnNames[i].split(" ")[1];
                    rowItem = rowItem.trim();
                    if (rowItem.contains("'")) {
                        if (!columnType.equals("string")) {
                            throw new IllegalArgumentException("Row does not match table");
                        }
                        Value rowItemValue = new Value(rowItem);
                        valuesToBe[i] = rowItemValue;
                    } else if (rowItem.contains(".")) {
                        if (!columnType.equals("float")) {
                            throw new IllegalArgumentException("Row does not match table");
                        }
                        Float tempFloat = new Float(rowItem);
                        Value floatValue = new Value(tempFloat);
                        valuesToBe[i] = floatValue;
                    } else if (rowItem.equals("NaN")) {
                        Value NaNValue = new Value("NaN");
                        NaNValue.setType(columnType);
                        valuesToBe[i] = NaNValue;
                    } else if (rowItem.equals("NOVALUE")) {
                        Value NOVALUE = new Value("NOVALUE");
                        NOVALUE.setType(columnType);
                        valuesToBe[i] = NOVALUE;
                    } else {
                        if (!columnType.equals("int")) {
                            throw new IllegalArgumentException("Row does not match table");
                        }
                        Integer tempInt = new Integer(rowItem);
                        Value intValue = new Value(tempInt);
                        valuesToBe[i] = intValue;
                    }
                }
                if (valuesToBe.length != columnNames.length) {
                    throw new IllegalArgumentException("Row does not match table");
                }
                t1.addRow(valuesToBe);
                line = input.readLine();
            }
            tableName = name;
            loadedTable = t1;
            toReturn = "";
        }
        catch (FileNotFoundException e) {
            toReturn = "ERROR: TBL file not found: " + name + ".tbl";
        }
        catch (IOException e) {
            toReturn = "ERROR: Problem reading from " + name + ".tbl";
        }
        catch (IllegalArgumentException e) {
            toReturn = "ERROR: " + e.getMessage();
        }
        catch (Exception e) {
            toReturn = "ERROR: Table " + name + ".tbl is malformed.";
        }
    }

    public String getTableName() {
        return tableName;
    }

    public Table getLoadedTable() {
        return loadedTable;
    }

    public String messageToReturn() {
        return toReturn;
    }
}
