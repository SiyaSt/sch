package itmo.anastasiya;

class Token {
    public enum Type {
        LET, IDENTIFIER, NUMBER, EQUAL, PLUS, PRINT, SEMICOLON,
        LEFTBRACKET, RIGHTBRACKET, COMMA, MINUS, STAR, LESS, GREATER,
        EQUALS, NOT_EQUALS, FUN, RETURN,
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
