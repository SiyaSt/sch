package itmo.anastasiya;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


public class VirtualMachine {
    private final List<Instruction> instructions = new ArrayList<>();
    private final MemoryManager memoryManager = new MemoryManager();

    private final Map<String, Instruction> functions = new HashMap<>();
    private boolean isReturning = false;

    public void loadFromFile(String filename) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
            while (in.available() > 0) {
                int opCodeOrdinal = in.readByte(); // Считываем код операции
                Instruction.OpCode opCode = Instruction.OpCode.values()[opCodeOrdinal];

                switch (opCode) {
                    case FUN -> {
                        String functionName = in.readUTF();
                        int parameterCount = in.readInt();
                        List<String> parameters = new ArrayList<>();
                        for (int i = 0; i < parameterCount; i++) {
                            parameters.add(in.readUTF());
                        }

                        List<Instruction> block = null;
                        int blockSize = in.readInt();
                        if (blockSize > 0) {
                            block = new ArrayList<>();
                            for (int i = 0; i < blockSize; i++) {
                                int nestedOpCodeOrdinal = in.readByte();
                                Instruction.OpCode nestedOpCode = Instruction.OpCode.values()[nestedOpCodeOrdinal];
                                if (nestedOpCode == Instruction.OpCode.RETURN) {
                                    String returnValue = in.readUTF();

                                    Instruction instruction = null;
                                    if (returnValue.isEmpty()) {
                                        nestedOpCodeOrdinal = in.readByte();
                                        nestedOpCode = Instruction.OpCode.values()[nestedOpCodeOrdinal];
                                        String nestedOperand1 = in.readUTF();
                                        String nestedOperand2 = in.readUTF();
                                        String nestedOperand3 = in.readUTF();
                                        instruction = new Instruction(nestedOpCode, nestedOperand1, nestedOperand2, nestedOperand3);
                                    }

                                    block.add(new Instruction(Instruction.OpCode.RETURN, returnValue, instruction));
                                } else if (nestedOpCode == Instruction.OpCode.IF || nestedOpCode == Instruction.OpCode.LOOP) {
                                    String operand1 = in.readUTF();
                                    String operand2 = in.readUTF();
                                    String operand3 = in.readUTF();
                                    List<Instruction> nestedBlock = readNestedBlock(in);
                                    block.add(new Instruction(nestedOpCode, operand1, operand2, operand3, nestedBlock));

                                } else {
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

                        Instruction functionInstruction = Instruction.FunctionInstruction(functionName, parameters, block);

                        functions.put(functionName, functionInstruction);
                        continue;
                    }
                    case RETURN -> {
                        String returnValue = in.readUTF();
                        instructions.add(new Instruction(Instruction.OpCode.RETURN, returnValue));
                        continue;
                    }
                }

                String operand1 = in.readUTF();
                String operand2 = in.readUTF();
                String operand3 = in.readUTF();

                List<Instruction> block = null;
                if (opCode == Instruction.OpCode.IF || opCode == Instruction.OpCode.LOOP) {
                    block = readNestedBlock(in);
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

    public List<Instruction> readNestedBlock(DataInputStream in) throws IOException {
        List<Instruction> block = new ArrayList<>();
        int blockSize = in.readInt();
        if (blockSize > 0) {
            block = new ArrayList<>();
            for (int i = 0; i < blockSize; i++) {
                int nestedOpCodeOrdinal = in.readByte();
                Instruction.OpCode nestedOpCode = Instruction.OpCode.values()[nestedOpCodeOrdinal];
                if (nestedOpCode == Instruction.OpCode.RETURN) {
                    String returnValue = in.readUTF();

                    Instruction instruction = null;
                    if (returnValue.isEmpty()) {
                        nestedOpCodeOrdinal = in.readByte();
                        nestedOpCode = Instruction.OpCode.values()[nestedOpCodeOrdinal];
                        String nestedOperand1 = in.readUTF();
                        String nestedOperand2 = in.readUTF();
                        String nestedOperand3 = in.readUTF();
                        instruction = new Instruction(nestedOpCode, nestedOperand1, nestedOperand2, nestedOperand3);
                    }

                    block.add(new Instruction(Instruction.OpCode.RETURN, returnValue, instruction));
                } else if (nestedOpCode == Instruction.OpCode.IF || nestedOpCode == Instruction.OpCode.LOOP) {
                    String operand1 = in.readUTF();
                    String operand2 = in.readUTF();
                    String operand3 = in.readUTF();
                    List<Instruction> nestedBlock = readNestedBlock(in);
                    block.add(new Instruction(nestedOpCode, operand1, operand2, operand3, nestedBlock));

                }  else {
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
        return block;
    }

    public void run() {
        for (Instruction instruction : instructions) {
            execute(instruction);
        }
    }

    private void execute(Instruction instruction) {
        switch (instruction.opCode) {
            case STORE -> {
                memoryManager.allocate(instruction.operand1, instruction.operand2);
            }
            case PRINT -> {
                Object value = memoryManager.getValue(instruction.operand1);
                if (value != null) {
                    System.out.println(value);
                } else {
                    throw new RuntimeException("Variable not found: " + instruction.operand1);
                }
            }
            case ADD -> {
                int result = getOperandValue(instruction.operand2) + getOperandValue(instruction.operand3);
                memoryManager.allocate(instruction.operand1, result);
            }
            case SUB -> {
                int result = getOperandValue(instruction.operand2) - getOperandValue(instruction.operand3);
                memoryManager.allocate(instruction.operand1, result);
            }
            case MUL -> {
                int result = getOperandValue(instruction.operand2) * getOperandValue(instruction.operand3);
                memoryManager.allocate(instruction.operand1, result);
            }
            case LESS -> {
                boolean result = getOperandValue(instruction.operand2) < getOperandValue(instruction.operand3);
                memoryManager.allocate(instruction.operand1, result);
            }
            case GREATER -> {
                boolean result = getOperandValue(instruction.operand2) > getOperandValue(instruction.operand3);
                memoryManager.allocate(instruction.operand1, result);
            }
            case EQUALS -> {
                boolean result = Objects.equals(getOperandValue(instruction.operand2), getOperandValue(instruction.operand3));
                memoryManager.allocate(instruction.operand1, result);
            }
            case NOT_EQUALS -> {
                boolean result = !Objects.equals(getOperandValue(instruction.operand2), getOperandValue(instruction.operand3));
                memoryManager.allocate(instruction.operand1, result);
            }
            case NEW -> {
                Object size = instruction.operand2;

                if (size instanceof String strSize) {
                    try {
                        int arraySize = Integer.parseInt(strSize);
                        memoryManager.allocateArray(instruction.operand1, arraySize);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Invalid array size: " + strSize, e);
                    }
                } else if (size instanceof  List) {
                    List<?> array = (List<?>) size;
                    memoryManager.allocateArray(instruction.operand1, array.toArray());
                }else {
                    throw new RuntimeException("Invalid array size: " + size);
                }
            }
            case WRITE_INDEX -> {
                var index = getOperandValue(instruction.operand2);
                var value = getOperandValue(instruction.operand3);
                memoryManager.setArrayElement(instruction.operand1, index, value);
            }

            case STORE_ARRAY_VAR -> {
                Object value = memoryManager.getArrayElement(instruction.operand1, getOperandValue(instruction.operand2));
                if (value == null) {
                    throw new RuntimeException("Variable not found: " + instruction.operand1);
                }
                memoryManager.allocate((String) instruction.operand3, value);
            }
            case READ_INDEX -> {
                Object value = memoryManager.getArrayElement(instruction.operand1, getOperandValue(instruction.operand2));
                if (value != null) {
                    System.out.println(value);
                } else {
                    throw new RuntimeException("Variable not found: " + instruction.operand2);
                }
            }
            case IF -> {
                boolean condition = conditions(instruction);
                if (condition && instruction.block != null) {
                    run(instruction.block);
                }
            }
            case LOOP -> {
                while (conditions(instruction)) {
                    if (instruction.block != null) {
                        run(instruction.block);
                    }
                }
            }
            case FUN -> {
                String functionName = instruction.operand1;
                List<String> parameters = instruction.parameters;

                List<Instruction> functionBody = instruction.block;

                // Создаем объект инструкции для функции и добавляем в список функций
                Instruction functionInstruction = new Instruction(
                        Instruction.OpCode.FUN, functionName, parameters, functionBody
                );
                functions.put(functionName, functionInstruction);
            }
            case CALL -> {
                String functionName = instruction.operand1;
                Instruction functionInstruction = functions.get(functionName);
                if (functionInstruction == null) {
                    throw new RuntimeException("Function " + functionName + " is not defined");
                }

                List<String> parameters = functionInstruction.parameters;

                List<Instruction> functionBody = functionInstruction.block;

                var args = instruction.operand2;
                List<Object> valsForAlloc = new ArrayList<>();

                List<Object> arguments = parseToListOfObjects(args);
                if (parameters.size() != arguments.size()) {
                    throw new RuntimeException("Function " + functionName + " expects " + parameters.size() + " arguments, but got " + arguments.size());
                }
                for (int i = 0; i < parameters.size(); i++) {
                    Object argument = arguments.get(i);
                    Object valueToAllocate;

                    if (argument instanceof String varName) {
                        valueToAllocate = memoryManager.getValue(varName);
                    } else {
                        valueToAllocate = argument;
                    }
                    valsForAlloc.add(valueToAllocate);
                }
                memoryManager.enterFunction();

                for (int i = 0; i < parameters.size(); i++) {
                    memoryManager.allocate(parameters.get(i), valsForAlloc.get(i));
                }

                run(functionBody);

                Object returnValue = memoryManager.getReturnValue();
                memoryManager.exitFunction();
                isReturning = false;

                if (instruction.operand3 != null) {
                    if (returnValue == null) {
                        throw new RuntimeException("Function did not return a value for assignment to: " + instruction.operand3);
                    }
                    memoryManager.allocate((String) instruction.operand3, returnValue);
                }
            }
            case RETURN -> {
                if (instruction.operand1 == null || instruction.operand1.isEmpty()) {
                    Instruction instruction1 = (Instruction) instruction.operand2;
                    String functionName = instruction1.operand1;
                    Instruction functionInstruction = functions.get(functionName);
                    if (functionInstruction == null) {
                        throw new RuntimeException("Function " + functionName + " is not defined");
                    }

                    List<String> parameters = functionInstruction.parameters;
                    List<Instruction> functionBody = functionInstruction.block;

                    String args = String.valueOf(instruction1.operand2);
                    List<Object> arguments = parseToListOfObjects(args);
                    if (parameters.size() != arguments.size()) {
                        throw new RuntimeException("Function " + functionName + " expects " + parameters.size() + " arguments, but got " + arguments.size());
                    }

                    List<Object> operandValues = new ArrayList<>();
                    for (int i = 0; i < parameters.size(); i++) {
                        Object argument = arguments.get(i);
                        Object valueToAllocate;

                        if (argument instanceof String varName) {
                            valueToAllocate = memoryManager.getValue(varName);
                            if(valueToAllocate == null){
                                valueToAllocate = argument;
                            }
                        } else {
                            valueToAllocate = argument;
                        }
                        operandValues.add(valueToAllocate);
                    }

                    memoryManager.enterFunction();
                    for (int i = 0; i < parameters.size(); i++) {
                        memoryManager.allocate(parameters.get(i), operandValues.get(i));
                    }
                    run(functionBody);
                    Object returnValue = memoryManager.getReturnValue();
                    memoryManager.exitFunction();
                    isReturning = false;

                    if (instruction1.operand3 != null) {
                        if (returnValue == null) {
                            throw new RuntimeException("Function did not return a value for assignment to: " + instruction1.operand3);
                        }
                        memoryManager.allocate((String) instruction1.operand3, returnValue);
                    }
                    return; // важно выйти из кейса return
                }
                Object returnValue = memoryManager.getValue(instruction.operand1);
                if (returnValue == null) {
                    throw new RuntimeException("Return value not found for variable: " + instruction.operand1);
                }

                memoryManager.setReturnValue(returnValue);
                isReturning = true;
            }
            default -> throw new RuntimeException("Unknown instruction: " + instruction.opCode);
        }
    }

    public void run(List<Instruction> block) {
        for (Instruction instruction : block) {
            if (isReturning) {
                break; // Завершаем выполнение текущего блока при возвращении из функции
            }
            execute(instruction);
        }
    }

    public boolean conditions(Instruction instruction) {
        switch (Token.Type.valueOf((String) instruction.operand2)) {
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
                return !Objects.equals(getOperandValue(instruction.operand1), getOperandValue(instruction.operand3));
            }
            default -> throw new RuntimeException("Unknown condition: " + instruction.operand2);
        }
    }

    private int getOperandValue(Object operand) {
        if (operand instanceof Integer) {
            return (int) operand;
        } else if (operand instanceof String varName) {
            Object value = memoryManager.getValue(varName);
            if (value instanceof Integer) {
                return (int) value;
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Variable " + varName + " is not a valid number: " + value);
                }
            } else if (value == null) {
                try {
                    return Integer.parseInt((String) operand);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Variable " + varName + " is not a valid number: " + value);
                }
            } else if (value instanceof Object[]){
                throw new RuntimeException("Variable " + varName + " is an array, not an Integer: " + value);
            }
            throw new RuntimeException("Variable " + varName + " has unsupported type: " + value.getClass());
        } else {
            throw new RuntimeException("Invalid operand type: " + operand);
        }
    }


    public static List<Object> parseToListOfObjects(Object input) {
        String str = (String) input;
        String trimmed = str.substring(1, str.length() - 1).trim(); // Убираем скобки и пробелы

        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }

        // Разбиваем строку по запятым
        String[] items = trimmed.split(",");
        List<Object> result = new ArrayList<>();
        for (String item : items) {
            String trimmedItem = item.trim();
            if (trimmedItem.startsWith("[") && trimmedItem.endsWith("]")) {
                // Handle array parameters
                String arrayContent = trimmedItem.substring(1, trimmedItem.length() - 1);
                String[] arrayItems = arrayContent.split(",");
                Object[] objectArray = new Object[arrayItems.length];
                for (int i = 0; i < arrayItems.length; i++) {
                    String arrayItem = arrayItems[i].trim();
                    try {
                        objectArray[i] = Integer.parseInt(arrayItem); // Assume array elements are integers
                    }catch (NumberFormatException e) {
                        objectArray[i] = arrayItem; //if element is not integer
                    }
                }
                result.add(objectArray);
            } else {
                try {
                    result.add(Integer.parseInt(trimmedItem)); // Assume it's an integer
                } catch (NumberFormatException e) {
                    result.add(trimmedItem); // If not an integer, treat as a String
                }
            }
        }
        return result;
    }
}