package itmo.anastasiya;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class VirtualMachine {
    private final List<Instruction> instructions = new ArrayList<>();
    private final Map<String, Object> variables = new HashMap<>();

    // для функций
    private final Map<String, Instruction> functions = new HashMap<>();
    // для возвращаемых значений
    private final Map<String, Object> functionReturnValues = new HashMap<>();

    public void loadFromFile(String filename) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
            while (in.available() > 0) {
                int opCodeOrdinal = in.readByte(); // Считываем код операции
                Instruction.OpCode opCode = Instruction.OpCode.values()[opCodeOrdinal];


                switch (opCode) {
                    case FUN:
                        String functionName = in.readUTF();
                        int parameterCount = in.readInt();
                        List<String> parameters = new ArrayList<>();
                        for (int i = 0; i < parameterCount; i++) {
                            parameters.add(in.readUTF());
                        }

                        Instruction functionInstruction = new Instruction(
                                Instruction.OpCode.FUN,
                                functionName,
                                parameters
                        );
                        functions.put(functionName, functionInstruction);
                        continue;

                    case RETURN:
                        String returnValue = in.readUTF();
                        Instruction returnInstruction = new Instruction(
                                Instruction.OpCode.RETURN,
                                returnValue
                        );
                        instructions.add(returnInstruction);
                        continue;
                }

                String operand1 = in.readUTF();
                String operand2 = in.readUTF();
                String operand3 = in.readUTF();

                List<Instruction> block = null;
                if (opCode == Instruction.OpCode.IF) {
                    int blockSize = in.readInt();
                    if (blockSize > 0) {
                        block = new ArrayList<>();
                        for (int i = 0; i < blockSize; i++) {
                            int nestedOpCodeOrdinal = in.readByte();
                            Instruction.OpCode nestedOpCode = Instruction.OpCode.values()[nestedOpCodeOrdinal];
                            String nestedOperand1 = in.readUTF();
                            String nestedOperand2 = in.readUTF();
                            String nestedOperand3 = in.readUTF();
                            block.add(new Instruction(
                                    nestedOpCode,
                                    nestedOperand1,
                                    nestedOperand2,
                                    nestedOperand3
                            ));
                        }
                    }
                }

                instructions.add(new Instruction(
                        opCode,
                        operand1,
                        operand2,
                        operand3,
                        block
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
                case IF -> {
                    boolean condition = conditions(instruction);
                    if (condition && instruction.block != null) {
                        run(instruction.block);
                    }
                }
                default -> throw new RuntimeException("Unknown instruction: " + instruction.opCode);
            }
        }
    }

    public void run(List<Instruction> block) {
        for (Instruction instruction : block) {
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
                case IF -> {
                    boolean condition = conditions(instruction);
                    if (condition && instruction.block != null) {
                        run(instruction.block);
                    }
                }

                case FUN -> {
                    // Функции уже загружены в `functions` при чтении файла
                }

                case RETURN -> {
                    functionReturnValues.put(
                            "lastReturn",
                            getOperandValue(instruction.operand1)
                    );
                }
                default -> throw new RuntimeException("Unknown instruction: " + instruction.opCode);
            }
        }
    }

    public boolean conditions(Instruction instruction){
        switch (Token.Type.valueOf((String) instruction.operand2)){
            case LESS -> {
                return getOperandValue(instruction.operand1) < getOperandValue(instruction.operand3);

            }
            case GREATER -> {
                return getOperandValue(instruction.operand1) > getOperandValue(instruction.operand3);

            }
            case EQUALS -> {
                return Objects.equals(getOperandValue(instruction.operand1), getOperandValue(instruction.operand3));

            }
            case NOT_EQUALS -> {
                return  !Objects.equals(getOperandValue(instruction.operand1), getOperandValue(instruction.operand3));

            }
            default -> throw new RuntimeException("Unknown instruction: " + instruction.operand2);
        }
    }
    private int getOperandValue(Object operand) {
        if (operand instanceof Integer) {

            return (int) operand;
        } else if (operand instanceof String varName) {

            if (variables.containsKey(varName)) {
                Object value = variables.get(varName);
                if (value instanceof Integer) {
                    return (int) value;
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
