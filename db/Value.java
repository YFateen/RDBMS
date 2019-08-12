package db;

/**
 * Created by Yusuf on 2/26/2017.
 */
public class Value {
    private Object value;
    private String type;
    private String strRepr;

    public Value(String str) {
        value = str;
        if (str.equals("NaN")) {
            type = "NaN";
            strRepr = "NaN";
        } else if (str.equals("NOVALUE")) {
            type = "NOVALUE";
            strRepr = "NOVALUE";
        } else if (str.contains("'")) {
            type = "string";
            strRepr = str;
        } else {
            type = "column name";
            strRepr = str;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public Value(int integer) {
        value = integer;
        type = "int";
        strRepr = Integer.toString(integer);
    }

    public Value(float f) {
        value = f;
        type = "float";
        strRepr = Float.toString(f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Value value1 = (Value) o;

        if (value != null ? !value.equals(value1.value) : value1.value != null) return false;
        if (type != null ? !type.equals(value1.type) : value1.type != null) return false;
        return strRepr != null ? strRepr.equals(value1.strRepr) : value1.strRepr == null;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getStrRepr() {
        return strRepr;
    }
}
