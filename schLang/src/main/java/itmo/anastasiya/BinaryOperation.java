package itmo.anastasiya;

public record BinaryOperation(Object left, Token operator, Object right) {

    @Override
    public String toString() {
        return "(" + left + " " + operator.value + " " + right + ")";
    }
}
