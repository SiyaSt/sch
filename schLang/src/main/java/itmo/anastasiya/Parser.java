package itmo.anastasiya;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
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


                if (currentToken().type == Token.Type.NUMBER || currentToken().type == Token.Type.IDENTIFIER) {
                    Object operand1 = currentToken().value;
                    eat(currentToken().type);


                    if (currentToken().type == Token.Type.PLUS) {
                        eat(Token.Type.PLUS);
                        Object operand2 = currentToken().value;
                        eat(currentToken().type);
                        instructions.add(new Instruction(Instruction.OpCode.ADD, varName, operand1, operand2));
                    } else if (currentToken().type == Token.Type.MINUS) {
                        eat(Token.Type.MINUS);
                        Object operand2 = currentToken().value;
                        eat(currentToken().type);
                        instructions.add(new Instruction(Instruction.OpCode.SUB, varName, operand1, operand2));
                    } else if (currentToken().type == Token.Type.STAR) {
                        eat(Token.Type.STAR);
                        Object operand2 = currentToken().value;
                        eat(currentToken().type);
                        instructions.add(new Instruction(Instruction.OpCode.MUL, varName, operand1, operand2));
                    } else if (currentToken().type == Token.Type.LESS) {
                        eat(Token.Type.LESS);
                        Object operand2 = currentToken().value;
                        eat(currentToken().type);
                        instructions.add(new Instruction(Instruction.OpCode.LESS, varName, operand1, operand2));
                    } else if (currentToken().type == Token.Type.GREATER) {
                        eat(Token.Type.GREATER);
                        Object operand2 = currentToken().value;
                        eat(currentToken().type);
                        instructions.add(new Instruction(Instruction.OpCode.GREATER, varName, operand1, operand2));
                    } else if (currentToken().type == Token.Type.EQUALS) {
                        eat(Token.Type.EQUALS);
                        Object operand2 = currentToken().value;
                        eat(currentToken().type);
                        instructions.add(new Instruction(Instruction.OpCode.EQUALS, varName, operand1, operand2));
                    } else if (currentToken().type == Token.Type.NOT_EQUALS) {
                        eat(Token.Type.NOT_EQUALS);
                        Object operand2 = currentToken().value;
                        eat(currentToken().type);
                        instructions.add(new Instruction(Instruction.OpCode.NOT_EQUALS, varName, operand1, operand2));
                    } else {

                        instructions.add(new Instruction(Instruction.OpCode.STORE, varName, operand1));
                    }
                } else {

                    throw new RuntimeException("Invalid expression after '='");
                }

                eat(Token.Type.SEMICOLON);
            } else if (currentToken().type == Token.Type.PRINT) {
                eat(Token.Type.PRINT);
                eat(Token.Type.LEFTBRACKET);
                String varName = currentToken().value;
                eat(Token.Type.IDENTIFIER);
                eat(Token.Type.RIGHTBRACKET);
                eat(Token.Type.SEMICOLON);
                instructions.add(new Instruction(Instruction.OpCode.PRINT, varName));
            } else {
                throw new RuntimeException("Unknown statement: " + currentToken());
            }
        }
        return instructions;
    }
}
