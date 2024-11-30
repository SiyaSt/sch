package itmo.anastasiya;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    private Token currentToken() {
        return tokens.get(pos);
    }

    private void eat(Token.Type type) {
        if (currentToken().type == type) {
            pos++;
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken());
        }
    }

    public List<Instruction> parse() {
        List<Instruction> instructions = new ArrayList<>();
        while (pos < tokens.size()) {
            if (currentToken().type == Token.Type.LET) {
                eat(Token.Type.LET);
                String varName = currentToken().value;
                eat(Token.Type.IDENTIFIER);
                eat(Token.Type.EQUAL);

                if (currentToken().type == Token.Type.LEFTBRACKET) {
                    eat(Token.Type.LEFTBRACKET);
                    List<Integer> values = new ArrayList<>();
                    while (currentToken().type != Token.Type.RIGHTBRACKET) {
                        values.add(Integer.parseInt(currentToken().value));
                        eat(Token.Type.NUMBER);
                        if (currentToken().type == Token.Type.COMMA) {
                            eat(Token.Type.COMMA);
                        }
                    }
                    eat(Token.Type.RIGHTBRACKET);
                    eat(Token.Type.SEMICOLON);
                    instructions.add(new Instruction(Instruction.OpCode.ARRAY, varName, values));
                } else {
                    int value = Integer.parseInt(currentToken().value);
                    eat(Token.Type.NUMBER);
                    eat(Token.Type.SEMICOLON);
                    instructions.add(new Instruction(Instruction.OpCode.STORE, varName, value));
                }

            } else if (currentToken().type == Token.Type.PRINT) {
                eat(Token.Type.PRINT);
                String varName = currentToken().value;
                eat(Token.Type.IDENTIFIER);
                eat(Token.Type.SEMICOLON);
                instructions.add(new Instruction(Instruction.OpCode.PRINT, varName));
            } else {
                throw new RuntimeException("Unknown statement: " + currentToken());
            }
        }
        return instructions;
    }
}
