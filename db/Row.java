package db;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Yusuf on 2/26/2017.
 */
public class Row {
    private final ArrayList<String> colNames;
    private HashMap<String, Value> valueMap;
    private boolean noValues;

    public Row(String[] colNames, Value[] values) {
        valueMap = new HashMap<>();
        this.colNames = new ArrayList<>(Arrays.asList(colNames));
        for (int i = 0; i < colNames.length; i = i + 1) {
            valueMap.put(colNames[i], values[i]);
        }
    }

    public Row(String[] colNames) {
        this.colNames = new ArrayList<>(Arrays.asList(colNames));
        noValues = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Row row = (Row) o;

        if (noValues != row.noValues) return false;
        if (colNames != null ? !colNames.equals(row.colNames) : row.colNames != null) return false;
        return valueMap != null ? valueMap.equals(row.valueMap) : row.valueMap == null;
    }

    public Value get(String columnName) throws Exception {
        if (!valueMap.containsKey(columnName)){
            throw new IllegalArgumentException("No such column in the table being selected from.");
        }
        return valueMap.get(columnName);
    }

    public boolean hasColumn(String columnName) {
        return colNames.contains(columnName);
    }

    public String printColNames() {
        StringBuilder printedColNames = new StringBuilder();
        for (int i = 0; i < colNames.size(); i = i + 1) {
            printedColNames.append(colNames.get(i));
            if (i + 1 != colNames.size()) {
                printedColNames.append(",");
            }
        }
        return printedColNames.toString();
    }

    public boolean hasNoValues() {
        return noValues;
    }

    public String printValues() {
        StringBuilder printedValues = new StringBuilder();
        for (int i = 0; i < valueMap.size(); i = i + 1) {
            Value value = valueMap.get(colNames.get(i));
            String valueString = value.getStrRepr();
            if (!valueString.contains("'") && valueString.contains(".")) {
                Float floatValue = new Float(valueString);
                // String.format from: https://piazza.com/class/ir6ikxxrjtm3j5?cid=2392
                // and https://www.dotnetperls.com/format-java
                valueString = String.format("%.3f", floatValue);
                /*
                Old substring solution that didn't work for huge float numbers.
                int dotIndex = valueString.indexOf(".");
                String valueSubStr = valueString.substring(dotIndex);
                if (valueSubStr.length() > 4) {
                valueSubStr = valueSubStr.substring(0, 4);
                valueString = valueString.substring(0, dotIndex) + valueSubStr;
                } else if (valueSubStr.length() < 4) {
                for (int j = 0; j < 5 - valueSubStr.length(); j = j + 1) {
                valueSubStr = valueSubStr + "0";
                }
                valueString = valueString.substring(0, dotIndex) + valueSubStr;
                } else {
                valueSubStr = valueSubStr;
                }
                */
            }
            printedValues.append(valueString);
            if (i + 1 != valueMap.size()) {
                printedValues.append(",");
            }
        }
        return printedValues.toString();
    }
}
