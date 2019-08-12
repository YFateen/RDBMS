package db;

/**
 * Created by Yusuf on 2/26/2017.
 */
public class Operator {
    private final String operator;

    public Operator(String operator) throws Exception {
        if (!operator.equals("+") && !operator.equals("-") && !operator.equals("*") && !operator.equals("/")) {
            throw new IllegalArgumentException("Operation not supported. Can only use +, -, *, and /");
        } else {
            this.operator = operator;
        }
    }

    // int + int
    public int apply(int first, int second) {
        if (operator.equals("+")) {
            return first + second;
        } else if (operator.equals("-")) {
            return first - second;
        } else if (operator.equals("*")) {
            return first * second;
        } else {
            return first / second;
        }
    }

    // float + float
    public float apply(float first, float second) {
        if (operator.equals("+")) {
            return first + second;
        } else if (operator.equals("-")) {
            return first - second;
        } else if (operator.equals("*")) {
            return first * second;
        } else {
            return first / second;
        }
    }

    // int + float
    public float apply(int first, float second) {
        if (operator.equals("+")) {
            return first + second;
        } else if (operator.equals("-")) {
            return first - second;
        } else if (operator.equals("*")) {
            return first * second;
        } else {
            return first / second;
        }
    }

    // float + int
    public float apply(float first, int second) throws Exception {
        if (operator.equals("+")) {
            return first + second;
        } else if (operator.equals("-")) {
            return first - second;
        } else if (operator.equals("*")) {
            return first * second;
        } else {
            return first / second;
        }
    }

    // string + string
    public String apply(String first, String second) throws Exception {
        if (operator.equals("+")) {
            return first.substring(0, first.length() - 1) + second.substring(1);
        } else {
            throw new IllegalArgumentException("Operation not supported. Can only concatenate Strings using +");
        }
    }
}
