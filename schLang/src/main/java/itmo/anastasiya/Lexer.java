package itmo.anastasiya;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos;
    private char currentChar;

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
        this.currentChar = input.charAt(pos);
    }

    private void advance() {
        pos++;
        currentChar = pos < input.length() ? input.charAt(pos) : '\0';
    }

    private char peekNext() {
        return pos + 1 < input.length() ? input.charAt(pos + 1) : '\0';
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private String collectNumber() {
        StringBuilder number = new StringBuilder();
        while (Character.isDigit(currentChar)) {
            number.append(currentChar);
            advance();
        }
        return number.toString();
    }

    private String collectIdentifier() {
        StringBuilder identifier = new StringBuilder();
        while (Character.isLetterOrDigit(currentChar)) {
            identifier.append(currentChar);
            advance();
        }
        return identifier.toString();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
            } else if (Character.isDigit(currentChar)) {
                tokens.add(new Token(Token.Type.NUMBER, collectNumber()));
            } else if (Character.isLetter(currentChar)) {
                String id = collectIdentifier();
                switch (id) {
                    case "let" -> tokens.add(new Token(Token.Type.LET, id));
                    case "print" -> tokens.add(new Token(Token.Type.PRINT, id));
                    case "if" -> tokens.add(new Token(Token.Type.IF, id));
                    case "loop" -> tokens.add(new Token(Token.Type.LOOP, id));
                    case "fun"  -> tokens.add(new Token(Token.Type.FUN, id));
                    case "return" -> tokens.add(new Token(Token.Type.RETURN, id));
                    case "new" -> tokens.add(new Token(Token.Type.NEW, id));
                    default -> tokens.add(new Token(Token.Type.IDENTIFIER, id));
                }
            } else if (currentChar == '=') {
                if (peekNext() == '=') {
                    tokens.add(new Token(Token.Type.EQUALS, "=="));
                    advance();
                } else {
                    tokens.add(new Token(Token.Type.EQUAL, "="));
                }
                advance();
            } else if (currentChar == '+') {
                tokens.add(new Token(Token.Type.PLUS, "+"));
                advance();
            } else if (currentChar == '-') {
                tokens.add(new Token(Token.Type.MINUS, "-"));
                advance();
            } else if (currentChar == '*') {
                tokens.add(new Token(Token.Type.STAR, "*"));
                advance();
            } else if (currentChar == '<') {
                tokens.add(new Token(Token.Type.LESS, "<"));
                advance();
            } else if (currentChar == '>') {
                tokens.add(new Token(Token.Type.GREATER, ">"));
                advance();
            } else if (currentChar == ';') {
                tokens.add(new Token(Token.Type.SEMICOLON, ";"));
                advance();
            } else if (currentChar == '[') {
                tokens.add(new Token(Token.Type.LEFT_BRACKET, "["));
                advance();
            } else if (currentChar == ']') {
                tokens.add(new Token(Token.Type.RIGHT_BRACKET, "]"));
                advance();
            } else if (currentChar == '(') {
                tokens.add(new Token(Token.Type.CALL_FUN_OPEN, "("));
                advance();
            } else if (currentChar == ')') {
                tokens.add(new Token(Token.Type.CALL_FUN_CLOSE, ")"));
                advance();
            } else if (currentChar == ',') {
                tokens.add(new Token(Token.Type.COMMA, ","));
                advance();
            } else if (currentChar == '!') {
                if (peekNext() == '=') {
                    tokens.add(new Token(Token.Type.NOT_EQUALS, "!="));
                    advance();
                    advance();
                } else {
                    throw new RuntimeException("Unexpected character: !");
                }
            } else {
                throw new RuntimeException("Unexpected character: " + currentChar);
            }
        }
        return tokens;
    }
}
