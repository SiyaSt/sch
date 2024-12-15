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

    // Function to get the current token
    private Token currentToken() {
        return tokens.get(pos);
    }

    // Function to consume a token of a specific type
    private void eat(Token.Type type) {
        if (currentToken().type == type) {
            pos++;
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken());
        }
    }

    private Object parseOperand() {
        Object operand = currentToken().value;
        eat(currentToken().type); // Consume the operand token (NUMBER or IDENTIFIER)
        return operand;
    }

    // Function to parse a single assignment
    private List<Instruction> parseAssignment(String varName) {
        List<Instruction> instructions = new ArrayList<>();
        eat(Token.Type.EQUAL);

        if (currentToken().type == Token.Type.NEW) {
            eat(Token.Type.NEW);
            eat(Token.Type.LEFT_BRACKET);
            Object amount = currentToken().value;
            eat(Token.Type.NUMBER);
            eat(Token.Type.RIGHT_BRACKET);
            instructions.add(new Instruction(Instruction.OpCode.NEW, varName, amount));
        } else if (currentToken().type == Token.Type.NUMBER || currentToken().type == Token.Type.IDENTIFIER) {
            Object operand1 = parseOperand();

            if (currentToken().type == Token.Type.LEFT_BRACKET) {
                eat(Token.Type.LEFT_BRACKET);
                // Изменено: Теперь индекс может быть либо NUMBER, либо IDENTIFIER
                Object index = currentToken().value;
                eat(currentToken().type);
                eat(Token.Type.RIGHT_BRACKET);
                instructions.add(new Instruction(Instruction.OpCode.STORE_ARRAY_VAR, (String)operand1, index, varName));
            }else if (currentToken().type == Token.Type.PLUS) {
                eat(Token.Type.PLUS);
                Object operand2 = parseOperand();
                instructions.add(new Instruction(Instruction.OpCode.ADD, varName, operand1, operand2));
            } else if (currentToken().type == Token.Type.MINUS) {
                eat(Token.Type.MINUS);
                Object operand2 = parseOperand();
                instructions.add(new Instruction(Instruction.OpCode.SUB, varName, operand1, operand2));
            } else if (currentToken().type == Token.Type.STAR) {
                eat(Token.Type.STAR);
                Object operand2 = parseOperand();
                instructions.add(new Instruction(Instruction.OpCode.MUL, varName, operand1, operand2));
            } else if (currentToken().type == Token.Type.LESS) {
                eat(Token.Type.LESS);
                Object operand2 = parseOperand();
                instructions.add(new Instruction(Instruction.OpCode.LESS, varName, operand1, operand2));
            } else if (currentToken().type == Token.Type.GREATER) {
                eat(Token.Type.GREATER);
                Object operand2 = parseOperand();
                instructions.add(new Instruction(Instruction.OpCode.GREATER, varName, operand1, operand2));
            } else if (currentToken().type == Token.Type.EQUALS) {
                eat(Token.Type.EQUALS);
                Object operand2 = parseOperand();
                instructions.add(new Instruction(Instruction.OpCode.EQUALS, varName, operand1, operand2));
            } else if (currentToken().type == Token.Type.NOT_EQUALS) {
                eat(Token.Type.NOT_EQUALS);
                Object operand2 = parseOperand();
                instructions.add(new Instruction(Instruction.OpCode.NOT_EQUALS, varName, operand1, operand2));
            } else if (currentToken().type == Token.Type.CALL_FUN_OPEN) {
                eat(Token.Type.CALL_FUN_OPEN);
                String functionName = (String) operand1; // Используем operand1 как имя функции
                List<Object> arguments = new ArrayList<>();
                while (currentToken().type != Token.Type.CALL_FUN_CLOSE) {
                    if (currentToken().type == Token.Type.NUMBER || currentToken().type == Token.Type.IDENTIFIER) {
                        arguments.add(currentToken().value);
                        eat(currentToken().type);
                    } else if (currentToken().type == Token.Type.COMMA) {
                        eat(Token.Type.COMMA); // Пропускаем запятые между аргументами
                    }
                }
                eat(Token.Type.CALL_FUN_CLOSE);
                instructions.add(new Instruction(Instruction.OpCode.CALL, functionName, arguments, varName));
            }else {
                instructions.add(new Instruction(Instruction.OpCode.STORE, varName, operand1));
            }
        } else {
            throw new RuntimeException("Invalid expression after '='");
        }

        eat(Token.Type.SEMICOLON);
        return instructions;
    }
    // Function to parse a single assignment with array index
    private List<Instruction> parseArrayAssignment(String varName) {
        List<Instruction> instructions = new ArrayList<>();
        eat(Token.Type.LEFT_BRACKET);
        // Изменено: Теперь индекс может быть либо NUMBER, либо IDENTIFIER
        Object index = currentToken().value;
        eat(currentToken().type);
        eat(Token.Type.RIGHT_BRACKET);
        eat(Token.Type.EQUAL);
        String value = currentToken().value;
        eat(Token.Type.NUMBER);
        instructions.add(new Instruction(Instruction.OpCode.WRITE_INDEX, varName, index, value));
        eat(Token.Type.SEMICOLON);
        return instructions;
    }
    // Function to parse a print statement
    private List<Instruction> parsePrintStatement() {
        List<Instruction> instructions = new ArrayList<>();
        eat(Token.Type.PRINT);
        eat(Token.Type.LEFT_BRACKET);
        String varName = currentToken().value;
        eat(Token.Type.IDENTIFIER);
        if (currentToken().type == Token.Type.LEFT_BRACKET) {
            eat(Token.Type.LEFT_BRACKET);
            // Изменено: Теперь индекс может быть либо NUMBER, либо IDENTIFIER
            String index = currentToken().value;
            eat(currentToken().type);
            eat(Token.Type.RIGHT_BRACKET);
            eat(Token.Type.RIGHT_BRACKET);
            eat(Token.Type.SEMICOLON);
            instructions.add(new Instruction(Instruction.OpCode.READ_INDEX, varName, index));
            return instructions;
        }

        eat(Token.Type.RIGHT_BRACKET);
        eat(Token.Type.SEMICOLON);
        instructions.add(new Instruction(Instruction.OpCode.PRINT, varName));
        return instructions;
    }

    // Function to parse a conditional (if) block
    private List<Instruction> parseConditionalStatement() {
        List<Instruction> instructions = new ArrayList<>();
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
            case RETURN -> Instruction.OpCode.RETURN;
            default -> throw new RuntimeException("Unsupported comparison operator: " + comparisonType);
        };

        instructions.add(new Instruction(
                Instruction.OpCode.IF,
                conditionOperand1.toString(),
                comparisonOpCode,
                conditionOperand2,
                blockInstructions
        ));
        return instructions;
    }

    // Function to parse a loop
    private List<Instruction> parseLoopStatement() {
        List<Instruction> instructions = new ArrayList<>();
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
        return instructions;
    }

    // основной метод парсинга
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

                if (currentToken().type == Token.Type.LEFT_BRACKET) {
                    instructions.addAll(parseArrayAssignment(varName));
                }else {
                    instructions.addAll(parseAssignment(varName));
                }
            } else if (currentToken().type == Token.Type.PRINT) {
                instructions.addAll(parsePrintStatement());
            } else if (currentToken().type == Token.Type.IF) {
                instructions.addAll(parseConditionalStatement());
            } else if (currentToken().type == Token.Type.RETURN) {
                instructions.add(parseReturnStatement());
            }else if (currentToken().type == Token.Type.LOOP) {
                instructions.addAll(parseLoopStatement());
            } else {
                throw new RuntimeException("Unknown statement: " + currentToken());
            }
        }
        return instructions;
    }


    // парсит объявление функции
    private Instruction parseFunctionDeclaration() {
        List<Instruction> instructions = new ArrayList<>();
        eat(Token.Type.FUN);
        String functionName = currentToken().value;
        eat(Token.Type.IDENTIFIER);

        // Parse function parameters
        eat(Token.Type.LEFT_BRACKET);
        List<String> parameters = new ArrayList<>();
        while (currentToken().type != Token.Type.RIGHT_BRACKET) {
            eat(Token.Type.LET);
            parameters.add(currentToken().value);
            eat(Token.Type.IDENTIFIER);
            if (currentToken().type == Token.Type.COMMA) {
                eat(Token.Type.COMMA);
            }
        }
        eat(Token.Type.RIGHT_BRACKET);

        while (currentToken().type != Token.Type.RETURN) {
            if (currentToken().type == Token.Type.LET) {
                eat(Token.Type.LET);
                String varName = currentToken().value;
                eat(Token.Type.IDENTIFIER);

                if (currentToken().type == Token.Type.LEFT_BRACKET) {
                    instructions.addAll(parseArrayAssignment(varName));
                }else {
                    instructions.addAll(parseAssignment(varName));
                }
            } else if (currentToken().type == Token.Type.PRINT) {
                instructions.addAll(parsePrintStatement());
            }else if (currentToken().type == Token.Type.IF) {
                instructions.addAll(parseConditionalStatement());
            } else if (currentToken().type == Token.Type.LOOP) {
                instructions.addAll(parseLoopStatement());
            } else {
                throw new RuntimeException("Unknown statement: " + currentToken());
            }
        }

        instructions.add(parseReturnStatement());

        return Instruction.FunctionInstruction(functionName, parameters, instructions);
    }


    // будет парсить возвращаемые значения
    private Instruction parseReturnStatement() {
        eat(Token.Type.RETURN);

        String returnValue = currentToken().value;
        eat(currentToken().type); // Can be IDENTIFIER or NUMBER

        if (currentToken().type == Token.Type.CALL_FUN_OPEN) {
            eat(Token.Type.CALL_FUN_OPEN);
            List<Object> arguments = new ArrayList<>();

            while (currentToken().type != Token.Type.CALL_FUN_CLOSE) {
                if (currentToken().type == Token.Type.NUMBER || currentToken().type == Token.Type.IDENTIFIER) {
                    arguments.add(currentToken().value);
                    eat(currentToken().type);
                } else if (currentToken().type == Token.Type.COMMA) {
                    eat(Token.Type.COMMA); // Пропускаем запятые между аргументами
                }

            }

            eat(Token.Type.CALL_FUN_CLOSE);

            Instruction instruction = new Instruction(Instruction.OpCode.CALL, returnValue, arguments, null);
            eat(Token.Type.SEMICOLON);
            return new Instruction(Instruction.OpCode.RETURN, null, instruction);
        }

        eat(Token.Type.SEMICOLON);
        return new Instruction(Instruction.OpCode.RETURN, returnValue);
    }
    // Method to parse a single statement
    public List<Instruction> parseSingle() {
        List<Instruction> instructions = new ArrayList<>();
        if (currentToken().type == Token.Type.LET) {
            eat(Token.Type.LET);
            String varName = currentToken().value;
            eat(Token.Type.IDENTIFIER);

            if (currentToken().type == Token.Type.LEFT_BRACKET) {
                instructions.addAll(parseArrayAssignment(varName));
            }else {
                instructions.addAll(parseAssignment(varName));
            }
        } else if (currentToken().type == Token.Type.PRINT) {
            instructions.addAll(parsePrintStatement());
        }else if (currentToken().type == Token.Type.FUN) {
            instructions.add(parseFunctionDeclaration());
        } else if (currentToken().type == Token.Type.RETURN) {
            instructions.add(parseReturnStatement());
        }  else {
            throw new RuntimeException("Unknown statement: " + currentToken());
        }
        return instructions;
    }
}