package db;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Database {
    private final HashMap<String, Table> tableMap;
    private static final String EXIT   = "exit";
    private static final String PROMPT = "> ";

    public Database() {
        // YOUR CODE HERE
        tableMap = new HashMap<>();
    }

    public void createTable(String tableName, String[] columnNames) throws Exception {
        if (tableMap.containsKey(tableName)) {
            throw new IllegalArgumentException
            ("Table " + tableName + " already exists in database");
        } else {
            Table newTable = new Table(columnNames);
            tableMap.put(tableName, newTable);
        }
    }

    public void createTable(String tableName, Table table) {
        if (tableMap.containsKey(tableName)) {
            throw new IllegalArgumentException
            ("Table " + tableName + " already exists in database");
        } else {
            tableMap.put(tableName, table);
        }
    }

    public void loadTable(String tableName, Table newTable) {
        tableMap.put(tableName, newTable);
    }

    public Table getTable(String tableName) throws Exception {
        if (!tableMap.containsKey(tableName)) {
            throw new IllegalArgumentException
            ("Table " + tableName + " does not exist in database");
        } else {
            return tableMap.get(tableName);
        }
    }

    public void dropTable(String tableName) throws Exception {
        if (!tableMap.containsKey(tableName)) {
            throw new
            IllegalArgumentException("Table " + tableName + " does not exist in database");
        } else {
            tableMap.remove(tableName);
        }
    }

    public void insertInto(String tableName, Value[] values) throws Exception {
        if (!tableMap.containsKey(tableName)) {
            throw new IllegalArgumentException
            ("Table " + tableName + " does not exist in database");
        } else {
            tableMap.get(tableName).addRow(values);
        }
    }

    public void printTable(String tableName) throws Exception {
        if (!tableMap.containsKey(tableName)) {
            throw new IllegalArgumentException
            ("Table " + tableName + " does not exist in database");
        } else {
            tableMap.get(tableName).print();
        }
    }

    public String transact(String query) {
        Parse p = new Parse(query, this);
        return p.getOutput();
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Database db = new Database();
        System.out.print(PROMPT);

        String line = "";
        while ((line = in.readLine()) != null) {
            if (EXIT.equals(line)) {
                break;
            }

            if (!line.trim().isEmpty()) {
                String result = db.transact(line);
                if (result.length() > 0) {
                    System.out.println(result);
                }
            }
            System.out.print(PROMPT);
        }

        in.close();
    }
}
