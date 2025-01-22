package itmo.anastasiya;

class Token {
    public enum Type {
        LET, IDENTIFIER, NUMBER, EQUAL, PLUS, PRINT, SEMICOLON,
        LEFT_BRACKET, RIGHT_BRACKET, COMMA, MINUS, STAR, LESS, GREATER,
        EQUALS, NOT_EQUALS, IF, LOOP, FUN, RETURN, NEW, WRITE_INDEX, STORE_ARRAY_VAR, CALL_FUN_OPEN, CALL_FUN_CLOSE, MOD,
        BITWISE_SHIFT
        }

    public Type type;
    public String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Token{" + "type=" + type + ", value='" + value + '\'' + '}';
    }
}
