package db;
import java.util.ArrayList;

/**
 * Created by Yusuf on 2/22/2017.
 */
public class Column {
    private final ArrayList<Value> items;
    private final String columnName;
    private final String name;
    private final String type;
    private int size;

    public Column(String name) {
        items = new ArrayList<>();
        columnName = name;
        this.name = name.split(" ")[0];
        type = name.split(" ")[1];
        if (!type.equals("int") && !type.equals("float") && !type.equals("string")) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        this.size = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        if (size != column.size) return false;
        if (items != null ? !items.equals(column.items) : column.items != null) return false;
        if (columnName != null ? !columnName.equals(column.columnName) : column.columnName != null) return false;
        if (name != null ? !name.equals(column.name) : column.name != null) return false;
        return type != null ? type.equals(column.type) : column.type == null;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return items.size();
    }

    public void addItem(Value x) {
        items.add(x);
        size = size + 1;
    }

    public String getType() {
        return type;
    }

    public String getColumnName() { return columnName; }

    public Value get(int index) {
        return items.get(index);
    }

}
