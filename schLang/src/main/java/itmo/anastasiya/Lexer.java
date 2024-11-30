package itmo.anastasiya;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String input;
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
                if (id.equals("let")) {
                    tokens.add(new Token(Token.Type.LET, id));
                } else if (id.equals("print")) {
                    tokens.add(new Token(Token.Type.PRINT, id));
                } else {
                    tokens.add(new Token(Token.Type.IDENTIFIER, id));
                }
            } else if (currentChar == '=') {
                tokens.add(new Token(Token.Type.EQUAL, "="));
                advance();
            } else if (currentChar == '+') {
                tokens.add(new Token(Token.Type.PLUS, "+"));
                advance();
            } else if (currentChar == ';') {
                tokens.add(new Token(Token.Type.SEMICOLON, ";"));
                advance();
            } else if (currentChar == '[') {
                    tokens.add(new Token(Token.Type.LEFTBRACKET, "["));
                    advance();
            } else if (currentChar == ']') {
                    tokens.add(new Token(Token.Type.RIGHTBRACKET, "]"));
                    advance();
            } else if (currentChar == ',') {
                    tokens.add(new Token(Token.Type.COMMA, ","));
                    advance();
            } else {
                throw new RuntimeException("Unexpected character: " + currentChar);
            }
        }
        return tokens;
    }
}
