package itmo.anastasiya;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static java.lang.Character.isDigit;

public class VirtualMachine {
    private final List<Instruction> instructions = new ArrayList<>();
    private final Map<String, Object> variables = new HashMap<>();

    public void loadFromFile(String filename) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
            while (in.available() > 0) {
                int opCodeOrdinal = in.readByte(); // Считываем код операции
                Instruction.OpCode opCode = Instruction.OpCode.values()[opCodeOrdinal];

                String operand1 = in.readUTF();
                String operand2 = in.readUTF();
                String operand3 = in.readUTF();

                instructions.add(new Instruction(
                        opCode,
                        operand1,
                        operand2,
                        operand3
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading bytecode from file: " + e.getMessage());
        }
    }


    public void run() {
        for (Instruction instruction : instructions) {
            switch (instruction.opCode) {
                case STORE -> variables.put(instruction.operand1, instruction.operand2);
                case PRINT -> {
                    if (variables.containsKey(instruction.operand1)) {
                        System.out.println(variables.get(instruction.operand1));
                    } else {
                        throw new RuntimeException("Variable not found: " + instruction.operand1);
                    }
                }
                case ARRAY -> {
                    String varName = instruction.operand1;
                    @SuppressWarnings("unchecked")
                    List<Integer> values = (List<Integer>) instruction.operand2;
                    variables.put(varName, values);
                }
                case ADD -> {
                    int result = getOperandValue(instruction.operand2) + getOperandValue(instruction.operand3);
                    variables.put(instruction.operand1, result);
                }
                case SUB -> {
                    int result = getOperandValue(instruction.operand2) - getOperandValue(instruction.operand3);
                    variables.put(instruction.operand1, result);
                }
                case MUL -> {
                    int result = getOperandValue(instruction.operand2) * getOperandValue(instruction.operand3);
                    variables.put(instruction.operand1, result);
                }
                case LESS -> {
                    boolean result = getOperandValue(instruction.operand2) < getOperandValue(instruction.operand3);
                    variables.put(instruction.operand1, result);
                }
                case GREATER -> {
                    boolean result = getOperandValue(instruction.operand2) > getOperandValue(instruction.operand3);
                    variables.put(instruction.operand1, result);
                }
                case EQUALS -> {
                    boolean result = Objects.equals(getOperandValue(instruction.operand2), getOperandValue(instruction.operand3));
                    variables.put(instruction.operand1, result);
                }
                case NOT_EQUALS -> {
                    boolean result = !Objects.equals(getOperandValue(instruction.operand2), getOperandValue(instruction.operand3));
                    variables.put(instruction.operand1, result);
                }

                default -> throw new RuntimeException("Unknown instruction: " + instruction.opCode);
            }
        }
    }

    private int getOperandValue(Object operand) {
        if (operand instanceof Integer) {
            // Если операнд — это уже число
            return (int) operand;
        } else if (operand instanceof String varName) {
            // Если это строка, проверяем две ситуации
            if (variables.containsKey(varName)) {
                Object value = variables.get(varName);
                if (value instanceof Integer) {
                    return (int) value; // Переменная — целое число
                } else if (value instanceof String) {
                    try {
                        return Integer.parseInt((String) value); // Преобразуем строку в число
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Variable " + varName + " is not a valid number: " + value);
                    }
                } else {
                    throw new RuntimeException("Variable " + varName + " has unsupported type: " + value.getClass());
                }
            } else {
                try {
                    // Если переменной с таким именем нет, проверяем: это может быть число в виде строки
                    return Integer.parseInt(varName);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Variable not found or invalid literal: " + operand);
                }
            }
        } else {
            throw new RuntimeException("Invalid operand type: " + operand);
        }
    }


}
