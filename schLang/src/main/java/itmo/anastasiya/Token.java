package itmo.anastasiya;

class Token {
    public enum Type {
        LET, IDENTIFIER, NUMBER, EQUAL, PLUS, PRINT, SEMICOLON
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
