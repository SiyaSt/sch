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

            if (currentToken().type == Token.Type.FUN) {
                Instruction instruction = parseFunctionDeclaration();
                instructions.add(instruction);
            } else if (currentToken().type == Token.Type.LET) {
                eat(Token.Type.LET);
                String varName = currentToken().value;
                eat(Token.Type.IDENTIFIER);
                eat(Token.Type.EQUAL);


                if (currentToken().type == Token.Type.NUMBER || currentToken().type == Token.Type.IDENTIFIER) {
                    Object operand1 = currentToken().value;
                    eat(currentToken().type);

                     if (currentToken().type == Token.Type.LEFT_BRACKET) {
                         eat(Token.Type.LEFT_BRACKET);
                         String index = currentToken().value;
                         eat(Token.Type.NUMBER);
                         eat(Token.Type.RIGHT_BRACKET);
                         eat(Token.Type.SEMICOLON);
                         instructions.add(new Instruction(Instruction.OpCode.STORE_ARRAY_VAR, (String)operand1, index, varName));
                         continue;
                     }

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
                eat(Token.Type.LEFT_BRACKET);
                String varName = currentToken().value;
                eat(Token.Type.IDENTIFIER);

                if (currentToken().type == Token.Type.LEFT_BRACKET) {
                    eat(Token.Type.LEFT_BRACKET);
                    String index = currentToken().value;
                    eat(Token.Type.NUMBER);
                    eat(Token.Type.RIGHT_BRACKET);
                    eat(Token.Type.RIGHT_BRACKET);
                    eat(Token.Type.SEMICOLON);
                    instructions.add(new Instruction(Instruction.OpCode.READ_INDEX, varName, index));
                    continue;
                }

                eat(Token.Type.RIGHT_BRACKET);
                eat(Token.Type.SEMICOLON);
                instructions.add(new Instruction(Instruction.OpCode.PRINT, varName));
            } else if (currentToken().type == Token.Type.IF) {
                eat(Token.Type.IF);
                eat(Token.Type.LEFT_BRACKET);
                Object conditionOperand1 = currentToken().value;
                eat(currentToken().type);

                Token.Type comparisonType = currentToken().type;
                eat(comparisonType);

                Object conditionOperand2 = currentToken().value;
                eat(currentToken().type);
                eat(Token.Type.RIGHT_BRACKET);

                List<Instruction> blockInstructions = new ArrayList<>();
                eat(Token.Type.LEFT_BRACKET);
                while (currentToken().type != Token.Type.RIGHT_BRACKET) {
                    blockInstructions.addAll(parseSingle());
                }
                eat(Token.Type.RIGHT_BRACKET);

                Instruction.OpCode comparisonOpCode = switch (comparisonType) {
                    case LESS -> Instruction.OpCode.LESS;
                    case GREATER -> Instruction.OpCode.GREATER;
                    case EQUALS -> Instruction.OpCode.EQUALS;
                    case NOT_EQUALS -> Instruction.OpCode.NOT_EQUALS;
                    default -> throw new RuntimeException("Unsupported comparison operator: " + comparisonType);
                };

                instructions.add(new Instruction(
                        Instruction.OpCode.IF,
                        conditionOperand1.toString(),
                        comparisonOpCode,
                        conditionOperand2,
                        blockInstructions
                ));
            } else if (currentToken().type == Token.Type.RETURN) {
                instructions.add(parseReturnStatement());
            } else if (currentToken().type == Token.Type.LOOP) {
                eat(Token.Type.LOOP);
                eat(Token.Type.LEFT_BRACKET);
                Object conditionOperand1 = currentToken().value;
                eat(currentToken().type);

                Token.Type comparisonType = currentToken().type;
                eat(comparisonType);

                Object conditionOperand2 = currentToken().value;
                eat(currentToken().type);
                eat(Token.Type.RIGHT_BRACKET);

                List<Instruction> blockInstructions = new ArrayList<>();
                eat(Token.Type.LEFT_BRACKET);
                while (currentToken().type != Token.Type.RIGHT_BRACKET) {
                    blockInstructions.addAll(parseSingle());
                }
                eat(Token.Type.RIGHT_BRACKET);

                Instruction.OpCode comparisonOpCode = switch (comparisonType) {
                    case LESS -> Instruction.OpCode.LESS;
                    case GREATER -> Instruction.OpCode.GREATER;
                    case EQUALS -> Instruction.OpCode.EQUALS;
                    case NOT_EQUALS -> Instruction.OpCode.NOT_EQUALS;
                    default -> throw new RuntimeException("Unsupported comparison operator: " + comparisonType);
                };

                instructions.add(new Instruction(
                        Instruction.OpCode.LOOP,
                        conditionOperand1.toString(),
                        comparisonOpCode,
                        conditionOperand2,
                        blockInstructions
                ));
            }else {
                throw new RuntimeException("Unknown statement: " + currentToken());
            }
        }
        return instructions;
    }


    // парсит объявление функции
    private Instruction parseFunctionDeclaration() {
        eat(Token.Type.FUN);
        String functionName = currentToken().value;
        eat(Token.Type.IDENTIFIER);

        // Parse function parameters
        eat(Token.Type.LEFT_BRACKET);
        List<String> parameters = new ArrayList<>();
        while (currentToken().type != Token.Type.RIGHT_BRACKET) {
            parameters.add(currentToken().value);
            eat(Token.Type.IDENTIFIER);
            if (currentToken().type == Token.Type.COMMA) {
                eat(Token.Type.COMMA);
            }
        }
        eat(Token.Type.RIGHT_BRACKET);

        return new Instruction(Instruction.OpCode.FUN, functionName, parameters);
    }

    // будет парсить возвращаемые значения
    private Instruction parseReturnStatement() {
        eat(Token.Type.RETURN);
        String returnValue = currentToken().value;
        eat(currentToken().type); // Can be IDENTIFIER or NUMBER
        eat(Token.Type.SEMICOLON);
        return new Instruction(Instruction.OpCode.RETURN, returnValue);
    }

    public List<Instruction> parseSingle() {
        List<Instruction> instructions = new ArrayList<>();
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
            eat(Token.Type.LEFT_BRACKET);
            String varName = currentToken().value;
            eat(Token.Type.IDENTIFIER);
            eat(Token.Type.RIGHT_BRACKET);
            eat(Token.Type.SEMICOLON);
            instructions.add(new Instruction(Instruction.OpCode.PRINT, varName));
        } else if (currentToken().type == Token.Type.FUN) {
            instructions.add(parseFunctionDeclaration());
            // add return
        } else if (currentToken().type == Token.Type.RETURN) {
            instructions.add(parseReturnStatement());
        }  else {
            throw new RuntimeException("Unknown statement: " + currentToken());
        }

        return instructions;
    }
}